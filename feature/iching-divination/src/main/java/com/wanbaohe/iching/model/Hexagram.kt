package com.wanbaohe.iching.model

/** A line produced by the three-coin method. Values are 6, 7, 8, or 9. */
data class HexagramLine(val value: Int) {
    init {
        require(value in 6..9) { "A hexagram line must be 6, 7, 8, or 9" }
    }

    val isYang: Boolean get() = value == 7 || value == 9
    val isChanging: Boolean get() = value == 6 || value == 9
    val changedIsYang: Boolean get() = if (isChanging) !isYang else isYang
}

data class HexagramInfo(
    val number: Int,
    val name: String,
    val upperTrigram: String,
    val lowerTrigram: String,
)

data class DivinationResult(
    val question: String,
    /** Lines in traditional bottom-to-top order. */
    val lines: List<HexagramLine>,
    val primary: HexagramInfo,
    val changed: HexagramInfo?,
) {
    val changingLineNumbers: List<Int>
        get() = lines.mapIndexedNotNull { index, line -> (index + 1).takeIf { line.isChanging } }
}

