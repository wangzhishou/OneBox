package com.shifenmiao.model.ai

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
enum class AiRequestProtocol : Parcelable {
    @SerialName("openai_compatible")
    OPENAI_COMPATIBLE,

    /**
     * OpenAI Responses API。
     *
     * 与 Chat Completions 相比，Responses 原生支持 previous_response_id、
     * output item / function_call_output 等结构，因此单独建模为独立协议，
     * 避免在 OPENAI_COMPATIBLE 分支里堆积兼容判断。
     */
    @SerialName("responses_compatible")
    RESPONSES_COMPATIBLE,

    @SerialName("own_proxy")
    OWN_PROXY,

    @SerialName("anthropic_compatible")
    ANTHROPIC_COMPATIBLE,

    /**
     * 端侧本地推理（llama.cpp / MediaPipe / ONNX 等）。
     *
     * 注意：本地模型不应伪装为 OPENAI_COMPATIBLE，否则会污染 URL、Path、鉴权、
     * 代理、API Key 校验逻辑。本协议下 AiEngine 的 requestUrl / path / apiKey 字段
     * 全部失效，由 [LocalLlmModelSpec] 描述物理文件、backend、chat template 等。
     */
    @SerialName("local_on_device")
    LOCAL_ON_DEVICE;

    companion object {
        /**
         * 云端协议集合（不含 [LOCAL_ON_DEVICE]）。
         *
         * 用于"添加云端引擎"等 UI 入口，避免本地协议污染云端表单字段。
         * 本地模型走独立的"本地模型管理"页，Phase 2 引入。
         */
        val cloudProtocols: List<AiRequestProtocol>
            get() = entries.filter { it != LOCAL_ON_DEVICE }

        fun fromValue(value: String?): AiRequestProtocol {
            val normalizedValue = value
                ?.trim()
                ?.replace('-', '_')
                ?.replace(' ', '_')
                ?.lowercase()

            return when (normalizedValue) {
                null, "" -> OPENAI_COMPATIBLE
                "openai_compatible", "openaicompatible", "openai", "compat", "compatible" -> OPENAI_COMPATIBLE
                "responses_compatible", "responsescompatible", "responses_api", "responsesapi", "responses", "response" -> RESPONSES_COMPATIBLE
                "own_proxy", "ownproxy", "proxy", "self_proxy", "self_hosted_proxy" -> OWN_PROXY
                "anthropic_compatible", "anthropiccompatible", "anthropic", "claude", "mimo" -> ANTHROPIC_COMPATIBLE
                "local_on_device", "local", "on_device", "ondevice", "local_llm", "localllm" -> LOCAL_ON_DEVICE
                else -> entries.firstOrNull {
                    it.name.lowercase() == normalizedValue
                } ?: OPENAI_COMPATIBLE
            }
        }
    }
}
