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
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.plantuml.PlantUmlDeploymentParser
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal class PlantUmlDeploymentSubPipeline(
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

    private data class DeploymentPalette(
        val scopes: Map<String, ScopePalette>,
        val edgeColor: ArgbColor?,
    )

    private val parser = PlantUmlDeploymentParser()
    private val colors = ThemeResolver.resolvePlantUmlDeployment(theme)
    private val shadowColor = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f, weight = 600)
    private val groupFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private var currentPalette: DeploymentPalette = DeploymentPalette(emptyMap(), null)
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(176f, 76f),
        maxWidth = 180f,
        minWidth = 124f,
        minHeight = 56f,
        labelOf = ::labelTextOf,
        fontOf = { node -> scopedFont(currentPalette.scopes[node.payload[PlantUmlDeploymentParser.KIND_KEY]], labelFont) },
        customSizeOf = { node -> measureNodeSize(node, currentPalette) },
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.PLANTUML,
        defaultNodeSize = Size(176f, 76f),
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
        val palette = paletteOf(rawIr)
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
        currentPalette = DeploymentPalette(emptyMap(), null)
        kernel.clear()
    }

    private fun measureNodeSize(node: Node, palette: DeploymentPalette): Size {
        val label = labelTextOf(node)
        val kind = node.payload[PlantUmlDeploymentParser.KIND_KEY]
        val metrics = textMeasurer.measure(label, scopedFont(palette.scopes[kind], labelFont), maxWidth = 180f)
        return when (kind) {
            "actor" -> Size((metrics.width + 36f).coerceAtLeast(120f), (metrics.height + 68f).coerceAtLeast(92f))
            "database" -> Size((metrics.width + 42f).coerceAtLeast(136f), (metrics.height + 34f).coerceAtLeast(64f))
            "storage" -> Size((metrics.width + 42f).coerceAtLeast(136f), (metrics.height + 34f).coerceAtLeast(64f))
            "cloud" -> Size((metrics.width + 48f).coerceAtLeast(152f), (metrics.height + 34f).coerceAtLeast(72f))
            "artifact", "queue", "note" -> Size((metrics.width + 34f).coerceAtLeast(124f), (metrics.height + 28f).coerceAtLeast(56f))
            else -> Size((metrics.width + 36f).coerceAtLeast(140f), (metrics.height + 28f).coerceAtLeast(60f))
        }
    }

    private fun computeClusterRect(
        cluster: Cluster,
        nodePositions: Map<NodeId, Rect>,
        out: LinkedHashMap<NodeId, Rect>,
        palette: DeploymentPalette,
    ): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        val nestedRects = cluster.nestedClusters.mapNotNull { computeClusterRect(it, nodePositions, out, palette) }
        childRects += nestedRects
        if (childRects.isEmpty()) return null
        val (kind, title) = parseClusterLabel(cluster)
        val titleMetrics = textMeasurer.measure(title.ifBlank { cluster.id.value }, scopedFont(palette.scopes[kind.lowercase()], groupFont), maxWidth = 220f)
        val headerHeight = if (kind.equals("node", ignoreCase = true)) {
            max(titleMetrics.height + 34f, 46f)
        } else {
            titleMetrics.height + 26f
        }
        val horizontalPadding = if (kind.equals("node", ignoreCase = true)) 24f else 20f
        val bottomPadding = if (kind.equals("node", ignoreCase = true)) 24f else 18f
        val left = childRects.minOf { it.left } - horizontalPadding
        val top = childRects.minOf { it.top } - headerHeight
        val right = max(childRects.maxOf { it.right } + horizontalPadding, left + titleMetrics.width + 62f)
        val bottom = childRects.maxOf { it.bottom } + bottomPadding
        val rect = Rect.ltrb(left, top, right, bottom)
        out[cluster.id] = rect
        return rect
    }

    private fun computeBounds(rects: Collection<Rect>): Rect {
        if (rects.isEmpty()) return Rect.ltrb(0f, 0f, 400f, 240f)
        val viewportPadding = 24f
        val minLeft = rects.minOf { it.left }.coerceAtMost(0f)
        val minTop = rects.minOf { it.top }.coerceAtMost(0f)
        val maxRight = rects.maxOf { it.right }
        val maxBottom = rects.maxOf { it.bottom }
        return Rect.ltrb(minLeft - viewportPadding, minTop - viewportPadding, maxRight + viewportPadding, maxBottom + viewportPadding)
    }

    private fun render(ir: GraphIR, laidOut: LaidOutDiagram, palette: DeploymentPalette): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        val bounds = laidOut.bounds
        out += DrawCommand.FillRect(Rect(Point(bounds.left, bounds.top), Size(bounds.size.width, bounds.size.height)), colors.canvas, z = 0)
        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, out, palette)
        for (node in ir.nodes) drawNode(node, laidOut, out, palette)
        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            drawEdge(edge, route, out)
        }
        return out.entities()
    }

    private fun drawCluster(cluster: Cluster, clusterRects: Map<NodeId, Rect>, out: MutableList<DrawCommand>, palette: DeploymentPalette) {
        val rect = clusterRects[cluster.id] ?: return
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        val (kind, title) = parseClusterLabel(cluster)
        val scoped = palette.scopes[kind.lowercase()]
        if (kind.equals("node", ignoreCase = true)) {
            drawDeploymentNodeCluster(
                rect = rect,
                title = title.ifBlank { cluster.id.value },
                fill = fill,
                strokeColor = strokeColor,
                stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f),
                textColor = scoped?.text?.let { Color(it.argb) } ?: colors.clusterText,
                font = scopedFont(scoped, groupFont),
                shadowing = scoped?.shadowing == true,
                out = out,
            )
            for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, out, palette)
            return
        }
        val stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f, dash = listOf(7f, 5f))
        if (scoped?.shadowing == true) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowColor,
                corner = 14f,
                z = 0,
            )
        }
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 14f, z = 0)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 14f, z = 1)
        val textColor = palette.scopes[kind.lowercase()]?.text?.let { Color(it.argb) } ?: colors.clusterText
        val chipWidth = min(160f, rect.size.width - 24f)
        val chipRect = Rect.ltrb(rect.left + 12f, rect.top + 10f, rect.left + chipWidth, rect.top + 34f)
        out += DrawCommand.FillRect(rect = chipRect, color = colors.clusterChipFill, corner = 12f, z = 2)
        out += DrawCommand.StrokeRect(rect = chipRect, stroke = Stroke(width = 1f), color = strokeColor, corner = 12f, z = 3)
        out += DrawCommand.DrawText(
            text = "${kind.uppercase()}  ${title.ifBlank { cluster.id.value }}",
            origin = Point(chipRect.left + 10f, (chipRect.top + chipRect.bottom) / 2f),
            font = scopedFont(scoped, groupFont),
            color = textColor,
            maxWidth = chipRect.size.width - 20f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Middle,
            z = 4,
        )
        for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, out, palette)
    }

    private fun drawDeploymentNodeCluster(
        rect: Rect,
        title: String,
        fill: Color,
        strokeColor: Color,
        stroke: Stroke,
        textColor: Color,
        font: FontSpec,
        shadowing: Boolean,
        out: MutableList<DrawCommand>,
    ) {
        val depth = 10f
        val front = Rect.ltrb(rect.left, rect.top + depth, rect.right - depth, rect.bottom)
        if (shadowing) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(front, 4f, 4f),
                color = shadowColor,
                corner = 6f,
                z = 0,
            )
        }
        val topFace = PathCmd(
            listOf(
                PathOp.MoveTo(Point(front.left, front.top)),
                PathOp.LineTo(Point(front.left + depth, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top)),
                PathOp.LineTo(Point(front.right, front.top)),
                PathOp.Close,
            ),
        )
        val rightFace = PathCmd(
            listOf(
                PathOp.MoveTo(Point(front.right, front.top)),
                PathOp.LineTo(Point(rect.right, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.bottom - depth)),
                PathOp.LineTo(Point(front.right, front.bottom)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillRect(rect = front, color = fill, corner = 6f, z = 0)
        out += DrawCommand.FillPath(path = topFace, color = fill, z = 0)
        out += DrawCommand.FillPath(path = rightFace, color = fill, z = 0)
        out += DrawCommand.StrokeRect(rect = front, stroke = stroke, color = strokeColor, corner = 6f, z = 1)
        out += DrawCommand.StrokePath(path = topFace, stroke = Stroke(width = stroke.width), color = strokeColor, z = 1)
        out += DrawCommand.StrokePath(path = rightFace, stroke = Stroke(width = stroke.width), color = strokeColor, z = 1)
        out += DrawCommand.DrawText(
            text = title,
            origin = Point(front.left + 14f, front.top + 12f),
            font = font,
            color = textColor,
            maxWidth = front.size.width - 28f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 4,
        )
    }

    private fun drawNode(node: Node, laidOut: LaidOutDiagram, out: MutableList<DrawCommand>, palette: DeploymentPalette) {
        val rect = laidOut.nodePositions[node.id] ?: return
        val kind = node.payload[PlantUmlDeploymentParser.KIND_KEY]
        val defaultFill = if (kind == "note") colors.noteFill else colors.nodeFill
        val defaultStroke = if (kind == "note") colors.noteStroke else colors.nodeStroke
        val defaultText = if (kind == "note") colors.noteText else colors.nodeText
        val fill = node.style.fill?.let { Color(it.argb) } ?: defaultFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: defaultStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: defaultText
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f)
        val scoped = palette.scopes[kind]
        val nodeFont = scopedFont(scoped, labelFont)
        when (kind) {
            "actor" -> {
                drawActor(node, rect, out, scoped?.shadowing == true, nodeFont)
                return
            }
            "note" -> {
                drawNote(node, rect, out, scoped?.shadowing == true, nodeFont)
                return
            }
        }
        if (scoped?.shadowing == true) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowColor,
                corner = 10f,
                z = 3,
            )
        }
        when (node.shape) {
            NodeShape.Cylinder -> drawCylinder(rect, fill, strokeColor, stroke, out)
            NodeShape.Cloud -> drawCloud(rect, fill, strokeColor, stroke, out)
            NodeShape.Note -> drawArtifact(rect, fill, strokeColor, stroke, out)
            NodeShape.Package -> drawPackage(rect, fill, strokeColor, stroke, out)
            else -> {
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = 10f, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 10f, z = 5)
            }
        }
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
            font = nodeFont,
            color = textColor,
            maxWidth = rect.size.width - 18f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 7,
        )
    }

    private fun drawPackage(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 10f, z = 4)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 10f, z = 5)
        val tab = Rect.ltrb(rect.left + 10f, rect.top - 8f, rect.left + max(46f, rect.size.width * 0.34f), rect.top + 10f)
        out += DrawCommand.FillRect(rect = tab, color = fill, corner = 6f, z = 5)
        out += DrawCommand.StrokeRect(rect = tab, stroke = Stroke(width = 1.2f), color = strokeColor, corner = 6f, z = 6)
    }

    private fun drawArtifact(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 8f, z = 4)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 8f, z = 5)
        val fold = 14f
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(rect.right - fold, rect.top)),
                PathOp.LineTo(Point(rect.right - fold, rect.top + fold)),
                PathOp.LineTo(Point(rect.right, rect.top + fold)),
            ),
        )
        out += DrawCommand.StrokePath(path = path, stroke = Stroke(width = 1.2f), color = strokeColor, z = 6)
    }

    private fun drawCylinder(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 10f, z = 4)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 10f, z = 5)
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(rect.left + 12f, rect.top + 10f)),
                    PathOp.CubicTo(
                        Point(rect.left + 30f, rect.top),
                        Point(rect.right - 30f, rect.top),
                        Point(rect.right - 12f, rect.top + 10f),
                    ),
                ),
            ),
            stroke = Stroke(width = 1f),
            color = strokeColor,
            z = 6,
        )
    }

    private fun drawCloud(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val l = rect.left
        val t = rect.top
        val r = rect.right
        val b = rect.bottom
        val w = rect.size.width
        val h = rect.size.height
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(l + w * 0.18f, b - h * 0.18f)),
                PathOp.CubicTo(Point(l - w * 0.02f, b - h * 0.26f), Point(l + w * 0.02f, t + h * 0.38f), Point(l + w * 0.20f, t + h * 0.38f)),
                PathOp.CubicTo(Point(l + w * 0.20f, t + h * 0.08f), Point(l + w * 0.42f, t + h * 0.02f), Point(l + w * 0.54f, t + h * 0.20f)),
                PathOp.CubicTo(Point(l + w * 0.64f, t - h * 0.02f), Point(r - w * 0.10f, t + h * 0.06f), Point(r - w * 0.12f, t + h * 0.32f)),
                PathOp.CubicTo(Point(r + w * 0.02f, t + h * 0.36f), Point(r + w * 0.02f, b - h * 0.12f), Point(r - w * 0.12f, b - h * 0.12f)),
                PathOp.CubicTo(Point(r - w * 0.18f, b + h * 0.04f), Point(l + w * 0.34f, b + h * 0.04f), Point(l + w * 0.18f, b - h * 0.18f)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillPath(path = path, color = fill, z = 4)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 5)
    }

    private fun applyAnchoredNotes(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val notes = ir.nodes.filter { it.payload[PlantUmlDeploymentParser.KIND_KEY] == "note" && it.payload.containsKey(PlantUmlDeploymentParser.NOTE_TARGET_KEY) }
        if (notes.isEmpty()) return laidOut
        val nodePositions = LinkedHashMap(laidOut.nodePositions)
        val edgeRoutes = laidOut.edgeRoutes.toMutableList()
        for (note in notes) {
            val target = note.payload[PlantUmlDeploymentParser.NOTE_TARGET_KEY]?.let(::NodeId) ?: continue
            val placement = note.payload[PlantUmlDeploymentParser.NOTE_PLACEMENT_KEY].orEmpty()
            val targetRect = nodePositions[target] ?: continue
            val current = nodePositions[note.id] ?: continue
            val anchored = anchoredNoteRect(current.size, targetRect, placement)
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
        return laidOut.copy(
            nodePositions = nodePositions,
            edgeRoutes = edgeRoutes,
            bounds = computeBounds(nodePositions.values + laidOut.clusterRects.values),
        )
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

    private fun anchoredNoteRoute(noteRect: Rect, targetRect: Rect, placement: String): List<Point> {
        return when (placement.lowercase()) {
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
        val edgeColor = edge.style.color?.let { Color(it.argb) } ?: colors.edge
        val stroke = Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash)
        out += DrawCommand.StrokePath(path = PathCmd(ops), stroke = stroke, color = edgeColor, z = 2)
        val tail = pts[pts.size - 2]
        val head = pts.last()
        val startTail = pts[1]
        val startHead = pts[0]
        when (edge.arrow) {
            com.hrm.diagram.core.ir.ArrowEnds.None -> Unit
            com.hrm.diagram.core.ir.ArrowEnds.ToOnly -> out += arrowHead(tail, head, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.FromOnly -> out += arrowHead(startTail, startHead, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.Both -> {
                out += arrowHead(tail, head, edgeColor)
                out += arrowHead(startTail, startHead, edgeColor)
            }
        }
        val text = (edge.label as? RichLabel.Plain)?.text.orEmpty()
        if (text.isNotEmpty()) {
            val mid = pts[pts.size / 2]
            out += DrawCommand.DrawText(
                text = text,
                origin = Point(mid.x, mid.y - 4f),
                font = edgeLabelFont,
                color = colors.edgeLabelText,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Bottom,
                z = 5,
            )
        }
    }

    private fun drawActor(node: Node, rect: Rect, out: MutableList<DrawCommand>, shadowing: Boolean, font: FontSpec) {
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.nodeStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.nodeText
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f)
        val cx = (rect.left + rect.right) / 2f
        val top = rect.top + 8f
        val headRadius = 11f
        val headRect = Rect.ltrb(cx - headRadius, top, cx + headRadius, top + headRadius * 2f)
        if (shadowing) {
            val shadowRect = PlantUmlTreeRenderSupport.offsetRect(headRect, 4f, 4f)
            val shadowColor = shadowColor
            out += DrawCommand.StrokeRect(rect = shadowRect, stroke = stroke, color = shadowColor, corner = headRadius, z = 4)
            out += DrawCommand.StrokePath(
                path = PathCmd(
                    listOf(
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, bodyTop(headRect)), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, bodyTop(headRect) + 30f), 4f, 4f)),
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx - 16f, bodyTop(headRect) + 12f), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx + 16f, bodyTop(headRect) + 12f), 4f, 4f)),
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, bodyTop(headRect) + 30f), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx - 14f, rect.bottom - 28f), 4f, 4f)),
                        PathOp.MoveTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx, bodyTop(headRect) + 30f), 4f, 4f)),
                        PathOp.LineTo(PlantUmlTreeRenderSupport.offsetPoint(Point(cx + 14f, rect.bottom - 28f), 4f, 4f)),
                    ),
                ),
                stroke = stroke,
                color = shadowColor,
                z = 4,
            )
        }
        out += DrawCommand.StrokeRect(rect = headRect, stroke = stroke, color = strokeColor, corner = headRadius, z = 5)
        val bodyTop = bodyTop(headRect)
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
            z = 5,
        )
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point(cx, rect.bottom - 10f),
            font = font,
            color = textColor,
            maxWidth = rect.size.width - 12f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Bottom,
            z = 6,
        )
    }

    private fun drawNote(node: Node, rect: Rect, out: MutableList<DrawCommand>, shadowing: Boolean, font: FontSpec) {
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.noteFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.noteStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.noteText
        if (shadowing) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowColor,
                corner = 8f,
                z = 3,
            )
        }
        drawArtifact(rect, fill, strokeColor, Stroke(width = node.style.strokeWidth ?: 1.25f), out)
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point(rect.left + 12f, rect.top + 10f),
            font = font,
            color = textColor,
            maxWidth = rect.size.width - 24f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 7,
        )
    }

    private fun arrowHead(from: Point, to: Point, color: Color): DrawCommand {
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
        val text = (cluster.label as? RichLabel.Plain)?.text ?: return "node" to cluster.id.value
        val parts = text.split('\n', limit = 2)
        return if (parts.size == 2) parts[0] to parts[1] else "node" to text
    }

    private fun scopedFont(scope: ScopePalette?, base: FontSpec): FontSpec =
        PlantUmlTreeRenderSupport.resolveFontSpec(base, scope?.fontName, scope?.fontSize?.toString())

    private fun bodyTop(headRect: Rect): Float = headRect.bottom

    private fun applyPalette(ir: GraphIR, palette: DeploymentPalette): GraphIR {
        val nodes = ir.nodes.map { node ->
            val kind = node.payload[PlantUmlDeploymentParser.KIND_KEY]
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

    private fun applyClusterPalette(cluster: Cluster, palette: DeploymentPalette): Cluster {
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

    private fun paletteOf(ir: GraphIR): DeploymentPalette {
        val extras = ir.styleHints.extras
        fun c(key: String): ArgbColor? =
            extras[key]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) }
        fun f(key: String): Float? = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras[key])
        fun s(key: String): String? = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(extras[key])
        fun b(key: String): Boolean? = PlantUmlTreeRenderSupport.parsePlantUmlBoolean(extras[key])
        return DeploymentPalette(
            scopes = mapOf(
                "actor" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_ACTOR_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_ACTOR_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_ACTOR_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("actor")), s(PlantUmlDeploymentParser.styleFontNameKey("actor")), f(PlantUmlDeploymentParser.styleLineThicknessKey("actor")), b(PlantUmlDeploymentParser.styleShadowingKey("actor"))),
                "artifact" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_ARTIFACT_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_ARTIFACT_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_ARTIFACT_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("artifact")), s(PlantUmlDeploymentParser.styleFontNameKey("artifact")), f(PlantUmlDeploymentParser.styleLineThicknessKey("artifact")), b(PlantUmlDeploymentParser.styleShadowingKey("artifact"))),
                "database" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_DATABASE_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_DATABASE_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_DATABASE_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("database")), s(PlantUmlDeploymentParser.styleFontNameKey("database")), f(PlantUmlDeploymentParser.styleLineThicknessKey("database")), b(PlantUmlDeploymentParser.styleShadowingKey("database"))),
                "storage" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_STORAGE_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_STORAGE_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_STORAGE_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("storage")), s(PlantUmlDeploymentParser.styleFontNameKey("storage")), f(PlantUmlDeploymentParser.styleLineThicknessKey("storage")), b(PlantUmlDeploymentParser.styleShadowingKey("storage"))),
                "queue" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_QUEUE_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_QUEUE_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_QUEUE_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("queue")), s(PlantUmlDeploymentParser.styleFontNameKey("queue")), f(PlantUmlDeploymentParser.styleLineThicknessKey("queue")), b(PlantUmlDeploymentParser.styleShadowingKey("queue"))),
                "note" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_NOTE_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_NOTE_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_NOTE_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("note")), s(PlantUmlDeploymentParser.styleFontNameKey("note")), f(PlantUmlDeploymentParser.styleLineThicknessKey("note")), b(PlantUmlDeploymentParser.styleShadowingKey("note"))),
                "package" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_PACKAGE_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_PACKAGE_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_PACKAGE_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("package")), s(PlantUmlDeploymentParser.styleFontNameKey("package")), f(PlantUmlDeploymentParser.styleLineThicknessKey("package")), b(PlantUmlDeploymentParser.styleShadowingKey("package"))),
                "frame" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_FRAME_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_FRAME_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_FRAME_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("frame")), s(PlantUmlDeploymentParser.styleFontNameKey("frame")), f(PlantUmlDeploymentParser.styleLineThicknessKey("frame")), b(PlantUmlDeploymentParser.styleShadowingKey("frame"))),
                "cloud" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_CLOUD_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_CLOUD_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_CLOUD_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("cloud")), s(PlantUmlDeploymentParser.styleFontNameKey("cloud")), f(PlantUmlDeploymentParser.styleLineThicknessKey("cloud")), b(PlantUmlDeploymentParser.styleShadowingKey("cloud"))),
                "node" to ScopePalette(c(PlantUmlDeploymentParser.STYLE_NODE_FILL_KEY), c(PlantUmlDeploymentParser.STYLE_NODE_STROKE_KEY), c(PlantUmlDeploymentParser.STYLE_NODE_TEXT_KEY), f(PlantUmlDeploymentParser.styleFontSizeKey("node")), s(PlantUmlDeploymentParser.styleFontNameKey("node")), f(PlantUmlDeploymentParser.styleLineThicknessKey("node")), b(PlantUmlDeploymentParser.styleShadowingKey("node"))),
            ),
            edgeColor = c(PlantUmlDeploymentParser.STYLE_EDGE_COLOR_KEY),
        )
    }
}
