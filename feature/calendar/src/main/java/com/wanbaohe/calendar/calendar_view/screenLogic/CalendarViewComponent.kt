package com.wanbaohe.calendar.calendar_view.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.calendar.data.CalendarDayInfo
import com.wanbaohe.calendar.data.FestivalType
import com.wanbaohe.calendar.data.FotoData
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.TaoData
import com.wanbaohe.calendar.data.LunarDate
import com.wanbaohe.calendar.data.LunarJavaBridge
import com.wanbaohe.calendar.data.LunarTimeSlot
import com.wanbaohe.calendar.data.UpcomingFestivalItem
import com.wanbaohe.calendar.data.YiJi
import com.wanbaohe.calendar.data.YiJiCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 日历查看子组件状态
 */
@Immutable
data class CalendarViewState(
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val lunarDate: LunarDate? = null,
    val yiJi: YiJi = YiJi(),
    val timeSlots: List<LunarTimeSlot> = emptyList(),
    val fotoData: FotoData? = null,
    val taoData: TaoData? = null,
    val calendarDays: List<CalendarDayInfo> = emptyList(),
    val nextSolarTerm: Pair<String, String>? = null,
    val upcomingFestivalItems: List<UpcomingFestivalItem> = emptyList(),
    val isDataReady: Boolean = false,
)

/**
 * 日历查看子组件
 *
 * 职责：月历网格、选中日详情、宜忌、时辰、佛历道历、未来节日
 */
class CalendarViewComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("initialYear") private val initialYear: Int,
    @Assisted("initialMonth") private val initialMonth: Int,
    @Assisted("initialDay") private val initialDay: Int,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val shareProvider: ImageShareProvider<Bitmap>,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        createInitialState(initialYear, initialMonth, initialDay)
    )
    private var refreshJob: Job? = null
    val uiState = _uiState.asStateFlow()

    init {
        refreshCalendar()
    }

    /** 选择日期（支持跨月选择） */
    fun selectDate(year: Int, month: Int, day: Int) {
        val state = _uiState.value
        val monthChanged = (year != state.currentYear || month != state.currentMonth)
        _uiState.value = state.copy(
            currentYear = year,
            currentMonth = month,
            selectedDay = day
        )
        if (monthChanged) {
            refreshCalendar()
        } else {
            refreshSelectedDayInfo()
        }
    }

    /** 切换到上一个月 */
    fun previousMonth() {
        val state = _uiState.value
        val newMonth = if (state.currentMonth == 1) 12 else state.currentMonth - 1
        val newYear = if (state.currentMonth == 1) state.currentYear - 1 else state.currentYear
        _uiState.value = state.copy(currentYear = newYear, currentMonth = newMonth, selectedDay = 1)
        refreshCalendar()
    }

    /** 切换到下一个月 */
    fun nextMonth() {
        val state = _uiState.value
        val newMonth = if (state.currentMonth == 12) 1 else state.currentMonth + 1
        val newYear = if (state.currentMonth == 12) state.currentYear + 1 else state.currentYear
        _uiState.value = state.copy(currentYear = newYear, currentMonth = newMonth, selectedDay = 1)
        refreshCalendar()
    }

    /** 回到今天 */
    fun goToToday() {
        val now = Calendar.getInstance()
        _uiState.value = _uiState.value.copy(
            currentYear = now.get(Calendar.YEAR),
            currentMonth = now.get(Calendar.MONTH) + 1,
            selectedDay = now.get(Calendar.DAY_OF_MONTH)
        )
        refreshCalendar()
    }

    private fun createInitialState(
        initialYear: Int,
        initialMonth: Int,
        initialDay: Int,
    ): CalendarViewState {
        val cal = Calendar.getInstance()
        val year = if (initialYear > 0) initialYear else cal.get(Calendar.YEAR)
        val month = if (initialMonth > 0) initialMonth else cal.get(Calendar.MONTH) + 1
        val day = if (initialDay > 0) initialDay else cal.get(Calendar.DAY_OF_MONTH)
        return CalendarViewState(currentYear = year, currentMonth = month, selectedDay = day)
    }

    /** 截图分享 */
    fun shareBitmap(bitmap: Bitmap, onComplete: () -> Unit = {}) {
        componentScope.launch {
            shareProvider.shareImage(
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat.Png.Lossless
                ),
                image = bitmap,
                onComplete = onComplete
            )
        }
    }

    // ─── 内部刷新方法 ────────────────────────────────────────────────

    private fun refreshCalendar() {
        refreshJob?.cancel()
        refreshJob = componentScope.launch {
            val state = _uiState.value
            val days = withContext(defaultDispatcher) {
                LunarCalendarCalculator.getMonthCalendarDays(state.currentYear, state.currentMonth)
            }
            _uiState.value = _uiState.value.copy(calendarDays = days)
            refreshSelectedDayInfo()
        }
    }

    private fun refreshSelectedDayInfo() {
        refreshJob?.cancel()
        refreshJob = componentScope.launch {
            val state = _uiState.value
            val year = state.currentYear
            val month = state.currentMonth
            val day = state.selectedDay

            // 第一阶段：加载核心日历数据（农历、宜忌、时辰），快速显示
            val (lunar, yiJi, timeSlots, nextSolarTerm) = withContext(defaultDispatcher) {
                val l = LunarCalendarCalculator.solarToLunar(year, month, day)
                val y = YiJiCalculator.getYiJi(year, month, day)
                val t = LunarJavaBridge.getTimeSlots(year, month, day)
                val n = LunarCalendarCalculator.getNextSolarTerm(year, month, day)
                CoreDayInfo(l, y, t, n)
            }
            ensureActive()
            _uiState.value = _uiState.value.copy(
                lunarDate = lunar,
                yiJi = yiJi,
                timeSlots = timeSlots,
                nextSolarTerm = nextSolarTerm,
                isDataReady = true,
            )

            // 第二阶段：后台加载节日数据
            launch {
                val upcomingItems = withContext(defaultDispatcher) {
                    buildUpcomingFestivalItems(year, month, day)
                }
                ensureActive()
                _uiState.value = _uiState.value.copy(
                    upcomingFestivalItems = upcomingItems
                )
            }
            // 第三阶段：后台加载佛历/道历数据
            launch {
                val (foto, tao) = withContext(defaultDispatcher) {
                    Pair(
                        LunarJavaBridge.getFotoData(year, month, day),
                        LunarJavaBridge.getTaoData(year, month, day)
                    )
                }
                ensureActive()
                _uiState.value = _uiState.value.copy(
                    fotoData = foto,
                    taoData = tao
                )
            }
        }
    }

    private fun buildUpcomingFestivalItems(
        year: Int,
        month: Int,
        day: Int
    ): List<UpcomingFestivalItem> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val result = linkedSetOf<UpcomingFestivalItem>()
        repeat(31) { offset ->
            val cal = start.clone() as Calendar
            cal.add(Calendar.DAY_OF_MONTH, offset)

            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val lunarDay = LunarCalendarCalculator.solarToLunar(y, m, d)
            val dateText = "${m}月${d}日"

            lunarDay.solarTerm?.takeIf { it.isNotBlank() }?.let { name ->
                result += UpcomingFestivalItem(name, dateText, offset, FestivalType.SolarTerm)
            }
            val lunarFestivals = if (lunarDay.lunarFestivals.isNotEmpty()) {
                lunarDay.lunarFestivals
            } else {
                listOfNotNull(lunarDay.lunarFestival)
            }
            lunarFestivals.filter { it.isNotBlank() }.forEach { name ->
                result += UpcomingFestivalItem(name, dateText, offset, FestivalType.LunarFestival)
            }

            val solarFestivals = if (lunarDay.solarFestivals.isNotEmpty()) {
                lunarDay.solarFestivals
            } else {
                listOfNotNull(lunarDay.solarFestival)
            }
            solarFestivals.filter { it.isNotBlank() }.forEach { name ->
                result += UpcomingFestivalItem(name, dateText, offset, FestivalType.SolarFestival)
            }

            if (lunarDay.isLegalHoliday && !lunarDay.legalHolidayName.isNullOrBlank()) {
                result += UpcomingFestivalItem(
                    lunarDay.legalHolidayName,
                    dateText,
                    offset,
                    FestivalType.LegalHoliday
                )
            }
        }

        return result
            .sortedBy { it.daysUntil }
            .take(12)
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("initialYear") initialYear: Int = -1,
            @Assisted("initialMonth") initialMonth: Int = -1,
            @Assisted("initialDay") initialDay: Int = -1,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): CalendarViewComponent
    }
}

private data class CoreDayInfo(
    val lunar: LunarDate,
    val yiJi: YiJi,
    val timeSlots: List<LunarTimeSlot>,
    val nextSolarTerm: Pair<String, String>?
)
