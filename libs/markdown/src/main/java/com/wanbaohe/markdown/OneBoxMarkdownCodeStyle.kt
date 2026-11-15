package com.wanbaohe.markdown

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.hrm.codehigh.renderer.CodeColors
import com.hrm.codehigh.renderer.CodeDefaults

@Immutable
data class OneBoxMarkdownCodeStyle(
    val surface: Color,
    val content: Color,
    val secondaryContent: Color,
)

val LocalOneBoxMarkdownCodeStyle = staticCompositionLocalOf {
    oneBoxMarkdownCodeStyle(isDark = false)
}

fun oneBoxMarkdownCodeStyle(isDark: Boolean): OneBoxMarkdownCodeStyle = if (isDark) {
    OneBoxMarkdownCodeStyle(
        surface = Color(0xFF282C34),
        content = Color(0xFFE6EDF3),
        secondaryContent = Color(0xFFABB2BF),
    )
} else {
    OneBoxMarkdownCodeStyle(
        surface = Color(0xFFF6F8FA),
        content = Color(0xFF24292F),
        secondaryContent = Color(0xFF57606A),
    )
}

internal fun OneBoxMarkdownCodeStyle.toEngineColors(): CodeColors {
    val preset = if (surface.luminance() < 0.5f) {
        CodeDefaults.oneDarkProColors()
    } else {
        CodeDefaults.githubLightColors()
    }
    return preset.copy(
        background = Color.Transparent,
        text = content,
        title = content,
        language = content,
        action = content,
        lineNumber = secondaryContent,
        lineNumberDivider = secondaryContent,
        comment = secondaryContent,
        inlineText = content,
        inlineBackground = Color.Transparent,
        inlineBorder = Color.Transparent,
    )
}
