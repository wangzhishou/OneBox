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
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.plantuml.PlantUmlObjectParser
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlObjectSubPipeline(
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

    private data class ObjectPalette(
        val scopes: Map<String, ScopePalette>,
        val edgeColor: ArgbColor?,
    )

    private val parser = PlantUmlObjectParser()
    private val colors = ThemeResolver.resolvePlantUmlComponent(theme)
    private val shadowTint = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private val secondaryText = theme.colors.textSecondary
    private var cachedPaletteExtras: Map<String, String> = emptyMap()
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 13f, weight = 600)
    private val memberFont = FontSpec(family = "monospace", sizeSp = 11f)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private var currentPalette: ObjectPalette = ObjectPalette(emptyMap(), null)
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(176f, 92f),
        maxWidth = 220f,
        minWidth = 124f,
        minHeight = 56f,
        labelOf = ::titleOf,
        fontOf = { node ->
            val kind = node.payload[PlantUmlObjectParser.KIND_KEY]
            scopedFont(currentPalette.scopes[kind], if (kind == "note") edgeLabelFont else titleFont)
        },
        customSizeOf = { node -> measureNodeSize(node, currentPalette) },
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.PLANTUML,
        defaultNodeSize = Size(176f, 92f),
        measurePolicy = measurePolicy,
        postLayout = { ir, laid ->
            val clusterRects = LinkedHashMap<NodeId, Rect>()
            for (cluster in ir.clusters) computeClusterRect(cluster, laid.nodePositions, clusterRects, currentPalette)
            applyAnchoredNotes(
                ir,
                laid.copy(
                    clusterRects = clusterRects,
                    bounds = computeBounds(laid.nodePositions.values + clusterRects.values),
                ),
            )
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
        currentPalette = ObjectPalette(emptyMap(), null)
        kernel.clear()
    }

    private fun measureNodeSize(node: Node, palette: ObjectPalette): Size {
        val title = titleOf(node)
        val members = membersOf(node)
        val kind = node.payload[PlantUmlObjectParser.KIND_KEY]
        val isNote = kind == "note"
        val scoped = palette.scopes[kind]
        val titleMetrics = textMeasurer.measure(title, scopedFont(scoped, if (isNote) edgeLabelFont else titleFont), maxWidth = 220f)
        val resolvedMemberFont = scopedFont(scoped, memberFont)
        val memberMetrics = if (members.isEmpty()) null else textMeasurer.measure(members.joinToString("\n"), resolvedMemberFont, maxWidth = 220f)
        val width = maxOf(
            titleMetrics.width + 28f,
            (memberMetrics?.width ?: 0f) + 28f,
            if (isNote) 120f else 124f,
        )
        val height = titleMetrics.height + 22f + if (memberMetrics != null) memberMetrics.height + 18f else 0f
        return Size(width, height.coerceAtLeast(if (isNote) 50f else 56f))
    }

    private fun computeClusterRect(
        cluster: Cluster,
        nodePositions: Map<NodeId, Rect>,
        out: LinkedHashMap<NodeId, Rect>,
        palette: ObjectPalette,
    ): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        childRects += cluster.nestedClusters.mapNotNull { computeClusterRect(it, nodePositions, out, palette) }
        if (childRects.isEmpty()) return null
        val (kind, title) = parseClusterLabel(cluster)
        val titleMetrics = textMeasurer.measure(title.ifBlank { cluster.id.value }, scopedFont(palette.scopes[kind.lowercase()], titleFont), maxWidth = 220f)
        val rect = Rect.ltrb(
            childRects.minOf { it.left } - 20f,
            childRects.minOf { it.top } - (titleMetrics.height + 24f),
            childRects.maxOf { it.right } + 20f,
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

    private fun render(ir: GraphIR, laidOut: LaidOutDiagram, palette: ObjectPalette): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, out, palette)
        for (node in ir.nodes) {
            val rect = laidOut.nodePositions[node.id] ?: continue
            drawNode(node, rect, out, palette)
        }
        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            drawEdge(edge, route, out)
        }
        return out.entities()
    }

    private fun drawCluster(cluster: Cluster, clusterRects: Map<NodeId, Rect>, out: MutableList<DrawCommand>, palette: ObjectPalette) {
        val rect = clusterRects[cluster.id] ?: return
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill
        val stroke = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        val kind = parseClusterLabel(cluster).first.lowercase()
        val scoped = palette.scopes[kind]
        val textColor = palette.scopes[kind]?.text?.let { Color(it.argb) } ?: stroke
        if (scoped?.shadowing == true) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowTint,
                corner = 12f,
                z = 0,
            )
        }
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 0)
        out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f, dash = listOf(7f, 5f)), color = stroke, corner = 12f, z = 1)
        val (_, title) = parseClusterLabel(cluster)
        out += DrawCommand.DrawText(
            text = "${kind.uppercase()}  ${title.ifBlank { cluster.id.value }}",
            origin = Point(rect.left + 12f, rect.top + 8f),
            font = scopedFont(scoped, titleFont),
            color = textColor,
            maxWidth = rect.size.width - 24f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 2,
        )
        for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, out, palette)
    }

    private fun drawNode(node: Node, rect: Rect, out: MutableList<DrawCommand>, palette: ObjectPalette) {
        val kind = node.payload[PlantUmlObjectParser.KIND_KEY]
        val fill = node.style.fill?.let { Color(it.argb) } ?: if (kind == "note") colors.noteFill else colors.nodeFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: if (kind == "note") colors.noteStroke else colors.nodeStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: if (kind == "note") colors.noteText else colors.nodeText
        val members = membersOf(node)
        val title = titleOf(node)
        val scoped = palette.scopes[kind]
        val isNote = kind == "note"
        val resolvedTitleFont = scopedFont(scoped, if (isNote) edgeLabelFont else titleFont)
        val resolvedMemberFont = scopedFont(scoped, memberFont)
        val titleMetrics = textMeasurer.measure(title, resolvedTitleFont, maxWidth = rect.size.width - 20f)
        val headerBottom = rect.top + titleMetrics.height + 18f

        if (scoped?.shadowing == true) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowTint,
                corner = 8f,
                z = 1,
            )
        }
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 8f, z = 2)
        out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = node.style.strokeWidth ?: 1.5f), color = strokeColor, corner = 8f, z = 3)
        out += DrawCommand.DrawText(
            text = title,
            origin = Point((rect.left + rect.right) / 2f, rect.top + 9f),
            font = resolvedTitleFont,
            color = textColor,
            maxWidth = rect.size.width - 20f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Top,
            z = 4,
        )
        if (members.isNotEmpty() && !isNote) {
            out += DrawCommand.StrokePath(
                path = PathCmd(
                    listOf(
                        PathOp.MoveTo(Point(rect.left, headerBottom)),
                        PathOp.LineTo(Point(rect.right, headerBottom)),
                    ),
                ),
                stroke = Stroke(width = 1f),
                color = strokeColor,
                z = 4,
            )
            var y = headerBottom + 8f
            for (member in members) {
                out += DrawCommand.DrawText(
                    text = member,
                    origin = Point(rect.left + 10f, y),
                    font = resolvedMemberFont,
                    color = textColor,
                    maxWidth = rect.size.width - 20f,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 4,
                )
                val metrics = textMeasurer.measure(member, resolvedMemberFont, maxWidth = rect.size.width - 20f)
                y += metrics.height + 2f
            }
        }
    }

    private fun drawEdge(edge: com.hrm.diagram.core.ir.Edge, route: com.hrm.diagram.layout.EdgeRoute, out: MutableList<DrawCommand>) {
        val pts = route.points
        if (pts.size < 2) return
        val ops = ArrayList<PathOp>()
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
        val color = edge.style.color?.let { Color(it.argb) } ?: colors.edgeColor
        out += DrawCommand.StrokePath(
            path = PathCmd(ops),
            stroke = Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash),
            color = color,
            z = 1,
        )
        when (edge.arrow) {
            com.hrm.diagram.core.ir.ArrowEnds.None -> Unit
            com.hrm.diagram.core.ir.ArrowEnds.ToOnly -> out += arrowHead(pts[pts.size - 2], pts.last(), color)
            com.hrm.diagram.core.ir.ArrowEnds.FromOnly -> out += arrowHead(pts[1], pts[0], color)
            com.hrm.diagram.core.ir.ArrowEnds.Both -> {
                out += arrowHead(pts[pts.size - 2], pts.last(), color)
                out += arrowHead(pts[1], pts[0], color)
            }
        }
        val text = (edge.label as? RichLabel.Plain)?.text.orEmpty()
        if (text.isNotEmpty()) {
            val mid = pts[pts.size / 2]
            out += DrawCommand.DrawText(
                text = text,
                origin = Point(mid.x, mid.y - 4f),
                font = edgeLabelFont,
                color = secondaryText,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Bottom,
                z = 3,
            )
        }
    }

    private fun arrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(to))),
            stroke = Stroke(width = 1f),
            color = color,
            z = 2,
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
            z = 2,
        )
    }

    private fun titleOf(node: Node): String =
        when (val label = node.label) {
            is RichLabel.Plain -> label.text.ifEmpty { node.id.value }
            is RichLabel.Markdown -> label.source.ifEmpty { node.id.value }
            is RichLabel.Html -> label.html.ifEmpty { node.id.value }
        }

    private fun membersOf(node: Node): List<String> =
        node.payload[PlantUmlObjectParser.MEMBERS_KEY]
            ?.split('\n')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

    private fun parseClusterLabel(cluster: Cluster): Pair<String, String> {
        val text = (cluster.label as? RichLabel.Plain)?.text ?: return "package" to cluster.id.value
        val parts = text.split('\n', limit = 2)
        return if (parts.size == 2) parts[0] to parts[1] else "package" to text
    }

    private fun applyPalette(ir: GraphIR, palette: ObjectPalette): GraphIR {
        val nodes = ir.nodes.map { node ->
            val kind = node.payload[PlantUmlObjectParser.KIND_KEY]
            val scoped = palette.scopes[kind]
            if (scoped == null) node else {
                node.copy(
                    style = node.style.copy(
                        fill = scoped.fill ?: node.style.fill,
                        stroke = scoped.stroke ?: node.style.stroke,
                        textColor = scoped.text ?: node.style.textColor,
                        strokeWidth = scoped.lineThickness ?: node.style.strokeWidth,
                    ),
                )
            }
        }
        val edges = ir.edges.map { edge ->
            edge.copy(
                style = edge.style.copy(
                    color = when {
                        edge.from.value.contains("__note_") -> palette.scopes["note"]?.stroke ?: edge.style.color
                        else -> palette.edgeColor ?: edge.style.color
                    },
                ),
            )
        }
        val clusters = ir.clusters.map { applyClusterPalette(it, palette) }
        return ir.copy(nodes = nodes, edges = edges, clusters = clusters)
    }

    private fun applyClusterPalette(cluster: Cluster, palette: ObjectPalette): Cluster {
        val kind = parseClusterLabel(cluster).first.lowercase()
        val scoped = palette.scopes[kind]
        return cluster.copy(
            style = ClusterStyle(
                fill = scoped?.fill ?: cluster.style.fill,
                stroke = scoped?.stroke ?: cluster.style.stroke,
                strokeWidth = scoped?.lineThickness ?: cluster.style.strokeWidth,
            ),
            nestedClusters = cluster.nestedClusters.map { applyClusterPalette(it, palette) },
        )
    }

    private fun resolvePalette(ir: GraphIR): ObjectPalette {
        val extras = ir.styleHints.extras
        if (cachedPaletteExtras != extras) {
            cachedPaletteExtras = extras.toMap()
            currentPalette = paletteOf(cachedPaletteExtras)
        }
        return currentPalette
    }

    private fun paletteOf(extras: Map<String, String>): ObjectPalette {
        fun c(key: String): ArgbColor? =
            extras[key]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) }
        fun f(key: String): Float? = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras[key])
        fun s(key: String): String? = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(extras[key])
        fun b(key: String): Boolean? = PlantUmlTreeRenderSupport.parsePlantUmlBoolean(extras[key])
        return ObjectPalette(
            scopes = mapOf(
                "object" to ScopePalette(c(PlantUmlObjectParser.STYLE_OBJECT_FILL_KEY), c(PlantUmlObjectParser.STYLE_OBJECT_STROKE_KEY), c(PlantUmlObjectParser.STYLE_OBJECT_TEXT_KEY), f(PlantUmlObjectParser.styleFontSizeKey("object")), s(PlantUmlObjectParser.styleFontNameKey("object")), f(PlantUmlObjectParser.styleLineThicknessKey("object")), b(PlantUmlObjectParser.styleShadowingKey("object"))),
                "map" to ScopePalette(c(PlantUmlObjectParser.STYLE_MAP_FILL_KEY), c(PlantUmlObjectParser.STYLE_MAP_STROKE_KEY), c(PlantUmlObjectParser.STYLE_MAP_TEXT_KEY), f(PlantUmlObjectParser.styleFontSizeKey("map")), s(PlantUmlObjectParser.styleFontNameKey("map")), f(PlantUmlObjectParser.styleLineThicknessKey("map")), b(PlantUmlObjectParser.styleShadowingKey("map"))),
                "json" to ScopePalette(c(PlantUmlObjectParser.STYLE_JSON_FILL_KEY), c(PlantUmlObjectParser.STYLE_JSON_STROKE_KEY), c(PlantUmlObjectParser.STYLE_JSON_TEXT_KEY), f(PlantUmlObjectParser.styleFontSizeKey("json")), s(PlantUmlObjectParser.styleFontNameKey("json")), f(PlantUmlObjectParser.styleLineThicknessKey("json")), b(PlantUmlObjectParser.styleShadowingKey("json"))),
                "note" to ScopePalette(c(PlantUmlObjectParser.STYLE_NOTE_FILL_KEY), c(PlantUmlObjectParser.STYLE_NOTE_STROKE_KEY), c(PlantUmlObjectParser.STYLE_NOTE_TEXT_KEY), f(PlantUmlObjectParser.styleFontSizeKey("note")), s(PlantUmlObjectParser.styleFontNameKey("note")), f(PlantUmlObjectParser.styleLineThicknessKey("note")), b(PlantUmlObjectParser.styleShadowingKey("note"))),
                "package" to ScopePalette(c(PlantUmlObjectParser.STYLE_PACKAGE_FILL_KEY), c(PlantUmlObjectParser.STYLE_PACKAGE_STROKE_KEY), c(PlantUmlObjectParser.STYLE_PACKAGE_TEXT_KEY), f(PlantUmlObjectParser.styleFontSizeKey("package")), s(PlantUmlObjectParser.styleFontNameKey("package")), f(PlantUmlObjectParser.styleLineThicknessKey("package")), b(PlantUmlObjectParser.styleShadowingKey("package"))),
                "namespace" to ScopePalette(c(PlantUmlObjectParser.STYLE_NAMESPACE_FILL_KEY), c(PlantUmlObjectParser.STYLE_NAMESPACE_STROKE_KEY), c(PlantUmlObjectParser.STYLE_NAMESPACE_TEXT_KEY), f(PlantUmlObjectParser.styleFontSizeKey("namespace")), s(PlantUmlObjectParser.styleFontNameKey("namespace")), f(PlantUmlObjectParser.styleLineThicknessKey("namespace")), b(PlantUmlObjectParser.styleShadowingKey("namespace"))),
            ),
            edgeColor = c(PlantUmlObjectParser.STYLE_EDGE_COLOR_KEY),
        )
    }

    private fun scopedFont(scope: ScopePalette?, base: FontSpec): FontSpec =
        PlantUmlTreeRenderSupport.resolveFontSpec(base, scope?.fontName, scope?.fontSize?.toString())

    private fun applyAnchoredNotes(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val noteNodes = ir.nodes.filter { it.payload[PlantUmlObjectParser.KIND_KEY] == "note" }
        if (noteNodes.isEmpty()) return laidOut
        val nodePositions = LinkedHashMap(laidOut.nodePositions)
        val edgeRoutes = laidOut.edgeRoutes.toMutableList()
        for (note in noteNodes) {
            val target = note.payload[PlantUmlObjectParser.NOTE_TARGET_KEY]?.let(::NodeId) ?: continue
            val placement = note.payload[PlantUmlObjectParser.NOTE_PLACEMENT_KEY].orEmpty()
            val targetRect = nodePositions[target] ?: continue
            val noteRect = nodePositions[note.id] ?: continue
            val anchored = anchoredNoteRect(noteRect.size, targetRect, placement)
            nodePositions[note.id] = anchored
            val edgeIndex = ir.edges.indexOfFirst { it.from == note.id && it.to == target }
            if (edgeIndex >= 0) {
                val route = com.hrm.diagram.layout.EdgeRoute(
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
            "left" -> listOf(Point(noteRect.right, (noteRect.top + noteRect.bottom) / 2f), Point(targetRect.left, (targetRect.top + targetRect.bottom) / 2f))
            "top" -> listOf(Point((noteRect.left + noteRect.right) / 2f, noteRect.bottom), Point((targetRect.left + targetRect.right) / 2f, targetRect.top))
            "bottom" -> listOf(Point((noteRect.left + noteRect.right) / 2f, noteRect.top), Point((targetRect.left + targetRect.right) / 2f, targetRect.bottom))
            else -> listOf(Point(noteRect.left, (noteRect.top + noteRect.bottom) / 2f), Point(targetRect.right, (targetRect.top + targetRect.bottom) / 2f))
        }
}
