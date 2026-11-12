package com.wanbaohe.diagram

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.ir.ArgbColor
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.render.theme.material3

internal fun oneBoxDiagramTheme(colorScheme: ColorScheme): DiagramTheme {
    val base = DiagramTheme.material3(colorScheme)
    val accent = Color(colorScheme.onPrimaryContainer.toArgb())
    val nodeFill = Color(colorScheme.primaryContainer.toArgb())
    val labelBackground = Color(colorScheme.surfaceContainer.toArgb())
    return base.copy(
        background = Color.Transparent,
        nodeDefaults = base.nodeDefaults.copy(
            fill = ArgbColor(nodeFill.argb),
            stroke = ArgbColor(accent.argb),
            textColor = ArgbColor(accent.argb),
        ),
        edgeDefaults = base.edgeDefaults.copy(color = ArgbColor(accent.argb)),
        arrowDefaults = base.arrowDefaults.copy(color = accent),
        graphColors = base.graphColors.copy(
            background = Color.Transparent,
            nodeFill = nodeFill,
            nodeStroke = accent,
            nodeText = accent,
            edge = accent,
            edgeLabelText = accent,
            edgeLabelBackground = labelBackground,
        ),
    )
}
