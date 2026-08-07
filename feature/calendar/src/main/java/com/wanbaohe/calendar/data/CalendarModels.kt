package com.wanbaohe.calendar.data

import androidx.compose.runtime.Immutable

/**
 * 农历日期数据
 */
@Immutable
data class LunarDate(
    /** 农历年 */
    val year: Int,
    /** 农历月（1-12，闰月为负数，如闰4月=-4） */
    val month: Int,
    /** 农历日（1-30） */
    val day: Int,
    /** 是否闰月 */
    val isLeapMonth: Boolean = false,
    /** 天干地支年（如"甲辰"） */
    val ganZhiYear: String = "",
    /** 天干地支月（如"丙寅"） */
    val ganZhiMonth: String = "",
    /** 天干地支日（如"庚子"） */
    val ganZhiDay: String = "",
    /** 生肖 */
    val zodiac: String = "",
    /** 农历月名（如"二月"） */
    val monthName: String = "",
    /** 农历日名（如"十七"） */
    val dayName: String = "",
    /** 节气（当天若有节气） */
    val solarTerm: String? = null,
    /** 农历节日 */
    val lunarFestival: String? = null,
    /** 农历节日列表 */
    val lunarFestivals: List<String> = emptyList(),
    /** 阳历节日 */
    val solarFestival: String? = null,
    /** 阳历/国际节日列表 */
    val solarFestivals: List<String> = emptyList(),
    /** 法定节假日名称 */
    val legalHolidayName: String? = null,
    /** 是否法定休假日 */
    val isLegalHoliday: Boolean = false,
    /** 星座 */
    val constellation: String = "",
    /** 二十八宿 */
    val star28: String = "",
    /** 建除十二值 */
    val jianChu: String = "",
    /** 五行纳音 */
    val naYin: String = "",
    /** 彭祖百忌（天干） */
    val pengZuGan: String = "",
    /** 彭祖百忌（地支） */
    val pengZuZhi: String = "",
    /** 喜神方位 */
    val xiShen: String = "",
    /** 福神方位 */
    val fuShen: String = "",
    /** 财神方位 */
    val caiShen: String = "",
    /** 冲（如“冲龙(甲辰)”） */
    val chong: String = "",
    /** 煞（如“煞北”） */
    val sha: String = "",
    /** 值神 */
    val zhiShen: String = "",
    /** 吉神宜趋 */
    val dayJiShen: String = "",
    /** 凶神宜忌 */
    val dayXiongSha: String = "",
    /** 胎神占方 */
    val taiShen: String = "",
    /** 九星 */
    val nineStar: String = "",
)

/**
 * 阳历日期数据（用于阴历转阳历结果展示）
 */
@Immutable
data class SolarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val weekDay: String = ""
)

@Immutable
data class UpcomingFestivalItem(
    val name: String,
    val dateText: String,
    val daysUntil: Int,
    val type: FestivalType
)

enum class FestivalType {
    SolarTerm,
    LunarFestival,
    SolarFestival,
    LegalHoliday
}

/**
 * 时辰数据（用于日历页横向时辰卡片）
 */
@Immutable
data class LunarTimeSlot(
    /** 时辰干支（如“甲子”） */
    val ganZhi: String = "",
    /** 时间范围（如“00:00-00:59”） */
    val timeRange: String = "",
    /** 天神 */
    val tianShen: String = "",
    /** 冲 */
    val chong: String = "",
    /** 煞 */
    val sha: String = "",
    /** 黄道/黑道 */
    val tianShenType: String = "",
    /** 吉/凶 */
    val luck: String = "",
    /** 宜 */
    val yi: List<String> = emptyList(),
    /** 忌 */
    val ji: List<String> = emptyList()
)

/**
 * 宜忌数据
 */
@Immutable
data class YiJi(
    /** 宜做之事 */
    val yi: List<String> = emptyList(),
    /** 忌做之事 */
    val ji: List<String> = emptyList()
)

/**
 * 八字四柱
 */
@Immutable
data class BaZiPillar(
    /** 天干 */
    val tianGan: String,
    /** 地支 */
    val diZhi: String,
    /** 十神（如"比肩"、"食神"等） */
    val shiShen: String = "",
    /** 柱名（年柱、月柱、日柱、时柱） */
    val pillarName: String = ""
)

/**
 * 八字完整数据
 */
@Immutable
data class BaZiData(
    /** 年柱 */
    val yearPillar: BaZiPillar,
    /** 月柱 */
    val monthPillar: BaZiPillar,
    /** 日柱 */
    val dayPillar: BaZiPillar,
    /** 时柱 */
    val hourPillar: BaZiPillar,
    /** 日元（日干） */
    val dayMaster: String = "",
    /** 五行分布 */
    val wuXingDistribution: Map<String, Float> = emptyMap(),
    /** 身强/身弱 */
    val strength: String = "",
    /** 喜用神 */
    val favorableElements: String = ""
)

/**
 * 大运数据
 */
@Immutable
data class DaYunItem(
    /** 起始年份 */
    val startYear: Int,
    /** 天干地支 */
    val ganZhi: String,
    /** 是否当前大运 */
    val isCurrent: Boolean = false
)

/**
 * 流年运势
 */
@Immutable
data class FortuneData(
    /** 年份 */
    val year: Int,
    /** 天干地支年 */
    val ganZhiYear: String,
    /** 运势标题 */
    val title: String = "",
    /** 运势详情 */
    val description: String = "",
    /** 运势评分（0-100） */
    val fortuneScore: Int = 50,
    /** 事业评价 */
    val careerLevel: String = "",
    /** 月运列表 */
    val monthlyFortunes: List<MonthlyFortune> = emptyList()
)

/**
 * 月运势
 */
@Immutable
data class MonthlyFortune(
    /** 月份干支（如"丁卯"） */
    val ganZhi: String,
    /** 月份显示（如"二月"） */
    val monthDisplay: String,
    /** 关键词 */
    val keyword: String = "",
    /** 标签 */
    val tag: String = "",
    /** 标签颜色类型：positive/negative/neutral */
    val tagType: String = "neutral"
)

// ─── 佛历 ────────────────────────────────────────────────

/**
 * 佛历因果犯忌
 */
@Immutable
data class FotoFestivalItem(
    /** 是日何日（如"雷斋日"） */
    val name: String,
    /** 犯之因果（如"犯者夺纪"） */
    val result: String = "",
    /** 备注 */
    val remark: String = ""
)

/**
 * 佛历完整数据（通过 lunar-java Foto 获取）
 */
@Immutable
data class FotoData(
    /** 佛历年份 */
    val year: Int,
    /** 佛历年中文（如"二五七〇"） */
    val yearInChinese: String = "",
    /** 佛历月中文 */
    val monthInChinese: String = "",
    /** 佛历日中文 */
    val dayInChinese: String = "",
    /** 因果犯忌列表 */
    val festivals: List<FotoFestivalItem> = emptyList(),
    /** 纪念日列表 */
    val otherFestivals: List<String> = emptyList(),
    /** 是否月斋（正月、五月、九月） */
    val isMonthZhai: Boolean = false,
    /** 是否杨公忌 */
    val isDayYangGong: Boolean = false,
    /** 是否朔望斋（初一、十五） */
    val isDayZhaiShuoWang: Boolean = false,
    /** 是否六斋日 */
    val isDayZhaiSix: Boolean = false,
    /** 是否十斋日 */
    val isDayZhaiTen: Boolean = false,
    /** 是否观音斋 */
    val isDayZhaiGuanYin: Boolean = false,
    /** 星宿 */
    val xiu: String = "",
    /** 宿吉凶 */
    val xiuLuck: String = "",
    /** 宿歌诀 */
    val xiuSong: String = "",
    /** 完整字符串 */
    val fullString: String = ""
)

// ─── 道历 ────────────────────────────────────────────────

/**
 * 道历节日
 */
@Immutable
data class TaoFestivalItem(
    /** 节日名称 */
    val name: String,
    /** 备注 */
    val remark: String = ""
)

/**
 * 道历完整数据（通过 lunar-java Tao 获取）
 */
@Immutable
data class TaoData(
    /** 道历年份 */
    val year: Int,
    /** 道历年中文 */
    val yearInChinese: String = "",
    /** 道历月中文 */
    val monthInChinese: String = "",
    /** 道历日中文 */
    val dayInChinese: String = "",
    /** 节日列表 */
    val festivals: List<TaoFestivalItem> = emptyList(),
    /** 是否三会日 */
    val isDaySanHui: Boolean = false,
    /** 是否三元日 */
    val isDaySanYuan: Boolean = false,
    /** 是否八节日 */
    val isDayBaJie: Boolean = false,
    /** 是否五腊日 */
    val isDayWuLa: Boolean = false,
    /** 是否八会日 */
    val isDayBaHui: Boolean = false,
    /** 是否戊日（明戊或暗戊） */
    val isDayWu: Boolean = false,
    /** 是否天赦日 */
    val isDayTianShe: Boolean = false,
    /** 完整字符串（含干支纪年） */
    val fullString: String = ""
)

/**
 * 万年历 UI 状态（共享数据类，用于兼容现有 Tab Composable）
 */
@Immutable
data class CalendarUiState(
    /** 当前选中的公历年 */
    val currentYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    /** 当前选中的公历月 */
    val currentMonth: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
    /** 当前选中的公历日 */
    val selectedDay: Int = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH),
    /** 当前选中日的农历信息 */
    val lunarDate: LunarDate? = null,
    /** 当前选中日的宜忌 */
    val yiJi: YiJi = YiJi(),
    /** 当前选中日的时辰数据 */
    val timeSlots: List<LunarTimeSlot> = emptyList(),
    /** 当前选中日的佛历数据 */
    val fotoData: FotoData? = null,
    /** 当前选中日的道历数据 */
    val taoData: TaoData? = null,
    /** 月历天数据 */
    val calendarDays: List<CalendarDayInfo> = emptyList(),
    /** 八字数据 */
    val baZiData: BaZiData? = null,
    /** 大运数据 */
    val daYunList: List<DaYunItem> = emptyList(),
    /** 流年详批 */
    val fortuneData: FortuneData? = null,
    /** 八字 Tab 独立日期：年 */
    val baZiYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    /** 八字 Tab 独立日期：月 */
    val baZiMonth: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
    /** 八字 Tab 独立日期：日 */
    val baZiDay: Int = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH),
    /** 八字 Tab 独立时辰（0-23） */
    val baZiHour: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
    /** 下一个节气信息 */
    val nextSolarTerm: Pair<String, String>? = null,
    /** 未来节气/节日列表 */
    val upcomingFestivalItems: List<UpcomingFestivalItem> = emptyList(),
    // ─── 转换 Tab 状态 ───
    /** 转换模式：true=阳历转阴历，false=阴历转阳历 */
    val isConvertSolarToLunar: Boolean = true,
    /** 转换输入年 */
    val convertYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    /** 转换输入月 */
    val convertMonth: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
    /** 转换输入日 */
    val convertDay: Int = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH),
    /** 转换输入时辰（0-23） */
    val convertHour: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
    /** 阴历输入是否闰月 */
    val isConvertLunarLeapMonth: Boolean = false,
    /** 转换结果 */
    val convertResult: LunarDate? = null,
    /** 阴历转阳历结果 */
    val convertSolarResult: SolarDate? = null,
    /** 转换结果对应时辰信息 */
    val convertTimeSlot: LunarTimeSlot? = null,
    /** 佛历数据（由 lunar-java Foto 提供） */
    val convertFotoData: FotoData? = null,
    /** 道历数据（由 lunar-java Tao 提供） */
    val convertTaoData: TaoData? = null,
    // ─── 择日 Tab 状态 ───
    /** 是否为忌事避讳模式（false=吉日择取，true=忌事避讳） */
    val isAvoidMode: Boolean = false,
    /** 当前选中的事项（宜或忌，取决于 isAvoidMode） */
    val selectedAuspiciousItems: Set<String> = emptySet(),
    /** 择日搜索结果 */
    val auspiciousDayResults: List<AuspiciousDayResult> = emptyList(),
    /** 择日搜索是否正在加载 */
    val isAuspiciousLoading: Boolean = false,
    /** 核心日历数据是否已就绪（首屏加载完成） */
    val isDataReady: Boolean = false,
)

