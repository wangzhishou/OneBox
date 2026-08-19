package com.wanbaohe.dsh.wire

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration

/**
 * RPC 业务错误(RpcResult 的 error 分支)。
 *
 * 错误码是封闭集合(提取自 DSH 的 RpcErrorDetailsMap,共 40 个),
 * 未收录的码一律兜底为 [RpcErrorCodes.Internal]。
 */
@Serializable
data class RpcError(
    val code: String,
    val message: String,
    val details: JsonObject? = null
) {
    companion object {
        /** 构造并归一化错误码:未知码折叠为 internal,消息/details 原样保留 */
        fun of(code: String, message: String, details: JsonObject? = null): RpcError =
            RpcError(
                code = if (code in RpcErrorCodes.All) code else RpcErrorCodes.Internal,
                message = message,
                details = details
            )
    }
}

/** 封闭错误码集(RpcErrorDetailsMap 的键,共 40 个) */
object RpcErrorCodes {
    const val BadRequest = "bad-request"
    const val Cancelled = "cancelled"
    const val SessionNotFound = "session-not-found"
    const val ModelUnavailable = "model-unavailable"
    const val SessionConflict = "session-conflict"
    const val InvalidTimeZone = "invalid-time-zone"
    const val WorkspaceAttachFailed = "workspace-attach-failed"
    const val WorkspaceNotFound = "workspace-not-found"
    const val WorkspaceInvalidPath = "workspace-invalid-path"
    const val WorkspaceNameConflict = "workspace-name-conflict"
    const val WorkspaceMoveInvalid = "workspace-move-invalid"
    const val DirectoryUnreadable = "directory-unreadable"
    const val DirectoryExists = "directory-exists"
    const val DirectoryCreateFailed = "directory-create-failed"
    const val DirectoryPickerUnavailable = "directory-picker-unavailable"
    const val AgentPresetReadOnly = "agent-preset-read-only"
    const val AgentPresetLocked = "agent-preset-locked"
    const val AgentPresetConflict = "agent-preset-conflict"
    const val AgentPresetNotFound = "agent-preset-not-found"
    const val AgentPresetInvalid = "agent-preset-invalid"
    const val AgentBusy = "agent-busy"
    const val AttachmentError = "attachment-error"
    const val QueueItemNotFound = "queue-item-not-found"
    const val SteerUnavailable = "steer-unavailable"
    const val CommandError = "command-error"
    const val UnknownCommand = "unknown-command"
    const val SettingsRejected = "settings-rejected"
    const val SettingsNotExposed = "settings-not-exposed"
    const val SettingsConflict = "settings-conflict"
    const val CredentialRejected = "credential-rejected"
    const val ModelDiscoveryFailed = "model-discovery-failed"
    const val TitleInvalid = "title-invalid"
    const val ForkUnavailable = "fork-unavailable"
    const val SubagentParentUnavailable = "subagent-parent-unavailable"
    const val SubagentNotFound = "subagent-not-found"
    const val SubagentCatalogDiagnostic = "subagent-catalog-diagnostic"
    const val SubagentNotResumable = "subagent-not-resumable"
    const val SubagentUnauthorized = "subagent-unauthorized"
    const val SubagentDeliveryUnavailable = "subagent-delivery-unavailable"

    /** 兜底码:任何不在封闭集内的错误码都折叠到这里 */
    const val Internal = "internal"

    /** 封闭集合全量(含 internal,共 40 个) */
    val All: Set<String> = setOf(
        BadRequest, Cancelled, SessionNotFound, ModelUnavailable, SessionConflict,
        InvalidTimeZone, WorkspaceAttachFailed, WorkspaceNotFound, WorkspaceInvalidPath,
        WorkspaceNameConflict, WorkspaceMoveInvalid, DirectoryUnreadable, DirectoryExists,
        DirectoryCreateFailed, DirectoryPickerUnavailable, AgentPresetReadOnly,
        AgentPresetLocked, AgentPresetConflict, AgentPresetNotFound, AgentPresetInvalid,
        AgentBusy, AttachmentError, QueueItemNotFound, SteerUnavailable, CommandError,
        UnknownCommand, SettingsRejected, SettingsNotExposed, SettingsConflict,
        CredentialRejected, ModelDiscoveryFailed, TitleInvalid, ForkUnavailable,
        SubagentParentUnavailable, SubagentNotFound, SubagentCatalogDiagnostic,
        SubagentNotResumable, SubagentUnauthorized, SubagentDeliveryUnavailable, Internal
    )
}

/** 业务失败:RpcResult 的 ok:false 分支,内含封闭错误码的 [RpcError] */
class RpcBusinessException(val error: RpcError) : Exception(
    "RpcBusinessException(${error.code}: ${error.message})"
)

/** 载波/传输层失败:连接拒绝、HTTP 非 200、信封畸形、rpcId 不回显 */
class CarrierException(
    val reason: String,
    val httpStatus: Int? = null,
    cause: Throwable? = null
) : Exception(
    "CarrierException($reason${httpStatus?.let { " (http $it)" } ?: ""})",
    cause
)

/** unary 超时(默认 30s;host.pickDirectory 等用户节奏方法由调用方放宽) */
class ApiTimeoutException(
    val method: String,
    val limit: Duration
) : Exception("ApiTimeoutException($method after ${limit.inWholeMilliseconds}ms)")
