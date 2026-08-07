package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class TextRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Text"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val text = context.resolveString(component.properties["text"]) ?: ""
        val style = context.resolveString(component.properties["style"])
        val maxLines = context.resolveInt(component.properties["maxLines"])
        val fontWeight = context.resolveString(component.properties["weight"])
            ?: context.resolveString(component.properties["fontWeight"])
        val fontSize = context.resolveInt(component.properties["fontSize"])
        val textAlign = context.resolveString(component.properties["align"])
            ?: context.resolveString(component.properties["textAlign"])

        Text(
            text = text,
            style = mapTextStyle(style).let { baseStyle ->
                if (fontSize != null) baseStyle.copy(fontSize = fontSize.sp) else baseStyle
            },
            fontWeight = mapFontWeight(fontWeight),
            fontStyle = if (context.resolveBoolean(component.properties["italic"]) == true)
                FontStyle.Italic else FontStyle.Normal,
            color = context.resolveString(component.properties["color"])
                ?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                ?: Color.Unspecified,
            maxLines = maxLines ?: Int.MAX_VALUE,
            overflow = if (maxLines != null) TextOverflow.Ellipsis else TextOverflow.Clip,
            textAlign = mapTextAlign(textAlign),
        )
    }

    @Composable
    private fun mapTextStyle(style: String?): TextStyle = when (style) {
        "displayLarge" -> MaterialTheme.typography.displayLarge
        "displayMedium" -> MaterialTheme.typography.displayMedium
        "displaySmall" -> MaterialTheme.typography.displaySmall
        "headlineLarge" -> MaterialTheme.typography.headlineLarge
        "headlineMedium" -> MaterialTheme.typography.headlineMedium
        "headlineSmall" -> MaterialTheme.typography.headlineSmall
        "titleLarge" -> MaterialTheme.typography.titleLarge
        "titleMedium" -> MaterialTheme.typography.titleMedium
        "titleSmall" -> MaterialTheme.typography.titleSmall
        "bodyLarge" -> MaterialTheme.typography.bodyLarge
        "bodyMedium" -> MaterialTheme.typography.bodyMedium
        "bodySmall" -> MaterialTheme.typography.bodySmall
        "labelLarge" -> MaterialTheme.typography.labelLarge
        "labelMedium" -> MaterialTheme.typography.labelMedium
        "labelSmall" -> MaterialTheme.typography.labelSmall
        else -> MaterialTheme.typography.bodyMedium
    }

    private fun mapFontWeight(weight: String?): FontWeight? = when (weight) {
        "bold" -> FontWeight.Bold
        "semibold" -> FontWeight.SemiBold
        "medium" -> FontWeight.Medium
        "light" -> FontWeight.Light
        "normal" -> FontWeight.Normal
        else -> null
    }

    private fun mapTextAlign(align: String?): TextAlign = when (align) {
        "center" -> TextAlign.Center
        "end", "right" -> TextAlign.End
        "start", "left" -> TextAlign.Start
        "justify" -> TextAlign.Justify
        else -> TextAlign.Start
    }
}
