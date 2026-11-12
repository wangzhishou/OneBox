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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration

/**
 * RPC 业务错误(RpcResult 的 error 分支)。
 *
 * **错误码词表在 0.1.5-rc.2 整体改名**:从裸短码(`bad-request`)改成
 * `命名空间/短码`(`gateway/bad-request`、`session/not-found` …),
 * 另有 gateway 基础设施码(`gateway/arguments-invalid` 等)。
 *
 * 处理纪律:码**原样保留**——不能像旧版那样把不认识的码折叠成 `internal`,
 * 否则新协议的错误全部退化成无信息的 internal。
 */
@Serializable
data class RpcError(
    val code: String,
    val message: String,
    val details: JsonObject? = null
) {
    companion object {
        /** 构造错误:码原样保留(是否为已知码见 [RpcErrorCodes.isKnown]) */
        fun of(code: String, message: String, details: JsonObject? = null): RpcError =
            RpcError(code = code, message = message, details = details)
    }
}

/**
 * 错误码词表(DSH 0.1.5-rc.2):gateway 基础设施码 + 各域斜杠码 + 本地兜底码。
 * 旧版裸短码只在 [modernize] 里做识别映射,新主机不会再发。
 */
object RpcErrorCodes {

    // ── gateway 基础设施 ──
    const val GatewayAmbiguousEndpoint = "gateway/ambiguous-endpoint"
    const val GatewayArgumentsInvalid = "gateway/arguments-invalid"
    const val GatewayBadRequest = "gateway/bad-request"
    const val GatewayBindingInvalid = "gateway/binding-invalid"
    const val GatewayContextFailed = "gateway/context-failed"
    const val GatewayContextNotFound = "gateway/context-not-found"
    const val GatewayContextUnavailable = "gateway/context-unavailable"
    const val GatewayDefinitionUnavailable = "gateway/definition-unavailable"
    const val GatewayInputInvalid = "gateway/input-invalid"
    const val GatewayInternal = "gateway/internal"
    const val GatewayInvocationUnavailable = "gateway/invocation-unavailable"
    const val GatewayLookupFailed = "gateway/lookup-failed"
    const val GatewayLookupNotFound = "gateway/lookup-not-found"
    const val GatewayLookupUnavailable = "gateway/lookup-unavailable"
    const val GatewayMethodUnavailable = "gateway/method-unavailable"
    const val GatewayProviderMismatch = "gateway/provider-mismatch"
    const val GatewayResultInvalid = "gateway/result-invalid"
    const val GatewayServiceUnavailable = "gateway/service-unavailable"
    const val GatewaySignatureInvalid = "gateway/signature-invalid"

    // ── session 域 ──
    const val SessionNotFound = "session/not-found"
    const val SessionModelUnavailable = "session/model-unavailable"
    const val SessionConflict = "session/conflict"
    const val SessionAgentBusy = "session/agent-busy"
    const val SessionInvalidTimeZone = "session/invalid-time-zone"
    const val SessionWorkspaceAttachFailed = "session/workspace-attach-failed"
    const val SessionAttachmentInvalid = "session/attachment-invalid"
    const val SessionQueueItemNotFound = "session/queue-item-not-found"
    const val SessionSteerUnavailable = "session/steer-unavailable"
    const val SessionTitleInvalid = "session/title-invalid"
    const val SessionForkUnavailable = "session/fork-unavailable"

    // ── workspace / directory-picker 域 ──
    const val WorkspaceNotFound = "workspace/not-found"
    const val WorkspaceInvalidPath = "workspace/invalid-path"
    const val WorkspaceNameConflict = "workspace/name-conflict"
    const val WorkspaceMoveInvalid = "workspace/move-invalid"
    const val DirectoryPickerUnavailable = "directory-picker/unavailable"
    const val DirectoryUnreadable = "directory-picker/unreadable"
    const val DirectoryExists = "directory-picker/exists"
    const val DirectoryCreateFailed = "directory-picker/create-failed"

    // ── settings / credentials / llm 域 ──
    const val SettingsRejected = "settings/rejected"
    const val SettingsConflict = "settings/conflict"
    const val CredentialRejected = "credential/rejected"
    const val ModelDiscoveryRejected = "llm/model-discovery-rejected"

    // ── agent-preset 域 ──
    const val AgentPresetNotFound = "agent-preset/not-found"
    const val AgentPresetInvalid = "agent-preset/invalid"
    const val AgentPresetReadOnly = "agent-preset/read-only"
    const val AgentPresetLocked = "agent-preset/locked"
    const val AgentPresetConflict = "agent-preset/conflict"

    // ── subagent 域 ──
    const val SubagentNotFound = "subagent/not-found"
    const val SubagentCatalogDiagnostic = "subagent/catalog-diagnostic"
    const val SubagentInvalidTimeZone = "subagent/invalid-time-zone"
    const val SubagentParentUnavailable = "subagent/parent-unavailable"
    const val SubagentNotResumable = "subagent/not-resumable"
    const val SubagentUnauthorized = "subagent/unauthorized"
    const val SubagentAttachmentInvalid = "subagent/attachment-invalid"
    const val SubagentDeliveryUnavailable = "subagent/delivery-unavailable"
    const val SubagentProjectionsUnavailable = "subagent/projections-unavailable"

    // ── commands 域(0.1.5 新增服务端命令目录) ──
    const val UnknownCommand = "commands/unknown"
    const val CommandError = "commands/error"

    // ── 本地兜底码(非主机上报) ──
    const val Internal = "internal"
    const val Cancelled = "cancelled"

    /** 鉴权类码 */
    val AuthCodes: Set<String> = setOf(GatewayBadRequest, "unauthorized", "forbidden")

    /** 参数/载荷形状错误:几乎总是客户端与主机 Harness 版本不匹配 */
    val ProtocolMismatchCodes: Set<String> = setOf(
        GatewayArgumentsInvalid, GatewayInputInvalid, GatewaySignatureInvalid,
        GatewayBindingInvalid, GatewayProviderMismatch, GatewayAmbiguousEndpoint
    )

    /** 端点不存在:版本不匹配的强信号 */
    val EndpointMissingCodes: Set<String> = setOf(
        GatewayMethodUnavailable, GatewayServiceUnavailable,
        GatewayDefinitionUnavailable, GatewayInvocationUnavailable
    )

    /** 词表全集(0.1.5 斜杠码 + 本地兜底码) */
    val All: Set<String> = setOf(
        GatewayAmbiguousEndpoint, GatewayArgumentsInvalid, GatewayBadRequest,
        GatewayBindingInvalid, GatewayContextFailed, GatewayContextNotFound,
        GatewayContextUnavailable, GatewayDefinitionUnavailable, GatewayInputInvalid,
        GatewayInternal, GatewayInvocationUnavailable, GatewayLookupFailed,
        GatewayLookupNotFound, GatewayLookupUnavailable, GatewayMethodUnavailable,
        GatewayProviderMismatch, GatewayResultInvalid, GatewayServiceUnavailable,
        GatewaySignatureInvalid,
        SessionNotFound, SessionModelUnavailable, SessionConflict, SessionAgentBusy,
        SessionInvalidTimeZone, SessionWorkspaceAttachFailed, SessionAttachmentInvalid,
        SessionQueueItemNotFound, SessionSteerUnavailable, SessionTitleInvalid,
        SessionForkUnavailable,
        WorkspaceNotFound, WorkspaceInvalidPath, WorkspaceNameConflict, WorkspaceMoveInvalid,
        DirectoryPickerUnavailable, DirectoryUnreadable, DirectoryExists, DirectoryCreateFailed,
        SettingsRejected, SettingsConflict, CredentialRejected, ModelDiscoveryRejected,
        AgentPresetNotFound, AgentPresetInvalid, AgentPresetReadOnly, AgentPresetLocked,
        AgentPresetConflict,
        SubagentNotFound, SubagentCatalogDiagnostic, SubagentInvalidTimeZone,
        SubagentParentUnavailable, SubagentNotResumable, SubagentUnauthorized,
        SubagentAttachmentInvalid, SubagentDeliveryUnavailable, SubagentProjectionsUnavailable,
        UnknownCommand, CommandError,
        Internal, Cancelled
    )

    /** 是否为词表内已知码(仅用于日志/诊断,不参与折叠) */
    fun isKnown(code: String): Boolean = code in All

    /** 旧版裸短码 → 0.1.5 斜杠码(仅诊断/展示用;wire 上收到的码一律原样保留) */
    fun modernize(code: String): String = when (code) {
        "bad-request" -> GatewayBadRequest
        "session-not-found" -> SessionNotFound
        "model-unavailable" -> SessionModelUnavailable
        "session-conflict" -> SessionConflict
        "agent-busy" -> SessionAgentBusy
        "invalid-time-zone" -> SessionInvalidTimeZone
        "workspace-attach-failed" -> SessionWorkspaceAttachFailed
        "workspace-not-found" -> WorkspaceNotFound
        "workspace-invalid-path" -> WorkspaceInvalidPath
        "workspace-name-conflict" -> WorkspaceNameConflict
        "workspace-move-invalid" -> WorkspaceMoveInvalid
        "directory-unreadable" -> DirectoryUnreadable
        "directory-exists" -> DirectoryExists
        "directory-create-failed" -> DirectoryCreateFailed
        "directory-picker-unavailable" -> DirectoryPickerUnavailable
        "agent-preset-read-only" -> AgentPresetReadOnly
        "agent-preset-locked" -> AgentPresetLocked
        "agent-preset-conflict" -> AgentPresetConflict
        "agent-preset-not-found" -> AgentPresetNotFound
        "agent-preset-invalid" -> AgentPresetInvalid
        "attachment-error" -> SessionAttachmentInvalid
        "queue-item-not-found" -> SessionQueueItemNotFound
        "steer-unavailable" -> SessionSteerUnavailable
        "command-error" -> CommandError
        "unknown-command" -> UnknownCommand
        "settings-rejected" -> SettingsRejected
        "settings-conflict" -> SettingsConflict
        "credential-rejected" -> CredentialRejected
        "model-discovery-failed" -> ModelDiscoveryRejected
        "title-invalid" -> SessionTitleInvalid
        "fork-unavailable" -> SessionForkUnavailable
        "subagent-parent-unavailable" -> SubagentParentUnavailable
        "subagent-not-found" -> SubagentNotFound
        "subagent-catalog-diagnostic" -> SubagentCatalogDiagnostic
        "subagent-not-resumable" -> SubagentNotResumable
        "subagent-unauthorized" -> SubagentUnauthorized
        "subagent-delivery-unavailable" -> SubagentDeliveryUnavailable
        else -> code
    }
}

/** 业务失败:RpcResult 的 ok:false 分支,内含原始(未折叠)错误码 */
class RpcBusinessException(val error: RpcError) : Exception(
    buildString {
        append("RpcBusinessException(")
        append(error.code)
        append(": ")
        append(error.message)
        append(')')
    }
) {
    /** 参数/端点形状错误 → 通常意味着客户端与主机 Harness 版本不匹配 */
    val isProtocolMismatch: Boolean
        get() = error.code in RpcErrorCodes.ProtocolMismatchCodes ||
            error.code in RpcErrorCodes.EndpointMissingCodes
}

/** 载波/传输层失败:连接拒绝、HTTP 非 200、信封畸形 */
class CarrierException(
    val reason: String,
    val httpStatus: Int? = null,
    cause: Throwable? = null
) : Exception(
    buildString {
        append("CarrierException(")
        append(reason)
        if (httpStatus != null) {
            append(" (http ")
            append(httpStatus)
            append(')')
        }
    },
    cause
)

/** unary 超时(默认 30s;目录选择等用户节奏方法由调用方放宽) */
class ApiTimeoutException(
    val method: String,
    val limit: Duration
) : Exception(
    buildString {
        append("ApiTimeoutException(")
        append(method)
        append(" after ")
        append(limit.inWholeMilliseconds)
        append("ms)")
    }
)
