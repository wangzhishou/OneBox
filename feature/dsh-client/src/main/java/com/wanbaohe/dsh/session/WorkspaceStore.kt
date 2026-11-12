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

package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.WorkspaceFollowFrame
import com.wanbaohe.dsh.wire.model.WorkspaceArchiveSessionValue
import com.wanbaohe.dsh.wire.model.WorkspaceCreateRequest
import com.wanbaohe.dsh.wire.model.WorkspaceCreateValue
import com.wanbaohe.dsh.wire.model.WorkspaceDeleteValue
import com.wanbaohe.dsh.wire.model.WorkspaceInsertBeforeValue
import com.wanbaohe.dsh.wire.model.WorkspaceInsertSessionBeforeValue
import com.wanbaohe.dsh.wire.model.WorkspaceRenameValue
import com.wanbaohe.dsh.wire.model.WorkspaceView
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
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * workspace 域状态(DSH 0.1.5-rc.2)。
 *
 * **0.1.5 删掉了 `workspace/list`**:工作区列表改为 `workspace/follow` 流的
 * baseline / upsert / remove / order / archived 增量(旧版是 host 流的
 * `host/workspace-changed` 等帧 + 全量重取)。
 *
 * - 变更方法成功后仍以响应回带数据落地(不等推帧)
 * - `session/sessions 归属` 由 WorkspaceView.sessionIds 给出;会话列表本身在 SessionStore
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
                // 新代际 ready:主机重开 workspace/follow,基线会重新推来;这里只做代际记账
                if (!disposed &&
                    snapshot.phase == ConnectionPhase.Ready &&
                    snapshot.generation > lastReadyGeneration
                ) {
                    lastReadyGeneration = snapshot.generation
                }
            }
        }
        scope.launch { connection.workspaceFrames.collect(::onWorkspaceFrame) }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 归档判定(UI 过滤用) */
    fun isArchived(sessionId: String): Boolean = sessionId in _archivedSessionIds.value

    /** workspace/create:响应回带 WorkspaceView 落地并广播(created=false 同样 upsert) */
    suspend fun create(path: String): WorkspaceCreateValue {
        val request = WorkspaceCreateRequest(path)
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(WorkspaceCreateRequest.serializer(), request))
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceCreateValue>(
            api.callRemote(DshEndpoints.WORKSPACE_CREATE, args)
        )
        upsert(value.workspace)
        return value
    }

    /** workspace/rename:响应回带行落地(同 create 语义,不等重取) */
    suspend fun rename(workspaceId: String, title: String): WorkspaceRenameValue {
        val args = buildJsonObject {
            putJsonObject("request") {
                put("workspaceId", workspaceId)
                put("title", title)
            }
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceRenameValue>(
            api.callRemote(DshEndpoints.WORKSPACE_RENAME, args)
        )
        upsert(value.workspace)
        return value
    }

    /** workspace/delete:非破坏性(会话移入未分组);成功后本地移除该行 */
    suspend fun delete(workspaceId: String): WorkspaceDeleteValue {
        val args = buildJsonObject {
            putJsonObject("request") { put("workspaceId", workspaceId) }
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceDeleteValue>(
            api.callRemote(DshEndpoints.WORKSPACE_DELETE, args)
        )
        _workspaces.value = _workspaces.value.filterNot { it.workspaceId == workspaceId }
        return value
    }

    /**
     * workspace/insertBefore:工作区排序(beforeWorkspaceId 缺席 = 移到末尾)。
     * 响应回带完整排序;按序重排本地列表(未知 id 保持原位兜底)。
     */
    suspend fun insertBefore(
        workspaceId: String,
        beforeWorkspaceId: String? = null
    ): WorkspaceInsertBeforeValue {
        val args = buildJsonObject {
            putJsonObject("request") {
                put("workspaceId", workspaceId)
                beforeWorkspaceId?.let { put("beforeWorkspaceId", it) }
            }
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceInsertBeforeValue>(
            api.callRemote(DshEndpoints.WORKSPACE_INSERT_BEFORE, args)
        )
        reorder(value.workspaceIds)
        return value
    }

    /** workspace/insertSessionBefore:把会话移入(或在工作区内排序)指定工作区 */
    suspend fun insertSessionBefore(
        workspaceId: String,
        sessionId: String,
        beforeSessionId: String? = null
    ): WorkspaceInsertSessionBeforeValue {
        val args = buildJsonObject {
            putJsonObject("request") {
                put("workspaceId", workspaceId)
                put("sessionId", sessionId)
                beforeSessionId?.let { put("beforeSessionId", it) }
            }
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceInsertSessionBeforeValue>(
            api.callRemote(DshEndpoints.WORKSPACE_INSERT_SESSION_BEFORE, args)
        )
        upsert(value.workspace)
        return value
    }

    /** workspace/archiveSession:归档(非破坏性);响应回带完整归档集合,收敛替换 */
    suspend fun archiveSession(sessionId: String): WorkspaceArchiveSessionValue {
        val args = buildJsonObject {
            putJsonObject("request") { put("sessionId", sessionId) }
        }
        val value = DshJson.decodeFromJsonElement<WorkspaceArchiveSessionValue>(
            api.callRemote(DshEndpoints.WORKSPACE_ARCHIVE_SESSION, args)
        )
        _archivedSessionIds.value = value.archivedSessionIds
        return value
    }

    /** workspace/follow 帧(基线 + 四类增量) */
    private fun onWorkspaceFrame(frame: WorkspaceFollowFrame) {
        if (disposed) return
        when (frame.type) {
            "baseline" -> {
                val baseline = frame.value ?: return
                _workspaces.value = baseline.items.mapNotNull(::decodeWorkspace)
                _archivedSessionIds.value = baseline.archivedSessionIds
            }

            "upsert" -> frame.workspace?.let { decodeWorkspace(it) }?.let(::upsert)

            "remove" -> frame.workspaceId?.let { id ->
                _workspaces.value = _workspaces.value.filterNot { it.workspaceId == id }
            }

            "order" -> frame.workspaceIds?.let(::reorder)

            "archived" -> frame.archivedSessionIds?.let { _archivedSessionIds.value = it }
        }
    }

    private fun decodeWorkspace(json: kotlinx.serialization.json.JsonObject): WorkspaceView? =
        runCatching {
            DshJson.decodeFromJsonElement(WorkspaceView.serializer(), json)
        }.getOrNull()

    private fun reorder(workspaceIds: List<String>) {
        val byId = _workspaces.value.associateBy { it.workspaceId }.toMutableMap()
        val next = ArrayList<WorkspaceView>(byId.size)
        for (id in workspaceIds) {
            byId.remove(id)?.let(next::add)
        }
        next.addAll(byId.values)
        _workspaces.value = next
    }

    private fun upsert(workspace: WorkspaceView) {
        val current = _workspaces.value
        val idx = current.indexOfFirst { it.workspaceId == workspace.workspaceId }
        _workspaces.value = if (idx >= 0) {
            current.toMutableList().also { it[idx] = workspace }
        } else {
            current + workspace
        }
    }
}
