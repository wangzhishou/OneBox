package com.wanbaohe.diceroller.data

import com.tencent.mmkv.MMKV
import com.wanbaohe.diceroller.component.DiceResult
import com.wanbaohe.diceroller.component.DiceRollerUiState
import com.wanbaohe.diceroller.component.DiceType
import com.wanbaohe.diceroller.component.RollRecord
import org.json.JSONArray
import org.json.JSONObject

/**
 * 骰子历史记录持久化 —— 基于 MMKV 的轻量存储
 *
 * 编解码采用 JSON 字符串，避免 Parcelable 跨版本兼容问题。
 * 最多保留 [DiceRollerUiState.MAX_HISTORY] 条记录。
 */
object DiceHistoryStorage {

    private val mmkv: MMKV = MMKV.mmkvWithID("dice_roller")

    private const val KEY_HISTORY = "history_v1"
    private const val KEY_DICE_TYPE = "dice_type"
    private const val KEY_DICE_COUNT = "dice_count"

    // ─── 历史记录 ────────────────────────────────────────────────────────────

    /** 追加一条投掷记录并持久化（超出上限时自动丢弃最旧条目） */
    fun appendRecord(record: RollRecord) {
        val current = loadHistory().toMutableList()
        current.add(0, record) // 最新在前
        if (current.size > DiceRollerUiState.MAX_HISTORY) {
            current.subList(DiceRollerUiState.MAX_HISTORY, current.size).clear()
        }
        mmkv.encode(KEY_HISTORY, encodeHistory(current))
    }

    /** 从持久化存储加载历史记录 */
    fun loadHistory(): List<RollRecord> {
        val json = mmkv.decodeString(KEY_HISTORY) ?: return emptyList()
        return decodeHistory(json)
    }

    /** 清空所有历史记录 */
    fun clearHistory() {
        mmkv.remove(KEY_HISTORY)
    }

    // ─── 偏好设置 ────────────────────────────────────────────────────────────

    /** 持久化用户选择的骰子类型 */
    fun saveDiceType(type: DiceType) {
        mmkv.encode(KEY_DICE_TYPE, type.name)
    }

    /** 读取上次使用的骰子类型，默认 D6 */
    fun loadDiceType(): DiceType {
        val name = mmkv.decodeString(KEY_DICE_TYPE) ?: return DiceType.D6
        return DiceType.entries.find { it.name == name } ?: DiceType.D6
    }

    /** 持久化骰子数量 */
    fun saveDiceCount(count: Int) {
        mmkv.encode(KEY_DICE_COUNT, count)
    }

    /** 读取上次使用的骰子数量，默认 1 */
    fun loadDiceCount(): Int {
        val n = mmkv.decodeInt(KEY_DICE_COUNT, 1)
        return n.coerceIn(1, DiceRollerUiState.MAX_DICE)
    }

    // ─── 编解码 ──────────────────────────────────────────────────────────────

    private fun encodeHistory(list: List<RollRecord>): String {
        val arr = JSONArray()
        list.forEach { record ->
            val obj = JSONObject().apply {
                put("id", record.id)
                put("timestamp", record.timestamp)
                put("dice", JSONArray().also { diceArr ->
                    record.dice.forEach { d ->
                        diceArr.put(JSONObject().apply {
                            put("type", d.type.name)
                            put("value", d.value)
                        })
                    }
                })
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decodeHistory(json: String): List<RollRecord> {
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val diceArr = obj.getJSONArray("dice")
                val dice = (0 until diceArr.length()).map { j ->
                    val d = diceArr.getJSONObject(j)
                    DiceResult(
                        type = DiceType.entries.find { it.name == d.getString("type") } ?: DiceType.D6,
                        value = d.getInt("value")
                    )
                }
                RollRecord(
                    id = obj.getLong("id"),
                    dice = dice,
                    timestamp = obj.getLong("timestamp")
                )
            }
        }.getOrElse { emptyList() }
    }
}

