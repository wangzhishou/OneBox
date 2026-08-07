package com.t8rin.imagetoolbox.core.ui.widget.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

object EditorUiDefaults {

    fun resolveScreenTitle(
        editTitle: String?,
        fallbackTitle: String,
        secondaryTitle: String? = null
    ): String = editTitle ?: secondaryTitle ?: fallbackTitle

    @Composable
    fun titleTextStyle(): TextStyle = MaterialTheme.typography.titleLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )

    @Composable
    fun titlePlaceholderTextStyle(): TextStyle = titleTextStyle().copy(
        color = MaterialTheme.colorScheme.outline
    )

    @Composable
    fun contentTextStyle(monospaced: Boolean = false): TextStyle {
        return MaterialTheme.typography.bodyMedium.copy(
            fontFamily = if (monospaced) FontFamily.Monospace else null,
            fontSize = if (monospaced) 14.sp else 15.sp,
            lineHeight = if (monospaced) 20.sp else 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EditorTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val textStyle = EditorUiDefaults.titleTextStyle()
    val density = LocalDensity.current
    val minHeight = with(density) {
        textStyle.lineHeight.toDp()
    }

    BasicTextField(
        value = value,
        onValueChange = {
            onValueChange(it.replace("\n", " "))
        },
        modifier = modifier.defaultMinSize(minHeight = minHeight),
        textStyle = textStyle,
        singleLine = true,
        maxLines = 1,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.defaultMinSize(minHeight = minHeight),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = EditorUiDefaults.titlePlaceholderTextStyle()
                    )
                }
                innerTextField()
            }
        }
    )
}
