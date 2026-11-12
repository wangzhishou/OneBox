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

package com.wanbaohe.idphoto.domain

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.wanbaohe.idphoto.R

/**
 * 证件照背景色
 *
 * 名称只存字符串资源 id，「原图/透明」靠 [isOriginal]/[isTransparent] 判定，
 * 不再用中文 name 当业务标识。
 */
data class IdPhotoBackground(
    @StringRes val nameRes: Int,
    val color: Long,
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
            nameRes = R.string.id_photo_bg_original,
            color = 0x00000000,
        )

        /** 透明背景:触发 AI 抠图但不填色,颜色占位与「原图」相同,靠 isTransparent 区分 */
        val TRANSPARENT = IdPhotoBackground(
            nameRes = R.string.id_photo_bg_transparent,
            color = 0x00000000,
        )

        /**
         * 预置的常用背景色;前两位为「原图」「透明」,选择真实颜色/透明才会触发 AI 抠图合成
         */
        val PRESETS = listOf(
            ORIGINAL,
            TRANSPARENT,
            IdPhotoBackground(
                nameRes = R.string.id_photo_bg_white,
                color = 0xFFFFFFFF,
            ),
            IdPhotoBackground(
                nameRes = R.string.id_photo_bg_blue,
                color = 0xFF438EDB,
            ),
            IdPhotoBackground(
                nameRes = R.string.id_photo_bg_red,
                color = 0xFFD03D33,
            ),
            IdPhotoBackground(
                nameRes = R.string.id_photo_bg_gradient_blue,
                color = 0xFF5B9BD5,
            ),
        )

        val DEFAULT = ORIGINAL
    }
}
