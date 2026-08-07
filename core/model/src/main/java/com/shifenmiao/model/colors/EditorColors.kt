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