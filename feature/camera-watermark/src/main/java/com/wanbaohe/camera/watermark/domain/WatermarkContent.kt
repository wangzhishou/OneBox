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
 * 水印内容配置
 * 自定义内容优先，其次使用图片 EXIF；两者都没有时不绘制该字段。
 */
data class WatermarkContent(
    /** 左上角 - 相机+镜头信息 (如 "LEICA MP + AGFA APX100") */
    val topLeft: String? = null,

    /** 右上角 - 拍摄参数 (如 "120mm f/4.1 1/100 ISO90") */
    val topRight: String? = null,

    /** 左下角 - 时间或自定义文字 (如 "2023.03.19 08:46:12" 或 "Power by 万宝盒") */
    val bottomLeft: String? = null,

    /** 右下角 - GPS 坐标 (如 "40°3'13\"N 116°19'25\"E") */
    val bottomRight: String? = null,
) {
    companion object {
        /** 空内容，全部使用 EXIF 元数据 */
        val EMPTY = WatermarkContent()

        /** Power by 万宝盒 预设 */
        val POWER_BY = WatermarkContent(
            bottomLeft = "Power by 万宝盒"
        )

    }

    fun getTopLeftText(metadata: WatermarkMetadata): String =
        topLeft?.takeUnless { it.isBlank() } ?: metadata.getCameraInfo()

    fun getTopRightText(metadata: WatermarkMetadata): String =
        topRight?.takeUnless { it.isBlank() } ?: metadata.getParamsInfo()

    fun getBottomLeftText(metadata: WatermarkMetadata): String =
        bottomLeft?.takeUnless { it.isBlank() } ?: metadata.dateTime

    fun getBottomRightText(metadata: WatermarkMetadata): String =
        bottomRight?.takeUnless { it.isBlank() } ?: metadata.getGpsInfo()

    /**
     * 检查是否有自定义内容
     */
    fun hasCustomContent(): Boolean {
        return topLeft != null || topRight != null || bottomLeft != null || bottomRight != null
    }
}
