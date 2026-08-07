package com.halilibo.richtext.markwon

import androidx.compose.ui.text.TextStyle

data class JLatexMathStyle(
    val textStyle: TextStyle = TextStyle.Default,
) {
    companion object {
        val Default = JLatexMathStyle()
    }
}

fun JLatexMathStyle.resolveDefaults() = JLatexMathStyle(
    textStyle = textStyle,
)