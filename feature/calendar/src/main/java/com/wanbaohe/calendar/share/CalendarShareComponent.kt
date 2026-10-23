package com.wanbaohe.calendar.share

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.CalendarFestivalResolver
import com.wanbaohe.calendar.data.DayFestival
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.LunarDate
import com.wanbaohe.calendar.data.LunarJavaBridge
import com.wanbaohe.calendar.data.UpcomingFestivalItem
import com.wanbaohe.calendar.data.YiJi
import com.wanbaohe.calendar.data.YiJiCalculator
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.PlaceWeather
import com.wanbaohe.core.weather.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class CalendarShareUiState(
    val year: Int,
    val month: Int,
    val day: Int,
    val lunarDate: LunarDate? = null,
    val yiJi: YiJi = YiJi(),
    val luckyHours: List<CalendarShareLuckyHour> = emptyList(),
    val todayFestivals: List<DayFestival> = emptyList(),
    val upcomingFestivals: List<UpcomingFestivalItem> = emptyList(),
    val weather: CalendarShareWeather? = null,
    val isWeatherLoading: Boolean = false,
    val note: String = "",
    val isGeneratingNote: Boolean = false,
    val colorStyle: CalendarShareColorStyle = CalendarShareColorStyle.Theme,
    val showTodayFestival: Boolean = true,
    val showUpcomingFestivals: Boolean = true,
    val showWeather: Boolean = true,
    val showNote: Boolean = true,
    val isDataReady: Boolean = false,
    val isSaving: Boolean = false,
    val isToday: Boolean = false,
)

sealed interface CalendarShareEvent {
    data object SaveFailed : CalendarShareEvent

    data class Message(val text: String) : CalendarShareEvent
}

/**
 * 日历分享页状态。
 *
 * 数据层只负责日期/农历/宜忌/节日/天气/心语，配色与显示开关由 UI 传给 Renderer。
 */
class CalendarShareComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("year") initialYear: Int,
    @Assisted("month") initialMonth: Int,
    @Assisted("day") initialDay: Int,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val weatherRepository: WeatherRepository,
    private val narrativeService: CalendarShareNarrativeService,
    private val imageSaver: CalendarShareImageSaver,
    private val preferences: CalendarSharePreferences,
    private val activityLogRecorder: ActivityLogRecorder,
    private val shareProvider: ImageShareProvider<Bitmap>,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val todayDate: LocalDate = LocalDate.now()

    private val date: LocalDate = runCatching {
        if (initialYear > 0 && initialMonth in 1..12 && initialDay in 1..31) {
            LocalDate.of(initialYear, initialMonth, initialDay)
        } else {
            todayDate
        }
    }.getOrDefault(todayDate)

    private val dateKey: String =
        "%04d-%02d-%02d".format(date.year, date.monthValue, date.dayOfMonth)

    private val isToday: Boolean = date == todayDate

    private val initialSettings = preferences.load()

    private val _uiState = MutableStateFlow(
        CalendarShareUiState(
            year = date.year,
            month = date.monthValue,
            day = date.dayOfMonth,
            note = preferences.note(dateKey),
            colorStyle = initialSettings.colorStyle,
            showTodayFestival = initialSettings.showTodayFestival,
            showUpcomingFestivals = initialSettings.showUpcomingFestivals,
            showWeather = initialSettings.showWeather,
            showNote = initialSettings.showNote,
            isToday = isToday,
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CalendarShareEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    init {
        refresh()
    }

    fun setNote(value: String) {
        val text = value.take(MAX_NOTE_LENGTH)
        _uiState.update { it.copy(note = text) }
        preferences.saveNote(dateKey, text)
    }

    fun setColorStyle(style: CalendarShareColorStyle) {
        _uiState.update { it.copy(colorStyle = style) }
        persistSettings()
    }

    fun setShowTodayFestival(show: Boolean) {
        _uiState.update { it.copy(showTodayFestival = show) }
        persistSettings()
    }

    fun setShowUpcomingFestivals(show: Boolean) {
        _uiState.update { it.copy(showUpcomingFestivals = show) }
        persistSettings()
    }

    fun setShowWeather(show: Boolean) {
        _uiState.update { it.copy(showWeather = show) }
        persistSettings()
    }

    fun setShowNote(show: Boolean) {
        _uiState.update { it.copy(showNote = show) }
        persistSettings()
    }

    /**
     * 用户主动点「AI 写一句」：先拉起登录，再由 AIPromptExecutor MANAGED 执行器
     * 做积分闸门与代理路由扣费。
     */
    fun generateNote() {
        val snapshot = _uiState.value
        val lunar = snapshot.lunarDate ?: return
        if (snapshot.isGeneratingNote) return
        ActionUtils.showLogin(source = AI_SOURCE) {
            componentScope.launch {
                _uiState.update { it.copy(isGeneratingNote = true) }
                val result = narrativeService.generate(
                    CalendarShareNarrativeInput(
                        date = dateKey,
                        weekday = LunarCalendarCalculator.getWeekDayName(
                            date.year, date.monthValue, date.dayOfMonth
                        ),
                        lunar = buildString {
                            append(lunar.ganZhiYear)
                            append("年 ")
                            append(lunar.monthName)
                            append(lunar.dayName)
                        },
                        festivals = snapshot.todayFestivals.joinToString("、") { it.name },
                        yi = snapshot.yiJi.yi.take(5),
                        ji = snapshot.yiJi.ji.take(5),
                        weather = snapshot.weather?.summary(),
                    )
                )
                _uiState.update { it.copy(isGeneratingNote = false) }
                result.onSuccess(::setNote).onFailure { error ->
                    _events.emit(
                        CalendarShareEvent.Message(error.message ?: "AI 生成失败")
                    )
                }
            }
        }
    }

    fun saveImage(bitmap: Bitmap) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        componentScope.launch {
            val outcome = imageSaver.save(bitmap)
            _uiState.update { it.copy(isSaving = false) }
            when (outcome) {
                is CalendarShareSaveOutcome.Gallery -> {
                    showSavedToast(
                        uri = outcome.uri,
                        message = AppContext.getString(
                            R.string.calendar_share_saved_gallery,
                            outcome.relativePath
                        ),
                        fileName = outcome.fileName,
                        fileUri = outcome.uri.toString(),
                        savePath = outcome.relativePath
                    )
                }

                is CalendarShareSaveOutcome.AppStorage -> {
                    showSavedToast(
                        uri = null,
                        message = AppContext.getString(
                            R.string.calendar_share_saved_app,
                            outcome.relativePath
                        ),
                        fileName = outcome.fileName,
                        fileUri = Uri.fromFile(outcome.file).toString(),
                        savePath = outcome.relativePath
                    )
                }

                is CalendarShareSaveOutcome.Failed ->
                    _events.emit(CalendarShareEvent.SaveFailed)
            }
        }
    }

    /**
     * 保存成功：走全局文件成功提示（带「打开」，Toast 消失后由 AppToastHost 触发
     * InAppReviewPrompt 的 SAVE_SUCCESS），同时写一条活动日志。
     */
    private fun showSavedToast(
        uri: Uri?,
        message: String,
        fileName: String,
        fileUri: String,
        savePath: String,
    ) {
        AppToastHost.showFileSuccessToast(
            uri = uri,
            message = message,
            trigger = ReviewPromptTrigger.SAVE_SUCCESS
        )
        val lunar = _uiState.value.lunarDate
        val description = buildString {
            append("保存分享卡 · ")
            append(dateKey)
            if (lunar != null) {
                append(" 农历")
                append(lunar.monthName)
                append(lunar.dayName)
            }
            if (savePath.isNotBlank()) {
                append(" · ")
                append(savePath)
            }
        }
        componentScope.launch {
            runCatching {
                activityLogRecorder.recordImageSave(
                    screenId = SCREEN_ID,
                    screenName = AppContext.getString(R.string.calendar_share_page_title),
                    description = description,
                    fileUri = fileUri,
                    fileName = fileName,
                    savePath = savePath,
                )
            }
        }
    }

    fun shareBitmap(bitmap: Bitmap) {
        componentScope.launch {
            shareProvider.shareImage(
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat.Png.Lossless
                ),
                image = bitmap,
                onComplete = {}
            )
        }
    }

    // ─── 内部 ────────────────────────────────────────────────────────

    private fun refresh() {
        componentScope.launch {
            val core = withContext(defaultDispatcher) {
                val lunar = LunarCalendarCalculator.solarToLunar(
                    date.year, date.monthValue, date.dayOfMonth
                )
                CalendarShareCore(
                    lunar = lunar,
                    yiJi = YiJiCalculator.getYiJi(
                        date.year, date.monthValue, date.dayOfMonth
                    ),
                    luckyHours = LunarJavaBridge.getTimeSlots(
                        date.year, date.monthValue, date.dayOfMonth
                    ).toShareLuckyHours(),
                    todayFestivals = CalendarFestivalResolver.today(lunar),
                    upcomingFestivals = CalendarFestivalResolver.upcoming(
                        date.year, date.monthValue, date.dayOfMonth
                    ),
                )
            }
            _uiState.update {
                it.copy(
                    lunarDate = core.lunar,
                    yiJi = core.yiJi,
                    luckyHours = core.luckyHours,
                    todayFestivals = core.todayFestivals,
                    upcomingFestivals = core.upcomingFestivals,
                    isDataReady = true,
                )
            }
            if (isToday) loadWeather()
        }
    }

    private fun loadWeather() {
        if (_uiState.value.isWeatherLoading) return
        _uiState.update { it.copy(isWeatherLoading = true) }
        componentScope.launch {
            val latest = runCatching { weatherRepository.latestPlace() }.getOrNull()
            latest?.let { publishWeather(it.city, it.place) }

            val city = runCatching { weatherRepository.getCityFromNetwork() }
                .getOrNull()
                ?.getOrNull()
            if (city != null) {
                val place = runCatching {
                    weatherRepository.getPlaceWeather(city.lat, city.lon)
                }.getOrNull()?.getOrNull()
                if (place != null) publishWeather(city, place)
            }
            _uiState.update { it.copy(isWeatherLoading = false) }
        }
    }

    private fun publishWeather(city: CityInfo, place: PlaceWeather) {
        val weather = city.toShareWeather(place) ?: return
        _uiState.update { it.copy(weather = weather) }
    }

    private fun persistSettings() {
        val state = _uiState.value
        preferences.save(
            CalendarSharePreferences.Settings(
                colorStyle = state.colorStyle,
                showTodayFestival = state.showTodayFestival,
                showUpcomingFestivals = state.showUpcomingFestivals,
                showWeather = state.showWeather,
                showNote = state.showNote,
            )
        )
    }

    private data class CalendarShareCore(
        val lunar: LunarDate,
        val yiJi: YiJi,
        val luckyHours: List<CalendarShareLuckyHour>,
        val todayFestivals: List<DayFestival>,
        val upcomingFestivals: List<UpcomingFestivalItem>,
    )

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("year") year: Int = -1,
            @Assisted("month") month: Int = -1,
            @Assisted("day") day: Int = -1,
            onGoBack: () -> Unit,
        ): CalendarShareComponent
    }

    private companion object {
        const val AI_SOURCE = "calendar_share_note"
        const val SCREEN_ID = "1120"
    }
}
