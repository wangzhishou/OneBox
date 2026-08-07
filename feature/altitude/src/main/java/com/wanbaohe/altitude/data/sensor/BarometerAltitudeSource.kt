package com.wanbaohe.altitude.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * 气压计海拔数据源
 * 使用 TYPE_PRESSURE 传感器 + 国际标准大气公式换算海拔（无需任何运行时权限）
 */
@Singleton
class BarometerAltitudeSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val pressureSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    /** 设备是否具备气压传感器 */
    val isAvailable: Boolean get() = pressureSensor != null

    /**
     * 返回实时海拔 Flow（米），收集时注册监听，取消时自动注销
     * 公式：altitude = 44330 * (1 − (p / SEA_PRESSURE)^0.1903)
     */
    fun altitudeFlow(): Flow<Float> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val pressure = event.values[0]           // hPa
                val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
                trySend(altitude)
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        sensorManager.registerListener(
            listener,
            pressureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

