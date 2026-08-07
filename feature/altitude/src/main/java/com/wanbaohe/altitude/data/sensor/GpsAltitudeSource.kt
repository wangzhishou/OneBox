package com.wanbaohe.altitude.data.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GPS 定位读数（每次定位更新发射一次）
 */
data class GpsReading(
    /**
     * 海拔高度（米，WGS-84 椭球面高度）
     * null = 本次定位无可信高程（NETWORK_PROVIDER / GPS 仅 2D 定位 / 设备未提供垂直精度）
     */
    val altitudeMeters: Double?,
    /** 水平精度（米，越小越精准） */
    val accuracyMeters: Float,
    /** 垂直精度（米，API 26+，不支持时为 0） */
    val verticalAccuracyMeters: Float = 0f,
    /** 设备是否提供了垂直精度（API 26+） */
    val isVerticalAccuracyAvailable: Boolean = false,
    /** 纬度（WGS-84） */
    val latitude: Double = 0.0,
    /** 经度（WGS-84） */
    val longitude: Double = 0.0
)

/**
 * GPS 持续海拔数据源
 * 每次定位更新都向 Flow 发射新读数，协程取消时自动注销监听器
 * 需要 ACCESS_FINE_LOCATION 权限
 */
@Singleton
class GpsAltitudeSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /** GPS 或网络定位是否可用 */
    val isAvailable: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    /** 是否已获得精确定位权限 */
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * 返回持续更新的海拔 Flow
     * 优先使用 GPS_PROVIDER，其次 NETWORK_PROVIDER
     * 每 3 秒或移动 0 米触发一次更新（GPS 硬件决定实际频率）
     */
    @SuppressLint("MissingPermission")
    fun altitudeFlow(): Flow<GpsReading> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER
            else -> { close(); return@callbackFlow }
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 无论是否有高程，只要有经纬度就发射，经纬度与海拔解耦
                val verticalAvailable =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasVerticalAccuracy()
                val verticalAccuracy =
                    if (verticalAvailable) location.verticalAccuracyMeters else 0f
                // 只要设备报告 hasAltitude，就先展示高度；垂直精度可用性由 UI 单独提示。
                val altitude: Double? = if (location.hasAltitude()) location.altitude else null
                trySend(
                    GpsReading(
                        altitudeMeters = altitude,
                        accuracyMeters = location.accuracy,
                        verticalAccuracyMeters = verticalAccuracy,
                        isVerticalAccuracyAvailable = verticalAvailable,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                )
            }

            @Deprecated("Deprecated in API 29+")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) = Unit
        }

        // 先发射最后已知位置，提供即时显示（减少"搜索中"等待感）
        try {
            val lastKnown = locationManager.getLastKnownLocation(provider)
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnown != null) {
                val verticalAvailable =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && lastKnown.hasVerticalAccuracy()
                val va = if (verticalAvailable) lastKnown.verticalAccuracyMeters else 0f
                val altitude: Double? = if (lastKnown.hasAltitude()) lastKnown.altitude else null
                trySend(
                    GpsReading(
                        altitudeMeters = altitude,
                        accuracyMeters = lastKnown.accuracy,
                        verticalAccuracyMeters = va,
                        isVerticalAccuracyAvailable = verticalAvailable,
                        latitude = lastKnown.latitude,
                        longitude = lastKnown.longitude
                    )
                )
            }
        } catch (_: Exception) { /* 权限异常或设备不支持，静默忽略 */ }

        // 最小更新间隔 3 秒，最小移动距离 0 米
        locationManager.requestLocationUpdates(provider, 3_000L, 0f, listener)

        awaitClose { locationManager.removeUpdates(listener) }
    }
}
