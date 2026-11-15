package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.WireBox
import com.hrm.diagram.core.ir.WireframeIR
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.wireframe.WireframeLayout
import com.hrm.diagram.parser.plantuml.PlantUmlSaltParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver

internal class PlantUmlSaltSubPipeline(
    textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser = PlantUmlSaltParser()
    private val colors = ThemeResolver.resolvePlantUmlSalt(theme)
    private val layout = WireframeLayout(textMeasurer)
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val textFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val buttonFont = FontSpec(family = "sans-serif", sizeSp = 13f, weight = 600)

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun render(ir: WireframeIR, laid: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laid)
        val rootRect = laid.nodePositions[NodeId("wire:root")]
        if (rootRect != null) {
            out += DrawCommand.FillRect(rootRect, colors.rootFill, corner = 8f, z = 0)
            out += DrawCommand.StrokeRect(rootRect, Stroke(width = 1.2f), colors.rootStroke, corner = 8f, z = 1)
            out += DrawCommand.DrawText(
                text = "Salt",
                origin = Point(rootRect.left + 12f, rootRect.top + 10f),
                font = titleFont,
                color = colors.titleText,
                anchorY = TextAnchorY.Top,
                z = 2,
            )
        }

        fun draw(box: WireBox, path: String) {
            val id = NodeId("wire:$path")
            if (path != "root") {
                val rect = laid.nodePositions[id] ?: return
                when (box) {
                    is WireBox.Plain if (isTable(box)) -> {
                        out += DrawCommand.FillRect(rect, colors.panelFill, corner = 4f, z = 2)
                        out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.rootStroke, corner = 4f, z = 3)
                        box.children.filterIsInstance<WireBox.Plain>().forEachIndexed { rowIndex, row ->
                            row.children.forEachIndexed { columnIndex, cell ->
                                val cellRect = laid.nodePositions[NodeId("wire:$path.$rowIndex.$columnIndex")] ?: return@forEachIndexed
                                if (rowIndex == 0) {
                                    out += DrawCommand.FillRect(cellRect, colors.rootFill, z = 3)
                                }
                                out += DrawCommand.StrokeRect(cellRect, Stroke(width = 1f), colors.mutedStroke, z = 4)
                                out += DrawCommand.DrawText(
                                    text = labelOf(cell),
                                    origin = Point(cellRect.left + 8f, cellRect.top + cellRect.size.height / 2f),
                                    font = if (rowIndex == 0) buttonFont else textFont,
                                    color = colors.text,
                                    maxWidth = cellRect.size.width - 16f,
                                    anchorY = TextAnchorY.Middle,
                                    z = 5,
                                )
                            }
                        }
                    }
                    is WireBox.Plain if (box.children.isNotEmpty()) -> drawContainer(box, path, rect, laid, out)
                    is WireBox.Plain if (labelOf(box).startsWith("Separator:")) -> {
                        val dash = when (labelOf(box).substringAfter(':')) {
                            ".." -> listOf(2f, 4f)
                            "--" -> listOf(7f, 5f)
                            else -> null
                        }
                        out += DrawCommand.StrokePath(
                            PathCmd(listOf(PathOp.MoveTo(Point(rect.left + 8f, (rect.top + rect.bottom) / 2f)), PathOp.LineTo(Point(rect.right - 8f, (rect.top + rect.bottom) / 2f)))),
                            Stroke(width = 1.2f, dash = dash),
                            colors.rootStroke,
                            z = 5,
                        )
                    }
                    is WireBox.Button -> {
                        out += DrawCommand.FillRect(rect, colors.buttonFill, corner = 6f, z = 3)
                        out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.buttonStroke, corner = 6f, z = 4)
                        out += DrawCommand.DrawText(
                            text = labelOf(box),
                            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                            font = buttonFont,
                            color = colors.text,
                            maxWidth = rect.size.width - 16f,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Middle,
                            z = 5,
                        )
                    }
                    is WireBox.Input -> {
                        out += DrawCommand.FillRect(rect, colors.panelFill, corner = 5f, z = 3)
                        out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.mutedStroke, corner = 5f, z = 4)
                        out += DrawCommand.DrawText(
                            text = labelOf(box),
                            origin = Point(rect.left + 10f, rect.top + rect.size.height / 2f),
                            font = textFont,
                            color = colors.inputText,
                            maxWidth = rect.size.width - 20f,
                            anchorY = TextAnchorY.Middle,
                            z = 5,
                        )
                    }
                    is WireBox.Image -> {
                        out += DrawCommand.FillRect(rect, colors.rootFill, corner = 6f, z = 3)
                        out += DrawCommand.StrokeRect(rect, Stroke(width = 1f, dash = listOf(5f, 4f)), colors.rootStroke, corner = 6f, z = 4)
                        out += DrawCommand.DrawText(
                            text = labelOf(box),
                            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                            font = textFont,
                            color = colors.mutedText,
                            maxWidth = rect.size.width - 20f,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Middle,
                            z = 5,
                        )
                    }
                    is WireBox.TabbedGroup -> {
                        out += DrawCommand.FillRect(rect, colors.panelFill, corner = 6f, z = 3)
                        out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.mutedStroke, corner = 6f, z = 4)
                        val tabs = box.tabs
                        val tabWidth = rect.size.width / tabs.size.coerceAtLeast(1)
                        tabs.forEachIndexed { index, tab ->
                            val left = rect.left + tabWidth * index
                            val tabRect = rect.copy(origin = Point(left, rect.top), size = rect.size.copy(width = tabWidth))
                            if (index == 0) {
                                out += DrawCommand.FillRect(tabRect, colors.accentFill, corner = 6f, z = 4)
                            }
                            if (index > 0) {
                                out += DrawCommand.StrokePath(
                                    path = PathCmd(
                                        listOf(
                                            PathOp.MoveTo(Point(left, rect.top + 5f)),
                                            PathOp.LineTo(Point(left, rect.bottom - 5f)),
                                        ),
                                    ),
                                    stroke = Stroke(width = 1f),
                                    color = colors.mutedStroke,
                                    z = 5,
                                )
                            }
                            out += DrawCommand.DrawText(
                                text = labelOf(tab),
                                origin = Point(left + tabWidth / 2f, rect.top + rect.size.height / 2f),
                                font = buttonFont,
                                color = colors.text,
                                maxWidth = tabWidth - 12f,
                                anchorX = TextAnchorX.Center,
                                anchorY = TextAnchorY.Middle,
                                z = 6,
                            )
                        }
                    }
                    else -> {
                        out += DrawCommand.DrawText(
                            text = labelOf(box),
                            origin = Point(rect.left + 8f, rect.top + rect.size.height / 2f),
                            font = textFont,
                            color = colors.text,
                            maxWidth = rect.size.width - 16f,
                            anchorY = TextAnchorY.Middle,
                            z = 5,
                        )
                    }
                }
            }
            when (box) {
                is WireBox.Plain -> box.children.forEachIndexed { index, child -> draw(child, "$path.$index") }
                is WireBox.Button,
                is WireBox.Image,
                is WireBox.Input,
                is WireBox.TabbedGroup,
                -> Unit
            }
        }

        draw(ir.root, "root")
        return out.entities()
    }

    private fun labelOf(box: WireBox): String =
        when (val label = box.label) {
            is RichLabel.Plain -> label.text
            is RichLabel.Markdown -> label.source
            is RichLabel.Html -> label.html
        }

    private fun drawContainer(
        box: WireBox.Plain,
        path: String,
        rect: com.hrm.diagram.core.draw.Rect,
        laid: LaidOutDiagram,
        out: MutableList<DrawCommand>,
    ) {
        val label = labelOf(box)
        val style = containerStyle(label)
        if (style.drawFrame) {
            out += DrawCommand.FillRect(rect, style.fill, corner = 7f, z = 2)
            out += DrawCommand.StrokeRect(rect, Stroke(width = 1f, dash = style.dash), style.stroke, corner = 7f, z = 3)
        }
        out += DrawCommand.DrawText(
            text = style.title,
            origin = Point(rect.left + 12f, rect.top + 8f),
            font = buttonFont,
            color = colors.mutedText,
            maxWidth = rect.size.width - 24f,
            anchorY = TextAnchorY.Top,
            z = 5,
        )
        if (style.kind in setOf("Tree", "List", "Menu")) {
            box.children.forEachIndexed { index, _ ->
                val childRect = laid.nodePositions[NodeId("wire:$path.$index")] ?: return@forEachIndexed
                val start = Point(rect.left + 16f, rect.top + 28f)
                val mid = Point(rect.left + 16f, childRect.top + childRect.size.height / 2f)
                val end = Point(childRect.left - 6f, childRect.top + childRect.size.height / 2f)
                out += DrawCommand.StrokePath(
                    path = PathCmd(listOf(PathOp.MoveTo(start), PathOp.LineTo(mid), PathOp.LineTo(end))),
                    stroke = Stroke(width = 1f),
                    color = colors.mutedStroke,
                    z = 3,
                )
            }
        }
        if (style.kind == "Scroll") {
            val x = rect.right - 12f
            out += DrawCommand.StrokePath(
                PathCmd(listOf(PathOp.MoveTo(Point(x, rect.top + 28f)), PathOp.LineTo(Point(x, rect.bottom - 10f)))),
                Stroke(width = 2f),
                colors.mutedStroke,
                z = 5,
            )
        }
    }

    private data class ContainerStyle(
        val kind: String,
        val title: String,
        val fill: Color,
        val stroke: Color,
        val dash: List<Float>? = null,
        val drawFrame: Boolean = true,
    )

    private fun containerStyle(label: String): ContainerStyle {
        val kind = label.substringBefore(':').trim()
        val title = label.substringAfter(':', label).trim()
        return when (kind) {
            "Frame" -> ContainerStyle(kind, title, colors.panelFill, colors.rootStroke)
            "Grid" -> ContainerStyle(kind, title, colors.rootFill, colors.rootStroke)
            "Menu" -> ContainerStyle(kind, title, colors.accentFill, colors.buttonStroke)
            "List" -> ContainerStyle(kind, title, colors.panelFill, colors.buttonStroke)
            "Scroll" -> ContainerStyle(kind, title, colors.rootFill, colors.buttonStroke, dash = listOf(5f, 4f))
            "Group" -> ContainerStyle(kind, title, Color.Transparent, colors.mutedStroke, drawFrame = false)
            else -> ContainerStyle("Tree", label, Color.Transparent, colors.mutedStroke, drawFrame = false)
        }
    }

    private fun isTable(box: WireBox): Boolean =
        box is WireBox.Plain && labelOf(box) == "Table" && box.children.any { isTableRow(it) }

    private fun isTableRow(box: WireBox): Boolean =
        box is WireBox.Plain && labelOf(box) == "Row"
}
