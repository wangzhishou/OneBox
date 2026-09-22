package com.shifenmiao.model.ai

import com.google.gson.annotations.SerializedName

/**
 * Pikafish 走棋接口请求（go-proxy `POST /xiangqi/engine/bestmove`）。
 *
 * movetime_ms 与 depth 二选一；都为空时服务端用默认 movetime。
 * skill: 0-20 越高越强, -1 表示不限制。
 */
data class XiangqiEngineRequest(
    @SerializedName("fen")
    val fen: String,
    @SerializedName("moves")
    val moves: String? = null,
    @SerializedName("movetime_ms")
    val moveTimeMs: Int? = null,
    @SerializedName("depth")
    val depth: Int? = null,
    @SerializedName("skill")
    val skill: Int? = null,
)

data class XiangqiEngineResponse(
    @SerializedName("bestmove")
    val bestmove: String = "",
    @SerializedName("ponder")
    val ponder: String? = null,
    @SerializedName("score_cp")
    val scoreCp: Int? = null,
    @SerializedName("score_mate")
    val scoreMate: Int? = null,
    @SerializedName("depth")
    val depth: Int? = null,
    @SerializedName("pv")
    val pv: String? = null,
    @SerializedName("time_ms")
    val timeMs: Long? = null,
)
