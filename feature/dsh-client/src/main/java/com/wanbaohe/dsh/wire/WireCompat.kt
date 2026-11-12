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

package com.wanbaohe.dsh.wire

/**
 * DSH 线协议版本锚点。
 *
 * 本模块的客户端代码只针对下面这一个 DeepSeek Harness 版本验证过;协议面
 * (端点命名、帧类型、错误码)在该版本上不存在向后兼容保证:
 *
 * - 旧版(`<= 0.1.1-rc.2`)用点号端点(`session.list`)、`/api/events.mux` + `/api/events.host`
 *   两条固定 WS、`host.describe` 探针、`POST /api/respond` 应答 —— 本客户端**不再支持**
 * - 当前版(`>= 0.1.2`)用斜杠端点(`session/list`)、单条 `/api/remote.mux` 多路复用流、
 *   `$events` 转发流 + `POST /api/$events/result` 应答 —— 本客户端按此实现
 *
 * 因此:换 Harness 版本后必须重新对接,不能假定老版本或新版本可连。握手失败时
 * 用 [WireCompat] 的常量做提示文案与日志,不要把版本号当成能力协商依据
 * (Harness 未暴露任何版本/能力端点的稳定面)。
 */
object WireCompat {

    /** 本客户端唯一验证通过的 Harness 版本(源码 checkout 与 npm `next` 同版本)。 */
    const val VERIFIED_HARNESS_VERSION = "0.1.5-rc.2"

    /** 验证通过的 Harness 发布线(用来判断"明显过旧/过新"的提示语)。 */
    const val VERIFIED_RELEASE_LINE = "0.1.x (>= 0.1.2)"

    /** 协议面变更导致本客户端不可用的最早版本;早于它的主机一律连不上。 */
    const val MINIMUM_HARNESS_RELEASE_LINE = "0.1.2"

    /** 未验证:比 this 版本更新的 Harness 可能再次变更协议面。 */
    const val COMPATIBILITY_NOTE =
        "仅在 DeepSeek Harness $VERIFIED_HARNESS_VERSION 上验证通过;不保证其他版本可用"
}

/** 端点名(集中一处,便于与 Harness 对照升级) */
object DshEndpoints {
    // HTTP 一元(mux 之外)
    const val SESSION_EXPORT = "/api/session.export"

    // Remote 端点(命名空间/方法)
    const val SESSION_LIST = "session/list"
    const val SESSION_SEARCH = "session/search"
    const val SESSION_CREATE = "session/create"
    const val SESSION_RENAME = "session/rename"
    const val SESSION_FORK = "session/fork"
    const val SESSION_PROMPT = "session/prompt"
    const val SESSION_CANCEL = "session/cancel"
    const val SESSION_UPDATE_QUEUE = "session/updateQueue"
    const val SESSION_MODEL_CATALOG = "session/modelCatalog"
    const val SESSION_SELECT_MODEL = "session/selectModel"
    const val SESSION_PAGE = "session/page"
    const val SESSION_FOLLOW = "session/follow"
    const val SESSION_CONTROL = "session/control"
    const val SESSION_ATTACHMENT = "session/attachment"
    const val SESSION_OPEN_WORKSPACE_PATH = "session/openWorkspacePath"

    const val SKILLS_LIST = "skills/list"

    const val WORKSPACE_CREATE = "workspace/create"
    const val WORKSPACE_RENAME = "workspace/rename"
    const val WORKSPACE_DELETE = "workspace/delete"
    const val WORKSPACE_INSERT_BEFORE = "workspace/insertBefore"
    const val WORKSPACE_INSERT_SESSION_BEFORE = "workspace/insertSessionBefore"
    const val WORKSPACE_ARCHIVE_SESSION = "workspace/archiveSession"
    const val WORKSPACE_FOLLOW = "workspace/follow"

    const val SETTINGS_DESCRIBE = "settings/describe"
    const val SETTINGS_MUTATE = "settings/mutate"
    const val SETTINGS_UPDATE = "settings/update"
    const val SETTINGS_OPEN_DOCUMENT = "settings/openSettingsDocument"

    const val CREDENTIALS_DESCRIBE = "credentials/describe"
    const val CREDENTIALS_SET = "credentials/set"
    const val CREDENTIALS_UNSET = "credentials/unset"

    const val LLM_LIST_PROVIDERS = "llm/listProviders"

    /**
     * 带配置位置的提供方目录(0.1.5 新增):回值 `{provider, displayName, settingsNs,
     * settingsPath, declared?, error?}`,可直接定位可编辑配置段,免去按 settings 快照猜。
     * `llm/listProviders` 只给 `{id, name}`。
     */
    const val LLM_LIST_CONFIGURABLE_PROVIDERS = "llm/listConfigurableProviders"

    const val LLM_DISCOVER_MODELS = "llm/discoverModels"

    const val GOALS_GET = "goals/get"
    const val GOALS_CREATE = "goals/create"
    const val GOALS_EDIT = "goals/edit"
    const val GOALS_PAUSE = "goals/pause"
    const val GOALS_RESUME = "goals/resume"
    const val GOALS_COMPLETE = "goals/complete"
    const val GOALS_CLEAR = "goals/clear"

    const val SUBAGENTS_LIST = "subagents/list"
    const val SUBAGENTS_PROMPT = "subagents/prompt"
    const val SUBAGENTS_INTERRUPT_BY_PARENT = "subagents/interruptByParent"

    const val COMMANDS_LIST = "commands/list"
    const val COMMANDS_EXECUTE = "commands/execute"

    // 逻辑流 / 转发事件
    const val MUX_PATH = "/api/remote.mux"
    /** `$events` 逻辑流(注意 Kotlin 字符串里 $ 要转义) */
    const val EVENT_STREAM = "\$events"
    const val EVENT_RESULT = "\$events/result"
}
