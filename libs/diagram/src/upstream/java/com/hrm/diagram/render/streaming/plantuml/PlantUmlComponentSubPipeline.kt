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
import com.hrm.diagram.core.ir.Direction
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatch
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.EdgeRoute
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.plantuml.PlantUmlComponentParser
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlComponentSubPipeline(
    private val textMeasurer: TextMeasurer,
    private val theme: DiagramTheme,
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

    private data class ComponentPalette(
        val scopes: Map<String, ScopePalette>,
        val edgeColor: ArgbColor?,
    )

    private val parser = PlantUmlComponentParser()
    private val resolvedColors = ThemeResolver.resolvePlantUmlComponent(theme)
    private val shadowTint = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private val nodeSizes: MutableMap<NodeId, Size> = HashMap()
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f, weight = 600)
    private val groupFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val iconFallbackFont = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600)
    private var currentPalette: ComponentPalette = ComponentPalette(emptyMap(), null)
    private var currentPortCountByHost: Map<NodeId, Int> = emptyMap()
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(172f, 72f),
        maxWidth = 180f,
        minWidth = 56f,
        minHeight = 32f,
        labelOf = ::labelTextOf,
        fontOf = { node -> scopedFont(currentPalette.scopes[node.payload[PlantUmlComponentParser.KIND_KEY]], labelFont) },
        customSizeOf = { node ->
            measureNodeSize(node, currentPalette, currentPortCountByHost).also { nodeSizes[node.id] = it }
        },
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.PLANTUML,
        defaultNodeSize = Size(172f, 72f),
        measurePolicy = measurePolicy,
        layoutModel = ::primaryLayoutIr,
        layoutOptions = { ir, isFinal ->
            LayoutOptions(
                direction = ir.styleHints.direction,
                incremental = !isFinal,
                allowGlobalReflow = isFinal,
                nodeSpacing = 64f,
                rankSpacing = 112f,
            )
        },
        postLayout = { ir, laid ->
            val spacedLaid = applyPrimaryClearance(ir, laid.copy(source = ir))
            val portsLaid = applyPortAnchors(ir, spacedLaid)
            val notesLaid = applyAnchoredNotes(ir, portsLaid)
            val routedLaid = routeDecoratedEdges(ir, notesLaid)
            withClusterRects(ir, routedLaid, currentPalette, routedLaid.seq)
        },
    )
    private val kernel = profile.kernel(
        textMeasurer = textMeasurer,
        renderEntities = { ir, laid ->
            val edgeLabelRects = layoutEdgeLabels(ir, laid)
            render(ir, laid, currentPalette, edgeLabelRects)
        },
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState {
        val rawIr = parser.snapshot()
        val palette = paletteOf(rawIr)
        currentPalette = palette
        val ir = applyPalette(rawIr, palette)
        currentPortCountByHost = ir.nodes
            .filter { it.payload[PlantUmlComponentParser.KIND_KEY] == "port" }
            .mapNotNull { it.payload[PlantUmlComponentParser.PORT_HOST_KEY]?.let(::NodeId) }
            .groupingBy { it }
            .eachCount()
        return kernel.advanceRendered(
            previousSnapshot = previousSnapshot,
            seq = seq,
            isFinal = isFinal,
            ir = ir,
            diagnostics = parser.diagnosticsSnapshot(),
        )
    }


    private fun primaryLayoutIr(ir: GraphIR): GraphIR {
        val hostByPort = ir.nodes
            .filter { it.payload[PlantUmlComponentParser.KIND_KEY] == "port" }
            .mapNotNull { port -> port.payload[PlantUmlComponentParser.PORT_HOST_KEY]?.let { port.id to NodeId(it) } }
            .toMap()
        val primaryNodeIds = ir.nodes
            .filterNot { isDecorationNode(it) }
            .map { it.id }
            .toSet()
        val primaryEdges = ArrayList<com.hrm.diagram.core.ir.Edge>()
        val seen = HashSet<Pair<NodeId, NodeId>>()
        for (edge in ir.edges) {
            if (edge.from !in primaryNodeIds && edge.from !in hostByPort) continue
            if (edge.to !in primaryNodeIds && edge.to !in hostByPort) continue
            val from = hostByPort[edge.from] ?: edge.from
            val to = hostByPort[edge.to] ?: edge.to
            if (from == to) continue
            val key = from to to
            if (!seen.add(key)) continue
            primaryEdges += edge.copy(from = from, to = to, label = null)
        }
        return ir.copy(
            nodes = ir.nodes.filter { it.id in primaryNodeIds },
            edges = primaryEdges,
        )
    }

    private fun isDecorationNode(node: Node): Boolean =
        node.payload[PlantUmlComponentParser.KIND_KEY] == "port" || isAnchoredNote(node)

    private fun isAnchoredNote(node: Node): Boolean =
        node.payload[PlantUmlComponentParser.KIND_KEY] == "note" &&
            node.payload[PlantUmlComponentParser.NOTE_TARGET_KEY] != null

    private fun measureNodeSize(node: Node, palette: ComponentPalette, portCountByHost: Map<NodeId, Int>): Size {
        val label = labelTextOf(node)
        val kind = node.payload[PlantUmlComponentParser.KIND_KEY]
        val scoped = palette.scopes[kind]
        val nodeFont = scopedFont(scoped, labelFont)
        val noteFont = scopedFont(scoped, labelFont)
        val portFont = scopedFont(scoped, iconFallbackFont)
        return when (kind) {
            "interface", "port" -> {
                val metrics = textMeasurer.measure(label, if (kind == "port") portFont else nodeFont, maxWidth = 120f)
                Size(
                    width = (metrics.width + 20f).coerceAtLeast(24f),
                    height = (metrics.height + 20f).coerceAtLeast(24f),
                )
            }
            "note" -> {
                val metrics = textMeasurer.measure(label, noteFont, maxWidth = 180f)
                Size(
                    width = (metrics.width + 30f).coerceAtLeast(120f),
                    height = (metrics.height + 24f).coerceAtLeast(54f),
                )
            }
            else -> {
                val metrics = textMeasurer.measure(label, nodeFont)
                val hasPorts = (portCountByHost[node.id] ?: 0) > 0
                Size(
                    width = (metrics.width + if (hasPorts) 64f else 36f).coerceAtLeast(if (hasPorts) 172f else 132f),
                    height = (metrics.height + if (hasPorts) 58f else 28f).coerceAtLeast(if (hasPorts) 88f else 56f),
                )
            }
        }
    }

    private fun withClusterRects(ir: GraphIR, laidOut: LaidOutDiagram, palette: ComponentPalette, seq: Long): LaidOutDiagram {
        val clusterRects = LinkedHashMap<NodeId, Rect>()
        for (cluster in ir.clusters) computeClusterRect(cluster, laidOut.nodePositions, clusterRects, palette)
        val bounds = computeBounds(laidOut.nodePositions.values + clusterRects.values)
        return laidOut.copy(clusterRects = clusterRects, bounds = bounds, seq = seq)
    }

    private fun computeClusterRect(
        cluster: Cluster,
        nodePositions: Map<NodeId, Rect>,
        out: LinkedHashMap<NodeId, Rect>,
        palette: ComponentPalette,
    ): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        inlineClusterHost(cluster.id)?.let { hostId -> nodePositions[hostId]?.let { childRects += it } }
        val nestedRects = cluster.nestedClusters.mapNotNull { computeClusterRect(it, nodePositions, out, palette) }
        childRects += nestedRects
        if (childRects.isEmpty()) return null
        val (kind, title) = parseClusterLabel(cluster)
        val headerText = clusterHeaderText(kind, title, cluster.id)
        val headerFont = clusterHeaderFont(palette.scopes[kind.lowercase()])
        val headerMetrics = textMeasurer.measure(headerText, headerFont)
        val headerBandHeight = headerMetrics.height + 28f
        val headerMinWidth = headerMetrics.width + 44f
        val left = childRects.minOf { it.left } - 20f
        val top = childRects.minOf { it.top } - headerBandHeight
        val naturalRight = childRects.maxOf { it.right } + 20f
        val right = naturalRight.coerceAtLeast(left + headerMinWidth)
        val bottom = childRects.maxOf { it.bottom } + 18f
        val rect = Rect.ltrb(left, top, right, bottom)
        out[cluster.id] = rect
        return rect
    }

    private fun clusterHeaderText(kind: String, title: String, id: NodeId): String =
        "${kind.uppercase()} ${title.ifBlank { id.value }}"

    private fun clusterHeaderFont(scoped: ScopePalette?): FontSpec {
        val base = scopedFont(scoped, groupFont)
        if (scoped?.fontSize != null) return base
        return base.copy(sizeSp = (base.sizeSp * 0.86f).coerceAtLeast(9f))
    }

    private fun inlineClusterHost(clusterId: NodeId): NodeId? =
        clusterId.value.takeIf { it.endsWith("__cluster") }?.removeSuffix("__cluster")?.let(::NodeId)

    private fun applyPrimaryClearance(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val minHorizontalGap = 76f
        val nodePositions = LinkedHashMap(laidOut.nodePositions)
        val primaryNodes = ir.nodes.filterNot { isDecorationNode(it) }
        val primaryIds = primaryNodes.map { it.id }.toSet()
        val parentByNode = primaryNodes.associate { it.id to it.payload[PlantUmlComponentParser.PARENT_KEY].orEmpty() }
        var changed: Boolean
        var pass = 0
        do {
            changed = false
            pass++
            val ordered = primaryIds.mapNotNull { id -> nodePositions[id]?.let { id to it } }
                .sortedWith(compareBy<Pair<NodeId, Rect>> { it.second.left }.thenBy { it.second.top })
            for (i in ordered.indices) {
                val (leftId, leftRect) = ordered[i]
                for (j in i + 1 until ordered.size) {
                    val (rightId, rightRect) = ordered[j]
                    if (parentByNode[leftId] != parentByNode[rightId]) continue
                    if (!leftRect.overlapsVertically(rightRect)) continue
                    val desiredLeft = leftRect.right + minHorizontalGap
                    if (rightRect.left >= desiredLeft) continue
                    nodePositions[rightId] = rightRect.shift(dx = desiredLeft - rightRect.left, dy = 0f)
                    changed = true
                }
            }
        } while (changed && pass < 8)
        val bounds = computeBounds(nodePositions.values + laidOut.clusterRects.values)
        return laidOut.copy(nodePositions = nodePositions, bounds = bounds)
    }

    private fun computeBounds(rects: Collection<Rect>): Rect {
        if (rects.isEmpty()) return Rect.ltrb(0f, 0f, 400f, 240f)
        val minLeft = rects.minOf { it.left }.coerceAtMost(0f)
        val minTop = rects.minOf { it.top }.coerceAtMost(0f)
        val maxRight = rects.maxOf { it.right }
        val maxBottom = rects.maxOf { it.bottom }
        return Rect.ltrb(minLeft, minTop, maxRight + 20f, maxBottom + 20f)
    }

    private fun render(
        ir: GraphIR,
        laidOut: LaidOutDiagram,
        palette: ComponentPalette,
        edgeLabelRects: Map<Int, Rect>,
    ): List<com.hrm.diagram.render.cache.DrawEntity> {
        val colors = resolvedColors
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        val bounds = laidOut.bounds
        out += DrawCommand.FillRect(Rect(Point(bounds.left, bounds.top), Size(bounds.size.width, bounds.size.height)), colors.canvas, z = 0)
        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, out, palette, colors)
        for (node in ir.nodes) drawNode(node, ir, laidOut, out, palette, colors)
        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            drawEdge(edge, route, edgeLabelRects[index], out, colors)
        }
        return out.entities()
    }

    private fun drawCluster(
        cluster: Cluster,
        clusterRects: Map<NodeId, Rect>,
        out: MutableList<DrawCommand>,
        palette: ComponentPalette,
        colors: com.hrm.diagram.render.theme.ResolvedPlantUmlComponentColors,
    ) {
        val rect = clusterRects[cluster.id] ?: return
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        val stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f, dash = listOf(7f, 5f))
        val scoped = palette.scopes[parseClusterLabel(cluster).first.lowercase()]
        if (scoped?.shadowing == true) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowTint,
                corner = 14f,
                z = 0,
            )
        }
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 14f, z = 0)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 14f, z = 1)

        val (kind, title) = parseClusterLabel(cluster)
        val textColor = palette.scopes[kind.lowercase()]?.text?.let { Color(it.argb) } ?: colors.clusterText
        val headerText = clusterHeaderText(kind, title, cluster.id)
        val headerFont = clusterHeaderFont(scoped)
        val headerMetrics = textMeasurer.measure(headerText, headerFont)
        val chipWidth = (headerMetrics.width + 22f).coerceAtMost(rect.size.width - 24f).coerceAtLeast(40f)
        val chipHeight = (headerMetrics.height + 10f).coerceAtLeast(22f)
        val chipRect = Rect.ltrb(rect.left + 12f, rect.top + 8f, rect.left + 12f + chipWidth, rect.top + 8f + chipHeight)
        out += DrawCommand.FillRect(rect = chipRect, color = colors.clusterChipFill, corner = 12f, z = 2)
        out += DrawCommand.StrokeRect(rect = chipRect, stroke = Stroke(width = 1f), color = strokeColor, corner = 12f, z = 3)
        out += DrawCommand.DrawText(
            text = headerText,
            origin = Point(chipRect.left + 10f, (chipRect.top + chipRect.bottom) / 2f),
            font = headerFont,
            color = textColor,
            maxWidth = headerMetrics.width + 2f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Middle,
            z = 4,
        )
        for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, out, palette, colors)
    }

    private fun drawNode(
        node: Node,
        ir: GraphIR,
        laidOut: LaidOutDiagram,
        out: MutableList<DrawCommand>,
        palette: ComponentPalette,
        colors: com.hrm.diagram.render.theme.ResolvedPlantUmlComponentColors,
    ) {
        val rect = laidOut.nodePositions[node.id] ?: return
        val defaultFill = if (node.payload[PlantUmlComponentParser.KIND_KEY] == "note") colors.noteFill else colors.nodeFill
        val defaultStroke = if (node.payload[PlantUmlComponentParser.KIND_KEY] == "note") colors.noteStroke else colors.nodeStroke
        val defaultText = if (node.payload[PlantUmlComponentParser.KIND_KEY] == "note") colors.noteText else colors.nodeText
        val fill = node.style.fill?.let { Color(it.argb) } ?: defaultFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: defaultStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: defaultText
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f)
        val label = labelTextOf(node)
        val kind = node.payload[PlantUmlComponentParser.KIND_KEY]
        val scoped = palette.scopes[kind]
        val mainFont = scopedFont(scoped, labelFont)
        val smallFont = scopedFont(scoped, iconFallbackFont)
        when (kind) {
            "interface", "port" -> {
                val w = minOf(rect.size.width, rect.size.height) / 2f
                if (scoped?.shadowing == true) {
                    out += DrawCommand.FillRect(
                        rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                        color = shadowTint,
                        corner = w,
                        z = 3,
                    )
                }
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = w, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = w, z = 5)
                val labelOrigin = portLabelOrigin(node, rect, laidOut)
                val labelAnchorX = portLabelAnchorX(node, laidOut)
                val labelAnchorY = portLabelAnchorY(node, laidOut)
                out += DrawCommand.DrawText(
                    text = label,
                    origin = labelOrigin,
                    font = if (kind == "port") smallFont else mainFont,
                    color = textColor,
                    maxWidth = 120f,
                    anchorX = labelAnchorX,
                    anchorY = labelAnchorY,
                    z = 6,
                )
                if (kind == "interface") {
                    out += DrawCommand.DrawText(
                        text = "I",
                        origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                        font = smallFont,
                        color = strokeColor,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Middle,
                        z = 6,
                    )
                }
            }
            "note" -> {
                drawNote(rect, fill, strokeColor, stroke, out, scoped?.shadowing == true)
                out += DrawCommand.DrawText(
                    text = label,
                    origin = Point(rect.left + 12f, rect.top + 10f),
                    font = mainFont,
                    color = textColor,
                    maxWidth = rect.size.width - 24f,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 7,
                )
            }
            else -> {
                val corner = when (node.shape) {
                    NodeShape.Component -> 10f
                    else -> 8f
                }
                if (scoped?.shadowing == true) {
                    out += DrawCommand.FillRect(
                        rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                        color = shadowTint,
                        corner = corner,
                        z = 3,
                    )
                }
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = corner, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = corner, z = 5)
                if (kind == "component") {
                    val iconRect = Rect.ltrb(rect.right - 22f, rect.top + 8f, rect.right - 8f, rect.top + 22f)
                    out += DrawCommand.StrokeRect(rect = iconRect, stroke = Stroke(width = 1f), color = strokeColor, corner = 2f, z = 6)
                    out += DrawCommand.StrokePath(
                        path = PathCmd(
                            listOf(
                                PathOp.MoveTo(Point(iconRect.left + 3f, iconRect.top + 5f)),
                                PathOp.LineTo(Point(iconRect.right - 3f, iconRect.top + 5f)),
                                PathOp.MoveTo(Point(iconRect.left + 3f, iconRect.top + 9f)),
                                PathOp.LineTo(Point(iconRect.right - 3f, iconRect.top + 9f)),
                            ),
                        ),
                        stroke = Stroke(width = 1f),
                        color = strokeColor,
                        z = 6,
                    )
                }
                val hasPorts = kind == "component" && ir.nodes.any {
                    it.payload[PlantUmlComponentParser.KIND_KEY] == "port" &&
                        it.payload[PlantUmlComponentParser.PORT_HOST_KEY] == node.id.value
                }
                val labelY = if (hasPorts) rect.top + 28f else (rect.top + rect.bottom) / 2f
                out += DrawCommand.DrawText(
                    text = label,
                    origin = Point((rect.left + rect.right) / 2f, labelY),
                    font = mainFont,
                    color = textColor,
                    maxWidth = rect.size.width - 20f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 7,
                )
                if (kind == "queue") {
                    out += DrawCommand.DrawText(
                        text = "Q",
                        origin = Point(rect.left + 12f, rect.top + 10f),
                        font = smallFont,
                        color = strokeColor,
                        anchorX = TextAnchorX.Start,
                        anchorY = TextAnchorY.Top,
                        z = 8,
                    )
                }
            }
        }
    }

    private fun applyPortAnchors(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val portNodes = ir.nodes.filter { it.payload[PlantUmlComponentParser.KIND_KEY] == "port" }
        if (portNodes.isEmpty()) return laidOut
        val nodePositions = LinkedHashMap(laidOut.nodePositions)
        val edgeRoutes = laidOut.edgeRoutes.toMutableList()
        val portsByHost = LinkedHashMap<NodeId, MutableList<Node>>()
        for (port in portNodes) {
            val host = port.payload[PlantUmlComponentParser.PORT_HOST_KEY]?.let(::NodeId) ?: continue
            if (host !in nodePositions) continue
            portsByHost.getOrPut(host) { ArrayList() } += port
        }
        for ((hostId, ports) in portsByHost) {
            val hostRect = nodePositions[hostId] ?: continue
            val grouped = ports.groupBy { resolvedPortSide(it, ir) }
            for ((side, sidePorts) in grouped) {
                val ordered = sidePorts.sortedBy { portSortKey(it, ir, side) }
                ordered.forEachIndexed { index, port ->
                    val size = nodePositions[port.id]?.size ?: nodeSizes[port.id] ?: Size(24f, 24f)
                    nodePositions[port.id] = anchoredPortRect(hostRect, size, side, index, ordered.size)
                }
            }
        }
        ir.edges.forEachIndexed { index, edge ->
            val route = routedEdgeForPorts(edge, ir, nodePositions) ?: return@forEachIndexed
            if (index < edgeRoutes.size) edgeRoutes[index] = route else edgeRoutes += route
        }
        val bounds = computeBounds(nodePositions.values + laidOut.clusterRects.values)
        return laidOut.copy(nodePositions = nodePositions, edgeRoutes = edgeRoutes, bounds = bounds)
    }

    private fun applyAnchoredNotes(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val noteNodes = ir.nodes.filter { it.payload[PlantUmlComponentParser.KIND_KEY] == "note" }
        if (noteNodes.isEmpty()) return laidOut
        val nodePositions = LinkedHashMap(laidOut.nodePositions)
        val edgeRoutes = laidOut.edgeRoutes.toMutableList()
        for (note in noteNodes) {
            val target = note.payload[PlantUmlComponentParser.NOTE_TARGET_KEY]?.let(::NodeId) ?: continue
            val placement = note.payload[PlantUmlComponentParser.NOTE_PLACEMENT_KEY].orEmpty()
            val targetRect = nodePositions[target] ?: continue
            val noteRect = nodePositions[note.id] ?: Rect(Point.Zero, nodeSizes[note.id] ?: Size(120f, 54f))
            val obstacles = noteObstacles(ir, note.id, target, nodePositions)
            val anchored = anchoredNoteRect(noteRect.size, targetRect, placement, obstacles)
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

    private fun routeDecoratedEdges(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val routes = ir.edges.mapNotNull { edge -> routedEdge(edge, ir, laidOut.nodePositions) }
        val bounds = computeBounds(laidOut.nodePositions.values + laidOut.clusterRects.values)
        return laidOut.copy(edgeRoutes = routes, bounds = bounds)
    }

    private fun routedEdge(
        edge: com.hrm.diagram.core.ir.Edge,
        ir: GraphIR,
        nodePositions: Map<NodeId, Rect>,
    ): EdgeRoute? {
        val fromNode = ir.nodes.firstOrNull { it.id == edge.from } ?: return null
        val toNode = ir.nodes.firstOrNull { it.id == edge.to } ?: return null
        val fromRect = nodePositions[edge.from] ?: return null
        val toRect = nodePositions[edge.to] ?: return null
        if (fromNode.payload[PlantUmlComponentParser.KIND_KEY] == "note" && toNode.id == edge.to) {
            val placement = fromNode.payload[PlantUmlComponentParser.NOTE_PLACEMENT_KEY].orEmpty()
            return EdgeRoute(edge.from, edge.to, anchoredNoteRoute(fromRect, toRect, placement), RouteKind.Polyline)
        }
        val routedForPort = routedEdgeForPorts(edge, ir, nodePositions)
        if (routedForPort != null) return routedForPort
        val from = boundaryPoint(fromRect, centerOf(toRect))
        val to = boundaryPoint(toRect, centerOf(fromRect))
        return EdgeRoute(
            from = edge.from,
            to = edge.to,
            points = orthogonalRoute(from, to),
            kind = RouteKind.Polyline,
        )
    }

    private fun noteObstacles(ir: GraphIR, noteId: NodeId, targetId: NodeId, nodePositions: Map<NodeId, Rect>): List<Rect> {
        val noteNode = ir.nodes.firstOrNull { it.id == noteId }
        val noteParent = noteNode?.payload?.get(PlantUmlComponentParser.PARENT_KEY)
        val targetNode = ir.nodes.firstOrNull { it.id == targetId }
        val targetParent = targetNode?.payload?.get(PlantUmlComponentParser.PARENT_KEY)
        return ir.nodes
            .filter { node ->
                node.id != noteId &&
                    node.id != targetId &&
                    node.payload[PlantUmlComponentParser.KIND_KEY] != "port" &&
                    (
                        node.payload[PlantUmlComponentParser.PARENT_KEY] == noteParent ||
                            node.payload[PlantUmlComponentParser.PARENT_KEY] == targetParent
                        )
            }
            .mapNotNull { nodePositions[it.id] }
    }

    private fun anchoredNoteRect(size: Size, targetRect: Rect, placement: String, obstacles: List<Rect>): Rect {
        val gap = 18f
        val base = when (placement.lowercase()) {
            "left" -> Rect(Point(targetRect.left - size.width - gap, targetRect.top + (targetRect.size.height - size.height) / 2f), size)
            "top" -> Rect(Point(targetRect.left + (targetRect.size.width - size.width) / 2f, targetRect.top - size.height - gap), size)
            "bottom" -> Rect(Point(targetRect.left + (targetRect.size.width - size.width) / 2f, targetRect.bottom + gap), size)
            else -> Rect(Point(targetRect.right + gap, targetRect.top + (targetRect.size.height - size.height) / 2f), size)
        }
        return avoidNoteObstacles(base, placement, obstacles, gap)
    }

    private fun avoidNoteObstacles(base: Rect, placement: String, obstacles: List<Rect>, gap: Float): Rect {
        if (obstacles.none { base.overlaps(it) }) return base
        return when (placement.lowercase()) {
            "left" -> {
                val blockers = obstacles.filter { base.overlapsVertically(it) && it.left < base.right }
                val left = blockers.minOfOrNull { it.left }?.let { it - base.size.width - gap } ?: base.left
                Rect(Point(left, base.top), base.size)
            }
            "top" -> {
                val blockers = obstacles.filter { base.overlapsHorizontally(it) && it.top < base.bottom }
                val top = blockers.minOfOrNull { it.top }?.let { it - base.size.height - gap } ?: base.top
                Rect(Point(base.left, top), base.size)
            }
            "bottom" -> {
                val blockers = obstacles.filter { base.overlapsHorizontally(it) && it.bottom > base.top }
                val top = blockers.maxOfOrNull { it.bottom }?.let { it + gap } ?: base.top
                Rect(Point(base.left, top), base.size)
            }
            else -> {
                val blockers = obstacles.filter { base.overlapsVertically(it) && it.right > base.left }
                val left = blockers.maxOfOrNull { it.right }?.let { it + gap } ?: base.left
                Rect(Point(left, base.top), base.size)
            }
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

    private fun anchoredPortRect(hostRect: Rect, size: Size, side: PortSide, index: Int, total: Int): Rect {
        val center = portCenter(hostRect, side, index, total)
        return Rect(
            origin = Point(center.x - size.width / 2f, center.y - size.height / 2f),
            size = size,
        )
    }

    private fun portCenter(hostRect: Rect, side: PortSide, index: Int, total: Int): Point {
        val t = (index + 1f) / (total + 1f)
        return when (side) {
            PortSide.Left -> Point(hostRect.left, hostRect.top + hostRect.size.height * t)
            PortSide.Right -> Point(hostRect.right, hostRect.top + hostRect.size.height * t)
            PortSide.Top -> Point(hostRect.left + hostRect.size.width * t, hostRect.top)
            PortSide.Bottom -> Point(hostRect.left + hostRect.size.width * t, hostRect.bottom)
        }
    }

    private fun resolvedPortSide(node: Node, ir: GraphIR): PortSide {
        val explicit = node.payload[PlantUmlComponentParser.PORT_DIR_KEY]
        val direction = ir.styleHints.direction ?: Direction.LR
        if (explicit == "in") return inputSide(direction)
        if (explicit == "out") return outputSide(direction)
        val incoming = ir.edges.count { it.to == node.id }
        val outgoing = ir.edges.count { it.from == node.id }
        return when {
            outgoing > incoming -> outputSide(direction)
            incoming > outgoing -> inputSide(direction)
            else -> inputSide(direction)
        }
    }

    private fun inputSide(direction: Direction): PortSide = when (direction) {
        Direction.LR -> PortSide.Left
        Direction.RL -> PortSide.Right
        Direction.TB -> PortSide.Top
        Direction.BT -> PortSide.Bottom
        Direction.RADIAL -> PortSide.Left
    }

    private fun outputSide(direction: Direction): PortSide = when (direction) {
        Direction.LR -> PortSide.Right
        Direction.RL -> PortSide.Left
        Direction.TB -> PortSide.Bottom
        Direction.BT -> PortSide.Top
        Direction.RADIAL -> PortSide.Right
    }

    private fun portSortKey(node: Node, ir: GraphIR, side: PortSide): Int {
        val degree = ir.edges.count { it.from == node.id || it.to == node.id }
        val bias = when (side) {
            PortSide.Left, PortSide.Top -> 0
            PortSide.Right, PortSide.Bottom -> 1000
        }
        return bias + degree
    }

    private fun routedEdgeForPorts(edge: com.hrm.diagram.core.ir.Edge, ir: GraphIR, nodePositions: Map<NodeId, Rect>): EdgeRoute? {
        val fromNode = ir.nodes.firstOrNull { it.id == edge.from } ?: return null
        val toNode = ir.nodes.firstOrNull { it.id == edge.to } ?: return null
        val fromRect = nodePositions[edge.from] ?: return null
        val toRect = nodePositions[edge.to] ?: return null
        val fromIsPort = fromNode.payload[PlantUmlComponentParser.KIND_KEY] == "port"
        val toIsPort = toNode.payload[PlantUmlComponentParser.KIND_KEY] == "port"
        if (!fromIsPort && !toIsPort) return null
        val fromPoint = if (fromIsPort) centerOf(fromRect) else boundaryPoint(fromRect, centerOf(toRect))
        val toPoint = if (toIsPort) centerOf(toRect) else boundaryPoint(toRect, centerOf(fromRect))
        return EdgeRoute(
            from = edge.from,
            to = edge.to,
            points = orthogonalRoute(fromPoint, toPoint),
            kind = RouteKind.Polyline,
        )
    }

    private fun orthogonalRoute(from: Point, to: Point): List<Point> {
        if (kotlin.math.abs(from.x - to.x) < 1f || kotlin.math.abs(from.y - to.y) < 1f) {
            return listOf(from, to)
        }
        val midX = (from.x + to.x) / 2f
        return listOf(from, Point(midX, from.y), Point(midX, to.y), to)
    }

    private fun centerOf(rect: Rect): Point = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f)

    private fun Rect.overlaps(other: Rect): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

    private fun Rect.overlapsHorizontally(other: Rect): Boolean =
        left < other.right && right > other.left

    private fun Rect.overlapsVertically(other: Rect): Boolean =
        top < other.bottom && bottom > other.top

    private fun Rect.shift(dx: Float, dy: Float): Rect =
        Rect.ltrb(left + dx, top + dy, right + dx, bottom + dy)

    private fun Rect.expand(dx: Float, dy: Float): Rect =
        Rect.ltrb(left - dx, top - dy, right + dx, bottom + dy)

    private fun boundaryPoint(rect: Rect, toward: Point): Point {
        val center = centerOf(rect)
        val dx = toward.x - center.x
        val dy = toward.y - center.y
        val halfW = rect.size.width / 2f
        val halfH = rect.size.height / 2f
        if (dx == 0f && dy == 0f) return center
        val scaleX = if (dx != 0f) halfW / kotlin.math.abs(dx) else Float.POSITIVE_INFINITY
        val scaleY = if (dy != 0f) halfH / kotlin.math.abs(dy) else Float.POSITIVE_INFINITY
        val scale = minOf(scaleX, scaleY)
        return Point(center.x + dx * scale, center.y + dy * scale)
    }

    private fun portLabelOrigin(node: Node, rect: Rect, laidOut: LaidOutDiagram): Point {
        val side = portSideForNode(node, laidOut) ?: return Point((rect.left + rect.right) / 2f, rect.bottom + 12f)
        return when (side) {
            PortSide.Left -> Point(rect.left - 8f, (rect.top + rect.bottom) / 2f)
            PortSide.Right -> Point(rect.right + 8f, (rect.top + rect.bottom) / 2f)
            PortSide.Top -> Point((rect.left + rect.right) / 2f, rect.top - 8f)
            PortSide.Bottom -> Point((rect.left + rect.right) / 2f, rect.bottom + 8f)
        }
    }

    private fun portLabelAnchorX(node: Node, laidOut: LaidOutDiagram): TextAnchorX =
        when (portSideForNode(node, laidOut)) {
            PortSide.Left -> TextAnchorX.End
            PortSide.Right -> TextAnchorX.Start
            else -> TextAnchorX.Center
        }

    private fun portLabelAnchorY(node: Node, laidOut: LaidOutDiagram): TextAnchorY =
        when (portSideForNode(node, laidOut)) {
            PortSide.Top -> TextAnchorY.Bottom
            PortSide.Bottom -> TextAnchorY.Top
            else -> TextAnchorY.Middle
        }

    private fun portSideForNode(node: Node, laidOut: LaidOutDiagram): PortSide? {
        val hostId = node.payload[PlantUmlComponentParser.PORT_HOST_KEY]?.let(::NodeId) ?: return null
        val rect = laidOut.nodePositions[node.id] ?: return null
        val hostRect = laidOut.nodePositions[hostId] ?: return null
        val center = centerOf(rect)
        val dxLeft = kotlin.math.abs(center.x - hostRect.left)
        val dxRight = kotlin.math.abs(center.x - hostRect.right)
        val dyTop = kotlin.math.abs(center.y - hostRect.top)
        val dyBottom = kotlin.math.abs(center.y - hostRect.bottom)
        val min = minOf(dxLeft, dxRight, dyTop, dyBottom)
        return when (min) {
            dxLeft -> PortSide.Left
            dxRight -> PortSide.Right
            dyTop -> PortSide.Top
            else -> PortSide.Bottom
        }
    }

    private fun drawEdge(
        edge: com.hrm.diagram.core.ir.Edge,
        route: com.hrm.diagram.layout.EdgeRoute,
        labelRect: Rect?,
        out: MutableList<DrawCommand>,
        colors: com.hrm.diagram.render.theme.ResolvedPlantUmlComponentColors,
    ) {
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
        val edgeColor = edge.style.color?.let { Color(it.argb) } ?: colors.edgeColor
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
        val text = (edge.label as? RichLabel.Plain)?.text ?: return
        if (text.isEmpty()) return
        val rect = labelRect ?: edgeLabelRect(text, route).second
        out += DrawCommand.DrawText(
            text = text,
            origin = Point(rect.left + rect.size.width / 2f, rect.top + rect.size.height / 2f),
            font = edgeLabelFont,
            color = colors.edgeLabelText,
            maxWidth = rect.size.width,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 5,
        )
    }

    private fun layoutEdgeLabels(ir: GraphIR, laidOut: LaidOutDiagram): Map<Int, Rect> {
        val occupied = ArrayList<Rect>()
        occupied += laidOut.nodePositions.values.map { it.expand(8f, 6f) }
        occupied += portLabelRects(ir, laidOut)
        val result = LinkedHashMap<Int, Rect>()
        for ((index, edge) in ir.edges.withIndex()) {
            val label = (edge.label as? RichLabel.Plain)?.text?.takeIf { it.isNotBlank() } ?: continue
            val route = laidOut.edgeRoutes.getOrNull(index) ?: continue
            val candidates = edgeLabelCandidates(label, route)
            val chosen = candidates.firstOrNull { candidate -> occupied.none { it.overlaps(candidate) } }
                ?: candidates.minByOrNull { candidate -> occupied.count { it.overlaps(candidate) } }
                ?: edgeLabelRect(label, route).second
            result[index] = chosen
            occupied += chosen.expand(8f, 4f)
        }
        return result
    }

    private fun edgeLabelCandidates(text: String, route: EdgeRoute): List<Rect> {
        val (size, base) = edgeLabelRect(text, route)
        val candidates = ArrayList<Rect>()
        val (a, b) = semanticLabelSegment(route)
        val horizontal = kotlin.math.abs(b.x - a.x) >= kotlin.math.abs(b.y - a.y)
        val offsets = listOf(14f, 24f, 36f)
        val positions = listOf(0.5f, 0.42f, 0.58f)
        for (offset in offsets) {
            for (t in positions) {
                val p = Point(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
                if (horizontal) {
                    candidates += Rect(Point(p.x - size.width / 2f, p.y - size.height - offset), size)
                    candidates += Rect(Point(p.x - size.width / 2f, p.y + offset), size)
                } else {
                    candidates += Rect(Point(p.x + offset, p.y - size.height / 2f), size)
                    candidates += Rect(Point(p.x - size.width - offset, p.y - size.height / 2f), size)
                }
            }
        }
        candidates += base
        return candidates
    }

    private fun semanticLabelSegment(route: EdgeRoute): Pair<Point, Point> {
        val segments = route.points.zipWithNext()
        if (segments.isEmpty()) return route.points.first() to route.points.first()
        val overallHorizontal = kotlin.math.abs(route.points.last().x - route.points.first().x) >=
            kotlin.math.abs(route.points.last().y - route.points.first().y)
        return segments
            .filter { (a, b) ->
                val horizontal = kotlin.math.abs(b.x - a.x) >= kotlin.math.abs(b.y - a.y)
                horizontal == overallHorizontal
            }
            .maxByOrNull { (a, b) -> segmentLength(a, b) }
            ?: segments.maxBy { (a, b) -> segmentLength(a, b) }
    }

    private fun segmentLength(a: Point, b: Point): Float =
        kotlin.math.abs(b.x - a.x) + kotlin.math.abs(b.y - a.y)

    private fun edgeLabelRect(text: String, route: EdgeRoute): Pair<Size, Rect> {
        val metrics = textMeasurer.measure(text, edgeLabelFont)
        val size = Size(metrics.width + 10f, metrics.height + 4f)
        val pts = route.points
        val a = pts.first()
        val b = pts.last()
        val center = Point((a.x + b.x) / 2f, (a.y + b.y) / 2f)
        return size to Rect(Point(center.x - size.width / 2f, center.y - size.height - 12f), size)
    }

    private fun portLabelRects(ir: GraphIR, laidOut: LaidOutDiagram): List<Rect> =
        ir.nodes.filter { it.payload[PlantUmlComponentParser.KIND_KEY] == "port" }
            .mapNotNull { port ->
                val rect = laidOut.nodePositions[port.id] ?: return@mapNotNull null
                val label = labelTextOf(port)
                val metrics = textMeasurer.measure(label, iconFallbackFont)
                val size = Size(metrics.width + 8f, metrics.height + 4f)
                val origin = portLabelOrigin(port, rect, laidOut)
                when (portLabelAnchorX(port, laidOut)) {
                    TextAnchorX.End -> Rect(Point(origin.x - size.width, origin.y - size.height / 2f), size)
                    TextAnchorX.Center -> Rect(Point(origin.x - size.width / 2f, origin.y - size.height / 2f), size)
                    else -> Rect(Point(origin.x, origin.y - size.height / 2f), size)
                }
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

    private fun drawNote(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>, shadowing: Boolean) {
        if (shadowing) {
            out += DrawCommand.FillRect(
                rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                color = shadowTint,
                corner = 8f,
                z = 3,
            )
        }
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 8f, z = 4)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 8f, z = 5)
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
            z = 6,
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

    private fun applyPalette(ir: GraphIR, palette: ComponentPalette): GraphIR {
        val nodes = ir.nodes.map { node ->
            val kind = node.payload[PlantUmlComponentParser.KIND_KEY]
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

    private fun applyClusterPalette(cluster: Cluster, palette: ComponentPalette): Cluster {
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

    private fun paletteOf(ir: GraphIR): ComponentPalette {
        val extras = ir.styleHints.extras
        fun c(key: String): ArgbColor? =
            extras[key]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) }
        fun f(key: String): Float? = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras[key])
        fun s(key: String): String? = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(extras[key])
        fun b(key: String): Boolean? = PlantUmlTreeRenderSupport.parsePlantUmlBoolean(extras[key])
        return ComponentPalette(
            scopes = mapOf(
                "component" to ScopePalette(c(PlantUmlComponentParser.STYLE_COMPONENT_FILL_KEY), c(PlantUmlComponentParser.STYLE_COMPONENT_STROKE_KEY), c(PlantUmlComponentParser.STYLE_COMPONENT_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("component")), s(PlantUmlComponentParser.styleFontNameKey("component")), f(PlantUmlComponentParser.styleLineThicknessKey("component")), b(PlantUmlComponentParser.styleShadowingKey("component"))),
                "interface" to ScopePalette(c(PlantUmlComponentParser.STYLE_INTERFACE_FILL_KEY), c(PlantUmlComponentParser.STYLE_INTERFACE_STROKE_KEY), c(PlantUmlComponentParser.STYLE_INTERFACE_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("interface")), s(PlantUmlComponentParser.styleFontNameKey("interface")), f(PlantUmlComponentParser.styleLineThicknessKey("interface")), b(PlantUmlComponentParser.styleShadowingKey("interface"))),
                "port" to ScopePalette(c(PlantUmlComponentParser.STYLE_PORT_FILL_KEY), c(PlantUmlComponentParser.STYLE_PORT_STROKE_KEY), c(PlantUmlComponentParser.STYLE_PORT_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("port")), s(PlantUmlComponentParser.styleFontNameKey("port")), f(PlantUmlComponentParser.styleLineThicknessKey("port")), b(PlantUmlComponentParser.styleShadowingKey("port"))),
                "database" to ScopePalette(c(PlantUmlComponentParser.STYLE_DATABASE_FILL_KEY), c(PlantUmlComponentParser.STYLE_DATABASE_STROKE_KEY), c(PlantUmlComponentParser.STYLE_DATABASE_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("database")), s(PlantUmlComponentParser.styleFontNameKey("database")), f(PlantUmlComponentParser.styleLineThicknessKey("database")), b(PlantUmlComponentParser.styleShadowingKey("database"))),
                "queue" to ScopePalette(c(PlantUmlComponentParser.STYLE_QUEUE_FILL_KEY), c(PlantUmlComponentParser.STYLE_QUEUE_STROKE_KEY), c(PlantUmlComponentParser.STYLE_QUEUE_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("queue")), s(PlantUmlComponentParser.styleFontNameKey("queue")), f(PlantUmlComponentParser.styleLineThicknessKey("queue")), b(PlantUmlComponentParser.styleShadowingKey("queue"))),
                "note" to ScopePalette(c(PlantUmlComponentParser.STYLE_NOTE_FILL_KEY), c(PlantUmlComponentParser.STYLE_NOTE_STROKE_KEY), c(PlantUmlComponentParser.STYLE_NOTE_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("note")), s(PlantUmlComponentParser.styleFontNameKey("note")), f(PlantUmlComponentParser.styleLineThicknessKey("note")), b(PlantUmlComponentParser.styleShadowingKey("note"))),
                "package" to ScopePalette(c(PlantUmlComponentParser.STYLE_PACKAGE_FILL_KEY), c(PlantUmlComponentParser.STYLE_PACKAGE_STROKE_KEY), c(PlantUmlComponentParser.STYLE_PACKAGE_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("package")), s(PlantUmlComponentParser.styleFontNameKey("package")), f(PlantUmlComponentParser.styleLineThicknessKey("package")), b(PlantUmlComponentParser.styleShadowingKey("package"))),
                "rectangle" to ScopePalette(c(PlantUmlComponentParser.STYLE_RECTANGLE_FILL_KEY), c(PlantUmlComponentParser.STYLE_RECTANGLE_STROKE_KEY), c(PlantUmlComponentParser.STYLE_RECTANGLE_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("rectangle")), s(PlantUmlComponentParser.styleFontNameKey("rectangle")), f(PlantUmlComponentParser.styleLineThicknessKey("rectangle")), b(PlantUmlComponentParser.styleShadowingKey("rectangle"))),
                "frame" to ScopePalette(c(PlantUmlComponentParser.STYLE_FRAME_FILL_KEY), c(PlantUmlComponentParser.STYLE_FRAME_STROKE_KEY), c(PlantUmlComponentParser.STYLE_FRAME_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("frame")), s(PlantUmlComponentParser.styleFontNameKey("frame")), f(PlantUmlComponentParser.styleLineThicknessKey("frame")), b(PlantUmlComponentParser.styleShadowingKey("frame"))),
                "cloud" to ScopePalette(c(PlantUmlComponentParser.STYLE_CLOUD_FILL_KEY), c(PlantUmlComponentParser.STYLE_CLOUD_STROKE_KEY), c(PlantUmlComponentParser.STYLE_CLOUD_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("cloud")), s(PlantUmlComponentParser.styleFontNameKey("cloud")), f(PlantUmlComponentParser.styleLineThicknessKey("cloud")), b(PlantUmlComponentParser.styleShadowingKey("cloud"))),
                "node" to ScopePalette(c(PlantUmlComponentParser.STYLE_NODE_FILL_KEY), c(PlantUmlComponentParser.STYLE_NODE_STROKE_KEY), c(PlantUmlComponentParser.STYLE_NODE_TEXT_KEY), f(PlantUmlComponentParser.styleFontSizeKey("node")), s(PlantUmlComponentParser.styleFontNameKey("node")), f(PlantUmlComponentParser.styleLineThicknessKey("node")), b(PlantUmlComponentParser.styleShadowingKey("node"))),
            ),
            edgeColor = c(PlantUmlComponentParser.STYLE_EDGE_COLOR_KEY),
        )
    }

    private fun scopedFont(scope: ScopePalette?, base: FontSpec): FontSpec =
        PlantUmlTreeRenderSupport.resolveFontSpec(base, scope?.fontName, scope?.fontSize?.toString())

    override fun dispose() {
        nodeSizes.clear()
        currentPalette = ComponentPalette(emptyMap(), null)
        currentPortCountByHost = emptyMap()
        kernel.clear()
    }

    private enum class PortSide { Left, Right, Top, Bottom }
}
