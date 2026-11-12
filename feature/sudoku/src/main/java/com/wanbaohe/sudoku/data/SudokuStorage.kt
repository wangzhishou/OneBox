/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.sudoku.data

import com.tencent.mmkv.MMKV
import com.wanbaohe.sudoku.logic.SudokuDifficulty
import com.wanbaohe.sudoku.logic.SudokuEngine
import com.wanbaohe.sudoku.logic.SudokuRecord
import org.json.JSONArray
import org.json.JSONObject

/** 计时模式: 关闭 = 对局页不显示用时; 自动 = 随对局走; 手动 = 点用时卡开始/暂停。 */
enum class SudokuTimerMode { OFF, AUTO, MANUAL }

/** 设置页六项, 改动即时生效并整体落盘。 */
data class SudokuSettings(
    val defaultDifficulty: SudokuDifficulty = SudokuDifficulty.MEDIUM,
    val autoCheck: Boolean = true,
    val hintEnabled: Boolean = true,
    val timerMode: SudokuTimerMode = SudokuTimerMode.AUTO,
    val soundEnabled: Boolean = true,
    val bgmEnabled: Boolean = false
)

/**
 * 未完成对局的快照。puzzle/solution/entries 都是 81 字符('0' 为空格)。
 */
data class SudokuSnapshot(
    val puzzle: String,
    val solution: String,
    val entries: String,
    val difficulty: SudokuDifficulty,
    val elapsedSeconds: Int
)

/**
 * 数独持久化 —— 基于 MMKV 的轻量存储。
 *
 * 三类数据:
 * - 对局快照(退出后恢复"继续游戏")
 * - 设置六项
 * - 战绩 JSON 数组(滚动上限 1000 条) + 每难度最佳时间
 */
object SudokuStorage {

    private const val KEY_PUZZLE = "snapshot_puzzle"
    private const val KEY_SOLUTION = "snapshot_solution"
    private const val KEY_ENTRIES = "snapshot_entries"
    private const val KEY_SNAPSHOT_DIFFICULTY = "snapshot_difficulty"
    private const val KEY_SNAPSHOT_ELAPSED = "snapshot_elapsed_seconds"

    private const val KEY_DEFAULT_DIFFICULTY = "settings_default_difficulty"
    private const val KEY_AUTO_CHECK = "settings_auto_check"
    private const val KEY_HINT_ENABLED = "settings_hint_enabled"
    private const val KEY_TIMER_MODE = "settings_timer_mode"
    private const val KEY_SOUND_ENABLED = "settings_sound_enabled"
    private const val KEY_BGM_ENABLED = "settings_bgm_enabled"

    private const val KEY_RECORDS = "records_v1"
    private const val KEY_BEST_PREFIX = "best_seconds_"

    private const val RECORDS_LIMIT = 1000

    private val mmkv: MMKV = MMKV.mmkvWithID("sudoku")

    // ─── 对局快照 ──────────────────────────────────────────────────────────

    fun saveSnapshot(snapshot: SudokuSnapshot) {
        mmkv.encode(KEY_PUZZLE, snapshot.puzzle)
        mmkv.encode(KEY_SOLUTION, snapshot.solution)
        mmkv.encode(KEY_ENTRIES, snapshot.entries)
        mmkv.encode(KEY_SNAPSHOT_DIFFICULTY, snapshot.difficulty.name)
        mmkv.encode(KEY_SNAPSHOT_ELAPSED, snapshot.elapsedSeconds)
    }

    /**
     * 读取快照。三个 81 格字符串任一缺失/损坏都视为无存档并顺手清掉,
     * 免得坏快照把"继续游戏"卡在一个永远打不开的对局上。
     */
    fun loadSnapshot(): SudokuSnapshot? {
        val puzzle = mmkv.decodeString(KEY_PUZZLE) ?: return null
        val solution = mmkv.decodeString(KEY_SOLUTION) ?: return null
        val entries = mmkv.decodeString(KEY_ENTRIES) ?: return null
        if (SudokuEngine.decodeBoard(puzzle) == null ||
            SudokuEngine.decodeBoard(solution) == null ||
            SudokuEngine.decodeBoard(entries) == null
        ) {
            clearSnapshot()
            return null
        }
        val difficulty = runCatching {
            SudokuDifficulty.valueOf(mmkv.decodeString(KEY_SNAPSHOT_DIFFICULTY).orEmpty())
        }.getOrDefault(SudokuDifficulty.MEDIUM)
        return SudokuSnapshot(
            puzzle = puzzle,
            solution = solution,
            entries = entries,
            difficulty = difficulty,
            elapsedSeconds = mmkv.decodeInt(KEY_SNAPSHOT_ELAPSED, 0)
        )
    }

    fun clearSnapshot() {
        mmkv.remove(KEY_PUZZLE)
        mmkv.remove(KEY_SOLUTION)
        mmkv.remove(KEY_ENTRIES)
        mmkv.remove(KEY_SNAPSHOT_DIFFICULTY)
        mmkv.remove(KEY_SNAPSHOT_ELAPSED)
    }

    // ─── 设置 ─────────────────────────────────────────────────────────────

    fun loadSettings(): SudokuSettings = SudokuSettings(
        defaultDifficulty = runCatching {
            SudokuDifficulty.valueOf(mmkv.decodeString(KEY_DEFAULT_DIFFICULTY).orEmpty())
        }.getOrDefault(SudokuDifficulty.MEDIUM),
        autoCheck = mmkv.decodeBool(KEY_AUTO_CHECK, true),
        hintEnabled = mmkv.decodeBool(KEY_HINT_ENABLED, true),
        timerMode = runCatching {
            SudokuTimerMode.valueOf(mmkv.decodeString(KEY_TIMER_MODE).orEmpty())
        }.getOrDefault(SudokuTimerMode.AUTO),
        soundEnabled = mmkv.decodeBool(KEY_SOUND_ENABLED, true),
        bgmEnabled = mmkv.decodeBool(KEY_BGM_ENABLED, false)
    )

    fun saveSettings(settings: SudokuSettings) {
        mmkv.encode(KEY_DEFAULT_DIFFICULTY, settings.defaultDifficulty.name)
        mmkv.encode(KEY_AUTO_CHECK, settings.autoCheck)
        mmkv.encode(KEY_HINT_ENABLED, settings.hintEnabled)
        mmkv.encode(KEY_TIMER_MODE, settings.timerMode.name)
        mmkv.encode(KEY_SOUND_ENABLED, settings.soundEnabled)
        mmkv.encode(KEY_BGM_ENABLED, settings.bgmEnabled)
    }

    // ─── 战绩 ─────────────────────────────────────────────────────────────

    fun loadRecords(): List<SudokuRecord> {
        val json = mmkv.decodeString(KEY_RECORDS) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                SudokuRecord(
                    timestampMillis = obj.optLong("ts", 0L),
                    difficulty = runCatching {
                        SudokuDifficulty.valueOf(obj.optString("difficulty"))
                    }.getOrDefault(SudokuDifficulty.MEDIUM),
                    timeSec = obj.optInt("timeSec", 0),
                    completed = obj.optBoolean("completed", false)
                )
            }
        }.getOrDefault(emptyList())
    }

    /** 追加一条战绩, 滚动上限 [RECORDS_LIMIT] 条(超出丢最老的)。 */
    fun appendRecord(record: SudokuRecord) {
        val records = loadRecords().plus(record).takeLast(RECORDS_LIMIT)
        val arr = JSONArray()
        records.forEach { item ->
            arr.put(
                JSONObject()
                    .put("ts", item.timestampMillis)
                    .put("difficulty", item.difficulty.name)
                    .put("timeSec", item.timeSec)
                    .put("completed", item.completed)
            )
        }
        mmkv.encode(KEY_RECORDS, arr.toString())
    }

    // ─── 每难度最佳时间 ────────────────────────────────────────────────────

    /** 该难度历史最佳用时(秒, 仅完成局), 无纪录返回 null。 */
    fun loadBestSeconds(difficulty: SudokuDifficulty): Int? {
        val value = mmkv.decodeInt(KEY_BEST_PREFIX + difficulty.name, -1)
        return if (value >= 0) value else null
    }

    /** 完成一局后刷新最佳; 返回本次是否破了纪录。 */
    fun updateBestSeconds(difficulty: SudokuDifficulty, seconds: Int): Boolean {
        val current = loadBestSeconds(difficulty)
        if (current != null && current <= seconds) return false
        mmkv.encode(KEY_BEST_PREFIX + difficulty.name, seconds)
        return true
    }
}
