package com.wanbaohe.setting.ai.component

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType

// 各云端协议的默认 API 路径(统一去掉开头 / 再比较), 用于切换协议时识别"用户没改过的默认值"
private val PROTOCOL_DEFAULT_PATHS = setOf(
    "v1/chat/completions",
    "v1/responses",
    "v1/messages",
    UrlConstants.JEV_SYSTEMONE_ENDPOINT,
)

/**
 * 切换请求协议(新增页与详情页共用同一套, 避免两条入口的表单行为分叉):
 * - authType 跟随新协议默认(用户手改过的除外);
 * - Path 仍是上一个协议的默认值时一并跟随, 用户手改过的自定义路径保持不动;
 * - Jev 预填官网直连地址/端点与代理链路, Pikafish 清空直连字段(两者仅走 App 代理);
 * - 连接相关配置已变化, 已验证标记 isDetestPassed 一并复位。
 */
internal fun AiEngine.switchedRequestProtocol(protocol: AiRequestProtocol): AiEngine {
    val previousDefaultAuthType = AuthType.defaultFor(requestProtocol)
    val nextAuthType = if (authType == previousDefaultAuthType) {
        AuthType.defaultFor(protocol)
    } else {
        authType
    }
    val fallbackPath = when (protocol) {
        AiRequestProtocol.RESPONSES_COMPATIBLE -> "/v1/responses"
        AiRequestProtocol.ANTHROPIC_COMPATIBLE -> "/v1/messages"
        AiRequestProtocol.JEV -> UrlConstants.JEV_SYSTEMONE_ENDPOINT
        else -> "/v1/chat/completions"
    }
    val currentPath = requestPath.trim().trimStart('/')
    val isDefaultPath = currentPath.isEmpty() || currentPath in PROTOCOL_DEFAULT_PATHS
    return copy(
        requestProtocol = protocol,
        authType = nextAuthType,
        requestUrl = when (protocol) {
            AiRequestProtocol.JEV -> requestUrl.ifBlank { UrlConstants.TYPESAFE_AI_BASE_URL }
            AiRequestProtocol.PIKAFISH -> ""
            else -> requestUrl
        },
        requestPath = when (protocol) {
            AiRequestProtocol.PIKAFISH -> ""
            else -> if (isDefaultPath) fallbackPath else requestPath
        },
        proxyUrl = if (protocol == AiRequestProtocol.JEV || protocol == AiRequestProtocol.PIKAFISH) {
            proxyUrl.ifBlank { UrlConstants.RELEASE_URL }
        } else {
            proxyUrl
        },
        proxyPath = when (protocol) {
            AiRequestProtocol.JEV -> proxyPath.ifBlank { UrlConstants.JEV_PROXY_PATH }
            AiRequestProtocol.PIKAFISH -> proxyPath.ifBlank { UrlConstants.XIANGQI_ENGINE_PROXY_PATH }
            else -> proxyPath
        },
        isDetestPassed = false,
    )
}
