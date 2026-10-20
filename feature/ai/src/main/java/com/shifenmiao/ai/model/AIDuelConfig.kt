package com.shifenmiao.ai.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

@Serializable
@Keep
@Parcelize
data class AIDuelConfig(
    val personaA: String = "",
    val personaB: String = "",
    val avatarA: String = "",
    val avatarB: String = "",
    val maxRounds: Int = 5,
    /** 可选的开场白/场景；留空则由双方自己找话题。 */
    val scene: String = "",
    val engineA: AiEngine? = null,
    val engineB: AiEngine? = null,
    val promptIdA: Int = 0,
    val promptIdB: Int = 0,
    val promptNameA: String = "",
    val promptNameB: String = "",
    val roleNameA: String = "",
    val roleNameB: String = "",
    /** 会话结束原因，仅供恢复时还原状态：PAUSED / BATCH_LIMIT / ENDED。 */
    val sessionState: String = "",
) : Parcelable

internal object AIDuelConfigCodec {
    // AiModel.provider 经 AiProviderKSerializer 序列化为与 Gson 一致的字符串格式,
    // 持久化在 conversation.prompt 里的旧数据可直接读
    fun encode(config: AIDuelConfig): String = AppJson.encodeToString(config)

    fun decodeOrNull(prompt: String): AIDuelConfig? = kotlin.runCatching {
        val obj = AppJson.parseToJsonElement(prompt).jsonObject

        fun str(key: String): String =
            (obj[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

        fun int(key: String, default: Int): Int =
            (obj[key] as? JsonPrimitive)?.intOrNull ?: default

        val engineA = kotlin.runCatching {
            obj["engineA"]?.let { AppJson.decodeFromJsonElement(AiEngine.serializer(), it) }
        }.getOrNull()
        val engineB = kotlin.runCatching {
            obj["engineB"]?.let { AppJson.decodeFromJsonElement(AiEngine.serializer(), it) }
        }.getOrNull()

        AIDuelConfig(
            personaA = str("personaA"),
            personaB = str("personaB"),
            // 旧 JSON 里的 mode/topic 字段直接忽略，历史会话保持兼容
            avatarA = str("avatarA"),
            avatarB = str("avatarB"),
            maxRounds = int("maxRounds", 5),
            scene = str("scene"),
            engineA = engineA,
            engineB = engineB,
            promptIdA = int("promptIdA", 0),
            promptIdB = int("promptIdB", 0),
            promptNameA = str("promptNameA"),
            promptNameB = str("promptNameB"),
            roleNameA = str("roleNameA"),
            roleNameB = str("roleNameB"),
            sessionState = str("sessionState"),
        )
    }.getOrNull()
}

enum class DuelSpeaker {
    A,
    B;

    fun other(): DuelSpeaker = if (this == A) B else A
}

enum class DuelPhase {
    IDLE,
    RUNNING,
    PAUSED,
    ENDED,
}

data class AIDuelState(
    val phase: DuelPhase = DuelPhase.IDLE,
    val round: Int = 0,
    val speaker: DuelSpeaker = DuelSpeaker.A,
    val errorMessage: String = "",
    val retryAvailable: Boolean = false,
    val totalTokens: Int = 0,
    val batchLimitReached: Boolean = false,
    val batchRound: Int = 0,
    val batchTotal: Int = 0,
    val pauseRequested: Boolean = false,
) {
    val running: Boolean get() = phase == DuelPhase.RUNNING
    val paused: Boolean get() = phase == DuelPhase.PAUSED
    val hasError: Boolean get() = errorMessage.isNotBlank()
}
