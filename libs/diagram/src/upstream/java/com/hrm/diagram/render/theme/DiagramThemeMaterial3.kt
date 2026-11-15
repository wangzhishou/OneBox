package com.hrm.diagram.render.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.ir.ArgbColor
import com.hrm.diagram.core.ir.ClusterStyle
import com.hrm.diagram.core.ir.EdgeStyle
import com.hrm.diagram.core.ir.NodeStyle
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.core.theme.GraphColors
import com.hrm.diagram.core.theme.PieColors
import com.hrm.diagram.core.theme.SequenceColors
import com.hrm.diagram.core.theme.ThemeColors
import com.hrm.diagram.core.theme.TimeSeriesColors
import com.hrm.diagram.core.theme.TreeColors

fun DiagramTheme.Companion.material3(
    colorScheme: ColorScheme,
    base: DiagramTheme = DiagramTheme.Default,
): DiagramTheme {
    val colors = ThemeColors(
        canvas = colorScheme.surface.toDiagramColor(),
        surface = colorScheme.surface.toDiagramColor(),
        surfaceAlt = colorScheme.surfaceContainerHighest.toDiagramColor(),
        textPrimary = colorScheme.onSurface.toDiagramColor(),
        textSecondary = colorScheme.onSurfaceVariant.toDiagramColor(),
        border = colorScheme.outline.toDiagramColor(),
        accent = colorScheme.primary.toDiagramColor(),
        accentSecondary = colorScheme.secondary.toDiagramColor(),
        accentTertiary = colorScheme.tertiary.toDiagramColor(),
        danger = colorScheme.error.toDiagramColor(),
        success = blend(colorScheme.primary, colorScheme.tertiary, 0.35f).toDiagramColor(),
        warning = blend(colorScheme.tertiary, colorScheme.error, 0.2f).toDiagramColor(),
        selection = colorScheme.secondaryContainer.toDiagramColor(),
        diagnostic = colorScheme.errorContainer.toDiagramColor(),
    )

    return base.copy(
        colors = colors,
        nodeDefaults = NodeStyle(
            fill = ArgbColor(colors.surface.argb),
            stroke = ArgbColor(colors.border.argb),
            strokeWidth = base.nodeDefaults.strokeWidth,
            textColor = ArgbColor(colors.textPrimary.argb),
        ),
        edgeDefaults = EdgeStyle(
            color = ArgbColor(colors.textPrimary.argb),
            width = base.edgeDefaults.width,
            dash = base.edgeDefaults.dash,
            labelBg = base.edgeDefaults.labelBg,
        ),
        clusterDefaults = ClusterStyle(
            fill = base.clusterDefaults.fill ?: ArgbColor(colors.surfaceAlt.argb),
            stroke = ArgbColor(colors.border.argb),
            strokeWidth = base.clusterDefaults.strokeWidth,
        ),
        arrowDefaults = base.arrowDefaults.copy(
            color = colors.textPrimary,
            stroke = Stroke(width = base.arrowDefaults.stroke.width),
        ),
        background = colors.canvas,
        graphColors = GraphColors(
            background = colors.canvas,
            nodeFill = colors.surface,
            nodeStroke = colors.border,
            nodeText = colors.textPrimary,
            edge = colors.textPrimary,
            edgeLabelText = colors.textPrimary,
            edgeLabelBackground = colors.surfaceAlt.withAlpha(0.92f),
            clusterFill = colors.surfaceAlt,
            clusterStroke = colors.border,
            noteFill = colors.warning.withAlpha(0.14f),
            noteStroke = colors.warning,
            noteText = colors.textPrimary,
        ),
        pieColors = PieColors(
            titleText = colors.textPrimary,
            legendText = colors.textPrimary,
            border = colors.border,
            slices = listOf(
                colors.accent,
                colors.accentSecondary,
                colors.accentTertiary,
                colors.success,
                colors.warning,
                colors.danger,
            ),
        ),
        treeColors = TreeColors(
            nodeFill = colors.surface,
            nodeStroke = colors.border,
            nodeText = colors.textPrimary,
            rootFill = colors.accent.withAlpha(0.14f),
            rootStroke = colors.accent,
            rootText = colors.textPrimary,
            edge = colors.border,
        ),
        sequenceColors = SequenceColors(
            headerFill = colors.accent.withAlpha(0.14f),
            headerStroke = colors.accent,
            headerText = colors.textPrimary,
            lifeline = colors.textSecondary,
            message = colors.textPrimary,
            messageText = colors.textPrimary,
            noteFill = colors.warning.withAlpha(0.14f),
            noteStroke = colors.warning,
            noteText = colors.textPrimary,
            activationFill = colors.surface,
            activationStroke = colors.accent,
            fragmentStroke = colors.accentSecondary,
        ),
        timeSeriesColors = TimeSeriesColors(
            titleText = colors.textPrimary,
            labelText = colors.textPrimary,
            axis = colors.border,
            border = colors.border,
            alternateRowBackground = colors.surfaceAlt.withAlpha(0.55f),
            normalFill = colors.textSecondary.withAlpha(0.32f),
            activeFill = colors.accent.withAlpha(0.78f),
            doneFill = colors.success.withAlpha(0.78f),
            criticalFill = colors.danger.withAlpha(0.8f),
            milestoneFill = colors.warning.withAlpha(0.8f),
            slotFill = colors.accent.withAlpha(0.06f),
            slotStroke = colors.border,
            itemFill = colors.accent.withAlpha(0.16f),
            itemStroke = colors.accent,
        ),
    )
}

@Composable
fun DiagramTheme.Companion.material3(
    base: DiagramTheme = DiagramTheme.Default,
): DiagramTheme = material3(colorScheme = MaterialTheme.colorScheme, base = base)

private fun androidx.compose.ui.graphics.Color.toDiagramColor(): Color =
    Color(toArgb())

private fun blend(
    start: androidx.compose.ui.graphics.Color,
    end: androidx.compose.ui.graphics.Color,
    ratio: Float,
): androidx.compose.ui.graphics.Color {
    val t = ratio.coerceIn(0f, 1f)
    return androidx.compose.ui.graphics.Color(
        red = start.red + (end.red - start.red) * t,
        green = start.green + (end.green - start.green) * t,
        blue = start.blue + (end.blue - start.blue) * t,
        alpha = start.alpha + (end.alpha - start.alpha) * t,
    )
}

private fun Color.withAlpha(alpha: Float): Color {
    val clamped = alpha.coerceIn(0f, 1f)
    val a = (clamped * 255f).toInt().coerceIn(0, 255)
    return Color((argb and 0x00FFFFFF) or (a shl 24))
}
