package com.wanbaohe.compass.component

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.compass.data.sensor.CompassSensorSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

/** 低通滤波平滑系数：越小越平滑（0.05 ~ 0.2 较合适） */
private const val LOW_PASS_ALPHA = 0.12f

/**
 * 指南针 UI 状态（低频快照，供 Compose 重组）
 *
 * 只携带取整度数、8 方位索引与标志位，且仅在值变化时更新：
 * 传感器噪声引起的亚度级抖动不会触发整屏重组。
 * 表盘需要的高频平滑角度见 [CompassComponent.heading]。
 */
@Immutable
data class CompassUiState(
    /** 取整后的方位角（度，0~359，0=正北，顺时针增大） */
    val degrees: Int = 0,
    /** 8 方位索引（0=北 … 7=西北，顺时针每 45° 一格），由 UI 层映射为本地化文案 */
    val directionIndex: Int = 0,
    /** 设备是否具备旋转向量传感器 */
    val isSensorAvailable: Boolean = true,
    /** 传感器精度是否不可靠（需要校准） */
    val needsCalibration: Boolean = false,
    /** 磁偏角（度，东偏为正）；无位置授权或无缓存位置时为 null，UI 显示占位符 */
    val declination: Float? = null
)

/**
 * 指南针 Component
 *
 * 负责：
 * 1. 订阅 [CompassSensorSource] 获取原始方位角
 * 2. 低通滤波消除抖动（处理角度跨 0/360 边界的特殊情况）
 * 3. 输出两路状态：
 *    - [heading]：高频平滑角度，仅供表盘在绘制阶段读取（只触发重绘，不触发重组）
 *    - [uiState]：低频取整状态，值变化才更新，驱动文本类 UI
 */
class CompassComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val sensorSource: CompassSensorSource,
    @ApplicationContext private val context: Context
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        CompassUiState(
            isSensorAvailable = sensorSource.isAvailable,
            declination = resolveDeclination()
        )
    )
    val uiState = _uiState.asStateFlow()

    /** 高频平滑方位角（[0,360)），表盘专用，避免文本 UI 跟随传感器频率重组 */
    private val _heading = MutableStateFlow(0f)
    val heading = _heading.asStateFlow()

    /** 上一次低通滤波后的方位角，用于跨边界平滑 */
    private var smoothedDegrees: Float = 0f

    init {
        if (sensorSource.isAvailable) {
            sensorSource.bearingFlow()
                .onEach { reading ->
                    val smoothed = lowPassFilter(reading.degrees, smoothedDegrees)
                    smoothedDegrees = smoothed
                    _heading.value = smoothed

                    val rounded = smoothed.roundToInt() % 360
                    val direction = toDirectionIndex(smoothed)
                    val calibrating = reading.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE

                    val current = _uiState.value
                    if (current.degrees != rounded ||
                        current.directionIndex != direction ||
                        current.needsCalibration != calibrating
                    ) {
                        _uiState.value = current.copy(
                            degrees = rounded,
                            directionIndex = direction,
                            needsCalibration = calibrating
                        )
                    }
                }
                .catch { /* 传感器异常：保持现有状态，不崩溃 */ }
                .launchIn(componentScope)
        }
    }

    // ─── 私有工具 ─────────────────────────────────────────────────────────

    /**
     * 被动计算磁偏角（只在进入页面时算一次，不请求权限、不监听位置更新）：
     * 位置权限已授予（如用户用过海拔等定位功能）时取系统缓存的 last known location，
     * 经 [GeomagneticField] 算出当地磁偏角；任一步不可行返回 null，UI 显示占位符。
     */
    @SuppressLint("MissingPermission")
    private fun resolveDeclination(): Float? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return null

        return try {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: return null
            GeomagneticField(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                location.altitude.toFloat(),
                System.currentTimeMillis()
            ).declination
        } catch (e: SecurityException) {
            null
        }
    }

    /**
     * 低通滤波，特殊处理角度在 0/360 附近的回绕问题。
     * 通过先转换为有符号差值（-180..180），再加权平均，避免从 359° 直跳 1°。
     */
    private fun lowPassFilter(newDeg: Float, prevDeg: Float): Float {
        var delta = newDeg - prevDeg
        // 将差值归一化到 [-180, 180]
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        val result = prevDeg + LOW_PASS_ALPHA * delta
        return ((result % 360f) + 360f) % 360f
    }

    /** 将 [0,360) 方位角映射到 8 方位索引（0=北，顺时针每 45° 一格） */
    private fun toDirectionIndex(deg: Float): Int =
        ((deg + 22.5f) / 45f).toInt() % 8

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): CompassComponent
    }
}
