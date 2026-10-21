package com.wanbaohe.calendar.share

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.DayFestival
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.LunarDate
import com.wanbaohe.calendar.data.LunarTimeSlot
import com.wanbaohe.calendar.data.UpcomingFestivalItem
import com.wanbaohe.calendar.data.YiJi
import com.wanbaohe.calendar.data.getChineseHourSlot
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 日历分享卡的完整内容。
 *
 * 卡面本身是纯矢量绘制（[CalendarShareCardRenderer]），这里只描述「画什么」，
 * 样式、可选模块由 [CalendarShareRenderOptions] 决定。
 */
@Immutable
data class CalendarShareCardData(
    val solarYear: Int,
    val solarMonth: Int,
    val solarDay: Int,
    val lunarMonthName: String,
    val lunarDayName: String,
    val ganZhiYear: String,
    val ganZhiMonth: String,
    val ganZhiDay: String,
    val zodiac: String,
    val weekday: String,
    val weekdayEn: String,
    val yi: List<String>,
    val ji: List<String>,
    val luckyHours: List<CalendarShareLuckyHour>,
    val todayFestivals: List<DayFestival>,
    val upcomingFestivals: List<UpcomingFestivalItem>,
    val weather: CalendarShareWeather?,
    val note: String,
    val labels: CalendarShareLabels,
    val palette: CalendarSharePalette,
)

/**
 * 卡面上的固定文案。
 *
 * 农历月日、宜忌条目、生肖由 lunar-java 返回中文，因此这张卡整体是一张「中式黄历」，
 * 卡面汉字（农历 / 宜 / 忌 / 今日 / 未来 / 心语 / 祝福 / 品牌）在英文下也保留，
 * 属于版式元素；分享页的操作按钮才跟随应用语言。
 */
@Immutable
data class CalendarShareLabels(
    val lunarPrefix: String,
    val yiSeal: String,
    val jiSeal: String,
    val blessing: String,
    val brand: String,
    val yearSuffix: String,
    val monthSuffix: String,
    val daySuffix: String,
    val todayPrefix: String,
    val auspiciousTitle: String,
    val upcomingPrefix: String,
    val noteTitle: String,
    val airPrefix: String,
    val weatherTitle: String,
)

/**
 * 卡面配色。
 *
 * [CalendarShareColorStyle.Theme] 完全跟随当前 Material 主题；另外三套是固定的中式色，
 * 让用户在不改 App 主题的情况下也能得到朱砂 / 黛青 / 玄金的效果。
 */
@Immutable
data class CalendarSharePalette(
    val paper: Color,
    val mat: Color,
    val ink: Color,
    val inkSoft: Color,
    val accent: Color,
    val onAccent: Color,
    val yiSeal: Color,
    val onYiSeal: Color,
    val jiSeal: Color,
    val onJiSeal: Color,
    val isDark: Boolean,
) {
    companion object {
        fun from(
            style: CalendarShareColorStyle,
            scheme: ColorScheme,
        ): CalendarSharePalette = when (style) {
            CalendarShareColorStyle.Theme -> themePalette(scheme)
            CalendarShareColorStyle.Vermilion -> VERMILION
            CalendarShareColorStyle.InkGreen -> INK_GREEN
            CalendarShareColorStyle.GoldInk -> GOLD_INK
        }

        private fun themePalette(scheme: ColorScheme): CalendarSharePalette {
            val isDark = scheme.surface.luminance() < 0.5f
            val paper = lerp(
                scheme.surface,
                scheme.primary,
                if (isDark) 0.06f else 0.035f
            )
            return CalendarSharePalette(
                paper = paper,
                mat = lerp(paper, scheme.scrim, if (isDark) 0.35f else 0.10f),
                ink = scheme.onSurface,
                inkSoft = scheme.onSurfaceVariant,
                accent = scheme.primary,
                onAccent = scheme.onPrimary,
                yiSeal = scheme.primaryContainer,
                onYiSeal = scheme.onPrimaryContainer,
                jiSeal = scheme.errorContainer,
                onJiSeal = scheme.onErrorContainer,
                isDark = isDark
            )
        }

        private val VERMILION = CalendarSharePalette(
            paper = Color(0xFFF7F0E1),
            mat = Color(0xFFE1D5BF),
            ink = Color(0xFF2B2520),
            inkSoft = Color(0xFF75695A),
            accent = Color(0xFFA43B2A),
            onAccent = Color(0xFFFBF4E7),
            yiSeal = Color(0xFFF0D9D2),
            onYiSeal = Color(0xFF5A1E16),
            jiSeal = Color(0xFFE9C6C0),
            onJiSeal = Color(0xFF4A1511),
            isDark = false
        )

        private val INK_GREEN = CalendarSharePalette(
            paper = Color(0xFFF1F4F0),
            mat = Color(0xFFDCE4DE),
            ink = Color(0xFF1F2A26),
            inkSoft = Color(0xFF64706A),
            accent = Color(0xFF2F5D57),
            onAccent = Color(0xFFF3F7F2),
            yiSeal = Color(0xFFD7E4D9),
            onYiSeal = Color(0xFF21372A),
            jiSeal = Color(0xFFEFD2CC),
            onJiSeal = Color(0xFF4A1D17),
            isDark = false
        )

        private val GOLD_INK = CalendarSharePalette(
            paper = Color(0xFF1B1A17),
            mat = Color(0xFF0E0E0C),
            ink = Color(0xFFEDE6D6),
            inkSoft = Color(0xFFA79D8B),
            accent = Color(0xFFD9B36C),
            onAccent = Color(0xFF2A2113),
            yiSeal = Color(0xFF3D2F1C),
            onYiSeal = Color(0xFFF3DFB4),
            jiSeal = Color(0xFF3D211C),
            onJiSeal = Color(0xFFF2C7BA),
            isDark = true
        )
    }
}

enum class CalendarShareColorStyle(
    val id: String,
    @StringRes val labelRes: Int,
) {
    Theme("theme", R.string.calendar_share_style_theme),
    Vermilion("vermilion", R.string.calendar_share_style_vermilion),
    InkGreen("ink_green", R.string.calendar_share_style_ink_green),
    GoldInk("gold_ink", R.string.calendar_share_style_gold_ink);

    companion object {
        fun fromId(id: String?): CalendarShareColorStyle =
            entries.firstOrNull { it.id == id } ?: Theme
    }
}

@Immutable
data class CalendarShareRenderOptions(
    val showTodayFestival: Boolean = true,
    val showUpcomingFestivals: Boolean = true,
    val showWeather: Boolean = true,
    val showNote: Boolean = true,
)

/** 吉时：一个时辰名称 + 时间段，如「卯时」/「05:00–06:59」。 */
@Immutable
data class CalendarShareLuckyHour(
    val name: String,
    val range: String,
)

/**
 * 从 lunar-java 的时辰数据里挑 3 个白天的吉时。
 * 优先 05:00–19:00，避免分享卡中间全是半夜的吉时；不够再补其它时段。
 */
fun List<LunarTimeSlot>.toShareLuckyHours(): List<CalendarShareLuckyHour> {
    val lucky = filter { slot ->
        slot.luck.contains("吉") || slot.tianShenType.contains("黄道")
    }.mapNotNull { slot ->
        val startHour = slot.timeRange.substringBefore(":").toIntOrNull()
            ?: return@mapNotNull null
        slot to startHour
    }
    val daytime = lucky.filter { (_, hour) -> hour in 5..19 }
    return (daytime + lucky)
        .distinctBy { (slot, _) -> slot.timeRange }
        .take(3)
        .map { (slot, hour) ->
            CalendarShareLuckyHour(
                name = getChineseHourSlot(hour).displayName + "时",
                range = slot.timeRange.replace("-", "–")
            )
        }
}

/**
 * 当天天气（只在分享「今天」时展示，避免把今天的天气贴到历史/未来日期上）。
 */
@Immutable
data class CalendarShareWeather(
    val city: String = "",
    val text: String = "",
    val temp: String = "",
    val tempMin: String = "",
    val tempMax: String = "",
    val aqi: String = "",
    val aqiCategory: String = "",
    val iconCode: String = "",
) {
    val hasContent: Boolean
        get() = temp.isNotBlank() || text.isNotBlank()

    /** 例：北京 · 晴 21° / 9° · 空气优 */
    fun summary(separator: String = " · ", airPrefix: String = "空气"): String = buildString {
        if (city.isNotBlank()) append(city)
        if (text.isNotBlank()) {
            if (isNotEmpty()) append(separator)
            append(text)
        }
        val temperature = when {
            temp.isNotBlank() && tempMin.isNotBlank() && tempMax.isNotBlank() ->
                "$temp° / $tempMin°~$tempMax°"
            temp.isNotBlank() -> "$temp°"
            tempMin.isNotBlank() && tempMax.isNotBlank() -> "$tempMin°~$tempMax°"
            else -> ""
        }
        if (temperature.isNotBlank()) {
            if (isNotEmpty()) append(separator)
            append(temperature)
        }
        if (aqiCategory.isNotBlank()) {
            if (isNotEmpty()) append(separator)
            if (airPrefix.isNotBlank()) {
                append(airPrefix)
                append(' ')
            }
            append(aqiCategory)
        }
    }
}

/** 分享卡心语长度上限。 */
const val MAX_NOTE_LENGTH = 80

/**
 * 把日期/农历/宜忌/节日/天气/备注拼成卡面数据。
 */
fun buildCalendarShareCardData(
    year: Int,
    month: Int,
    day: Int,
    lunar: LunarDate,
    yiJi: YiJi,
    luckyHours: List<CalendarShareLuckyHour>,
    todayFestivals: List<DayFestival>,
    upcomingFestivals: List<UpcomingFestivalItem>,
    weather: CalendarShareWeather?,
    note: String,
    labels: CalendarShareLabels,
    palette: CalendarSharePalette,
): CalendarShareCardData? {
    val date = runCatching { LocalDate.of(year, month, day) }.getOrNull() ?: return null

    return CalendarShareCardData(
        solarYear = year,
        solarMonth = month,
        solarDay = day,
        lunarMonthName = LunarCalendarCalculator.normalizeLunarMonthName(
            monthName = lunar.monthName,
            isLeapMonth = lunar.isLeapMonth
        ),
        lunarDayName = lunar.dayName,
        ganZhiYear = lunar.ganZhiYear,
        ganZhiMonth = lunar.ganZhiMonth,
        ganZhiDay = lunar.ganZhiDay,
        zodiac = lunar.zodiac,
        weekday = LunarCalendarCalculator.getWeekDayName(year, month, day),
        weekdayEn = date.dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            .uppercase(Locale.ENGLISH),
        yi = yiJi.yi.filter { it.isNotBlank() }.take(MAX_YI_JI_ITEMS),
        ji = yiJi.ji.filter { it.isNotBlank() }.take(MAX_YI_JI_ITEMS),
        luckyHours = luckyHours.take(3),
        todayFestivals = todayFestivals,
        upcomingFestivals = upcomingFestivals,
        weather = weather?.takeIf { it.hasContent },
        note = note.trim().take(MAX_NOTE_LENGTH),
        labels = labels,
        palette = palette
    )
}

/** 卡面宜忌区每侧最多四行两列。 */
private const val MAX_YI_JI_ITEMS = 8
