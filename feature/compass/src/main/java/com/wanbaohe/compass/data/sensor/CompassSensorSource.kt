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

package com.wanbaohe.compass.data.sensor

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

/**
 * 罗盘传感器数据源
 *
 * 使用 [Sensor.TYPE_ROTATION_VECTOR]，该传感器融合了加速度计、陀螺仪
 * 及地磁传感器，提供相对于地理真北的方位角，精度优于纯磁力计方案。
 * 无需任何运行时权限。
 */
@Singleton
class CompassSensorSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    /** 设备是否具备所需传感器 */
    val isAvailable: Boolean get() = rotationVectorSensor != null

    /**
     * 实时方位角 Flow（单位：度，[0, 360)，0 = 正北，顺时针增大）
     * 订阅时自动注册监听，取消时自动注销，天然防泄漏。
     *
     * @return 每次传感器更新触发一个新值
     */
    fun bearingFlow(): Flow<BearingReading> = callbackFlow {
        val rotMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // 将旋转向量转换为旋转矩阵，再提取方位角
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                SensorManager.getOrientation(rotMatrix, orientation)

                // orientation[0] = 弧度制方位角，转换为 [0, 360) 度
                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    .let { if (it < 0f) it + 360f else it }

                trySend(
                    BearingReading(
                        degrees = azimuthDeg,
                        // SensorManager 精度常量：0=不可靠，1=低，2=中，3=高
                        accuracy = event.accuracy
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_GAME   // ~50 Hz，平衡流畅度与功耗
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

/**
 * 一次方位角读数快照
 * @param degrees  方位角（度，[0, 360)）
 * @param accuracy SensorManager 精度常量（0~3）
 */
data class BearingReading(
    val degrees: Float,
    val accuracy: Int
)

