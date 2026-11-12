/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.base.longtask

import android.content.Context
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.domain.saving.KeepAliveService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 长耗时任务的类别，只用于区分语义，不代表具体业务。 */
enum class LongTaskKind {
    /** AI 对话/互聊的流式回复（含回合级持有）。 */
    AI_STREAM,

    /** 一次性 AI 生成（创建智能体/提示词/工具草案等）。 */
    AI_GENERATION,

    /** 其他未分类长任务。 */
    GENERIC,
}

data class LongTaskInfo(
    val id: String,
    val kind: LongTaskKind,
    val title: String,
    val startedAtMs: Long,
)

/**
 * 全局长耗时任务注册中心：统一驱动前台保活服务与通知栏进度。
 *
 * 解决的问题：AI 聊天的前台保活（[com.shifenmiao.ai.request.AiStreamKeepAlive] 的前身）
 * 只能表述"AI 正在回复"一件事，创建智能体/提示词等一次性生成退后台或锁屏后会被
 * 系统冻结、网络被 defer，用户回来只剩失败。这里把所有长任务收敛到一个注册表：
 *
 * - 同一 [LongTaskInfo.id] 可重复 [begin]/[end]，内部引用计数，最后一个 end 才移除
 *   （双人对话多条流并发、聊天"流→工具→续轮流"的回合级持有都靠这个语义）；
 * - 通知始终是**一条**聚合通知：单任务显示任务标题，多任务追加"等 N 项"；
 *   不携带任何用户输入/AI 输出内容（锁屏通知栏可见 = 隐私泄露）；
 * - [activeTasks] 是唯一事实来源，通知、将来的画中画机器人进展都从这里订阅；
 * - 保活启动失败只降级为不保活，绝不影响任务本身。
 *
 * 注意：底层 dataSync 前台服务在 Android 15 上有 6 小时/24h 的运行上限，
 * 不要拿它保活以天计的任务。
 */
@Singleton
class LongTaskCenter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keepAliveService: KeepAliveService,
) {

    private val lock = Any()

    /** id -> (任务信息, 引用计数)，按注册顺序排列，首个注册的任务是通知主任务。 */
    private val tasks = LinkedHashMap<String, Pair<LongTaskInfo, Int>>()

    private val _activeTasks = MutableStateFlow<List<LongTaskInfo>>(emptyList())

    /** 当前进行中的任务列表（已按 id 去重）；画中画/机器人进展等 UI 订阅它。 */
    val activeTasks: StateFlow<List<LongTaskInfo>> = _activeTasks.asStateFlow()

    /** 开始（或叠加）一个任务；相同 [id] 的并发调用只保留一份任务信息。 */
    fun begin(id: String, kind: LongTaskKind, title: String) {
        synchronized(lock) {
            val existing = tasks[id]
            if (existing != null) {
                tasks[id] = existing.first to existing.second + 1
            } else {
                tasks[id] = LongTaskInfo(
                    id = id,
                    kind = kind,
                    title = title,
                    startedAtMs = System.currentTimeMillis(),
                ) to 1
            }
            publishLocked()
        }
    }

    /** 结束一次 [begin]；计数归零才真正移除任务并刷新通知。 */
    fun end(id: String) {
        synchronized(lock) {
            val existing = tasks[id] ?: return
            if (existing.second <= 1) {
                tasks.remove(id)
            } else {
                tasks[id] = existing.first to existing.second - 1
            }
            publishLocked()
        }
    }

    /** 包一段 suspend 任务，正常/异常/取消都会成对释放。 */
    suspend inline fun <T> withTask(
        id: String,
        kind: LongTaskKind,
        title: String,
        crossinline block: suspend () -> T,
    ): T {
        begin(id, kind, title)
        try {
            return block()
        } finally {
            end(id)
        }
    }

    /** 调用方须持有 [lock]。 */
    private fun publishLocked() {
        _activeTasks.value = tasks.values.map { it.first }
        refreshNotificationLocked()
    }

    /** 调用方须持有 [lock]。 */
    private fun refreshNotificationLocked() {
        val primary = tasks.values.firstOrNull()?.first
        if (primary == null) {
            keepAliveService.stop()
            return
        }
        val extraCount = tasks.size - 1
        keepAliveService.updateOrStart(
            title = primary.title,
            description = if (extraCount > 0) {
                context.getString(R.string.long_task_notification_more, extraCount)
            } else {
                ""
            },
            progress = KeepAliveService.PROGRESS_INDETERMINATE,
        )
    }
}
