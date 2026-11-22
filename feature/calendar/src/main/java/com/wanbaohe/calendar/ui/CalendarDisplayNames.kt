package com.wanbaohe.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import com.wanbaohe.calendar.R

private val zodiacKeys = listOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
private val constellationKeys = listOf("白羊", "金牛", "双子", "巨蟹", "狮子", "处女", "天秤", "天蝎", "射手", "摩羯", "水瓶", "双鱼")
private val solarFestivalKeys = listOf("元旦", "情人节", "愚人节", "万圣节", "感恩节", "平安夜", "圣诞节", "母亲节", "父亲节", "地球日")

@Composable
internal fun localizedZodiac(name: String): String =
    localizedCalendarName(name, zodiacKeys, R.array.calendar_zodiac_names)

@Composable
internal fun localizedConstellation(name: String): String =
    localizedCalendarName(name.removeSuffix("座"), constellationKeys, R.array.calendar_constellation_names)

@Composable
internal fun localizedSolarFestival(name: String): String =
    localizedCalendarName(name, solarFestivalKeys, R.array.calendar_solar_festival_names)

@Composable
private fun localizedCalendarName(name: String, keys: List<String>, labels: Int): String {
    if (LocalConfiguration.current.locales[0].language == "zh") return name
    val index = keys.indexOf(name)
    return if (index < 0) name else stringArrayResource(labels)[index]
}
