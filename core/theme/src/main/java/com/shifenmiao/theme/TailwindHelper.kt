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

package com.shifenmiao.theme

import androidx.compose.ui.graphics.toArgb
import java.util.Locale

object TailwindHelper {
    fun getTailwindConfig(): String {
        val colorScheme = AppTheme.colorScheme
        fun Int.toHex() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)

        return """
            (function() {
                var config = {
                    darkMode: 'class',
                    theme: {
                        extend: {
                            colors: {
                                'primary': '${colorScheme.primary.toArgb().toHex()}',
                                'on-primary': '${colorScheme.onPrimary.toArgb().toHex()}',
                                'primary-container': '${colorScheme.primaryContainer.toArgb().toHex()}',
                                'on-primary-container': '${colorScheme.onPrimaryContainer.toArgb().toHex()}',
                                'secondary': '${colorScheme.secondary.toArgb().toHex()}',
                                'on-secondary': '${colorScheme.onSecondary.toArgb().toHex()}',
                                'tertiary': '${colorScheme.tertiary.toArgb().toHex()}',
                                'surface': '${colorScheme.surface.toArgb().toHex()}',
                                'on-surface': '${colorScheme.onSurface.toArgb().toHex()}',
                                'on-surface-variant': '${colorScheme.onSurfaceVariant.toArgb().toHex()}',
                                'surface-variant': '${colorScheme.surfaceVariant.toArgb().toHex()}',
                                'surface-container': '${colorScheme.surfaceContainerLow.toArgb().toHex()}',
                                'surface-container-high': '${colorScheme.surfaceContainer.toArgb().toHex()}',
                                'surface-container-highest': '${colorScheme.surfaceContainer.toArgb().toHex()}',
                                'outline': '${colorScheme.outline.toArgb().toHex()}',
                                'outline-variant': '${colorScheme.outlineVariant.toArgb().toHex()}',
                                'background': '${colorScheme.background.toArgb().toHex()}',
                            }
                        }
                    }
                };
                
                if (typeof tailwind !== 'undefined') {
                    tailwind.config = config;
                } else {
                    window.tailwind = { config: config };
                }
            })();
        """.trimIndent()
    }
}
