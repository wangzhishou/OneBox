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

package com.wanbaohe.dsh.component

import com.wanbaohe.dsh.connection.ConnectionSnapshot
import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.PrivilegeScope
import com.wanbaohe.dsh.connection.StoredHost
import com.wanbaohe.dsh.session.CommandStore
import com.wanbaohe.dsh.session.FeedbackStore
import com.wanbaohe.dsh.session.GoalStore
import com.wanbaohe.dsh.session.InteractorStore
import com.wanbaohe.dsh.session.PendingImage
import com.wanbaohe.dsh.session.QueueStore
import com.wanbaohe.dsh.session.SessionStore
import com.wanbaohe.dsh.session.SettingsStore
import com.wanbaohe.dsh.session.SkillCatalog
import com.wanbaohe.dsh.session.SubagentStore
import com.wanbaohe.dsh.session.WorkspaceStore

/** 模块内页面:连接页(Connect)→ 聊天页(Chat);配对页(Pairing,P6 网关配对/登录);App 壳只认 DshRootComponent,页面在内部切换 */
enum class DshPage { Connect, Chat, Pairing }

/**
 * 连接页 UiState:地址输入、主机簿(复合键条目,P6)、连接快照(代际/阶段/失败原因)、
 * 401 标记、页面状态、设备名(配对上报)。
 * [privilegeScope] 特权面可见性(DSH-PROTOCOL §6):由当前连接目标推断,
 * 未连接时按 Lan(隐藏特权入口,fail-closed)。
 */
data class DshUiState(
    val address: String = "",
    val savedHosts: List<StoredHost> = emptyList(),
    val snapshot: ConnectionSnapshot = ConnectionSnapshot(
        generation = 0,
        phase = ConnectionPhase.Down
    ),
    val authBlocked: Boolean = false,
    val page: DshPage = DshPage.Connect,
    val privilegeScope: PrivilegeScope = PrivilegeScope.Lan,
    /** 本机设备名(配对/登录上报给网关的 device 字段) */
    val deviceName: String = "",
    /** 云端中继(P7)面板状态 */
    val cloud: CloudUiState = CloudUiState(),
    /** 当前连接是否为云端中继形态(401 文案/动作用:云端 401 = App 登录过期) */
    val currentHostIsCloud: Boolean = false
)

/**
 * 云端中继面板状态(P7):[available] = 有可用 App token(登录态或游客 token);
 * [code]/[expiresAtEpochMs] 为最近一次申请到的绑定码与过期时刻;
 * [claiming] = 扫码认领进行中(P8,按钮 loading)。
 */
data class CloudUiState(
    val available: Boolean = false,
    val requesting: Boolean = false,
    val claiming: Boolean = false,
    val code: String = "",
    val expiresAtEpochMs: Long = 0,
    /** 一次性错误(申请失败/认领失败/未登录等,已本地化) */
    val error: String? = null,
    /** 中继被 401 拒(token 失效/游客账号):需要重新登录,UI 给登录入口 */
    val authExpired: Boolean = false
)

/** 一次连接实例的会话域 store 组(随控制器同生共死;非 @Singleton) */
class ChatBundle(
    val sessionStore: SessionStore,
    val workspaceStore: WorkspaceStore,
    val interactorStore: InteractorStore,
    val queueStore: QueueStore,
    val goalStore: GoalStore,
    val skillCatalog: SkillCatalog,
    val commandStore: CommandStore,
    val subagentStore: SubagentStore,
    val settingsStore: SettingsStore,
    val feedbackStore: FeedbackStore
) {
    fun dispose() {
        sessionStore.dispose()
        workspaceStore.dispose()
        interactorStore.dispose()
        queueStore.dispose()
        goalStore.dispose()
        skillCatalog.dispose()
        commandStore.dispose()
        subagentStore.dispose()
        settingsStore.dispose()
        feedbackStore.dispose()
    }
}

/** 聊天页 UiState:选中会话、输入框、待发图片、发送/创建/导出中标记、一次性错误 */
data class ChatUiState(
    val selectedSessionId: String? = null,
    val input: String = "",
    /** 已 intake 待发送的图片附件(预览条数据源) */
    val attachments: List<PendingImage> = emptyList(),
    /** session.prompt 的 mode:queue 排队 / steer 插话(composer 可切换) */
    val promptMode: String = SessionStore.PromptModeQueue,
    val sending: Boolean = false,
    val creating: Boolean = false,
    val exporting: Boolean = false,
    val error: String? = null
)
