package com.wanbaohe.altitude.component

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.logger.makeLog
import com.wanbaohe.altitude.data.AltitudeRepository
import com.wanbaohe.altitude.data.sensor.BarometerAltitudeSource
import com.wanbaohe.altitude.data.sensor.GpsAltitudeSource
import com.wanbaohe.altitude.domain.AltitudeRecord
import com.wanbaohe.altitude.domain.AltitudeSource
import com.wanbaohe.altitude.domain.AltitudeUnit
import com.wanbaohe.altitude.domain.CitySnapshot
import com.wanbaohe.altitude.domain.WeatherSnapshot
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.wanbaohe.core.weather.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


private const val TREND_LIMIT = 50
/** 城市/天气刷新最小间隔（毫秒） */
private const val WEATHER_THROTTLE_MS = 60_000L

/**
 * UI 状态（不可变快照）
 */
@Immutable
data class AltitudeUiState(
    /** 当前 GPS 海拔（米），null = 尚未收到第一个定位 */
    val currentAltitudeMeters: Float? = null,
    /** 水平精度（米） */
    val accuracyMeters: Float = 0f,
    /** 垂直精度（米，不支持时为 0） */
    val verticalAccuracyMeters: Float = 0f,
    /** GPS 垂直精度是否可用（API 26+） */
    val isVerticalAccuracyAvailable: Boolean = false,
    /** 显示单位 */
    val unit: AltitudeUnit = AltitudeUnit.METERS,
    /** GPS 是否可用（硬件/权限层面） */
    val isGpsAvailable: Boolean = true,
    /** 正在等待第一个 GPS 信号 */
    val isAcquiring: Boolean = true,
    /** 相对上次更新的高度变化（米），正=上升，负=下降，null=首次 */
    val altitudeDeltaMeters: Float? = null,
    /** 历史保存记录列表 */
    val history: List<AltitudeRecord> = emptyList(),
    /** 趋势图数据点（时间正序，最近 TREND_LIMIT 条） */
    val trendPoints: List<Float> = emptyList(),
    /** 保存对话框 */
    val showSaveDialog: Boolean = false,
    /** 清空确认对话框 */
    val showClearAllDialog: Boolean = false,
    /** 是否已获得定位权限 */
    val locationPermissionGranted: Boolean = true,
    /** 当前纬度（null = 尚未定位） */
    val latitude: Double? = null,
    /** 当前经度（null = 尚未定位） */
    val longitude: Double? = null,
    /**
     * 当前海拔数据来源：GPS / BAROMETER / null（尚未获取到）
     * GPS 优先；GPS 无高程时自动降级到气压计
     */
    val altitudeSource: AltitudeSource? = null,
    /** 城市/位置信息 */
    val cityInfo: CityInfo? = null,
    /** 当前天气信息 */
    val weatherInfo: WeatherInfo? = null,
    /** 底部选中标签页索引：0=INSTRUMENT, 1=HISTORY, 2=BOOKMARKS */
    val selectedTab: Int = 0,
    /** 当前选中的历史记录（详情底部弹窗） */
    val selectedRecord: AltitudeRecord? = null,
    /** 待删除的记录 ID（删除确认对话框） */
    val pendingDeleteId: Long? = null
) {
    /** 当前海拔按显示单位格式化 */
    val displayAltitude: String
        get() = currentAltitudeMeters
            ?.let { "%.1f".format(unit.fromMeters(it)) }
            ?: "--"

    /** 精度等级（0~3，越高越精准，用于信号格显示） */
    val accuracyLevel: Int
        get() = when {
            currentAltitudeMeters == null -> 0
            altitudeSource == AltitudeSource.BAROMETER -> 3   // 气压计精度稳定，视为满格
            accuracyMeters <= 5f -> 3
            accuracyMeters <= 15f -> 2
            accuracyMeters <= 40f -> 1
            else -> 0
        }

    /** 趋势状态标签：ASCENDING / DESCENDING / LEVEL */
    val trendBadge: String
        get() {
            val d = altitudeDeltaMeters ?: return "LEVEL"
            return when {
                d > 0.5f -> "ASCENDING"
                d < -0.5f -> "DESCENDING"
                else -> "LEVEL"
            }
        }
}

/**
 * 海拔仪 Component（GPS-only）
 */
class AltitudeComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: AltitudeRepository,
    private val gpsSource: GpsAltitudeSource,
    private val barometerSource: BarometerAltitudeSource,
    private val weatherRepository: WeatherRepository,
    private val shareProvider: ImageShareProvider<Bitmap>
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        AltitudeUiState(
            isGpsAvailable = gpsSource.isAvailable,
            locationPermissionGranted = gpsSource.hasPermission()
        )
    )
    val uiState = _uiState.asStateFlow()

    /** 上一次海拔读数（用于计算 delta） */
    private var previousAltitudeMeters: Float? = null

    /** 上次城市/天气请求的时间戳，用于节流 */
    private var lastWeatherFetchMs: Long = 0L

    init {
        if (gpsSource.hasPermission()) {
            startGpsTracking()
            startBarometerFallback()
        }

        // 订阅历史记录（实时更新）
        repository.getAll()
            .onEach { records ->
                val trend = records.take(TREND_LIMIT).reversed().map { it.altitudeMeters }
                _uiState.value = _uiState.value.copy(
                    history = records,
                    trendPoints = trend
                )
            }
            .catch { }
            .launchIn(componentScope)
    }

    /**
     * 获取城市信息并更新到 UI 状态
     */
    private fun fetchCityInfo() {
        componentScope.launch {
            val lon = _uiState.value.longitude ?: return@launch
            val lat = _uiState.value.latitude ?: return@launch
            weatherRepository.getCityAtLocation(lat, lon)
                .onSuccess { cityInfo ->
                    makeLog("City: ${cityInfo.name}, ${cityInfo}")
                    _uiState.value = _uiState.value.copy(cityInfo = cityInfo)
                }
                .onFailure { e ->
                    makeLog("City error: ${e.message}")
                }
        }
    }

    /**
     * 获取当前天气信息并更新到 UI 状态
     */
    private fun fetchWeatherNow() {
        componentScope.launch {
            val lon = _uiState.value.longitude ?: return@launch
            val lat = _uiState.value.latitude ?: return@launch
            weatherRepository.getWeatherAtLocation(lat, lon)
                .onSuccess { weatherInfo ->
                    _uiState.value = _uiState.value.copy(weatherInfo = weatherInfo)
                }
                .onFailure { e ->
                    makeLog("Weather error: ${e.message}")
                }
        }
    }

    /**
     * 节流调用：获取城市和天气信息（至少间隔 WEATHER_THROTTLE_MS）
     */
    private fun fetchLocationDataThrottled() {
        val now = System.currentTimeMillis()
        if (now - lastWeatherFetchMs < WEATHER_THROTTLE_MS) return
        lastWeatherFetchMs = now
        fetchCityInfo()
        fetchWeatherNow()
    }

    // ─── 事件 ──────────────────────────────────────────────────────────────

    /**
     * 用户授予定位权限后调用，更新状态并启动 GPS 追踪
     */
    fun onLocationPermissionGranted() {
        if (_uiState.value.locationPermissionGranted) return  // 已在追踪，忽略重复调用
        _uiState.value = _uiState.value.copy(
            locationPermissionGranted = true,
            isAcquiring = true
        )
        startGpsTracking()
        startBarometerFallback()
    }

    fun toggleUnit() {
        _uiState.value = _uiState.value.copy(
            unit = if (_uiState.value.unit == AltitudeUnit.METERS) AltitudeUnit.FEET
                   else AltitudeUnit.METERS
        )
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    /** 截图分享：将传入的 Bitmap 通过 ImageShareProvider 分享 */
    fun shareBitmap(bitmap: Bitmap, onComplete: () -> Unit) {
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

    fun showSaveDialog() { _uiState.value = _uiState.value.copy(showSaveDialog = true) }
    fun dismissSaveDialog() { _uiState.value = _uiState.value.copy(showSaveDialog = false) }

    fun saveRecord(note: String) {
        val uiState = _uiState.value
        val meters = uiState.currentAltitudeMeters ?: return
        val source = uiState.altitudeSource ?: AltitudeSource.GPS
        componentScope.launch(defaultDispatcher) {
            repository.save(
                AltitudeRecord(
                    altitudeMeters = meters,
                    source = source,
                    accuracyMeters = uiState.accuracyMeters,
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    citySnapshot = uiState.cityInfo?.toSnapshot(),
                    weatherSnapshot = uiState.weatherInfo?.toSnapshot(),
                    note = note.trim()
                )
            )
        }
        _uiState.value = _uiState.value.copy(showSaveDialog = false)
    }

    fun deleteRecord(id: Long) {
        componentScope.launch(defaultDispatcher) { repository.deleteById(id) }
    }

    fun requestDelete(id: Long) {
        _uiState.value = _uiState.value.copy(pendingDeleteId = id)
    }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        componentScope.launch(defaultDispatcher) { repository.deleteById(id) }
        _uiState.value = _uiState.value.copy(pendingDeleteId = null)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(pendingDeleteId = null)
    }

    fun showClearAllDialog() { _uiState.value = _uiState.value.copy(showClearAllDialog = true) }
    fun dismissClearAllDialog() { _uiState.value = _uiState.value.copy(showClearAllDialog = false) }

    fun clearAll() {
        componentScope.launch(defaultDispatcher) { repository.clearAll() }
        _uiState.value = _uiState.value.copy(showClearAllDialog = false)
    }

    fun selectRecord(record: AltitudeRecord) {
        _uiState.value = _uiState.value.copy(selectedRecord = record)
    }

    fun dismissRecordDetail() {
        _uiState.value = _uiState.value.copy(selectedRecord = null)
    }

    // ─── 私有 ─────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun startGpsTracking() {
        gpsSource.altitudeFlow()
            .onEach { reading ->
                val altitudeFloat = reading.altitudeMeters?.toFloat()
                _uiState.value = if (altitudeFloat != null) {
                    // 收到有效 GPS 高程：更新全部字段，GPS 优先覆盖气压计
                    val delta = previousAltitudeMeters?.let { altitudeFloat - it }
                    previousAltitudeMeters = altitudeFloat
                    _uiState.value.copy(
                        currentAltitudeMeters = altitudeFloat,
                        accuracyMeters = reading.accuracyMeters,
                        verticalAccuracyMeters = reading.verticalAccuracyMeters,
                        isVerticalAccuracyAvailable = reading.isVerticalAccuracyAvailable,
                        isAcquiring = false,
                        altitudeDeltaMeters = delta,
                        latitude = reading.latitude,
                        longitude = reading.longitude,
                        altitudeSource = AltitudeSource.GPS
                    )
                } else {
                    // 只有经纬度（无可信高程）：更新坐标，保持其他状态
                    _uiState.value.copy(
                        accuracyMeters = reading.accuracyMeters,
                        latitude = reading.latitude,
                        longitude = reading.longitude
                    )
                }
                fetchLocationDataThrottled()
            }
            .catch { _uiState.value = _uiState.value.copy(isAcquiring = false) }
            .launchIn(componentScope)
    }

    /**
     * 气压计兜底：设备有气压传感器时，在 GPS 高程不可用时提供海拔数据。
     * 一旦 GPS 给出有效高程（altitudeSource == GPS），气压计读数自动让位。
     */
    private fun startBarometerFallback() {
        if (!barometerSource.isAvailable) return
        barometerSource.altitudeFlow()
            .onEach { baroMeters ->
                // 仅当当前没有 GPS 高程时才使用气压计值
                if (_uiState.value.altitudeSource != AltitudeSource.GPS) {
                    val delta = previousAltitudeMeters?.let { baroMeters - it }
                    previousAltitudeMeters = baroMeters
                    _uiState.value = _uiState.value.copy(
                        currentAltitudeMeters = baroMeters,
                        verticalAccuracyMeters = 0f,
                        isVerticalAccuracyAvailable = false,
                        isAcquiring = false,
                        altitudeDeltaMeters = delta,
                        altitudeSource = AltitudeSource.BAROMETER
                    )
                }
            }
            .catch { }
            .launchIn(componentScope)
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): AltitudeComponent
    }
}

private fun CityInfo.toSnapshot() = CitySnapshot(
    name = name,
    adm2 = adm2,
    adm1 = adm1,
    country = country,
    tz = tz,
    utcOffset = utcOffset
)

private fun WeatherInfo.toSnapshot() = WeatherSnapshot(
    forecast = buildString {
        if (text.isNotBlank()) append(text)
        if (temp.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(temp).append("°C")
        }
        if (windDir.isNotBlank() || windScale.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(windDir)
            if (windScale.isNotBlank()) append(AppContext.getString(CoreR.string.altitude_wind_scale, windScale))
        }
    },
    text = text,
    temp = temp,
    feelsLike = feelsLike,
    windDir = windDir,
    windScale = windScale,
    windSpeed = windSpeed,
    humidity = humidity,
    pressure = pressure,
    obsTime = obsTime,
    updateTime = updateTime,
    dew = dew,
    cloud = cloud,
    vis = vis,
    precip = precip
)

