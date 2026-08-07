package com.wanbaohe.blessingwall.service

import com.nlf.calendar.Solar
import com.nlf.calendar.util.HolidayUtil
import com.wanbaohe.blessingwall.model.BlessingCalendarDayLabel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class BlessingCalendarService @Inject constructor() {

    fun getMonthLabels(yearMonth: YearMonth): Map<LocalDate, BlessingCalendarDayLabel> {
        val startDate = yearMonth.atDay(1).minusDays(7)
        val endDate = yearMonth.atEndOfMonth().plusDays(7)
        return generateSequence(startDate) { date ->
            date.plusDays(1).takeIf { it <= endDate }
        }.associateWith(::getDayLabel)
    }

    private fun getDayLabel(date: LocalDate): BlessingCalendarDayLabel {
        val solar = Solar.fromYmd(date.year, date.monthValue, date.dayOfMonth)
        val lunar = solar.lunar
        val holiday = HolidayUtil.getHoliday(date.year, date.monthValue, date.dayOfMonth)
        val highlightedText = lunar.jieQi.takeIf(String::isNotBlank)
            ?: lunar.festivals.firstOrNull()
            ?: solar.festivals.firstOrNull()
            ?: holiday?.name?.takeIf(String::isNotBlank)
        return BlessingCalendarDayLabel(
            text = highlightedText ?: lunar.dayInChinese,
            isHighlighted = highlightedText != null,
            isRestDay = holiday?.isWork == false,
        )
    }
}
