package com.shifenmiao.model.ai

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * TypeSafe System One (Jev) 判断接口请求。
 *
 * state 与 questions 由调用方按业务自由拼装, 因此直接以 JsonObject 承载。
 * model 必须以 "jev" 开头(go-proxy 侧校验)。
 */
data class JevRequest(
    @SerializedName("state")
    val state: JsonObject,
    @SerializedName("model")
    val model: String,
    @SerializedName("questions")
    val questions: JsonObject,
)

data class JevResponse(
    @SerializedName("model")
    val model: String = "",
    @SerializedName("answers")
    val answers: Map<String, JevChoiceAnswer> = emptyMap(),
    @SerializedName("usage")
    val usage: JsonObject? = null,
)

/**
 * choice 判断题答案。
 *
 * 注意: 服务端实测 choice 是按 probabilities 采样的结果而非 argmax,
 * 需要稳定选择时应自行对 probabilities 取概率最高的键。
 */
data class JevChoiceAnswer(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("choice")
    val choice: String = "",
    @SerializedName("confidence")
    val confidence: Double = 0.0,
    @SerializedName("probabilities")
    val probabilities: Map<String, Double> = emptyMap(),
)
