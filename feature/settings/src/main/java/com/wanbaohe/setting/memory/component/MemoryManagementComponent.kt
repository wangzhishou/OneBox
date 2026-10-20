package com.wanbaohe.setting.memory.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.MemoryEntryEntity
import com.shifenmiao.storage.AIChatStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * AI 记忆管理页组件。
 *
 * 数据接入照 SystemPromptManagementComponent 模式：直接注入 AppDatabase 拿 DAO，
 * feature/settings 不依赖 feature/ai。全局总开关绑 [AIChatStorage.isEnableMemory]。
 */
class MemoryManagementComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val appDatabase: AppDatabase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val memoryEntryDao = appDatabase.memoryEntryDao()

    /** 全局记忆总开关 */
    val globalMemoryEnabled: StateFlow<Boolean> = AIChatStorage.isEnableMemory

    /** 全部 profile 档案条目（含停用，管理页需要能重新启用） */
    val profileEntries: StateFlow<List<MemoryEntryEntity>> = combine(
        memoryEntryDao.observeByKind(MemoryEntryEntity.KIND_PROFILE, enabled = true),
        memoryEntryDao.observeByKind(MemoryEntryEntity.KIND_PROFILE, enabled = false)
    ) { enabled, disabled -> enabled + disabled }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 全部 log 日志条目（含停用），按写入时间倒序（页面按日期分组） */
    val logEntries: StateFlow<List<MemoryEntryEntity>> = combine(
        memoryEntryDao.observeByKind(MemoryEntryEntity.KIND_LOG, enabled = true),
        memoryEntryDao.observeByKind(MemoryEntryEntity.KIND_LOG, enabled = false)
    ) { enabled, disabled -> (enabled + disabled).sortedByDescending { it.createdAt } }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setGlobalMemoryEnabled(enabled: Boolean) {
        AIChatStorage.saveIsEnableMemory(enabled)
    }

    /** 新增（entry.id = 0）或更新条目，更新时刷新 updated_at。 */
    fun saveEntry(entry: MemoryEntryEntity) {
        componentScope.launch {
            val stamped = entry.copy(updatedAt = System.currentTimeMillis())
            if (stamped.id == 0L) {
                memoryEntryDao.insert(stamped)
            } else {
                memoryEntryDao.update(stamped)
            }
        }
    }

    fun setEntryEnabled(entry: MemoryEntryEntity, enabled: Boolean) {
        componentScope.launch {
            memoryEntryDao.update(
                entry.copy(enabled = enabled, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun deleteEntry(id: Long) {
        componentScope.launch {
            memoryEntryDao.deleteById(id)
        }
    }

    /** 一键清空 log（profile 不动）。 */
    fun clearLog() {
        componentScope.launch {
            memoryEntryDao.clearByKind(MemoryEntryEntity.KIND_LOG)
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): MemoryManagementComponent
    }
}
