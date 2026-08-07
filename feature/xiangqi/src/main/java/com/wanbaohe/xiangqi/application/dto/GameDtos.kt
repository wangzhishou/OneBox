package com.wanbaohe.xiangqi.application.dto

import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side

data class GameSummary(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val status: GameStatus,
    val resultText: String,
    val updatedAt: Long,
)

data class GameDetail(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val status: GameStatus,
    val startedAt: Long,
    val lastMoveAt: Long,
    val plies: List<PlyRecord>,
    val onlineMetadata: OnlineGameMetadata = OnlineGameMetadata(),
)

data class OnlineGameMetadata(
    val roomId: String = "",
    val mySide: Side = Side.RED,
    val opponentName: String = "",
    val opponentAvatarUrl: String = "",
    val initialFen: String = "",
)

data class PlyRecord(
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
    val beforeFen: String,
    val afterFen: String,
    val aiReason: String,
    val aiRawResponse: String,
    val thinkDurationMs: Long,
)

data class ExportLabels(
    val header: String,
    val titleLabel: String,
    val initialFenLabel: String,
    val resultLabel: String,
)
