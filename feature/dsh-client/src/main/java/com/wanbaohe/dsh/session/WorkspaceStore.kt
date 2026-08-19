package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.HostFrame
import com.wanbaohe.dsh.wire.model.WorkspaceCreateRequest
import com.wanbaohe.dsh.wire.model.WorkspaceCreateValue
import com.wanbaohe.dsh.wire.model.WorkspaceListValue
import com.wanbaohe.dsh.wire.model.WorkspaceView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import com.wanbaohe.dsh.wire.model.WorkspaceArchiveSessionValue
import com.wanbaohe.dsh.wire.model.WorkspaceDeleteValue
import com.wanbaohe.dsh.wire.model.WorkspaceInsertBeforeValue
import com.wanbaohe.dsh.wire.model.WorkspaceInsertSessionBeforeValue
import com.wanbaohe.dsh.wire.model.WorkspaceRenameValue

/**
 * workspace 域状态(对齐 Flutter workspace_store.dart)。
 *
 * - 代际 ready → 全量重取 workspace.list(无 since 续传)
 * - host/workspace-changed、removed、order-changed、archived-sessions-changed
 *   → 简单收敛:整表重取(不等帧内数据)
 * - 变更方法(create)成功后以响应回带数据落地并广播,不等重取
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose]。
 */
class WorkspaceStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    parentScope: CoroutineScope
) {

    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val _workspaces = MutableStateFlow<List<WorkspaceView>>(emptyList())
    /** 工作区列表(顺序即 wire 顺序) */
    val workspaces: StateFlow<List<WorkspaceView>> = _workspaces.asStateFlow()

    private val _archivedSessionIds = MutableStateFlow<List<String>>(emptyList())
    /** 归档会话 id 集(UI 过滤用) */
    val archivedSessionIds: StateFlow<List<String>> = _archivedSessionIds.asStateFlow()

    @Volatile
    private var disposed = false
    private var started = false
    private var lastReadyGeneration = 0

    fun start() {
        if (started) return
        started = true
        scope.launch {
            connection.snapshots.collect { snapshot ->
                // 重连 = 全量重取;失败由下一代际重试
                if (!disposed &&
                    snapshot.phase == ConnectionPhase.Ready &&
                    snapshot.generation > lastReadyGeneration
                ) {
                    lastReadyGeneration = snapshot.generation
                    refresh()
                }
            }
        }
        scope.launch { connection.hostFrames.collect(::onHostFrame) }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 全量重取 workspace.list(items + archivedSessionIds);失败静默(下一代际重试) */
    fun refresh() {
        if (disposed) return
        scope.launch {
            try {
                val value = api.call(RpcWorkspaceList, buildJsonObject {})
                val parsed = DshJson.decodeFromJsonElement<WorkspaceListValue>(value)
                if (disposed) return@launch
                _workspaces.value = parsed.items
                _archivedSessionIds.value = parsed.archivedSessionIds
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // 重取失败不落地,等帧/代际触发下一轮
            }
        }
    }

    /** workspace.create:响应回带 WorkspaceView 落地并广播(created=false 同样 upsert) */
    suspend fun create(path: String): WorkspaceCreateValue {
        val payload = DshJson.encodeToJsonElement(WorkspaceCreateRequest(path)).jsonObject
        val value = DshJson.decodeFromJsonElement<WorkspaceCreateValue>(
            api.call(RpcWorkspaceCreate, payload)
        )
        upsert(value.workspace)
        return value
    }

    /** workspace.rename:响应回带行落地(同 create 语义,不等重取) */
    suspend fun rename(workspaceId: String, title: String): WorkspaceRenameValue {
        val payload = buildJsonObject {
            put("workspaceId", workspaceId)
            put("title", title)
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceRenameValue>(
            api.call(RpcWorkspaceRename, payload)
        )
        upsert(value.workspace)
        return value
    }

    /** workspace.delete:非破坏性(会话移入未分组);成功后本地移除该行 */
    suspend fun delete(workspaceId: String): WorkspaceDeleteValue {
        val payload = buildJsonObject { put("workspaceId", workspaceId) }
        val value = DshJson.decodeFromJsonElement<WorkspaceDeleteValue>(
            api.call(RpcWorkspaceDelete, payload)
        )
        _workspaces.value = _workspaces.value.filterNot { it.workspaceId == workspaceId }
        return value
    }

    /**
     * workspace.insertBefore:工作区排序(beforeWorkspaceId 缺席 = 移到末尾)。
     * 响应回带完整排序;按序重排本地列表(未知 id 保持原位兜底)。
     */
    suspend fun insertBefore(
        workspaceId: String,
        beforeWorkspaceId: String? = null
    ): WorkspaceInsertBeforeValue {
        val payload = buildJsonObject {
            put("workspaceId", workspaceId)
            beforeWorkspaceId?.let { put("beforeWorkspaceId", it) }
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceInsertBeforeValue>(
            api.call(RpcWorkspaceInsertBefore, payload)
        )
        val byId = _workspaces.value.associateBy { it.workspaceId }.toMutableMap()
        val next = ArrayList<WorkspaceView>(byId.size)
        for (id in value.workspaceIds) {
            byId.remove(id)?.let(next::add)
        }
        next.addAll(byId.values)
        _workspaces.value = next
        return value
    }

    /** workspace.insertSessionBefore:把会话移入(或在工作区内排序)指定工作区 */
    suspend fun insertSessionBefore(
        workspaceId: String,
        sessionId: String,
        beforeSessionId: String? = null
    ): WorkspaceInsertSessionBeforeValue {
        val payload = buildJsonObject {
            put("workspaceId", workspaceId)
            put("sessionId", sessionId)
            beforeSessionId?.let { put("beforeSessionId", it) }
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceInsertSessionBeforeValue>(
            api.call(RpcWorkspaceInsertSessionBefore, payload)
        )
        upsert(value.workspace)
        return value
    }

    /** workspace.archiveSession:归档(非破坏性);响应回带完整归档集合,收敛替换 */
    suspend fun archiveSession(sessionId: String): WorkspaceArchiveSessionValue {
        val payload = buildJsonObject { put("sessionId", sessionId) }
        val value = DshJson.decodeFromJsonElement<WorkspaceArchiveSessionValue>(
            api.call(RpcWorkspaceArchiveSession, payload)
        )
        _archivedSessionIds.value = value.archivedSessionIds
        return value
    }

    /** 归档判定(UI 过滤用) */
    fun isArchived(sessionId: String): Boolean = sessionId in _archivedSessionIds.value

    private fun upsert(workspace: WorkspaceView) {
        val current = _workspaces.value
        val idx = current.indexOfFirst { it.workspaceId == workspace.workspaceId }
        _workspaces.value = if (idx >= 0) {
            current.toMutableList().also { it[idx] = workspace }
        } else {
            current + workspace
        }
    }

    private fun onHostFrame(frame: HostFrame) {
        when (frame) {
            // 简单收敛:任意 workspace 域变更整表重取(无 since 续传)
            is HostFrame.WorkspaceChanged,
            is HostFrame.WorkspaceRemoved,
            is HostFrame.WorkspaceOrderChanged,
            is HostFrame.ArchivedSessionsChanged -> refresh()

            else -> Unit
        }
    }

    companion object {
        private const val RpcWorkspaceList = "workspace.list"
        private const val RpcWorkspaceCreate = "workspace.create"
        private const val RpcWorkspaceRename = "workspace.rename"
        private const val RpcWorkspaceDelete = "workspace.delete"
        private const val RpcWorkspaceInsertBefore = "workspace.insertBefore"
        private const val RpcWorkspaceInsertSessionBefore = "workspace.insertSessionBefore"
        private const val RpcWorkspaceArchiveSession = "workspace.archiveSession"
    }
}
