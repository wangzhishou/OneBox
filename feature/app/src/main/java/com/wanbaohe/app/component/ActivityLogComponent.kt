package com.wanbaohe.app.component

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.activity.repository.ActivityLogRepository
import com.shifenmiao.model.activity.ActivityCategory
import com.shifenmiao.model.activity.ActivityLogEntry
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import org.json.JSONObject

/**
 * 活动日志 Decompose Component — 替代旧的 HistoryComponent。
 *
 * 职责：
 * 1. 分页加载活动日志 → [activityLogFlow]
 * 2. 删除单条 / 清空全部
 * 3. 根据 [ActivityLogEntry] 解析跳转目标（不再硬编码 when-block）
 */
class ActivityLogComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    private val repository: ActivityLogRepository,
    private val recorder: ActivityLogRecorder,
    private val appDatabase: AppDatabase,
    resourceManager: ResourceManager,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext), ResourceManager by resourceManager {

    private val _activityLogFlow: MutableStateFlow<PagingData<ActivityLogEntry>> =
        MutableStateFlow(PagingData.empty())
    val activityLogFlow: StateFlow<PagingData<ActivityLogEntry>> get() = _activityLogFlow

    init {
        loadActivityLogs()
    }

    private fun loadActivityLogs() {
        componentScope.launch(Dispatchers.IO) {
            repository.observePaged()
                .distinctUntilChanged()
                .cachedIn(componentScope)
                .collect {
                    _activityLogFlow.value = it
                }
        }
    }

    /**
     * 删除单条日志。
     * 如果是 AI 类型，同时清理关联的 conversation/messages。
     */
    fun deleteEntry(entry: ActivityLogEntry) {
        componentScope.launch(Dispatchers.IO) {
            repository.deleteById(entry.id)

            // AI 类型：清理关联数据
            when (entry.category) {
                ActivityCategory.AI_CHAT,
                ActivityCategory.AI_DUEL,
                ActivityCategory.AI_AGENT -> {
                    val payload = runCatching { JSONObject(entry.payload) }.getOrNull()
                    val conversationId = payload?.optString("conversationId").orEmpty()
                    if (conversationId.isNotEmpty()) {
                        appDatabase.messageDao()
                            .deleteMessagesByConversationId(conversationId)
                        appDatabase.conversationDao()
                            .deleteConversationByConversationId(conversationId)
                    }
                }

                else -> { /* 无需额外清理 */ }
            }

            loadActivityLogs()
        }
    }

    /**
     * 清空全部日志。
     */
    fun clearAll() {
        componentScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            loadActivityLogs()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): ActivityLogComponent
    }
}

