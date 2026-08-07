package com.shifenmiao.model.ai

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 认证类型枚举
 *
 * 用于区分不同的API认证方式：
 * - BEARER: 标准OAuth2 Bearer Token (Authorization: Bearer <token>)
 * - API_KEY: 自定义API Key Header (api-key: <token>)
 * - NONE: 无认证（通过代理或公开接口）
 *
 * 一般情况下认证方式会跟随 [AiRequestProtocol]：
 * - Anthropic 兼容协议默认走 API Key
 * - 自建代理默认不要求额外认证
 * - 其余协议默认走 Bearer
 *
 * 但部分 provider 会“跑偏”，因此仍然保留显式 authType 字段用于覆盖默认推断。
 */
@Parcelize
@Serializable
enum class AuthType : Parcelable {
    @SerialName("bearer")
    BEARER,

    @SerialName("api_key")
    API_KEY,

    @SerialName("none")
    NONE;

    companion object {
        fun defaultFor(requestProtocol: AiRequestProtocol): AuthType {
            return when (requestProtocol) {
                AiRequestProtocol.ANTHROPIC_COMPATIBLE -> API_KEY
                AiRequestProtocol.OWN_PROXY -> NONE
                AiRequestProtocol.OPENAI_COMPATIBLE,
                AiRequestProtocol.RESPONSES_COMPATIBLE -> BEARER
                // 端侧本地推理不涉及网络鉴权；
                // 显式返回 NONE，避免被云端默认 BEARER 误填 Authorization 头污染请求。
                AiRequestProtocol.LOCAL_ON_DEVICE -> NONE
            }
        }

        fun fromValue(value: String?): AuthType {
            val normalizedValue = value
                ?.trim()
                ?.replace('-', '_')
                ?.replace(' ', '_')
                ?.lowercase()

            return when (normalizedValue) {
                null, "" -> BEARER
                "bearer", "authorization", "bearer_token" -> BEARER
                "api_key", "apikey", "api-key" -> API_KEY
                "none", "no_auth", "noauth" -> NONE
                else -> entries.firstOrNull {
                    it.name.lowercase() == normalizedValue
                } ?: BEARER
            }
        }

        fun resolve(value: String?, requestProtocol: AiRequestProtocol): AuthType {
            return if (value.isNullOrBlank()) {
                defaultFor(requestProtocol)
            } else {
                fromValue(value)
            }
        }
    }
}
