package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.ArgbColor
import com.hrm.diagram.core.ir.Cluster
import com.hrm.diagram.core.ir.ClusterStyle
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.EdgeRoute
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.plantuml.PlantUmlUsecaseParser
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlUsecaseSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private data class ScopePalette(
        val fill: ArgbColor?,
        val stroke: ArgbColor?,
        val text: ArgbColor?,
        val fontSize: Float?,
        val fontName: String?,
        val lineThickness: Float?,
        val shadowing: Boolean?,
    )

    private data class UsecasePalette(
        val actor: ScopePalette,
        val usecase: ScopePalette,
        val note: ScopePalette,
        val rectangle: ScopePalette,
        val `package`: ScopePalette,
        val edgeColor: ArgbColor?,
    )

    private val parser = PlantUmlUsecaseParser()
    private val colors = ThemeResolver.resolvePlantUmlUsecase(theme)
    private val themeColors = theme.colors
    private val shadowTint = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private var cachedPaletteExtras: Map<String, String> = emptyMap()
    private var currentPalette: UsecasePalette = paletteOf(emptyMap())
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val clusterFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(180f, 92f),
        maxWidth = 180f,
        minWidth = 72f,
        minHeight = 54f,
        labelOf = ::labelTextOf,
        fontOf = { node -> scopedFont(scopeForNode(node, currentPalette), labelFont) },
        customSizeOf = { node -> measureNodeSize(node, currentPalette) },
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.PLANTUML,
        defaultNodeSize = Size(180f, 92f),
        measurePolicy = measurePolicy,
        postLayout = { ir, baseLaid ->
            val clusterRects = LinkedHashMap<NodeId, Rect>()
            for (cluster in ir.clusters) computeClusterRect(cluster, baseLaid.nodePositions, clusterRects, currentPalette)
            val bounds = computeBounds(baseLaid.nodePositions.values + clusterRects.values)
            applyAnchoredNotes(ir, baseLaid.copy(clusterRects = clusterRects, bounds = bounds))
        },
    )
    private val kernel = profile.kernel(
        textMeasurer = textMeasurer,
        renderEntities = { ir, laid -> render(ir, laid, currentPalette) },
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState {
        val rawIr = parser.snapshot()
        val palette = resolvePalette(rawIr)
        currentPalette = palette
        val ir = applyPalette(rawIr, palette)
        return kernel.advanceRendered(
            previousSnapshot = previousSnapshot,
            seq = seq,
            isFinal = isFinal,
            ir = ir,
            diagnostics = parser.diagnosticsSnapshot(),
        )
    }

    override fun dispose() {
        currentPalette = paletteOf(emptyMap())
        kernel.clear()
    }

    private fun measureNodeSize(node: Node, palette: UsecasePalette): Size {
        val label = labelTextOf(node)
        val font = scopedFont(scopeForNode(node, palette), labelFont)
        return when (node.payload[PlantUmlUsecaseParser.KIND_KEY]) {
            "actor" -> {
                val metrics = textMeasurer.measure(label, font, maxWidth = 140f)
                Size(
                    width = (metrics.width + 28f).coerceAtLeast(72f),
                    height = (metrics.height + 96f).coerceAtLeast(116f),
                )
            }
            "note" -> {
                val metrics = textMeasurer.measure(label, font, maxWidth = 180f)
                Size(
                    width = (metrics.width + 30f).coerceAtLeast(120f),
                    height = (metrics.height + 24f).coerceAtLeast(54f),
                )
            }
            else -> {
                val metrics = textMeasurer.measure(label, font, maxWidth = 180f)
                Size(
                    width = (metrics.width + 56f).coerceAtLeast(132f),
                    height = (metrics.height + 36f).coerceAtLeast(72f),
                )
            }
        }
    }

    private fun computeClusterRect(
        cluster: Cluster,
        nodePositions: Map<NodeId, Rect>,
        out: LinkedHashMap<NodeId, Rect>,
        palette: UsecasePalette,
    ): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        childRects += cluster.nestedClusters.mapNotNull { computeClusterRect(it, nodePositions, out, palette) }
        if (childRects.isEmpty()) return null
        val (kind, title) = parseClusterLabel(cluster)
        val titleMetrics = textMeasurer.measure(
            title.ifBlank { cluster.id.value },
            scopedFont(clusterScope(kind, palette), clusterFont),
            maxWidth = 220f,
        )
        val rect = Rect.ltrb(
            childRects.minOf { it.left } - 22f,
            childRects.minOf { it.top } - (titleMetrics.height + 24f),
            childRects.maxOf { it.right } + 22f,
            childRects.maxOf { it.bottom } + 18f,
        )
        out[cluster.id] = rect
        return rect
    }

    private fun computeBounds(rects: Collection<Rect>): Rect {
        if (rects.isEmpty()) return Rect.ltrb(0f, 0f, 400f, 240f)
        return Rect.ltrb(
            rects.minOf { it.left }.coerceAtMost(0f),
            rects.minOf { it.top }.coerceAtMost(0f),
            rects.maxOf { it.right } + 20f,
            rects.maxOf { it.bottom } + 20f,
        )
    }

    private fun render(ir: GraphIR, laidOut: LaidOutDiagram, palette: UsecasePalette): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        val bounds = laidOut.bounds
        out += DrawCommand.FillRect(
            rect = Rect(Point(bounds.left, bounds.top), Size(bounds.size.width, bounds.size.height)),
            color = colors.canvas,
            z = 0,
        )
        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, out, palette)
        for (node in ir.nodes) drawNode(node, laidOut, out, palette)
        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            drawEdge(edge, route, out, palette)
        }
        return out.entities()
    }

    private fun drawCluster(cluster: Cluster, clusterRects: Map<NodeId, Rect>, out: MutableList<DrawCommand>, palette: UsecasePalette) {
        val rect = clusterRects[cluster.id] ?: return
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        val (kind, title) = parseClusterLabel(cluster)
        val scope = clusterScope(kind, palette)
        val textColor = scope.text?.let { Color(it.argb) } ?: colors.clusterText
        if (scope.shadowing == true) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowTint,
                corner = 12f,
                z = 0,
            )
        }
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 0)
        out += DrawCommand.StrokeRect(
            rect = rect,
            stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f),
            color = strokeColor,
            corner = 12f,
            z = 1,
        )
        out += DrawCommand.DrawText(
            text = "${kind.uppercase()}  ${title.ifBlank { cluster.id.value }}",
            origin = Point(rect.left + 12f, rect.top + 8f),
            font = scopedFont(scope, clusterFont),
            color = textColor,
            maxWidth = rect.size.width - 24f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 2,
        )
        for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, out, palette)
    }

    private fun drawNode(node: Node, laidOut: LaidOutDiagram, out: MutableList<DrawCommand>, palette: UsecasePalette) {
        val rect = laidOut.nodePositions[node.id] ?: return
        when (node.payload[PlantUmlUsecaseParser.KIND_KEY]) {
            "actor" -> drawActor(node, rect, out, palette.actor)
            "note" -> drawNote(node, rect, out, palette.note)
            else -> drawUsecase(node, rect, out, palette.usecase)
        }
    }

    private fun drawActor(node: Node, rect: Rect, out: MutableList<DrawCommand>, scope: ScopePalette) {
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.actorFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.actorStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.actorText
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f)
        val cx = (rect.left + rect.right) / 2f
        val top = rect.top + 8f
        val headRadius = 11f
        val headRect = Rect.ltrb(cx - headRadius, top, cx + headRadius, top + headRadius * 2f)
        if (scope.shadowing == true) {
            val shadowColor = shadowTint
            val shadowRect = PlantUmlTreeRenderSupport.offsetRect(headRect, 4f, 4f)
            out += DrawCommand.StrokeRect(rect = shadowRect, stroke = stroke, color = shadowColor, corner = headRadius, z = 2)
            out += DrawCommand.StrokePath(
                path = PathCmd(
                    listOf(
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, headRect.bottom), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, headRect.bottom + 30f), 4f, 4f)),
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx - 16f, headRect.bottom + 12f), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx + 16f, headRect.bottom + 12f), 4f, 4f)),
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, headRect.bottom + 30f), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx - 14f, rect.bottom - 28f), 4f, 4f)),
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, headRect.bottom + 30f), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx + 14f, rect.bottom - 28f), 4f, 4f)),
                    ),
                ),
                stroke = stroke,
                color = shadowTint,
                z = 2,
            )
        }
        if (fill.a > 0) out += DrawCommand.FillRect(rect = headRect, color = fill, corner = headRadius, z = 2)
        out += DrawCommand.StrokeRect(rect = headRect, stroke = stroke, color = strokeColor, corner = headRadius, z = 3)
        val bodyTop = headRect.bottom
        val bodyBottom = rect.bottom - 28f
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(cx, bodyTop)),
                    PathOp.LineTo(Point(cx, bodyTop + 30f)),
                    PathOp.MoveTo(Point(cx - 16f, bodyTop + 12f)),
                    PathOp.LineTo(Point(cx + 16f, bodyTop + 12f)),
                    PathOp.MoveTo(Point(cx, bodyTop + 30f)),
                    PathOp.LineTo(Point(cx - 14f, bodyBottom)),
                    PathOp.MoveTo(Point(cx, bodyTop + 30f)),
                    PathOp.LineTo(Point(cx + 14f, bodyBottom)),
                ),
            ),
            stroke = stroke,
            color = strokeColor,
            z = 3,
        )
        if (node.payload[PlantUmlUsecaseParser.ACTOR_VARIANT_KEY] == "business") {
            out += DrawCommand.StrokePath(
                path = PathCmd(
                    listOf(
                        PathOp.MoveTo(Point(cx - 12f, bodyTop + 2f)),
                        PathOp.LineTo(Point(cx + 12f, bodyTop + 28f)),
                    ),
                ),
                stroke = Stroke(width = 1.25f),
                color = strokeColor,
                z = 4,
            )
        }
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point(cx, rect.bottom - 12f),
            font = scopedFont(scope, labelFont),
            color = textColor,
            maxWidth = rect.size.width - 12f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Bottom,
            z = 4,
        )
    }

    private fun drawUsecase(node: Node, rect: Rect, out: MutableList<DrawCommand>, scope: ScopePalette) {
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.usecaseFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.usecaseStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.usecaseText
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f)
        val path = ellipsePath(rect)
        if (scope.shadowing == true) {
            out += DrawCommand.FillPath(
                path = ellipsePath(PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f)),
                color = shadowTint,
                z = 2,
            )
        }
        out += DrawCommand.FillPath(path = path, color = fill, z = 3)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 4)
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
            font = scopedFont(scope, labelFont),
            color = textColor,
            maxWidth = rect.size.width - 20f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 5,
        )
    }

    private fun drawNote(node: Node, rect: Rect, out: MutableList<DrawCommand>, scope: ScopePalette) {
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.noteFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.noteStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.noteText
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.25f)
        if (scope.shadowing == true) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowTint,
                corner = 8f,
                z = 2,
            )
        }
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 8f, z = 3)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 8f, z = 4)
        val fold = 14f
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(rect.right - fold, rect.top)),
                    PathOp.LineTo(Point(rect.right - fold, rect.top + fold)),
                    PathOp.LineTo(Point(rect.right, rect.top + fold)),
                ),
            ),
            stroke = Stroke(width = 1.2f),
            color = strokeColor,
            z = 5,
        )
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point(rect.left + 12f, rect.top + 10f),
            font = scopedFont(scope, labelFont),
            color = textColor,
            maxWidth = rect.size.width - 24f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 6,
        )
    }

    private fun ellipsePath(rect: Rect): PathCmd {
        val cx = (rect.left + rect.right) / 2f
        val cy = (rect.top + rect.bottom) / 2f
        val rx = rect.size.width / 2f
        val ry = rect.size.height / 2f
        val c = 0.55228475f
        return PathCmd(
            listOf(
                PathOp.MoveTo(Point(cx + rx, cy)),
                PathOp.CubicTo(Point(cx + rx, cy + ry * c), Point(cx + rx * c, cy + ry), Point(cx, cy + ry)),
                PathOp.CubicTo(Point(cx - rx * c, cy + ry), Point(cx - rx, cy + ry * c), Point(cx - rx, cy)),
                PathOp.CubicTo(Point(cx - rx, cy - ry * c), Point(cx - rx * c, cy - ry), Point(cx, cy - ry)),
                PathOp.CubicTo(Point(cx + rx * c, cy - ry), Point(cx + rx, cy - ry * c), Point(cx + rx, cy)),
                PathOp.Close,
            ),
        )
    }

    private fun drawEdge(edge: Edge, route: EdgeRoute, out: MutableList<DrawCommand>, palette: UsecasePalette) {
        val pts = route.points
        if (pts.size < 2) return
        val ops = ArrayList<PathOp>(pts.size)
        ops += PathOp.MoveTo(pts[0])
        when (route.kind) {
            RouteKind.Bezier -> {
                var i = 1
                while (i + 2 < pts.size) {
                    ops += PathOp.CubicTo(pts[i], pts[i + 1], pts[i + 2])
                    i += 3
                }
                if (i < pts.size) ops += PathOp.LineTo(pts.last())
            }
            else -> for (k in 1 until pts.size) ops += PathOp.LineTo(pts[k])
        }
        val edgeColor = edge.style.color?.let { Color(it.argb) } ?: semanticEdgeColor(edge)
        val stroke = Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash)
        out += DrawCommand.StrokePath(path = PathCmd(ops), stroke = stroke, color = edgeColor, z = 1)
        val headTail = pts[pts.size - 2]
        val head = pts.last()
        val startTail = pts[1]
        val start = pts[0]
        when (edge.arrow) {
            com.hrm.diagram.core.ir.ArrowEnds.None -> Unit
            com.hrm.diagram.core.ir.ArrowEnds.ToOnly -> out += openArrowHead(headTail, head, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.FromOnly -> out += openArrowHead(startTail, start, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.Both -> {
                out += openArrowHead(headTail, head, edgeColor)
                out += openArrowHead(startTail, start, edgeColor)
            }
        }
        val text = (edge.label as? RichLabel.Plain)?.text ?: return
        if (text.isEmpty()) return
        val mid = pts[pts.size / 2]
        out += DrawCommand.DrawText(
            text = text,
            origin = Point(mid.x, mid.y - 4f),
            font = scopedFont(palette.usecase, edgeLabelFont),
            color = palette.usecase.text?.let { Color(it.argb) } ?: colors.edgeLabelText,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Bottom,
            z = 5,
        )
    }

    private fun openArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(to))),
            stroke = Stroke(width = 1f),
            color = color,
            z = 4,
        )
        val ux = dx / len
        val uy = dy / len
        val size = 8f
        val baseX = to.x - ux * size
        val baseY = to.y - uy * size
        val nx = -uy
        val ny = ux
        val p1 = Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f)
        val p2 = Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
        return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(p1), PathOp.LineTo(to), PathOp.LineTo(p2))),
            stroke = Stroke(width = 1.5f),
            color = color,
            z = 4,
        )
    }

    private fun labelTextOf(node: Node): String =
        when (val label = node.label) {
            is RichLabel.Plain -> label.text.takeIf { it.isNotEmpty() } ?: node.id.value
            is RichLabel.Markdown -> label.source.takeIf { it.isNotEmpty() } ?: node.id.value
            is RichLabel.Html -> label.html.takeIf { it.isNotEmpty() } ?: node.id.value
        }

    private fun parseClusterLabel(cluster: Cluster): Pair<String, String> {
        val text = (cluster.label as? RichLabel.Plain)?.text ?: return "package" to cluster.id.value
        val parts = text.split('\n', limit = 2)
        return if (parts.size == 2) parts[0] to parts[1] else "package" to text
    }

    private fun semanticEdgeColor(edge: Edge): Color {
        val label = (edge.label as? RichLabel.Plain)?.text?.lowercase().orEmpty()
        return when {
            "<<include>>" in label -> themeColors.accentSecondary
            "<<extend>>" in label -> themeColors.success
            else -> colors.edge
        }
    }

    private fun scopeForNode(node: Node, palette: UsecasePalette): ScopePalette = when (node.payload[PlantUmlUsecaseParser.KIND_KEY]) {
        "actor" -> palette.actor
        "note" -> palette.note
        else -> palette.usecase
    }

    private fun clusterScope(kind: String, palette: UsecasePalette): ScopePalette =
        when (kind.lowercase()) {
            "rectangle" -> palette.rectangle
            else -> palette.`package`
        }

    private fun applyPalette(ir: GraphIR, palette: UsecasePalette): GraphIR {
        val nodes = ir.nodes.map { node ->
            val scope = scopeForNode(node, palette)
            node.copy(
                style = node.style.copy(
                    fill = scope.fill ?: node.style.fill,
                    stroke = scope.stroke ?: node.style.stroke,
                    textColor = scope.text ?: node.style.textColor,
                    strokeWidth = scope.lineThickness ?: node.style.strokeWidth,
                ),
            )
        }
        val edges = ir.edges.map { edge ->
            edge.copy(
                style = edge.style.copy(
                    color = when {
                        edge.from.value.contains("__note_") -> palette.note.stroke ?: edge.style.color
                        else -> palette.edgeColor ?: edge.style.color
                    },
                ),
            )
        }
        val clusters = ir.clusters.map { applyClusterPalette(it, palette) }
        return ir.copy(nodes = nodes, edges = edges, clusters = clusters)
    }

    private fun applyClusterPalette(cluster: Cluster, palette: UsecasePalette): Cluster {
        val scope = clusterScope(parseClusterLabel(cluster).first, palette)
        return cluster.copy(
            style = ClusterStyle(
                fill = scope.fill ?: cluster.style.fill,
                stroke = scope.stroke ?: cluster.style.stroke,
                strokeWidth = scope.lineThickness ?: cluster.style.strokeWidth,
            ),
            nestedClusters = cluster.nestedClusters.map { applyClusterPalette(it, palette) },
        )
    }

    private fun resolvePalette(ir: GraphIR): UsecasePalette {
        val extras = ir.styleHints.extras
        if (cachedPaletteExtras != extras) {
            cachedPaletteExtras = extras.toMap()
            currentPalette = paletteOf(cachedPaletteExtras)
        }
        return currentPalette
    }

    private fun paletteOf(extras: Map<String, String>): UsecasePalette {
        fun c(key: String): ArgbColor? =
            extras[key]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) }
        fun f(key: String): Float? = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras[key])
        fun s(key: String): String? = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(extras[key])
        fun b(key: String): Boolean? = PlantUmlTreeRenderSupport.parsePlantUmlBoolean(extras[key])
        return UsecasePalette(
            actor = ScopePalette(
                fill = c(PlantUmlUsecaseParser.STYLE_ACTOR_FILL_KEY),
                stroke = c(PlantUmlUsecaseParser.STYLE_ACTOR_STROKE_KEY),
                text = c(PlantUmlUsecaseParser.STYLE_ACTOR_TEXT_KEY),
                fontSize = f(PlantUmlUsecaseParser.STYLE_ACTOR_FONT_SIZE_KEY),
                fontName = s(PlantUmlUsecaseParser.STYLE_ACTOR_FONT_NAME_KEY),
                lineThickness = f(PlantUmlUsecaseParser.STYLE_ACTOR_LINE_THICKNESS_KEY),
                shadowing = b(PlantUmlUsecaseParser.STYLE_ACTOR_SHADOWING_KEY),
            ),
            usecase = ScopePalette(
                fill = c(PlantUmlUsecaseParser.STYLE_USECASE_FILL_KEY),
                stroke = c(PlantUmlUsecaseParser.STYLE_USECASE_STROKE_KEY),
                text = c(PlantUmlUsecaseParser.STYLE_USECASE_TEXT_KEY),
                fontSize = f(PlantUmlUsecaseParser.STYLE_USECASE_FONT_SIZE_KEY),
                fontName = s(PlantUmlUsecaseParser.STYLE_USECASE_FONT_NAME_KEY),
                lineThickness = f(PlantUmlUsecaseParser.STYLE_USECASE_LINE_THICKNESS_KEY),
                shadowing = b(PlantUmlUsecaseParser.STYLE_USECASE_SHADOWING_KEY),
            ),
            note = ScopePalette(
                fill = c(PlantUmlUsecaseParser.STYLE_NOTE_FILL_KEY),
                stroke = c(PlantUmlUsecaseParser.STYLE_NOTE_STROKE_KEY),
                text = c(PlantUmlUsecaseParser.STYLE_NOTE_TEXT_KEY),
                fontSize = f(PlantUmlUsecaseParser.STYLE_NOTE_FONT_SIZE_KEY),
                fontName = s(PlantUmlUsecaseParser.STYLE_NOTE_FONT_NAME_KEY),
                lineThickness = f(PlantUmlUsecaseParser.STYLE_NOTE_LINE_THICKNESS_KEY),
                shadowing = b(PlantUmlUsecaseParser.STYLE_NOTE_SHADOWING_KEY),
            ),
            rectangle = ScopePalette(
                fill = c(PlantUmlUsecaseParser.STYLE_RECTANGLE_FILL_KEY),
                stroke = c(PlantUmlUsecaseParser.STYLE_RECTANGLE_STROKE_KEY),
                text = null,
                fontSize = f(PlantUmlUsecaseParser.STYLE_RECTANGLE_FONT_SIZE_KEY),
                fontName = s(PlantUmlUsecaseParser.STYLE_RECTANGLE_FONT_NAME_KEY),
                lineThickness = f(PlantUmlUsecaseParser.STYLE_RECTANGLE_LINE_THICKNESS_KEY),
                shadowing = b(PlantUmlUsecaseParser.STYLE_RECTANGLE_SHADOWING_KEY),
            ),
            `package` = ScopePalette(
                fill = c(PlantUmlUsecaseParser.STYLE_PACKAGE_FILL_KEY),
                stroke = c(PlantUmlUsecaseParser.STYLE_PACKAGE_STROKE_KEY),
                text = null,
                fontSize = f(PlantUmlUsecaseParser.STYLE_PACKAGE_FONT_SIZE_KEY),
                fontName = s(PlantUmlUsecaseParser.STYLE_PACKAGE_FONT_NAME_KEY),
                lineThickness = f(PlantUmlUsecaseParser.STYLE_PACKAGE_LINE_THICKNESS_KEY),
                shadowing = b(PlantUmlUsecaseParser.STYLE_PACKAGE_SHADOWING_KEY),
            ),
            edgeColor = c(PlantUmlUsecaseParser.STYLE_EDGE_COLOR_KEY),
        )
    }

    private fun scopedFont(scope: ScopePalette, base: FontSpec): FontSpec =
        PlantUmlTreeRenderSupport.resolveFontSpec(base, scope.fontName, scope.fontSize?.toString())

    private fun applyAnchoredNotes(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val noteNodes = ir.nodes.filter { it.payload[PlantUmlUsecaseParser.KIND_KEY] == "note" }
        if (noteNodes.isEmpty()) return laidOut
        val nodePositions = LinkedHashMap(laidOut.nodePositions)
        val edgeRoutes = laidOut.edgeRoutes.toMutableList()
        for (note in noteNodes) {
            val target = note.payload[PlantUmlUsecaseParser.NOTE_TARGET_KEY]?.let(::NodeId) ?: continue
            val placement = note.payload[PlantUmlUsecaseParser.NOTE_PLACEMENT_KEY].orEmpty()
            val targetRect = nodePositions[target] ?: continue
            val noteRect = nodePositions[note.id] ?: continue
            val anchored = anchoredNoteRect(noteRect.size, targetRect, placement)
            nodePositions[note.id] = anchored
            val edgeIndex = ir.edges.indexOfFirst { it.from == note.id && it.to == target }
            if (edgeIndex >= 0) {
                val route = EdgeRoute(
                    from = note.id,
                    to = target,
                    points = anchoredNoteRoute(anchored, targetRect, placement),
                    kind = RouteKind.Polyline,
                )
                if (edgeIndex < edgeRoutes.size) edgeRoutes[edgeIndex] = route else edgeRoutes += route
            }
        }
        val bounds = computeBounds(nodePositions.values + laidOut.clusterRects.values)
        return laidOut.copy(nodePositions = nodePositions, edgeRoutes = edgeRoutes, bounds = bounds)
    }

    private fun anchoredNoteRect(size: Size, targetRect: Rect, placement: String): Rect {
        val gap = 18f
        return when (placement.lowercase()) {
            "left" -> Rect(Point(targetRect.left - size.width - gap, targetRect.top + (targetRect.size.height - size.height) / 2f), size)
            "top" -> Rect(Point(targetRect.left + (targetRect.size.width - size.width) / 2f, targetRect.top - size.height - gap), size)
            "bottom" -> Rect(Point(targetRect.left + (targetRect.size.width - size.width) / 2f, targetRect.bottom + gap), size)
            else -> Rect(Point(targetRect.right + gap, targetRect.top + (targetRect.size.height - size.height) / 2f), size)
        }
    }

    private fun anchoredNoteRoute(noteRect: Rect, targetRect: Rect, placement: String): List<Point> =
        when (placement.lowercase()) {
            "left" -> listOf(
                Point(noteRect.right, (noteRect.top + noteRect.bottom) / 2f),
                Point(targetRect.left, (targetRect.top + targetRect.bottom) / 2f),
            )
            "top" -> listOf(
                Point((noteRect.left + noteRect.right) / 2f, noteRect.bottom),
                Point((targetRect.left + targetRect.right) / 2f, targetRect.top),
            )
            "bottom" -> listOf(
                Point((noteRect.left + noteRect.right) / 2f, noteRect.top),
                Point((targetRect.left + targetRect.right) / 2f, targetRect.bottom),
            )
            else -> listOf(
                Point(noteRect.left, (noteRect.top + noteRect.bottom) / 2f),
                Point(targetRect.right, (targetRect.top + targetRect.bottom) / 2f),
            )
        }
}
