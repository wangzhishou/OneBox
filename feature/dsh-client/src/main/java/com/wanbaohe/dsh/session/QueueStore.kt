package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.MuxFrame
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.RpcErrorCodes
import com.wanbaohe.dsh.wire.model.QueueAction
import com.wanbaohe.dsh.wire.model.QueueItem
import com.wanbaohe.dsh.wire.model.TaskView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 队列/后台任务域 store(DSH-PROTOCOL §4/§5,对齐 Flutter interactor_store 的 queues
 * 与 job_store.dart)。
 *
 * - session/queue、session/jobs 均为**完整快照**帧:整帧收敛,直接替换该会话的本地列表
 * - 新代际(connecting)清场:host 在 mux open 后重推快照,保留旧快照会滞留幻影
 * - delete 按 MessageId 寻址(session.updateQueue kind:remove);被 claim 的删除 splice
 *   赢竞态,后来者 queue-item-not-found —— 合法竞态结果,折叠为无害不重试
 * - cancel 只中止当前 turn,保留 pending inbox;FIFO 认领由主机驱动,客户端永不重发/提升
 * - jobs 本阶段只建模快照(P5 做 UI 弹层):排序照抄 web —— 活跃(running/stopping)在前
 *   按 startedAt 升序,终态在后按 finishedAt 降序,完全并列保留帧内顺序(稳定排序)
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose](不做 @Singleton)。
 */
class QueueStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    parentScope: CoroutineScope
) {

    /** 子 scope:dispose 只取消自己,不动组件 scope */
    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val _queues = MutableStateFlow<Map<String, List<QueueItem>>>(emptyMap())
    /** 各会话待处理收件箱快照(sessionId → items,整帧替换) */
    val queues: StateFlow<Map<String, List<QueueItem>>> = _queues.asStateFlow()

    private val _jobs = MutableStateFlow<Map<String, List<TaskView>>>(emptyMap())
    /** 各会话后台任务快照(sessionId → 已排序 jobs,整帧替换;P5 做 UI) */
    val jobs: StateFlow<Map<String, List<TaskView>>> = _jobs.asStateFlow()

    @Volatile
    private var disposed = false
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch { connection.muxFrames.collect(::onMuxFrame) }
        scope.launch {
            connection.snapshots.collect { snapshot ->
                if (snapshot.phase != ConnectionPhase.Connecting) return@collect
                if (_queues.value.isEmpty() && _jobs.value.isEmpty()) return@collect
                _queues.value = emptyMap()
                _jobs.value = emptyMap()
            }
        }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 当前选中会话的队列快照(无帧时为空表) */
    fun queueFor(sessionId: String): List<QueueItem> = _queues.value[sessionId].orEmpty()

    /** 某会话当前排序后的任务列表(空会话返回空表) */
    fun jobsFor(sessionId: String): List<TaskView> = _jobs.value[sessionId].orEmpty()

    /** 会话活跃任务数(running+stopping;P5 角标用) */
    fun activeJobCount(sessionId: String): Int =
        _jobs.value[sessionId].orEmpty().count { it.isActive }

    /**
     * 删除队列项(按 MessageId 寻址)。queue-item-not-found 折叠为无害
     * (项刚被 claim 是合法竞态,本地不重试);其余错误上抛由调用方展示。
     */
    suspend fun delete(sessionId: String, itemId: String) {
        try {
            api.sessionUpdateQueue(sessionId, itemId, QueueAction.Remove)
        } catch (e: RpcBusinessException) {
            if (e.error.code != RpcErrorCodes.QueueItemNotFound) throw e
        }
    }

    /** 取消当前 turn(保留 pending inbox;客户端永不重发/提升排队消息) */
    suspend fun cancel(sessionId: String) {
        api.sessionCancel(sessionId)
    }

    /** 快照帧折叠:整帧替换该会话列表(收敛语义) */
    private fun onMuxFrame(frame: MuxFrame) {
        if (disposed) return
        when (frame) {
            is MuxFrame.SessionQueue -> {
                _queues.value = _queues.value + (frame.sessionId to frame.items)
            }

            is MuxFrame.SessionJobs -> {
                _jobs.value = _jobs.value + (frame.sessionId to sortJobs(frame.jobs))
            }

            else -> Unit
        }
    }

    /**
     * 排序(web 同款):活跃优先;活跃按 startedAt 升序,终态按 finishedAt 降序
     * (缺 finishedAt 按 0);完全并列保留帧内顺序(sortedWith 是稳定排序)。
     */
    private fun sortJobs(jobs: List<TaskView>): List<TaskView> =
        jobs.sortedWith { a, b ->
            if (a.isActive != b.isActive) {
                if (a.isActive) -1 else 1
            } else if (a.isActive) {
                a.startedAt.compareTo(b.startedAt)
            } else {
                (b.finishedAt ?: 0L).compareTo(a.finishedAt ?: 0L)
            }
        }
}

/**
 * 任务耗时格式化(对齐 Flutter job_store.dart):>1h 停在小时("2h"),
 * 否则 "m:ss"(活跃行每秒走表刷新);负值(时钟回拨/畸形数据)钳到 0。
 */
fun formatJobDuration(ms: Long): String {
    val clamped = ms.coerceAtLeast(0)
    val hours = clamped / 3_600_000
    if (hours >= 1) return "${hours}h"
    val minutes = (clamped % 3_600_000) / 60_000
    val seconds = (clamped % 60_000) / 1_000
    return "%d:%02d".format(minutes, seconds)
}

/** 任务耗时:活跃 = now-startedAt;终态 = finishedAt-startedAt(缺 finishedAt 读 0) */
fun jobElapsedMs(task: TaskView, now: Long): Long {
    val ms = if (task.isActive) now - task.startedAt else (task.finishedAt ?: 0L) - task.startedAt
    return ms.coerceAtLeast(0)
}
