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

package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable

/** workspace.list 的工作区行;createdAt/updatedAt 为 ISO 时间串 */
@Serializable
data class WorkspaceView(
    val workspaceId: String,
    val path: String,
    val title: String,
    val sessionIds: List<String>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class WorkspaceListValue(
    val items: List<WorkspaceView>,
    val archivedSessionIds: List<String>
)

/** workspace.create 上行载荷 */
@Serializable
data class WorkspaceCreateRequest(
    val path: String
)

@Serializable
data class WorkspaceCreateValue(
    val workspace: WorkspaceView,
    /** false 表示同路径已存在,服务端回带既有行(幂等) */
    val created: Boolean
)

/** workspace.rename 的响应 value:回带更新后的行(落地并广播,不等重取) */
@Serializable
data class WorkspaceRenameValue(
    val workspace: WorkspaceView
)

/** workspace.delete 的响应 value;删除非破坏性(会话移入未分组) */
@Serializable
data class WorkspaceDeleteValue(
    val deleted: Boolean
)

/** workspace.insertBefore 的响应 value:完整排序(整序收敛重排本地列表) */
@Serializable
data class WorkspaceInsertBeforeValue(
    val workspaceIds: List<String>
)

/** workspace.insertSessionBefore 的响应 value:回带更新后的行 */
@Serializable
data class WorkspaceInsertSessionBeforeValue(
    val workspace: WorkspaceView
)

/** workspace.archiveSession 的响应 value:完整归档集合(收敛语义,直接替换) */
@Serializable
data class WorkspaceArchiveSessionValue(
    val archivedSessionIds: List<String>
)
