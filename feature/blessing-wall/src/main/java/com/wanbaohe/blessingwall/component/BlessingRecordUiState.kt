package com.wanbaohe.blessingwall.component

import com.shifenmiao.model.remote.BlessingWallTabText
import com.wanbaohe.blessingwall.model.BlessingCalendarDayLabel
import com.wanbaohe.blessingwall.model.BlessingTabCustomization
import com.wanbaohe.blessingwall.model.BlessingType
import com.wanbaohe.blessingwall.model.DailyBlessingRecord
import java.time.LocalDate
import java.time.YearMonth

data class BlessingRecordUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: String? = null,
    val calendarDayLabels: Map<LocalDate, BlessingCalendarDayLabel> = emptyMap(),
    val monthRecords: List<DailyBlessingRecord> = emptyList(),
    val selectedDayRecords: List<DailyBlessingRecord> = emptyList(),
    /** key = 记录日期（yyyy-MM-dd），value = 该日期生效的标题/副标题自定义快照 */
    val tabCustomizationsByDate: Map<String, Map<BlessingType, BlessingTabCustomization>> = emptyMap(),
    val remoteTabTexts: Map<BlessingType, BlessingWallTabText> = emptyMap(),
)
