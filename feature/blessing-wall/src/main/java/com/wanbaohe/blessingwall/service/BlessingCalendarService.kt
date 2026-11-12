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
