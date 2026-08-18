package com.wanbaohe.idphoto.domain

import androidx.compose.ui.graphics.Color

/**
 * 证件照背景色
 */
data class IdPhotoBackground(
    val name: String,
    val color: Long,
    val description: String = ""
) {
    fun getColor(): Color = Color(color)

    /** 「原图」项:不做 AI 抠图换底色,保留照片原背景 */
    val isOriginal: Boolean
        get() = this == ORIGINAL

    /** 「透明」项:AI 抠图后不填色,保留透明通道(导出自动切 PNG) */
    val isTransparent: Boolean
        get() = this == TRANSPARENT

    companion object {
        /** 保留原图背景(默认):不触发抠图,颜色占位为透明 */
        val ORIGINAL = IdPhotoBackground(
            name = "原图",
            color = 0x00000000,
            description = "保留照片原背景"
        )

        /** 透明背景:触发 AI 抠图但不填色,颜色占位与「原图」相同,靠 name 区分 */
        val TRANSPARENT = IdPhotoBackground(
            name = "透明",
            color = 0x00000000,
            description = "透明背景,便于二次合成"
        )

        /**
         * 预置的常用背景色;前两位为「原图」「透明」,选择真实颜色/透明才会触发 AI 抠图合成
         */
        val PRESETS = listOf(
            ORIGINAL,
            TRANSPARENT,
            IdPhotoBackground(
                name = "白色",
                color = 0xFFFFFFFF,
                description = "护照、身份证、驾驶证"
            ),
            IdPhotoBackground(
                name = "蓝色",
                color = 0xFF438EDB,
                description = "毕业证、工作证、简历"
            ),
            IdPhotoBackground(
                name = "红色",
                color = 0xFFD03D33,
                description = "结婚证、保险、医保"
            ),
            IdPhotoBackground(
                name = "渐变蓝",
                color = 0xFF5B9BD5,
                description = "证件照常用"
            ),
        )

        val DEFAULT = ORIGINAL
    }
}

