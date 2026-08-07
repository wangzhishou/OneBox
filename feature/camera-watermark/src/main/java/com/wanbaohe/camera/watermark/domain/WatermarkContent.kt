package com.wanbaohe.camera.watermark.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 水印内容配置
 * 定义水印 5 个位置的自定义内容，为 null 时使用图片 EXIF 元数据
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

        // 占位符常量（Fake 数据，更有趣味性）
        private const val PLACEHOLDER_CAMERA = "Unknown Camera"
        private const val PLACEHOLDER_PARAMS = "Fake 50mm  f/1.8  1/125  ISO100"
        private const val PLACEHOLDER_GPS = "Fake 40°04'33\"N  116°19'12\"E"  // 北京回龙观

        /**
         * 获取当前日期时间作为占位符
         */
        private fun getCurrentDateTime(): String {
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
    }

    /**
     * 获取实际显示的左上角内容
     * @param metadata 图片 EXIF 元数据
     * @return 自定义内容 > 元数据 > 占位符
     */
    fun getTopLeftText(metadata: WatermarkMetadata): String {
        // 优先使用自定义内容
        if (!topLeft.isNullOrEmpty()) return topLeft
        // 其次使用 EXIF 元数据
        val cameraInfo = metadata.getCameraInfo()
        if (cameraInfo.isNotEmpty()) return cameraInfo
        // 最后使用占位符
        return PLACEHOLDER_CAMERA
    }

    /**
     * 获取实际显示的右上角内容
     * @param metadata 图片 EXIF 元数据
     * @return 自定义内容 > 元数据 > 占位符
     */
    fun getTopRightText(metadata: WatermarkMetadata): String {
        // 优先使用自定义内容
        if (!topRight.isNullOrEmpty()) return topRight
        // 其次使用 EXIF 元数据
        val paramsInfo = metadata.getParamsInfo()
        if (paramsInfo.isNotEmpty()) return paramsInfo
        // 最后使用占位符
        return PLACEHOLDER_PARAMS
    }

    /**
     * 获取实际显示的左下角内容
     * @param metadata 图片 EXIF 元数据
     * @return 自定义内容 > 元数据 > 当前日期时间
     */
    fun getBottomLeftText(metadata: WatermarkMetadata): String {
        // 优先使用自定义内容
        if (!bottomLeft.isNullOrEmpty()) return bottomLeft
        // 其次使用 EXIF 时间
        if (metadata.dateTime.isNotEmpty()) return metadata.dateTime
        // 最后使用当前时间
        return getCurrentDateTime()
    }

    /**
     * 获取实际显示的右下角内容
     * @param metadata 图片 EXIF 元数据
     * @return 自定义内容 > 元数据 > 空（GPS 没有时不显示占位符）
     */
    fun getBottomRightText(metadata: WatermarkMetadata): String {
        // 优先使用自定义内容
        if (!bottomRight.isNullOrEmpty()) return bottomRight
        // 其次使用 EXIF GPS
        val gpsInfo = metadata.getGpsInfo()
        if (gpsInfo.isNotEmpty()) return gpsInfo
        // GPS 没有时不显示（返回空）
        return PLACEHOLDER_GPS
    }

    /**
     * 检查是否有自定义内容
     */
    fun hasCustomContent(): Boolean {
        return topLeft != null || topRight != null || bottomLeft != null || bottomRight != null
    }
}

