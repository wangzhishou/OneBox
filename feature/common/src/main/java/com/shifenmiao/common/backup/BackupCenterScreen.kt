package com.shifenmiao.common.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.shifenmiao.common.backup.destination.BackupDestinationIds
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.model.channel.FlavorType
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.wanbaohe.core.ui.utils.content_pickers.FileType
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxPillButton
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 备份中心：创建、历史、导出到自己的网盘、恢复。
 *
 * 页面本身很薄——所有 IO 在 [BackupEngine]，这里只负责把状态翻成界面与对话框。
 */
@Composable
fun BackupCenterScreen(component: BackupCenterComponent) {
    val state by component.uiState.collectAsState()
    val context = LocalContext.current
    var pendingExport by remember { mutableStateOf<BackupRecord?>(null) }
    var pendingSave by remember { mutableStateOf<BackupRecord?>(null) }
    var pendingDelete by remember { mutableStateOf<BackupRecord?>(null) }

    // 系统保存器里海外用户能直接看到 Google Drive / OneDrive / Dropbox
    val documentSaver = rememberLauncherForActivityResult(
        contract = CreateBackupDocumentContract(),
        onResult = { uri ->
            val record = pendingExport
            pendingExport = null
            if (uri != null && record != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                    )
                }
                component.export(record, BackupDestinationIds.DOCUMENT, uri)
            }
        },
    )

    val filePicker = rememberFilePicker(
        type = FileType.Single,
        mimeType = MimeType.Zip,
    ) { uris ->
        uris.firstOrNull()?.let(component::requestRestoreFromDocument)
    }

    LaunchedEffect(state.notice) {
        val notice = state.notice ?: return@LaunchedEffect
        AppToastHost.showToast(notice.message(context))
        component.dismissNotice()
    }

    BaseScreen(
        title = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.backup_center),
        onGoBack = component.onGoBack,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CreateBackupCard(
                    lastRecord = state.records.firstOrNull(),
                    enabled = state.operation == null,
                    onCreate = component::createBackup,
                )
            }

            item {
                PickBackupFileCard(
                    enabled = state.operation == null,
                    onPick = { filePicker.pickFile() },
                )
            }

            item {
                Text(
                    text = stringResource(R.string.backup_center_history),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                )
            }

            state.preRestore?.let { snapshot ->
                item {
                    PreRestoreCard(
                        record = snapshot,
                        enabled = state.operation == null,
                        onRestore = { component.requestRestore(snapshot) },
                    )
                }
            }


            if (state.records.isEmpty()) {
                item { EmptyHistoryCard() }
            } else {
                items(state.records, key = { it.id }) { record ->
                    BackupRecordCard(
                        record = record,
                        enabled = state.operation == null,
                        onRestore = { component.requestRestore(record) },
                        onSaveToDrive = { pendingSave = record },
                        onShare = { component.share(record) },
                        onDelete = { pendingDelete = record },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    state.pendingRestore?.let { pending ->
        RestoreConfirmDialog(
            pending = pending,
            onConfirm = component::confirmRestore,
            onDismiss = component::cancelRestore,
        )
    }

    state.operation?.let { operation ->
        BusyDialog(operation = operation)
    }

    pendingDelete?.let { record ->
        DeleteBackupDialog(
            record = record,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                pendingDelete = null
                component.delete(record)
            },
        )
    }

    pendingSave?.let { record ->
        SaveBackupSheet(
            cloudLabel = state.cloud.label,
            onDismiss = { pendingSave = null },
            onSaveToDocument = {
                pendingSave = null
                pendingExport = record
                documentSaver.launch(record.fileName)
            },
            onSaveToCloud = {
                pendingSave = null
                if (state.cloud.available) {
                    component.exportToCloud(record)
                } else {
                    component.onOpenCloudStorage()
                }
            },
        )
    }
}

/**
 * `CreateDocument` 默认不带持久化授权标志，拿到的 URI 重启后就失效了。
 * 这里补上 `FLAG_GRANT_PERSISTABLE_URI_PERMISSION`，配合 `takePersistableUriPermission`，
 * 备份文件存在网盘里也能在下次启动后被读回来。
 */
private class CreateBackupDocumentContract : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(MimeType.Zip.entry)
            .putExtra(Intent.EXTRA_TITLE, input)
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            )

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        if (resultCode == Activity.RESULT_OK) intent?.data else null
}

@Composable
private fun CreateBackupCard(
    lastRecord: BackupRecord?,
    enabled: Boolean,
    onCreate: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.backup_center_create),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = lastRecord?.let {
                            stringResource(
                                R.string.backup_center_last_backup,
                                formatTime(it.createdAt),
                                formatSize(it.sizeBytes),
                            )
                        } ?: stringResource(R.string.backup_center_never),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            GlassTonalButton(
                onClick = onCreate,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = AppTheme.colors.filledTonalButtonColors(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp),
                )
                Text(stringResource(R.string.backup_center_create))
            }
        }
    }
}

@Composable
private fun PreRestoreCard(
    record: BackupRecord,
    enabled: Boolean,
    onRestore: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.backup_center_pre_restore),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(
                            R.string.backup_center_pre_restore_desc,
                            formatTime(record.createdAt),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OneBoxPillButton(
                text = stringResource(R.string.backup_center_restore),
                onClick = onRestore,
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(14.dp),
                    )
                },
                textStyle = MaterialTheme.typography.labelMedium,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun BackupRecordCard(
    record: BackupRecord,
    enabled: Boolean,
    onRestore: () -> Unit,
    onSaveToDrive: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val canRestore = record.localAvailable || record.documentUri != null
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatTime(record.createdAt),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = buildString {
                            append(formatSize(record.sizeBytes))
                            if (record.appVersionName.isNotBlank()) {
                                append(" · v")
                                append(record.appVersionName)
                            }
                            if (record.isLegacy) {
                                append(" · ")
                                append(stringResource(R.string.backup_center_legacy_tag))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!record.localAvailable) {
                    Text(
                        text = stringResource(R.string.backup_center_cleaned),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recordContentsLabel(record),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OneBoxPillButton(
                    text = stringResource(R.string.backup_center_restore),
                    onClick = onRestore,
                    enabled = enabled && canRestore,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Restore,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp),
                        )
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                )
                OneBoxPillButton(
                    text = stringResource(R.string.backup_center_save_to_drive),
                    onClick = onSaveToDrive,
                    enabled = enabled && record.localAvailable,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp),
                        )
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                )
                OneBoxPillButton(
                    text = stringResource(R.string.backup_center_share),
                    onClick = onShare,
                    enabled = enabled && record.localAvailable,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.IosShare,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(14.dp),
                        )
                    },
                    textStyle = MaterialTheme.typography.labelMedium,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.backup_center_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.backup_center_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.backup_center_history_empty_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PickBackupFileCard(
    enabled: Boolean,
    onPick: () -> Unit,
) {
    GlassCard(
        onClick = onPick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Restore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.backup_center_restore_from_file),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.backup_center_restore_from_file_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RestoreConfirmDialog(
    pending: PendingRestore,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        title = { Text(stringResource(R.string.backup_center_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = buildString {
                        append(formatTime(pending.createdAt))
                        append(" · ")
                        append(formatSize(pending.sizeBytes))
                        if (pending.appVersionName.isNotBlank()) {
                            append(" · v")
                            append(pending.appVersionName)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = pendingContentsLabel(pending),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.backup_center_confirm_message),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (pending.isLegacy) {
                    Text(
                        text = stringResource(R.string.backup_center_confirm_legacy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.backup_center_cancel),
                onClick = onDismiss,
            )
        },
        confirmButton = {
            OneBoxDangerButton(
                text = stringResource(R.string.backup_center_confirm_action),
                leadingIcon = Icons.Outlined.Restore,
                onClick = onConfirm,
            )
        },
    )
}

@Composable
private fun BusyDialog(operation: BackupOperation) {
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = {},
        icon = {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        },
        title = {
            Text(
                text = when (operation.mode) {
                    BackupOperationMode.CREATING -> stringResource(R.string.backup_center_creating)
                    BackupOperationMode.EXPORTING -> stringResource(R.string.backup_center_exporting)
                    BackupOperationMode.RESTORING -> stringResource(R.string.backup_center_restoring)
                }
            )
        },
        text = {
            Text(
                text = phaseLabel(operation.phase),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        confirmButton = {},
    )
}

@Composable
private fun DeleteBackupDialog(
    record: BackupRecord,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        title = { Text(stringResource(R.string.backup_center_delete_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = formatTime(record.createdAt) + " · " + formatSize(record.sizeBytes),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.backup_center_delete_confirm_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.backup_center_cancel),
                onClick = onDismiss,
            )
        },
        confirmButton = {
            OneBoxDangerButton(
                text = stringResource(R.string.backup_center_delete_action),
                leadingIcon = Icons.Outlined.DeleteOutline,
                onClick = onConfirm,
            )
        },
    )
}

@Composable
private fun SaveBackupSheet(
    cloudLabel: String?,
    onDismiss: () -> Unit,
    onSaveToDocument: () -> Unit,
    onSaveToCloud: () -> Unit,
) {
    val documentLabel = if (FlavorType.fromName().isOverseas) {
        stringResource(R.string.backup_save_to_document)
    } else {
        stringResource(R.string.backup_save_to_document_local)
    }
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.backup_save_dialog_title),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                GlassTonalButton(
                    onClick = onSaveToDocument,
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTheme.colors.filledTonalButtonColors(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp),
                    )
                    Text(documentLabel)
                }
                Text(
                    text = stringResource(R.string.backup_save_to_document_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                GlassTonalButton(
                    onClick = onSaveToCloud,
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTheme.colors.filledTonalButtonColors(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp),
                    )
                    Text(stringResource(R.string.backup_save_to_cloud))
                }
                Text(
                    text = cloudLabel ?: stringResource(R.string.backup_save_to_cloud_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        },
    )
}

@Composable
private fun phaseLabel(phase: BackupPhase?): String = when (phase) {
    BackupPhase.CHECKPOINT -> stringResource(R.string.backup_phase_checkpoint)
    BackupPhase.DATABASES -> stringResource(R.string.backup_phase_databases)
    BackupPhase.SETTINGS -> stringResource(R.string.backup_phase_settings)
    BackupPhase.HISTORIES -> stringResource(R.string.backup_phase_histories)
    BackupPhase.FILES -> stringResource(R.string.backup_phase_files)
    BackupPhase.PACKING -> stringResource(R.string.backup_phase_packing)
    BackupPhase.VALIDATING -> stringResource(R.string.backup_phase_validating)
    BackupPhase.SNAPSHOT -> stringResource(R.string.backup_phase_snapshot)
    BackupPhase.REMOVING_OLD -> stringResource(R.string.backup_phase_removing)
    BackupPhase.EXTRACTING_DATABASES -> stringResource(R.string.backup_phase_extract_databases)
    BackupPhase.EXTRACTING_SETTINGS -> stringResource(R.string.backup_phase_extract_settings)
    BackupPhase.EXTRACTING_HISTORIES -> stringResource(R.string.backup_phase_extract_histories)
    BackupPhase.EXTRACTING_FILES -> stringResource(R.string.backup_phase_extract_files)
    BackupPhase.ROLLING_BACK -> stringResource(R.string.backup_phase_rollback)
    null -> stringResource(R.string.backup_center_please_wait)
}

@Composable
private fun recordContentsLabel(record: BackupRecord): String = contentsLabel(
    historyCount = record.mmkvStoreCount,
    hasSettings = record.hasSettings,
    hasUserFiles = record.hasUserFiles,
)

@Composable
private fun pendingContentsLabel(pending: PendingRestore): String = contentsLabel(
    historyCount = pending.mmkvStoreCount,
    hasSettings = pending.hasSettings,
    hasUserFiles = pending.hasUserFiles,
)

@Composable
private fun contentsLabel(
    historyCount: Int,
    hasSettings: Boolean,
    hasUserFiles: Boolean,
): String {
    val parts = buildList {
        add(stringResource(R.string.backup_center_contents_databases))
        if (hasUserFiles) add(stringResource(R.string.backup_center_contents_files))
        if (historyCount > 0) add(stringResource(R.string.backup_center_contents_histories, historyCount))
        if (hasSettings) add(stringResource(R.string.backup_center_contents_settings))
    }
    return parts.joinToString(" · ")
}

private fun BackupNotice.message(context: Context): String = when (this) {
    is BackupNotice.Created -> context.getString(
        R.string.backup_center_created,
        formatSize(sizeBytes),
    )

    BackupNotice.CreateFailed -> context.getString(R.string.backup_center_create_failed)
    is BackupNotice.Exported -> when (destinationId) {
        BackupDestinationIds.DOCUMENT -> context.getString(R.string.backup_center_exported_document)
        BackupDestinationIds.CLOUD -> context.getString(R.string.backup_center_exported_cloud)
        else -> context.getString(R.string.backup_center_exported_share)
    }

    BackupNotice.ExportFailed -> context.getString(R.string.backup_center_export_failed)
    BackupNotice.Deleted -> context.getString(R.string.backup_center_deleted)
    BackupNotice.RestoreSucceeded -> context.getString(R.string.backup_center_restore_succeeded)
    is BackupNotice.RestoreRejected -> when (issue) {
        RestoreIssue.UNREADABLE -> context.getString(R.string.backup_center_rejected_unreadable)
        RestoreIssue.NEWER_SCHEMA -> context.getString(R.string.backup_center_rejected_schema)
        RestoreIssue.NEWER_APP -> context.getString(
            R.string.backup_center_rejected_app,
            requiredVersion.orEmpty(),
        )

        RestoreIssue.MISSING_APP_DATABASE -> context.getString(R.string.backup_center_rejected_missing_db)
        RestoreIssue.MISSING_LOCAL_COPY -> context.getString(R.string.backup_center_rejected_missing_copy)
    }

    is BackupNotice.RestoreFailed -> if (rolledBack) {
        context.getString(R.string.backup_center_restore_failed_rolled_back)
    } else {
        context.getString(R.string.backup_center_restore_failed)
    }
}

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatSize(bytes: Long): String = when {
    bytes <= 0L -> "-"
    bytes >= 1_048_576L -> String.format(Locale.US, "%.1f MB", bytes / 1_048_576f)
    bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes / 1024f)
    else -> "$bytes B"
}
