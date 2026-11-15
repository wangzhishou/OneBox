package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.StructIR
import com.hrm.diagram.core.ir.StructNode
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.struct.StructLayout
import com.hrm.diagram.parser.plantuml.PlantUmlParsing
import com.hrm.diagram.parser.plantuml.PlantUmlParsingFactory
import com.hrm.diagram.parser.plantuml.PlantUmlStructFormat
import com.hrm.diagram.parser.plantuml.PlantUmlStructParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver

internal class PlantUmlStructSubPipeline(
    private val format: PlantUmlStructFormat,
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser: PlantUmlParsing<StructIR> = PlantUmlParsingFactory.struct(format)
    private val layout = StructLayout(textMeasurer)
    private val colors = ThemeResolver.resolvePlantUmlStruct(theme)
    private var cachedCollapsibleRaw: String = ""
    private var cachedCollapsiblePaths: Set<String> = emptySet()
    private var cachedScalarKindsRaw: String = ""
    private var cachedScalarKinds: Map<String, String> = emptyMap()
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val font = FontSpec(family = "monospace", sizeSp = 12f)
    private val rootFont = font.copy(weight = 600)

    override fun acceptLine(line: String) = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean) = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun render(ir: StructIR, laid: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laid)
        val collapsiblePaths = resolveCollapsiblePaths(ir)
        val scalarKinds = resolveScalarKinds(ir)
        for (route in laid.edgeRoutes) {
            val ops = route.points.mapIndexed { index, point ->
                if (index == 0) PathOp.MoveTo(point) else PathOp.LineTo(point)
            }
            out += DrawCommand.StrokePath(PathCmd(ops), Stroke(width = 1.2f), colors.edge, z = 0)
        }

        fun drawNode(node: StructNode, path: String, isRoot: Boolean) {
            val id = NodeId("struct_$path")
            val rect = laid.nodePositions[id] ?: return
            out += DrawCommand.FillRect(rect, if (isRoot) colors.rootFill else colors.fill, corner = 6f, z = 1)
            out += DrawCommand.StrokeRect(rect, Stroke(width = if (isRoot) 1.6f else 1f), if (isRoot) colors.rootStroke else colors.stroke, corner = 6f, z = 2)
            out += DrawCommand.DrawText(
                text = labelFor(node, path in collapsiblePaths),
                origin = Point(rect.left + 12f, rect.top + rect.size.height / 2f),
                font = if (isRoot) rootFont else font,
                color = scalarTextColor(scalarKinds[path]) ?: colors.text,
                maxWidth = rect.size.width - 24f,
                anchorY = TextAnchorY.Middle,
                z = 3,
            )
            when (node) {
                is StructNode.ArrayNode -> node.items.forEachIndexed { index, child -> drawNode(child, "$path.$index", false) }
                is StructNode.ObjectNode -> node.entries.forEachIndexed { index, child -> drawNode(child, "$path.$index", false) }
                is StructNode.Scalar -> Unit
            }
        }

        drawNode(ir.root, "root", true)
        return out.entities()
    }

    private fun labelFor(node: StructNode, collapsible: Boolean): String {
        val prefix = node.key?.let { "$it: " }.orEmpty()
        val marker = if (collapsible) "[-] " else ""
        return when (node) {
            is StructNode.ArrayNode -> "$marker$prefix[${node.items.size}]"
            is StructNode.ObjectNode -> "$marker$prefix{${node.entries.size}}"
            is StructNode.Scalar -> "$prefix${node.value}"
        }
    }

    private fun scalarTextColor(kind: String?): Color? = when (kind) {
        "number" -> colors.numberText
        "boolean" -> colors.booleanText
        "null" -> colors.nullText
        else -> null
    }

    private fun resolveCollapsiblePaths(ir: StructIR): Set<String> {
        val raw = ir.styleHints.extras[PlantUmlStructParser.COLLAPSIBLE_PATHS_KEY].orEmpty()
        if (cachedCollapsibleRaw != raw) {
            cachedCollapsibleRaw = raw
            cachedCollapsiblePaths = parsePathSet(raw)
        }
        return cachedCollapsiblePaths
    }

    private fun resolveScalarKinds(ir: StructIR): Map<String, String> {
        val raw = ir.styleHints.extras[PlantUmlStructParser.SCALAR_KINDS_KEY].orEmpty()
        if (cachedScalarKindsRaw != raw) {
            cachedScalarKindsRaw = raw
            cachedScalarKinds = parsePathMap(raw)
        }
        return cachedScalarKinds
    }

    private fun parsePathSet(raw: String): Set<String> =
        raw.split("||").filterTo(LinkedHashSet()) { it.isNotBlank() }

    private fun parsePathMap(raw: String): Map<String, String> =
        raw.split("||").mapNotNull { entry ->
            val split = entry.lastIndexOf('|')
            if (split <= 0) null else entry.substring(0, split) to entry.substring(split + 1)
        }.toMap()
}
