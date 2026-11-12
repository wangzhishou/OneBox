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

package com.shifenmiao.model.colors

import androidx.annotation.ColorInt

/**
 * 编辑器颜色配置
 */
data class EditorColors(
    val primary: String,
    val onPrimary: String,
    val primaryContainer: String,
    val onPrimaryContainer: String,
    val secondary: String,
    val onSecondary: String,
    val secondaryContainer: String,
    val onSecondaryContainer: String,
    val surface: String,
    val onSurface: String,
    val surfaceVariant: String,
    val onSurfaceVariant: String,
    val outline: String,
    val outlineVariant: String,
    val background: String,
    val onBackground: String,
    val error: String,
    val onError: String
) {
    companion object {
        fun fromArgb(
            @ColorInt primary: Int,
            @ColorInt onPrimary: Int,
            @ColorInt primaryContainer: Int,
            @ColorInt onPrimaryContainer: Int,
            @ColorInt secondary: Int,
            @ColorInt onSecondary: Int,
            @ColorInt secondaryContainer: Int,
            @ColorInt onSecondaryContainer: Int,
            @ColorInt surface: Int,
            @ColorInt onSurface: Int,
            @ColorInt surfaceVariant: Int,
            @ColorInt onSurfaceVariant: Int,
            @ColorInt outline: Int,
            @ColorInt outlineVariant: Int,
            @ColorInt background: Int,
            @ColorInt onBackground: Int,
            @ColorInt error: Int,
            @ColorInt onError: Int
        ): EditorColors {
            fun toHex(color: Int) = String.format("#%06X", 0xFFFFFF and color)
            return EditorColors(
                primary = toHex(primary),
                onPrimary = toHex(onPrimary),
                primaryContainer = toHex(primaryContainer),
                onPrimaryContainer = toHex(onPrimaryContainer),
                secondary = toHex(secondary),
                onSecondary = toHex(onSecondary),
                secondaryContainer = toHex(secondaryContainer),
                onSecondaryContainer = toHex(onSecondaryContainer),
                surface = toHex(surface),
                onSurface = toHex(onSurface),
                surfaceVariant = toHex(surfaceVariant),
                onSurfaceVariant = toHex(onSurfaceVariant),
                outline = toHex(outline),
                outlineVariant = toHex(outlineVariant),
                background = toHex(background),
                onBackground = toHex(onBackground),
                error = toHex(error),
                onError = toHex(onError)
            )
        }
    }
}