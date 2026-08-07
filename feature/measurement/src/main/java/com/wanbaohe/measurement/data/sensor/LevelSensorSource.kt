package com.wanbaohe.measurement.data.sensor

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

@Singleton
class LevelSensorSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isAvailable: Boolean get() = accelerometer != null

    fun levelFlow(): Flow<LevelReading> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val pitch = Math.toDegrees(
                    kotlin.math.atan2(y.toDouble(), kotlin.math.sqrt((x * x + z * z).toDouble()))
                ).toFloat()
                val roll = Math.toDegrees(
                    kotlin.math.atan2(x.toDouble(), kotlin.math.sqrt((y * y + z * z).toDouble()))
                ).toFloat()

                trySend(
                    LevelReading(
                        pitch = pitch,
                        roll = roll,
                        accuracy = event.accuracy
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

data class LevelReading(
    val pitch: Float,
    val roll: Float,
    val accuracy: Int
)
