package com.shifenmiao.model.ai

import android.os.Parcelable
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
    @Parcelize @Serializable data object MinMax : AiProvider("minMax")
    @Parcelize @Serializable data object ZhiPu : AiProvider("zhipu")
    @Parcelize @Serializable data object OpenRouter : AiProvider("openrouter")
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

    companion object {
        fun fromValue(providerName: String?): AiProvider {
            return when (providerName?.trim()?.lowercase()) {
                QWen.value -> QWen
                OpenAi.value -> OpenAi
                Kimi.value -> Kimi
                DouBao.value -> DouBao
                Tencent.value -> Tencent
                DeepSeek.value -> DeepSeek
                MinMax.value.lowercase() -> MinMax
                ZhiPu.value -> ZhiPu
                OpenRouter.value -> OpenRouter
                Gemini.value -> Gemini
                Grok.value -> Grok
                Claude.value -> Claude
                Mimo.value -> Mimo
                Baidu.value -> Baidu
                Local.value -> Local
                else -> Default
            }
        }
    }
}

class AiProviderTypeAdapter : TypeAdapter<AiProvider>() {
    override fun write(out: JsonWriter, value: AiProvider?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(value.value)
    }

    override fun read(reader: JsonReader): AiProvider {
        val providerString = when (reader.peek()) {
            JsonToken.STRING -> reader.nextString()
            JsonToken.BEGIN_OBJECT -> {
                var value: String? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (name == "value" && reader.peek() == JsonToken.STRING) {
                        value = reader.nextString()
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()
                value.orEmpty()
            }
            JsonToken.NULL -> {
                reader.nextNull()
                ""
            }
            else -> {
                reader.skipValue()
                ""
            }
        }
        return AiProvider.fromValue(providerString)
    }
}
