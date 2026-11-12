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

package com.wanbaohe.code.editor.webview

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.model.colors.EditorColors

/**
 * CodeEditor 预加载配置
 */
data class CodeEditorPreloadConfig(
    val isDarkTheme: Boolean,
    val colors: EditorColors,
    val storageKey: String = "default",
    val fontSizePx: Float = 14f,
    val lineHeightPx: Float = 20f,
    val letterSpacingPx: Float = 0f,
    val fontWeight: Int = 400
) {
    companion object {
        fun fromColorScheme(
            colorScheme: ColorScheme,
            isDarkTheme: Boolean,
            storageKey: String = "default",
            fontSizePx: Float = 14f,
            lineHeightPx: Float = 20f,
            letterSpacingPx: Float = 0f,
            fontWeight: Int = 400
        ) = CodeEditorPreloadConfig(
            isDarkTheme = isDarkTheme,
            colors = EditorColors(
                primary = colorToHex(colorScheme.primary.toArgb()),
                onPrimary = colorToHex(colorScheme.onPrimary.toArgb()),
                primaryContainer = colorToHex(colorScheme.primaryContainer.toArgb()),
                onPrimaryContainer = colorToHex(colorScheme.onPrimaryContainer.toArgb()),
                secondary = colorToHex(colorScheme.secondary.toArgb()),
                onSecondary = colorToHex(colorScheme.onSecondary.toArgb()),
                secondaryContainer = colorToHex(colorScheme.secondaryContainer.toArgb()),
                onSecondaryContainer = colorToHex(colorScheme.onSecondaryContainer.toArgb()),
                surface = colorToHex(colorScheme.surface.toArgb()),
                onSurface = colorToHex(colorScheme.onSurface.toArgb()),
                surfaceVariant = colorToHex(colorScheme.surfaceVariant.toArgb()),
                onSurfaceVariant = colorToHex(colorScheme.onSurfaceVariant.toArgb()),
                outline = colorToHex(colorScheme.outline.toArgb()),
                outlineVariant = colorToHex(colorScheme.outlineVariant.toArgb()),
                background = colorToHex(colorScheme.background.toArgb()),
                onBackground = colorToHex(colorScheme.onBackground.toArgb()),
                error = colorToHex(colorScheme.error.toArgb()),
                onError = colorToHex(colorScheme.onError.toArgb())
            ),
            storageKey = storageKey,
            fontSizePx = fontSizePx,
            lineHeightPx = lineHeightPx,
            letterSpacingPx = letterSpacingPx,
            fontWeight = fontWeight
        )

        private fun colorToHex(color: Int) = String.format("#%06X", 0xFFFFFF and color)
    }
}
