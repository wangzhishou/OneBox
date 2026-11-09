package com.shifenmiao.model.ai

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Parcelize
@Serializable
sealed class AiProvider(val value: String) : Parcelable {
    @Parcelize @Serializable data object OpenAi : AiProvider("openai")
    @Parcelize @Serializable data object Kimi : AiProvider("kimi")
    @Parcelize @Serializable data object QWen : AiProvider("qwen")
    @Parcelize @Serializable data object Default : AiProvider("default")
    @Parcelize @Serializable data object DouBao : AiProvider("doubao")
    @Parcelize @Serializable data object Tencent : AiProvider("tencent")
    @Parcelize @Serializable data object DeepSeek : AiProvider("deepseek")
    // value 统一小写,与远程目录引擎名一致(曾用 camelCase "minMax",
    // DAO 精确匹配下与远程 "minmax" 去重失败导致列表出现两个 MiniMax;v6 迁移清理旧行)
    @Parcelize @Serializable data object MinMax : AiProvider("minmax")
    @Parcelize @Serializable data object ZhiPu : AiProvider("zhipu")
    @Parcelize @Serializable data object OpenRouter : AiProvider("openrouter")
    @Parcelize @Serializable data object Requesty : AiProvider("requesty")
    @Parcelize @Serializable data object Gemini : AiProvider("gemini")
    @Parcelize @Serializable data object Grok : AiProvider("grok")
    @Parcelize @Serializable data object Claude : AiProvider("claude")
    @Parcelize @Serializable data object Mimo : AiProvider("mimo")
    @Parcelize @Serializable data object Baidu : AiProvider("baidu")

    /**
     * 端侧本地推理（llama.cpp / MediaPipe / ONNX 等）。
     *
     * 语义上不代表"厂商"，而代表"运行位置 = 设备本地"。
     * 同一时间只允许一个本地引擎处于工作槽位，避免内存与 native runtime 互相争抢。
     * 设置页与统计分支应使用 AiEngine.requestProtocol == LOCAL_ON_DEVICE 判断，
     * 不应散落对 Local provider 的硬编码。
     */
    @Parcelize @Serializable data object Local : AiProvider("local")

    /**
     * TypeSafe System One 判断模型（Jev）。
     * 请求经 go-proxy 代理到 /ai/typesafe/systemone，鉴权由 App 登录 JWT 完成。
     */
    @Parcelize @Serializable data object Jev : AiProvider("jev")

    /**
     * Pikafish 象棋引擎（服务端 UCI/UCCI 远程走棋）。
     * 请求经 go-proxy 代理到 /xiangqi/engine/bestmove，鉴权由 App 登录 JWT 完成。
     */
    @Parcelize @Serializable data object Pikafish : AiProvider("pikafish")

    companion object {
        fun fromValue(providerName: String?): AiProvider {
            return when (providerName?.trim()?.lowercase()) {
                QWen.value -> QWen
                OpenAi.value -> OpenAi
                Kimi.value -> Kimi
                DouBao.value -> DouBao
                Tencent.value -> Tencent
                DeepSeek.value -> DeepSeek
                MinMax.value -> MinMax
                ZhiPu.value -> ZhiPu
                OpenRouter.value -> OpenRouter
                Requesty.value -> Requesty
                Gemini.value -> Gemini
                Grok.value -> Grok
                Claude.value -> Claude
                Mimo.value -> Mimo
                Baidu.value -> Baidu
                Local.value -> Local
                Jev.value, "typesafe" -> Jev
                Pikafish.value, "xiangqi_engine" -> Pikafish
                else -> Default
            }
        }
    }
}


/**
 * kotlinx.serialization 版 AiProvider 序列化器:
 * 写出 `"provider":"openai"` 字符串格式;读取兼容四种历史形态 ——
 * 字符串 / Gson 时代带 value 字段的对象 / v1.3.1 起 Room 存量的 kotlinx 多态形态
 * `{"type":"com.shifenmiao.model.ai.DeepSeek"}`(取末段类名)/ null,
 * 未知值一律回退 [AiProvider.Default]。
 */
object AiProviderKSerializer : KSerializer<AiProvider> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AiProvider", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AiProvider) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): AiProvider {
        if (decoder is JsonDecoder) {
            return when (val element = decoder.decodeJsonElement()) {
                is JsonObject -> AiProvider.fromValue(
                    (element["value"] as? JsonPrimitive)?.contentOrNull
                        ?: (element["type"] as? JsonPrimitive)?.contentOrNull
                            ?.substringAfterLast('.')
                )
                is JsonPrimitive -> AiProvider.fromValue(element.contentOrNull)
                else -> AiProvider.fromValue(null)
            }
        }
        return AiProvider.fromValue(decoder.decodeString())
    }
}
