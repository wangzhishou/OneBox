package com.shifenmiao.ai.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiProviderTypeAdapter
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Keep
@Parcelize
data class AIDuelConfig(
    @SerializedName("personaA")
    val personaA: String = "",
    @SerializedName("personaB")
    val personaB: String = "",
    @SerializedName("avatarA")
    val avatarA: String = "",
    @SerializedName("avatarB")
    val avatarB: String = "",
    @SerializedName("maxRounds")
    val maxRounds: Int = 5,
    @SerializedName("engineA")
    val engineA: AiEngine? = null,
    @SerializedName("engineB")
    val engineB: AiEngine? = null,
    @SerializedName("promptIdA")
    val promptIdA: Int = 0,
    @SerializedName("promptIdB")
    val promptIdB: Int = 0,
    @SerializedName("promptNameA")
    val promptNameA: String = "",
    @SerializedName("promptNameB")
    val promptNameB: String = "",
    @SerializedName("roleNameA")
    val roleNameA: String = "",
    @SerializedName("roleNameB")
    val roleNameB: String = ""
) : Parcelable

internal object AIDuelConfigCodec {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(AiProvider::class.java, AiProviderTypeAdapter())
        .create()

    fun encode(config: AIDuelConfig): String = gson.toJson(config)

    fun decodeOrNull(prompt: String): AIDuelConfig? = kotlin.runCatching {
        val obj = JsonParser.parseString(prompt).asJsonObject

        fun str(key: String): String =
            obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()

        fun int(key: String, default: Int): Int =
            obj.get(key)?.takeIf { it.isJsonPrimitive }?.asInt ?: default

        val engineA = kotlin.runCatching {
            obj.get("engineA")?.let { gson.fromJson(it, AiEngine::class.java) }
        }.getOrNull()
        val engineB = kotlin.runCatching {
            obj.get("engineB")?.let { gson.fromJson(it, AiEngine::class.java) }
        }.getOrNull()

        AIDuelConfig(
            personaA = str("personaA"),
            personaB = str("personaB"),
            // 旧 JSON 里的 mode/topic 字段直接忽略，历史会话保持兼容
            avatarA = str("avatarA"),
            avatarB = str("avatarB"),
            maxRounds = int("maxRounds", 5),
            engineA = engineA,
            engineB = engineB,
            promptIdA = int("promptIdA", 0),
            promptIdB = int("promptIdB", 0),
            promptNameA = str("promptNameA"),
            promptNameB = str("promptNameB"),
            roleNameA = str("roleNameA"),
            roleNameB = str("roleNameB"),
        )
    }.getOrNull()
}

enum class DuelSpeaker {
    A,
    B;

    fun other(): DuelSpeaker = if (this == A) B else A
}

data class AIDuelState(
    val running: Boolean = false,
    val round: Int = 0,
    val speaker: DuelSpeaker = DuelSpeaker.A,
    val errorMessage: String = "",
)
