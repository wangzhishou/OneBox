package com.wanbaohe.diceroller.component

import androidx.compose.runtime.Immutable

// ─── 骰子类型 ─────────────────────────────────────────────────────────────────

/** 标准桌游骰子类型，[faces] 表示面数 */
enum class DiceType(val faces: Int, val label: String) {
    D4(4, "D4"),
    D6(6, "D6"),
    D8(8, "D8"),
    D10(10, "D10"),
    D12(12, "D12"),
    D20(20, "D20");

    /** 随机投掷，返回 [1, faces] 范围内的点数 */
    fun roll(): Int = (1..faces).random()
}

// ─── 单颗骰子结果 ──────────────────────────────────────────────────────────────

/** 单颗骰子的投掷结果 */
@Immutable
data class DiceResult(
    val type: DiceType,
    val value: Int
)

// ─── 一次投掷记录 ──────────────────────────────────────────────────────────────

/** 一次投掷的完整记录（可包含多颗骰子） */
@Immutable
data class RollRecord(
    val id: Long = System.currentTimeMillis(),
    /** 每颗骰子的结果 */
    val dice: List<DiceResult>,
    /** 所有骰子点数总和 */
    val total: Int = dice.sumOf { it.value },
    /** 投掷时间戳 */
    val timestamp: Long = id
)

// ─── UI 状态快照 ───────────────────────────────────────────────────────────────

/** Dice Roller 页面的完整 UI 状态 */
@Immutable
data class DiceRollerUiState(
    /** 当前选中的骰子类型 */
    val diceType: DiceType = DiceType.D6,
    /** 骰子数量 (1-6) */
    val diceCount: Int = 1,
    /** 最新一次投掷的结果列表（与 diceCount 等长） */
    val currentRoll: List<DiceResult> = emptyList(),
    /** 是否正在翻滚动效中 */
    val isRolling: Boolean = false,
    /** 历史记录列表（最新在前，最多 MAX_HISTORY 条） */
    val history: List<RollRecord> = emptyList(),
    /** 历史面板是否展开 */
    val showHistory: Boolean = false
) {
    /** 当前轮总点数 */
    val total: Int get() = currentRoll.sumOf { it.value }

    companion object {
        const val MAX_DICE = 6
        const val MAX_HISTORY = 50
    }
}

