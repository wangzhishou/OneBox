package com.shifenmiao.common.backup

import android.content.Context
import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.backup.destination.BackupDestination
import com.shifenmiao.common.backup.destination.BackupDestinationIds
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.utils.AppRestarter
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 备份中心页面逻辑：创建、导出、恢复、删除。
 *
 * 这里只做状态编排，真正的读写都在 [BackupEngine] / [BackupHistoryStore]。
 */
class BackupCenterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("onGoBack") val onGoBack: () -> Unit,
    @Assisted("onOpenCloudStorage") val onOpenCloudStorage: () -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val engine: BackupEngine,
    private val cloudStatus: BackupCloudStatus,
    private val destinations: Set<@JvmSuppressWildcards BackupDestination>,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(BackupCenterUiState())
    val uiState: StateFlow<BackupCenterUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        componentScope.launch {
            val records = engine.records()
            val preRestore = engine.preRestoreRecord()
            val cloudAvailable = cloudStatus.isAvailable()
            _uiState.update {
                it.copy(
                    records = records,
                    preRestore = preRestore,
                    cloud = CloudBackupUi(
                        available = cloudAvailable,
                        label = if (cloudAvailable) cloudStatus.label() else null,
                    ),
                )
            }
        }
    }

    fun exportToCloud(record: BackupRecord) = export(record, BackupDestinationIds.CLOUD)

    fun createBackup() {
        if (_uiState.value.operation != null) return
        componentScope.launch {
            _uiState.update {
                it.copy(operation = BackupOperation(BackupOperationMode.CREATING, BackupPhase.CHECKPOINT))
            }
            val record = engine.createBackup { phase -> updatePhase(phase) }.getOrNull()
            _uiState.update {
                it.copy(
                    operation = null,
                    notice = if (record != null) {
                        BackupNotice.Created(record.sizeBytes)
                    } else {
                        BackupNotice.CreateFailed
                    },
                    records = engine.records(),
                    preRestore = engine.preRestoreRecord(),
                )
            }
        }
    }

    fun export(
        record: BackupRecord,
        destinationId: String,
        documentUri: Uri? = null,
    ) {
        val destination = destinations.firstOrNull { it.id == destinationId } ?: return
        if (_uiState.value.operation != null) return
        componentScope.launch {
            _uiState.update {
                it.copy(operation = BackupOperation(BackupOperationMode.EXPORTING))
            }
            val stored = destination.store(engine.fileOf(record), documentUri).getOrNull()
            _uiState.update { state ->
                state.copy(
                    operation = null,
                    notice = if (stored != null) {
                        BackupNotice.Exported(destinationId)
                    } else {
                        BackupNotice.ExportFailed
                    },
                    records = if (stored != null) {
                        engine.markExported(record, destinationId, stored.documentUri)
                    } else {
                        state.records
                    },
                )
            }
        }
    }

    fun share(record: BackupRecord) = export(record, BackupDestinationIds.SHARE)

    fun requestRestore(record: BackupRecord) {
        _uiState.update { it.copy(pendingRestore = PendingRestore(record = record)) }
    }

    fun requestRestoreFromDocument(uri: Uri) {
        componentScope.launch {
            val inspection = engine.inspect(uri).getOrNull()
            if (inspection == null || !inspection.compatible) {
                _uiState.update {
                    it.copy(
                        notice = BackupNotice.RestoreRejected(
                            issue = inspection?.issue ?: RestoreIssue.UNREADABLE,
                            requiredVersion = inspection?.manifest?.appVersionName,
                        )
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(pendingRestore = PendingRestore(inspection = inspection, uri = uri))
            }
        }
    }

    fun cancelRestore() {
        _uiState.update { it.copy(pendingRestore = null) }
    }

    fun confirmRestore() {
        val pending = _uiState.value.pendingRestore ?: return
        _uiState.update { it.copy(pendingRestore = null) }
        componentScope.launch {
            _uiState.update {
                it.copy(operation = BackupOperation(BackupOperationMode.RESTORING, BackupPhase.VALIDATING))
            }
            val outcome = when {
                pending.record != null -> engine.restore(pending.record) { phase -> updatePhase(phase) }
                pending.uri != null -> engine.restore(pending.uri) { phase -> updatePhase(phase) }
                else -> RestoreOutcome.Rejected(RestoreIssue.UNREADABLE)
            }
            when (outcome) {
                is RestoreOutcome.Success -> {
                    _uiState.update {
                        it.copy(operation = null, notice = BackupNotice.RestoreSucceeded)
                    }
                    // 恢复的库文件要等进程重启才会被 Room 重新打开。
                    // 用 NonCancellable：页面此时可能被弹栈销毁，不能因为组件作用域取消就不重启。
                    withContext(NonCancellable) {
                        delay(1_500)
                        AppRestarter.restartToColdStart(context)
                    }
                }

                is RestoreOutcome.Rejected -> _uiState.update {
                    it.copy(
                        operation = null,
                        notice = BackupNotice.RestoreRejected(outcome.issue, outcome.requiredVersion),
                    )
                }

                is RestoreOutcome.Failed -> _uiState.update {
                    it.copy(
                        operation = null,
                        notice = BackupNotice.RestoreFailed(outcome.rolledBack),
                        records = engine.records(),
                        preRestore = engine.preRestoreRecord(),
                    )
                }
            }
        }
    }

    fun delete(record: BackupRecord) {
        componentScope.launch {
            val records = engine.delete(record)
            _uiState.update {
                it.copy(
                    records = records,
                    preRestore = engine.preRestoreRecord(),
                    notice = BackupNotice.Deleted,
                )
            }
        }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    private fun updatePhase(phase: BackupPhase) {
        _uiState.update { state ->
            state.operation?.let { state.copy(operation = it.copy(phase = phase)) } ?: state
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("onGoBack") onGoBack: () -> Unit,
            @Assisted("onOpenCloudStorage") onOpenCloudStorage: () -> Unit,
        ): BackupCenterComponent
    }
}
