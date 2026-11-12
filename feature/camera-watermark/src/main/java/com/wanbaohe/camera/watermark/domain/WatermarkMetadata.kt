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

package com.wanbaohe.camera.watermark.domain

/**
 * 相机水印元数据
 * 从图片 EXIF 信息中提取的关键拍摄参数
 */
data class WatermarkMetadata(
    val cameraMake: String = "",           // 相机品牌 (如 LEICA, Canon, Nikon)
    val cameraModel: String = "",          // 相机型号 (如 MP, M11)
    val lensMake: String = "",             // 镜头品牌
    val lensModel: String = "",            // 镜头型号
    val filmModel: String = "",            // 胶片型号 (用户自定义，EXIF 无此信息)
    val focalLength: String = "",          // 焦距 (如 120mm)
    val aperture: String = "",             // 光圈 (如 f/4.1)
    val shutterSpeed: String = "",         // 快门速度 (如 1/100)
    val iso: String = "",                  // ISO (如 ISO90)
    val dateTime: String = "",             // 拍摄时间 (如 2023.03.19 08:46:12)
    val latitude: String = "",             // GPS 纬度 (如 40°3'13"N)
    val longitude: String = "",            // GPS 经度 (如 116°19'25"E)
) {
    /**
     * 格式化相机信息: 品牌 + 型号 + 胶片
     */
    fun formatCameraInfo(): String {
        val camera = buildString {
            if (cameraMake.isNotEmpty()) append(cameraMake.uppercase())
            if (cameraModel.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append(cameraModel)
            }
        }
        return if (filmModel.isNotEmpty()) {
            "$camera + $filmModel"
        } else {
            camera
        }
    }

    /**
     * 格式化拍摄参数: 焦距 光圈 快门 ISO
     */
    fun formatShootingParams(): String {
        return listOfNotNull(
            focalLength.takeIf { it.isNotEmpty() },
            aperture.takeIf { it.isNotEmpty() },
            shutterSpeed.takeIf { it.isNotEmpty() },
            iso.takeIf { it.isNotEmpty() }
        ).joinToString("  ")
    }

    /**
     * 格式化 GPS 坐标
     */
    fun formatGpsCoordinates(): String {
        return listOfNotNull(
            latitude.takeIf { it.isNotEmpty() },
            longitude.takeIf { it.isNotEmpty() }
        ).joinToString("  ")
    }

    /**
     * 是否有有效的相机信息
     */
    fun hasValidData(): Boolean {
        return cameraMake.isNotEmpty() || cameraModel.isNotEmpty() ||
                focalLength.isNotEmpty() || aperture.isNotEmpty() ||
                shutterSpeed.isNotEmpty() || iso.isNotEmpty()
    }

    /**
     * 获取相机信息（用于水印左上角）
     */
    fun getCameraInfo(): String = formatCameraInfo()

    /**
     * 获取拍摄参数信息（用于水印右上角）
     */
    fun getParamsInfo(): String = formatShootingParams()

    /**
     * 获取 GPS 信息（用于水印右下角）
     */
    fun getGpsInfo(): String = formatGpsCoordinates()

    companion object {
        val EMPTY = WatermarkMetadata()

        /**
         * 示例数据，用于预览
         */
        val SAMPLE = WatermarkMetadata(
            cameraMake = "LEICA",
            cameraModel = "MP",
            filmModel = "AGFA APX100",
            focalLength = "120mm",
            aperture = "f/4.1",
            shutterSpeed = "1/100",
            iso = "ISO90",
            dateTime = "2023.03.19 08:46:12",
            latitude = "40°3'13\"N",
            longitude = "116°19'25\"E"
        )
    }
}

