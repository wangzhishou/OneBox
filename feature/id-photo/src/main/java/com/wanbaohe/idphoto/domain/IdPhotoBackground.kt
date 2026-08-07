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

    companion object {
        /**
         * 预置的常用背景色
         */
        val PRESETS = listOf(
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

        val DEFAULT = PRESETS.first()
    }
}

