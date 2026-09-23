package com.shifenmiao.model.ai

import com.google.gson.annotations.SerializedName

/**
 * 服务端棋类引擎走棋接口请求（go-proxy `POST /{chess|gomoku}/engine/bestmove`）。
 * 与象棋 XiangqiEngineRequest 同形态,供国际象棋(Stockfish)/五子棋(Rapfi)复用。
 *
 * movetime_ms 与 depth 二选一；都为空时服务端用默认 movetime。
 * skill: 0-20 越高越强, -1 表示不限制。
 */
data class BoardGameEngineRequest(
    @SerializedName("fen")
    val fen: String,
    /** 服务端引擎标识（如 "stockfish"/"rapfi"），预留多引擎路由 */
    @SerializedName("engine")
    val engine: String? = null,
    @SerializedName("moves")
    val moves: String? = null,
    @SerializedName("movetime_ms")
    val moveTimeMs: Int? = null,
    @SerializedName("depth")
    val depth: Int? = null,
    @SerializedName("skill")
    val skill: Int? = null,
)

data class BoardGameEngineResponse(
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
