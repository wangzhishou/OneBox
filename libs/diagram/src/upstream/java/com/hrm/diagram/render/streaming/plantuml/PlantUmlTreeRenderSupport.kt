package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.color.ColorTokenParser
import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.text.TextMeasurer

internal data class PlantUmlTreeNodePalette(
    val defaultNodeFill: Color,
    val defaultNodeStroke: Color,
    val rootFill: Color,
    val rootStroke: Color,
    val textColor: Color,
    val edgeColor: Color,
)

internal data class PlantUmlTreeNodeChrome(
    val rootCornerRadius: Float,
    val childCornerRadius: Float,
    val rootStrokeWidth: Float,
    val childStrokeWidth: Float,
)

internal data class PlantUmlTreeLeadingVisualSpec(
    val kind: String,
    val name: String,
    val color: Color? = null,
)

internal object PlantUmlTreeRenderSupport {
    private const val THEME_SHADOW_ALPHA: Int = 56

    fun parseNodeStringMap(raw: String): Map<NodeId, String> =
        raw.split("||")
            .mapNotNull { entry ->
                val split = entry.lastIndexOf('|')
                if (split <= 0) return@mapNotNull null
                NodeId(entry.substring(0, split)) to entry.substring(split + 1)
            }
            .toMap()

    fun parseNodeColorMap(raw: String): Map<NodeId, Color> =
        parseNodeStringMap(raw).mapNotNull { (id, value) -> parsePlantUmlColor(value)?.let { id to it } }.toMap()

    fun parseNodeFloatMap(raw: String): Map<NodeId, Float> =
        parseNodeStringMap(raw).mapNotNull { (id, value) -> value.toFloatOrNull()?.let { id to it } }.toMap()

    fun parseNodeBooleanMap(raw: String): Map<NodeId, Boolean> =
        parseNodeStringMap(raw).mapNotNull { (id, value) -> parsePlantUmlBoolean(value)?.let { id to it } }.toMap()

    fun parsePlantUmlFloat(raw: String?): Float? =
        raw?.trim()?.toFloatOrNull()?.takeIf { it > 0f }

    fun parsePlantUmlBoolean(raw: String?): Boolean? =
        when (raw?.trim()?.lowercase()) {
            "true", "yes", "on", "1" -> true
            "false", "no", "off", "0" -> false
            else -> null
        }

    fun parsePlantUmlFontFamily(raw: String?): String? {
        val text = raw?.trim().orEmpty().trim('"')
        return text.takeIf { it.isNotEmpty() }
    }

    fun resolveFontSpec(base: FontSpec, familyRaw: String?, sizeRaw: String?): FontSpec =
        base.copy(
            family = parsePlantUmlFontFamily(familyRaw) ?: base.family,
            sizeSp = parsePlantUmlFloat(sizeRaw) ?: base.sizeSp,
        )

    fun resolveFontSpec(base: FontSpec, familyRaw: String?, sizeRaw: String?, styleRaw: String?): FontSpec {
        val lower = styleRaw?.lowercase().orEmpty()
        return resolveFontSpec(base, familyRaw, sizeRaw).copy(
            weight = if ("bold" in lower) 700 else base.weight,
            italic = "italic" in lower,
        )
    }

    fun parseNodeLeadingVisualMap(raw: String): Map<NodeId, PlantUmlTreeLeadingVisualSpec> =
        parseNodeStringMap(raw).mapNotNull { (id, value) ->
            decodeLeadingVisual(value)?.let { id to it }
        }.toMap()

    fun parsePlantUmlColor(raw: String): Color? = ColorTokenParser.parseColor(raw)

    fun isDark(color: Color): Boolean {
        val r = (color.argb shr 16) and 0xFF
        val g = (color.argb shr 8) and 0xFF
        val b = color.argb and 0xFF
        val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
        return luminance < 140f
    }

    fun darken(color: Color, factor: Float): Color {
        val f = 1f - factor.coerceIn(0f, 0.95f)
        val a = (color.argb ushr 24) and 0xFF
        val r = (((color.argb shr 16) and 0xFF) * f).toInt().coerceIn(0, 255)
        val g = (((color.argb shr 8) and 0xFF) * f).toInt().coerceIn(0, 255)
        val b = ((color.argb and 0xFF) * f).toInt().coerceIn(0, 255)
        return Color((a shl 24) or (r shl 16) or (g shl 8) or b)
    }

    fun themedShadowTint(border: Color): Color =
        Color.argb(THEME_SHADOW_ALPHA, border.r, border.g, border.b)

    fun offsetRect(rect: Rect, dx: Float, dy: Float): Rect =
        Rect.ltrb(rect.left + dx, rect.top + dy, rect.right + dx, rect.bottom + dy)

    fun offsetPoint(point: Point, dx: Float, dy: Float): Point = Point(point.x + dx, point.y + dy)

    fun formatStereotype(value: String): String = "\u00AB$value\u00BB"

    fun stripRenderedStereotype(label: String, stereotype: String): String {
        val prefix = "${formatStereotype(stereotype)}\n"
        return if (label.startsWith(prefix)) label.removePrefix(prefix) else label
    }

    fun stripLeadingVisualPrefix(label: String, leadingVisual: PlantUmlTreeLeadingVisualSpec): String {
        val fallback = leadingVisualFallbackLabel(leadingVisual)
        val prefix = "$fallback "
        return if (label.startsWith(prefix)) label.removePrefix(prefix) else label
    }

    fun appendCubicConnector(
        out: MutableList<DrawCommand>,
        parentRect: Rect,
        childRect: Rect,
        color: Color,
        strokeWidth: Float = 1.5f,
        z: Int = 0,
    ) {
        val childOnRight = childRect.left >= parentRect.left
        val from = if (childOnRight) {
            Point(parentRect.right, (parentRect.top + parentRect.bottom) / 2f)
        } else {
            Point(parentRect.left, (parentRect.top + parentRect.bottom) / 2f)
        }
        val to = if (childOnRight) {
            Point(childRect.left, (childRect.top + childRect.bottom) / 2f)
        } else {
            Point(childRect.right, (childRect.top + childRect.bottom) / 2f)
        }
        val midX = (from.x + to.x) / 2f
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(from),
                    PathOp.CubicTo(Point(midX, from.y), Point(midX, to.y), to),
                ),
            ),
            stroke = Stroke(width = strokeWidth),
            color = color,
            z = z,
        )
    }

    fun appendNodeChrome(
        out: MutableList<DrawCommand>,
        rect: Rect,
        isRoot: Boolean,
        boxless: Boolean,
        fill: Color,
        strokeColor: Color,
        chrome: PlantUmlTreeNodeChrome,
        shadowColor: Color,
        cornerRadiusOverride: Float? = null,
        strokeWidthOverride: Float? = null,
        shadow: Boolean = false,
        fillZ: Int = 1,
        strokeZ: Int = 2,
    ) {
        if (boxless) return
        val cornerRadius = cornerRadiusOverride ?: if (isRoot) chrome.rootCornerRadius else chrome.childCornerRadius
        val strokeWidth = strokeWidthOverride ?: if (isRoot) chrome.rootStrokeWidth else chrome.childStrokeWidth
        if (shadow) {
            out += DrawCommand.FillRect(
                rect = offsetRect(rect, 4f, 5f),
                color = shadowColor,
                corner = cornerRadius,
                z = fillZ - 1,
            )
        }
        out += DrawCommand.FillRect(
            rect = rect,
            color = fill,
            corner = cornerRadius,
            z = fillZ,
        )
        out += DrawCommand.StrokeRect(
            rect = rect,
            stroke = Stroke(width = strokeWidth),
            color = strokeColor,
            corner = cornerRadius,
            z = strokeZ,
        )
    }

    fun appendCenteredNodeText(
        out: MutableList<DrawCommand>,
        textMeasurer: TextMeasurer,
        rect: Rect,
        label: String,
        stereotype: String?,
        leadingVisual: PlantUmlTreeLeadingVisualSpec?,
        bodyFont: FontSpec,
        color: Color,
        stereotypeGap: Float,
        horizontalPadding: Float = 12f,
        z: Int = 3,
    ) {
        val stereotypeStripped = stereotype?.let { stripRenderedStereotype(label, it) } ?: label
        val bodyText = leadingVisual?.let { stripLeadingVisualPrefix(stereotypeStripped, it) } ?: stereotypeStripped
        if (stereotype == null) {
            appendBodyLine(
                out = out,
                textMeasurer = textMeasurer,
                rect = rect,
                text = bodyText,
                leadingVisual = leadingVisual,
                bodyFont = bodyFont,
                color = color,
                topY = (rect.top + rect.bottom) / 2f,
                horizontalPadding = horizontalPadding,
                centered = true,
                z = z,
            )
            return
        }

        val stereotypeText = formatStereotype(stereotype)
        val stereotypeFont = bodyFont.copy(sizeSp = (bodyFont.sizeSp - 1.5f).coerceAtLeast(9f), italic = true, weight = 500)
        val stereotypeMetrics = textMeasurer.measure(stereotypeText, stereotypeFont)
        val bodyMetrics = textMeasurer.measure(bodyText, bodyFont)
        val totalHeight = stereotypeMetrics.height + stereotypeGap + bodyMetrics.height
        val startY = ((rect.top + rect.bottom) - totalHeight) / 2f

        out += DrawCommand.DrawText(
            text = stereotypeText,
            origin = Point((rect.left + rect.right) / 2f, startY),
            font = stereotypeFont,
            color = color,
            maxWidth = rect.size.width - horizontalPadding,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Top,
            z = z,
        )
        appendBodyLine(
            out = out,
            textMeasurer = textMeasurer,
            rect = rect,
            text = bodyText,
            leadingVisual = leadingVisual,
            bodyFont = bodyFont,
            color = color,
            topY = startY + stereotypeMetrics.height + stereotypeGap,
            horizontalPadding = horizontalPadding,
            centered = false,
            z = z,
        )
    }

    private fun appendBodyLine(
        out: MutableList<DrawCommand>,
        textMeasurer: TextMeasurer,
        rect: Rect,
        text: String,
        leadingVisual: PlantUmlTreeLeadingVisualSpec?,
        bodyFont: FontSpec,
        color: Color,
        topY: Float,
        horizontalPadding: Float,
        centered: Boolean,
        z: Int,
    ) {
        if (leadingVisual == null) {
            out += DrawCommand.DrawText(
                text = text,
                origin = Point((rect.left + rect.right) / 2f, topY),
                font = bodyFont,
                color = color,
                maxWidth = rect.size.width - horizontalPadding,
                anchorX = TextAnchorX.Center,
                anchorY = if (centered) TextAnchorY.Middle else TextAnchorY.Top,
                z = z,
            )
            return
        }
        val iconSize = (bodyFont.sizeSp + 4f).coerceAtLeast(12f)
        val gap = 6f
        val iconFallback = leadingVisualFallbackLabel(leadingVisual)
        val iconFont = bodyFont.copy(sizeSp = (bodyFont.sizeSp - 1f).coerceAtLeast(10f), weight = 600)
        val iconTextMetrics = textMeasurer.measure(iconFallback, iconFont)
        val bodyMetrics = textMeasurer.measure(text, bodyFont)
        val iconWidth = iconSize
        val gapWidth = if (text.isNotEmpty()) gap else 0f
        val totalWidth = iconWidth + gapWidth + bodyMetrics.width
        val lineCenterY = if (centered) topY else topY + maxOf(iconSize, bodyMetrics.height) / 2f
        val startX = ((rect.left + rect.right) - totalWidth) / 2f
        val iconRect = Rect.ltrb(
            startX,
            lineCenterY - iconSize / 2f,
            startX + iconSize,
            lineCenterY + iconSize / 2f,
        )
        when (leadingVisual.kind) {
            "icon" -> {
                out += DrawCommand.DrawIcon(name = leadingVisual.name, rect = iconRect, z = z)
                out += DrawCommand.DrawText(
                    text = iconFallback,
                    origin = Point((iconRect.left + iconRect.right) / 2f, (iconRect.top + iconRect.bottom) / 2f),
                    font = iconFont,
                    color = color,
                    maxWidth = iconRect.size.width,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = z + 1,
                )
            }
            "emoji" -> {
                out += DrawCommand.DrawText(
                    text = iconFallback,
                    origin = Point((iconRect.left + iconRect.right) / 2f, (iconRect.top + iconRect.bottom) / 2f),
                    font = iconFont,
                    color = leadingVisual.color ?: color,
                    maxWidth = iconRect.size.width,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = z,
                )
            }
        }
        if (text.isNotEmpty()) {
            out += DrawCommand.DrawText(
                text = text,
                origin = Point(startX + iconWidth + gapWidth, if (centered) lineCenterY else topY),
                font = bodyFont,
                color = color,
                maxWidth = rect.right - (startX + iconWidth + gapWidth) - horizontalPadding / 2f,
                anchorX = TextAnchorX.Start,
                anchorY = if (centered) TextAnchorY.Middle else TextAnchorY.Top,
                z = z,
            )
        }
    }

    private fun decodeLeadingVisual(raw: String): PlantUmlTreeLeadingVisualSpec? {
        val firstComma = raw.indexOf(',')
        val secondComma = raw.indexOf(',', firstComma + 1)
        if (firstComma <= 0 || secondComma <= firstComma) return null
        val kind = when (raw.substring(0, firstComma)) {
            "icon" -> "icon"
            "emoji" -> "emoji"
            else -> return null
        }
        val color = raw.substring(firstComma + 1, secondComma).takeIf { it.isNotBlank() }?.let(::parsePlantUmlColor)
        val name = raw.substring(secondComma + 1).trim().takeIf { it.isNotEmpty() } ?: return null
        return PlantUmlTreeLeadingVisualSpec(kind = kind, name = name, color = color)
    }

    private fun leadingVisualFallbackLabel(leadingVisual: PlantUmlTreeLeadingVisualSpec): String {
        val raw = leadingVisual.name.substringAfterLast('/').substringAfterLast(':').replace('-', ' ').replace('_', ' ').trim()
        val parts = raw.split(' ').filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> if (leadingVisual.kind == "icon") "I" else "E"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }
}
