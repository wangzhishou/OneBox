package com.wanbaohe.game2048.data

import com.tencent.mmkv.MMKV
import org.json.JSONArray

/**
 * 2048 游戏持久化 —— 基于 MMKV 的轻量存储
 *
 * 持久化内容：
 * - 历史最高分
 * - 当前棋盘状态（用于退出后恢复游戏）
 * - 当前得分
 */
object Game2048Storage {

    private val mmkv: MMKV = MMKV.mmkvWithID("game_2048")

    private const val KEY_BEST_SCORE = "best_score"
    private const val KEY_CURRENT_SCORE = "current_score"
    private const val KEY_BOARD = "board_v1"

    // ─── 最高分 ─────────────────────────────────────────────────────────────

    /** 读取历史最高分 */
    fun loadBestScore(): Int = mmkv.decodeInt(KEY_BEST_SCORE, 0)

    /** 保存历史最高分 */
    fun saveBestScore(score: Int) {
        mmkv.encode(KEY_BEST_SCORE, score)
    }

    // ─── 当前棋盘快照 ──────────────────────────────────────────────────────

    /** 保存当前棋盘 + 得分，用于下次打开恢复 */
    fun saveBoard(grid: List<List<Int>>, score: Int) {
        val arr = JSONArray()
        grid.forEach { row ->
            val rowArr = JSONArray()
            row.forEach { rowArr.put(it) }
            arr.put(rowArr)
        }
        mmkv.encode(KEY_BOARD, arr.toString())
        mmkv.encode(KEY_CURRENT_SCORE, score)
    }

    /** 加载上次的棋盘，返回 null 表示无存档 */
    fun loadBoard(): List<List<Int>>? {
        val json = mmkv.decodeString(KEY_BOARD) ?: return null
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { r ->
                val rowArr = arr.getJSONArray(r)
                (0 until rowArr.length()).map { c -> rowArr.getInt(c) }
            }
        }.getOrNull()
    }

    /** 加载上次的得分 */
    fun loadCurrentScore(): Int = mmkv.decodeInt(KEY_CURRENT_SCORE, 0)

    /** 清除存档（开始新游戏时调用） */
    fun clearBoard() {
        mmkv.remove(KEY_BOARD)
        mmkv.remove(KEY_CURRENT_SCORE)
    }
}

