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
                                'surface-container': '${colorScheme.surfaceContainer.toArgb().toHex()}',
                                'surface-container-high': '${colorScheme.surfaceContainerHigh.toArgb().toHex()}',
                                'surface-container-highest': '${colorScheme.surfaceContainerHighest.toArgb().toHex()}',
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
