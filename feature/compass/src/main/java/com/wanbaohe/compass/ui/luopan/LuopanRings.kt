package com.wanbaohe.compass.ui.luopan

/**
 * 罗经盘圈层数据与方位换算（纯数据/纯函数，无 Android 依赖）
 *
 * 角度约定与表盘一致：0° = 正北，顺时针增大。
 * 圈层文字为固定中文古典内容，属数据而非 UI 文案，不进 string resources。
 */

// ─── 二十四山（地盘正针）───────────────────────────────────────────────────────

/** 二十四山，顺时针排列，子居正北 0°，每山 15°（子跨 352.5°~7.5°） */
val MOUNTAINS = listOf(
    "子", "癸", "丑", "艮", "寅", "甲", "卯", "乙",
    "辰", "巽", "巳", "丙", "午", "丁", "未", "坤",
    "申", "庚", "酉", "辛", "戌", "乾", "亥", "壬"
)

/** 度数（0~359）→ 二十四山索引（0=子）。边界 7.5° 对整数度数取 ±0.5° 近似 */
fun mountainIndex(degrees: Int): Int = ((degrees + 7) / 15) % 24

/**
 * 山索引 → 八方位索引（0=北 … 7=西北，与 compass_dir_* 字符串顺序一致）
 * 分组：壬子癸=北、丑艮寅=东北、甲卯乙=东、辰巽巳=东南、
 *      丙午丁=南、未坤申=西南、庚酉辛=西、戌乾亥=西北
 */
fun mountainDirectionIndex(mountainIdx: Int): Int = ((mountainIdx + 1) / 3) % 8

/**
 * 坐向：向 = 朝向所压之山，坐 = 对宫（+12）。
 * 返回 Pair(坐山, 向山)，如朝正北 0° → (坐=午, 向=子)，即「午山子向」。
 */
fun seatFacing(degrees: Int): Pair<String, String> {
    val facingIdx = mountainIndex(degrees)
    val sittingIdx = (facingIdx + 12) % MOUNTAINS.size
    return MOUNTAINS[sittingIdx] to MOUNTAINS[facingIdx]
}

// ─── 后天八卦（配洛书九星）─────────────────────────────────────────────────────

/**
 * 一卦：卦名 + 三爻（自上而下，true=阳爻实线，false=阴爻中断线）+ 配九星
 */
data class Trigram(
    val name: String,
    val lines: BooleanArray,
    val star: String
) {
    override fun equals(other: Any?): Boolean =
        other is Trigram && name == other.name && lines.contentEquals(other.lines)

    override fun hashCode(): Int = 31 * name.hashCode() + lines.contentHashCode()
}

/**
 * 后天八卦，顺时针自正北起，每卦 45°：
 * 坎(北) 艮(东北) 震(东) 巽(东南) 离(南) 坤(西南) 兑(西) 乾(西北)
 */
val TRIGRAMS = listOf(
    Trigram("坎", booleanArrayOf(false, true, false), "一白"),
    Trigram("艮", booleanArrayOf(true, false, false), "八白"),
    Trigram("震", booleanArrayOf(false, false, true), "三碧"),
    Trigram("巽", booleanArrayOf(true, true, false), "四绿"),
    Trigram("离", booleanArrayOf(true, false, true), "九紫"),
    Trigram("坤", booleanArrayOf(false, false, false), "二黑"),
    Trigram("兑", booleanArrayOf(false, true, true), "七赤"),
    Trigram("乾", booleanArrayOf(true, true, true), "六白")
)

// ─── 二十八宿 ─────────────────────────────────────────────────────────────────

/** 二十八宿，传统顺序：东方青龙 → 北方玄武 → 西方白虎 → 南方朱雀 */
val MANSIONS = listOf(
    "角", "亢", "氐", "房", "心", "尾", "箕",
    "斗", "牛", "女", "虚", "危", "室", "壁",
    "奎", "娄", "胃", "昴", "毕", "觜", "参",
    "井", "鬼", "柳", "星", "张", "翼", "轸"
)

/** 锚定：虚宿（索引 10）居正北 0°。此时房宿正东、星宿正南、昴宿正西，四象各居其位 */
private const val MANSION_ANCHOR_INDEX = 10

/** 每宿等分角度（简化：真实宿度不等宽，v1 取 360/28 等分） */
val MANSION_STEP = 360f / MANSIONS.size

/**
 * 第 k 宿的中心角度。宿序在罗经盘上逆时针推进（角度递减）：
 * 虚@北(0°)，危/室/壁向西，女/牛/斗向东，房@东(90°)，星@南(180°)，昴@西(270°)
 */
fun mansionCenterAngle(k: Int): Float =
    ((MANSION_ANCHOR_INDEX - k) * MANSION_STEP % 360f + 360f) % 360f
