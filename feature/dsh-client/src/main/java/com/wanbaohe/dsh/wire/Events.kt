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

import com.wanbaohe.dsh.wire.model.AskUserQuestionAnswer
import com.wanbaohe.dsh.wire.model.AskUserQuestionItem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

/**
 * `$events` 逻辑流的帧契约(DSH 0.1.5-rc.2,
 * `packages/api/gateway/src/stream-protocol.ts` 的 RemoteEventDownlinkFrame)。
 *
 * 这是**取代旧 `/api/events.host` + `POST /api/respond`** 的转发事件通道:
 * Host 在远端事件被选中转发时下发 `emit`(单向通知)或 `waterfall`(需要客户端
 * 应答的交互,目前是审批与结构化问答)。应答不走 HTTP RPC:
 * `POST /api/$events/result`,body 为 `{args:{clientId,eventId,outcome}}`。
 *
 * 幂等语义:`clientId` 标明当前流世代(每次重连都会变),`eventId` 标明一次待答
 * 请求;同一个 `eventId` 只会被第一个到达的应答占有,迟到者服务端忽略。
 * 重连后 Host 会重新发起仍在等待的 waterfall,因此客户端不要跨世代缓存待答项。
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class RemoteEventFrame {

    /** 流首帧:绑定当前世代身份,同时给出 Host 基础事实(home 目录) */
    @Serializable
    @SerialName("ready")
    data class Ready(
        val clientId: String,
        val host: ReadyHost = ReadyHost()
    ) : RemoteEventFrame()

    /** 单向通知:`event` 为 Cordis 事件名,`args` 为该事件的 wire 参数 */
    @Serializable
    @SerialName("emit")
    data class Emit(
        val event: String,
        val args: List<JsonElement> = emptyList()
    ) : RemoteEventFrame()

    /** 待答瀑布:必须回 [$events/result],否则 Host 侧一直挂起 */
    @Serializable
    @SerialName("waterfall")
    data class Waterfall(
        val event: String,
        val eventId: String,
        val agentId: String? = null,
        val request: JsonElement? = null
    ) : RemoteEventFrame()

    /** 取消一个此前下发的 waterfall(Host 侧已不再等待) */
    @Serializable
    @SerialName("cancel")
    data class Cancel(
        val eventId: String
    ) : RemoteEventFrame()
}

/** ready 帧携带的 Host 事实;只有 home(用于缩写路径),没有版本号 */
@Serializable
data class ReadyHost(
    val home: String? = null
)

/** `$events` 上需要客户端应答的事件名 */
object RemoteEventNames {
    const val APPROVAL_REQUEST = "approval/request"
    const val USER_QUESTIONS_REQUEST = "user-questions/request"

    const val SESSION_ADDED = "api-session/added"
    const val SESSION_REMOVED = "api-session/removed"
    const val SESSION_STATUS = "api-session/status"
    const val SESSION_ACTIVITY = "api-session/activity"
    const val SESSION_ERROR = "api-session/error"
}

/** 审批的应答值(闭合词表,服务端 fail-closed 到 unavailable) */
object ApprovalOutcome {
    const val ALLOWED_ONCE = "allowed-once"
    const val REJECTED = "rejected"
    const val CANCELLED = "cancelled"
}

/**
 * waterfall 事件的应答请求体(`$events/result` 的 args):
 * `{clientId, eventId, outcome}`;outcome 的 value 槽按事件类型装
 * 审批字符串或问答 [AskUserQuestionAnswer]。
 */
@Serializable
data class EventResultRequest(
    val clientId: String,
    val eventId: String,
    val outcome: EventResultOutcome
)

@Serializable
data class EventResultOutcome(
    val kind: String,
    val value: JsonElement? = null
) {
    companion object {
        const val KIND_RESULT = "result"
        const val KIND_NEXT = "next"
    }
}

/** 审批 waterfall 的请求体(agent 身份在帧的 agentId 上,不重复出现) */
@Serializable
data class ApprovalRequestPayload(
    val toolName: String = "",
    val callId: String? = null,
    val reason: String? = null
)

/** 问答 waterfall 的请求体 */
@Serializable
data class QuestionRequestPayload(
    val questions: List<AskUserQuestionItem> = emptyList()
)
