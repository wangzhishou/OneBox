package com.shifenmiao.database.xiangqi.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "xiangqi_game")
data class XiangqiGameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mode: String,
    @ColumnInfo(name = "red_player_type") val redPlayerType: String,
    @ColumnInfo(name = "black_player_type") val blackPlayerType: String,
    @ColumnInfo(name = "red_player_config_json") val redPlayerConfigJson: String = "{}",
    @ColumnInfo(name = "black_player_config_json") val blackPlayerConfigJson: String = "{}",
    @ColumnInfo(name = "initial_fen") val initialFen: String,
    @ColumnInfo(name = "current_fen") val currentFen: String,
    @ColumnInfo(name = "current_ply") val currentPly: Int = 0,
    val status: String,
    val result: String = "",
    @ColumnInfo(name = "winner_side") val winnerSide: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_played_at") val lastPlayedAt: Long = System.currentTimeMillis(),
    /** 玩家首次点击「开始」的时间;0 表示尚未开始 */
    @ColumnInfo(name = "started_at") val startedAt: Long = 0L,
    /** 上次本局有人(玩家/AI)落子的时间,用于推算下一步的思考耗时 */
    @ColumnInfo(name = "last_move_at") val lastMoveAt: Long = 0L,
    val archived: Boolean = false,
)

@Entity(
    tableName = "xiangqi_ply",
    indices = [
        Index(value = ["game_id", "ply"], unique = true),
        Index(value = ["game_id"]),
    ],
)
data class XiangqiPlyEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "game_id") val gameId: String,
    val ply: Int,
    @ColumnInfo(name = "move_ucci") val moveUcci: String,
    @ColumnInfo(name = "move_cn") val moveCn: String,
    @ColumnInfo(name = "mover_side") val moverSide: String,
    @ColumnInfo(name = "before_fen") val beforeFen: String,
    @ColumnInfo(name = "after_fen") val afterFen: String,
    @ColumnInfo(name = "is_capture") val isCapture: Boolean,
    @ColumnInfo(name = "is_check") val isCheck: Boolean,
    @ColumnInfo(name = "is_checkmate") val isCheckmate: Boolean,
    @ColumnInfo(name = "ai_reason") val aiReason: String = "",
    @ColumnInfo(name = "ai_raw_response") val aiRawResponse: String = "",
    /** 该步思考耗时(毫秒);从对手落子(或对局开始)到此次落子提交的时间差 */
    @ColumnInfo(name = "think_duration_ms") val thinkDurationMs: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "xiangqi_ai_task",
    indices = [Index(value = ["game_id", "status"])],
)
data class XiangqiAiTaskEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "game_id") val gameId: String,
    @ColumnInfo(name = "target_ply") val targetPly: Int,
    @ColumnInfo(name = "request_json") val requestJson: String,
    val status: String,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "response_json") val responseJson: String = "",
    @ColumnInfo(name = "validated_move") val validatedMove: String = "",
    @ColumnInfo(name = "error_message") val errorMessage: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
