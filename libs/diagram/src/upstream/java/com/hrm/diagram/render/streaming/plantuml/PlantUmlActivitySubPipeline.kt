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
import com.hrm.diagram.core.ir.ActivityBlock
import com.hrm.diagram.core.ir.ActivityIR
import com.hrm.diagram.core.ir.ArgbColor
import com.hrm.diagram.core.ir.ArrowEnds
import com.hrm.diagram.core.ir.Cluster
import com.hrm.diagram.core.ir.ClusterStyle
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.EdgeKind
import com.hrm.diagram.core.ir.EdgeStyle
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.NodeStyle
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.IncrementalLayout
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.layout.sugiyama.SugiyamaLayouts
import com.hrm.diagram.parser.plantuml.PlantUmlActivityParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlActivitySubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    companion object {
        private const val LANE_KEY = "plantuml.activity.lane"
        private const val LANE_FILL_KEY = "plantuml.activity.lane.fill"
        private const val ACTION_FILL_KEY = "plantuml.activity.action.fill"
    }

    private val parser = PlantUmlActivityParser()
    private val colors = ThemeResolver.resolvePlantUmlActivity(theme)
    private val stopFill = theme.colors.canvas
    private val shadowTint = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private var cachedPaletteExtras: Map<String, String> = emptyMap()
    private var cachedPalette: ActivityPalette = paletteOf(emptyMap())
    private val nodeSizes: MutableMap<NodeId, Size> = HashMap()
    private val layout: IncrementalLayout<GraphIR> = SugiyamaLayouts.forGraph(
        defaultNodeSize = Size(160f, 72f),
        nodeSizeOf = { id -> nodeSizes[id] ?: Size(160f, 72f) },
    )
    private val kernel = PlantUmlRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layoutModel = ::lower,
        beforeLayout = { model, lowered, _ -> measureNodes(lowered, resolvePalette(model)) },
        layout = { previous, lowered, options, _, _ -> layout.layout(previous, lowered, options) },
        postLayout = { _, lowered, baseLaid, seq, _ ->
            val clusterRects = LinkedHashMap<NodeId, Rect>()
            for (cluster in lowered.clusters) computeClusterRect(cluster, baseLaid.nodePositions, clusterRects)
            val bounds = computeBounds(baseLaid.nodePositions.values + clusterRects.values)
            baseLaid.copy(clusterRects = clusterRects, bounds = bounds, seq = seq)
        },
        renderEntities = { model, lowered, laidOut, _ -> render(lowered, laidOut, resolvePalette(model)) },
        layoutOptions = { _, lowered, isFinal ->
            LayoutOptions(direction = lowered.styleHints.direction, incremental = !isFinal, allowGlobalReflow = isFinal)
        },
    )
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private var idCounter: Int = 0

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    override fun dispose() {
        nodeSizes.clear()
    }

    private data class BuildResult(val entry: NodeId?, val exits: List<NodeId>)
    private data class LaneMarker(val lane: String, val fill: ArgbColor?)
    private data class LaneState(val name: String, val fill: ArgbColor?)
    private data class PendingEdgeLabel(val text: String)
    private data class PendingActionStyle(val fill: ArgbColor?)
    private data class SyncBarMarker(val label: String)
    private data class PendingSourceRef(val refKey: String)
    private data class PendingTargetRef(val refKey: String)
    private data class PendingNodeRef(val refKey: String)
    private data class PendingStopRef(val refKey: String)
    private data class ActivityPalette(
        val actionFill: ArgbColor?,
        val actionStroke: ArgbColor?,
        val actionText: ArgbColor?,
        val actionFontSize: Float?,
        val actionFontName: String?,
        val actionLineThickness: Float?,
        val actionShadowing: Boolean?,
        val decisionFill: ArgbColor?,
        val decisionStroke: ArgbColor?,
        val decisionText: ArgbColor?,
        val decisionFontSize: Float?,
        val decisionFontName: String?,
        val decisionLineThickness: Float?,
        val decisionShadowing: Boolean?,
        val noteFill: ArgbColor?,
        val noteStroke: ArgbColor?,
        val noteText: ArgbColor?,
        val noteFontSize: Float?,
        val noteFontName: String?,
        val noteLineThickness: Float?,
        val noteShadowing: Boolean?,
        val barFill: ArgbColor?,
        val barText: ArgbColor?,
        val barFontSize: Float?,
        val barFontName: String?,
        val barLineThickness: Float?,
        val barShadowing: Boolean?,
        val startFill: ArgbColor?,
        val stopStroke: ArgbColor?,
        val startLineThickness: Float?,
        val stopLineThickness: Float?,
        val startShadowing: Boolean?,
        val stopShadowing: Boolean?,
        val edgeColor: ArgbColor?,
    )

    private fun lower(ir: ActivityIR): GraphIR {
        idCounter = 0
        val nodes = ArrayList<Node>()
        val edges = ArrayList<Edge>()
        val refMap = LinkedHashMap<String, NodeId>()
        val stopRefs = LinkedHashSet<String>()
        val palette = resolvePalette(ir)
        val sequence = buildSequence(ir.blocks, nodes, edges, lane = null, palette = palette, refMap = refMap, stopRefs = stopRefs)
        val hasStart = ir.styleHints.extras[PlantUmlActivityParser.HAS_START_KEY] == "true"
        val hasStop = ir.styleHints.extras[PlantUmlActivityParser.HAS_STOP_KEY] == "true"
        if (hasStart) {
            val start = makeNode("start", "Start", NodeShape.StartCircle, style(start = true, palette = palette))
            nodes += start
            sequence.entry?.let { edges += solidEdge(start.id, it, palette = palette) }
        }
        if (hasStop) {
            val stop = makeNode("stop", "Stop", NodeShape.EndCircle, style(stop = true, palette = palette))
            nodes += stop
            for (exit in sequence.exits) edges += solidEdge(exit, stop.id, palette = palette)
            for (ref in stopRefs) {
                refMap[ref]?.let { source -> edges += solidEdge(source, stop.id, palette = palette) }
            }
        }
        val clusters = buildLaneClusters(nodes)
        return GraphIR(
            nodes = nodes,
            edges = edges,
            clusters = clusters,
            sourceLanguage = SourceLanguage.PLANTUML,
            styleHints = ir.styleHints,
        )
    }

    private fun buildSequence(
        blocks: List<ActivityBlock>,
        nodes: MutableList<Node>,
        edges: MutableList<Edge>,
        lane: String?,
        palette: ActivityPalette,
        refMap: MutableMap<String, NodeId>,
        stopRefs: MutableSet<String>,
    ): BuildResult {
        var entry: NodeId? = null
        var pendingExits = emptyList<NodeId>()
        var currentLane = lane?.let { LaneState(it, fill = null) }
        var pendingEdgeLabel: String? = null
        var pendingActionStyle: PendingActionStyle? = null
        var pendingSourceRef: String? = null
        val pendingNodeRefs = LinkedHashSet<String>()
        fun currentSources(): List<NodeId> =
            pendingSourceRef?.let(refMap::get)?.let(::listOf) ?: pendingExits
        for (block in blocks) {
            val marker = asLaneMarker(block)
            if (marker != null) {
                currentLane = LaneState(marker.lane, marker.fill)
                continue
            }
            val edgeLabel = asEdgeLabelMarker(block)
            if (edgeLabel != null) {
                pendingEdgeLabel = edgeLabel.text
                continue
            }
            val actionStyle = asActionStyleMarker(block)
            if (actionStyle != null) {
                pendingActionStyle = actionStyle
                continue
            }
            val sourceRef = asSourceRefMarker(block)
            if (sourceRef != null) {
                pendingSourceRef = sourceRef.refKey
                continue
            }
            val targetRef = asTargetRefMarker(block)
            if (targetRef != null) {
                val targetNode = refMap[targetRef.refKey]
                if (targetNode != null) {
                    if (entry == null) entry = targetNode
                    for (exit in currentSources()) edges += solidEdge(exit, targetNode, pendingEdgeLabel, palette)
                    pendingExits = listOf(targetNode)
                    pendingEdgeLabel = null
                    pendingActionStyle = null
                    pendingSourceRef = null
                }
                continue
            }
            val nodeRef = asNodeRefMarker(block)
            if (nodeRef != null) {
                pendingNodeRefs += nodeRef.refKey
                continue
            }
            val stopRef = asStopRefMarker(block)
            if (stopRef != null) {
                stopRefs += stopRef.refKey
                continue
            }
            val syncBar = asSyncBarMarker(block)
            val built = if (syncBar != null) {
                buildSyncBar(syncBar, nodes, lane = currentLane, palette = palette)
            } else {
                buildBlock(block, nodes, edges, lane = currentLane, actionStyle = pendingActionStyle, palette = palette, refMap = refMap, stopRefs = stopRefs)
            }
            if (built.entry != null) {
                if (entry == null) entry = built.entry
                for (exit in currentSources()) edges += solidEdge(exit, built.entry, pendingEdgeLabel, palette)
                pendingExits = built.exits
                for (ref in pendingNodeRefs) refMap[ref] = built.entry
                pendingNodeRefs.clear()
                pendingEdgeLabel = null
                pendingActionStyle = null
                pendingSourceRef = null
            }
        }
        return BuildResult(entry, pendingExits)
    }

    private fun buildSyncBar(
        marker: SyncBarMarker,
        nodes: MutableList<Node>,
        lane: LaneState?,
        palette: ActivityPalette,
    ): BuildResult {
        val id = nextId("sync")
        nodes += Node(
            id = id,
            label = marker.label.takeIf { it.isNotEmpty() }?.let(RichLabel::Plain) ?: RichLabel.Empty,
            shape = NodeShape.ForkBar,
            style = style(fork = true, palette = palette),
            payload = lanePayload(lane),
        )
        return BuildResult(id, listOf(id))
    }

    private fun buildBlock(
        block: ActivityBlock,
        nodes: MutableList<Node>,
        edges: MutableList<Edge>,
        lane: LaneState?,
        actionStyle: PendingActionStyle?,
        palette: ActivityPalette,
        refMap: MutableMap<String, NodeId>,
        stopRefs: MutableSet<String>,
    ): BuildResult = when (block) {
        is ActivityBlock.Action -> {
            val id = nextId("action")
            nodes += Node(
                id,
                block.label,
                NodeShape.RoundedBox,
                style(action = true, fillOverride = actionStyle?.fill, palette = palette),
                payload = lanePayload(lane),
            )
            BuildResult(id, listOf(id))
        }
        is ActivityBlock.Note -> {
            val id = nextId("note")
            nodes += Node(id, block.text, NodeShape.Note, style(note = true, palette = palette), payload = lanePayload(lane))
            BuildResult(id, listOf(id))
        }
        is ActivityBlock.IfElse -> {
            val condId = nextId("if")
            nodes += Node(condId, visibleCond(block.cond), NodeShape.Diamond, style(decision = true, palette = palette), payload = lanePayload(lane))
            val thenRes = buildSequence(block.thenBranch, nodes, edges, lane = lane?.name, palette = palette, refMap = refMap, stopRefs = stopRefs)
            val elseRes = buildSequence(block.elseBranch, nodes, edges, lane = lane?.name, palette = palette, refMap = refMap, stopRefs = stopRefs)
            thenRes.entry?.let { edges += labelledEdge(condId, it, "yes", palette) }
            elseRes.entry?.let { edges += labelledEdge(condId, it, if (block.elseBranch.isNotEmpty()) "no" else "", palette) }
            val exits = buildList {
                if (thenRes.exits.isNotEmpty()) addAll(thenRes.exits)
                if (block.elseBranch.isEmpty()) add(condId) else addAll(elseRes.exits)
            }
            BuildResult(condId, exits)
        }
        is ActivityBlock.While -> {
            val condId = nextId("while")
            val isRepeat = isRepeatCond(block.cond)
            nodes += Node(condId, visibleCond(block.cond), NodeShape.Diamond, style(decision = true, palette = palette), payload = lanePayload(lane))
            val bodyRes = buildSequence(block.body, nodes, edges, lane = lane?.name, palette = palette, refMap = refMap, stopRefs = stopRefs)
            bodyRes.entry?.let { edges += labelledEdge(condId, it, if (isRepeat) "" else "yes", palette) }
            if (isRepeat) {
                for (exit in bodyRes.exits) edges += labelledEdge(exit, condId, "repeat", palette)
            } else {
                for (exit in bodyRes.exits) edges += solidEdge(exit, condId, palette = palette)
            }
            BuildResult(condId, listOf(condId))
        }
        is ActivityBlock.ForkJoin -> {
            val forkId = nextId("fork")
            val joinId = nextId("join")
            nodes += Node(forkId, RichLabel.Empty, NodeShape.ForkBar, style(fork = true, palette = palette), payload = lanePayload(lane))
            nodes += Node(joinId, RichLabel.Empty, NodeShape.ForkBar, style(fork = true, palette = palette), payload = lanePayload(lane))
            for (branch in block.branches) {
                val res = buildSequence(branch, nodes, edges, lane = lane?.name, palette = palette, refMap = refMap, stopRefs = stopRefs)
                res.entry?.let { edges += solidEdge(forkId, it, palette = palette) }
                for (exit in res.exits) edges += solidEdge(exit, joinId, palette = palette)
            }
            BuildResult(forkId, listOf(joinId))
        }
    }

    private fun nextId(prefix: String): NodeId = NodeId("$prefix#${idCounter++}")

    private fun makeNode(prefix: String, label: String, shape: NodeShape, style: NodeStyle): Node {
        val id = nextId(prefix)
        return Node(id = id, label = if (label.isEmpty()) RichLabel.Empty else RichLabel.Plain(label), shape = shape, style = style)
    }

    private fun style(
        action: Boolean = false,
        decision: Boolean = false,
        note: Boolean = false,
        start: Boolean = false,
        stop: Boolean = false,
        fork: Boolean = false,
        fillOverride: ArgbColor? = null,
        palette: ActivityPalette,
    ): NodeStyle = when {
        start -> NodeStyle(fill = palette.startFill ?: ArgbColor(colors.startFill.argb), stroke = palette.startFill ?: ArgbColor(colors.startFill.argb), strokeWidth = palette.startLineThickness ?: 1.5f)
        stop -> NodeStyle(fill = ArgbColor(stopFill.argb), stroke = palette.stopStroke ?: ArgbColor(colors.stopStroke.argb), strokeWidth = palette.stopLineThickness ?: 1.5f)
        fork -> NodeStyle(fill = palette.barFill ?: ArgbColor(colors.barFill.argb), stroke = palette.barFill ?: ArgbColor(colors.barFill.argb), strokeWidth = palette.barLineThickness ?: 1.5f, textColor = palette.barText ?: ArgbColor(colors.barText.argb))
        note -> NodeStyle(fill = palette.noteFill ?: ArgbColor(colors.noteFill.argb), stroke = palette.noteStroke ?: ArgbColor(colors.noteStroke.argb), strokeWidth = palette.noteLineThickness ?: 1.5f, textColor = palette.noteText ?: ArgbColor(colors.noteText.argb))
        decision -> NodeStyle(fill = palette.decisionFill ?: ArgbColor(colors.decisionFill.argb), stroke = palette.decisionStroke ?: ArgbColor(colors.decisionStroke.argb), strokeWidth = palette.decisionLineThickness ?: 1.5f, textColor = palette.decisionText ?: ArgbColor(colors.decisionText.argb))
        action -> NodeStyle(fill = fillOverride ?: palette.actionFill ?: ArgbColor(colors.actionFill.argb), stroke = palette.actionStroke ?: ArgbColor(colors.actionStroke.argb), strokeWidth = palette.actionLineThickness ?: 1.5f, textColor = palette.actionText ?: ArgbColor(colors.actionText.argb))
        else -> NodeStyle.Default
    }

    private fun solidEdge(from: NodeId, to: NodeId, label: String? = null, palette: ActivityPalette): Edge = Edge(
        from = from,
        to = to,
        kind = EdgeKind.Solid,
        arrow = ArrowEnds.ToOnly,
        style = EdgeStyle(color = palette.edgeColor ?: ArgbColor(colors.edge.argb), width = 1.5f),
        label = label?.takeIf { it.isNotEmpty() }?.let(RichLabel::Plain),
    )

    private fun labelledEdge(from: NodeId, to: NodeId, label: String, palette: ActivityPalette): Edge =
        solidEdge(from, to, palette = palette).copy(label = label.takeIf { it.isNotEmpty() }?.let(RichLabel::Plain))

    private fun measureNodes(ir: GraphIR, palette: ActivityPalette) {
        for (node in ir.nodes) {
            val label = labelTextOf(node)
            val scope = scopeFor(node, palette)
            nodeSizes[node.id] = when (node.shape) {
                NodeShape.StartCircle, NodeShape.EndCircle -> Size(28f, 28f)
                NodeShape.ForkBar -> {
                    if (label.isBlank()) {
                        Size(80f, 12f)
                    } else {
                        val m = textMeasurer.measure(label, scopedFont(scope, edgeLabelFont), maxWidth = 180f)
                        Size((m.width + 44f).coerceAtLeast(100f), (m.height + 18f).coerceAtLeast(28f))
                    }
                }
                NodeShape.Note -> {
                    val m = textMeasurer.measure(label, scopedFont(scope, labelFont), maxWidth = 160f)
                    Size((m.width + 22f).coerceAtLeast(96f), (m.height + 18f).coerceAtLeast(56f))
                }
                NodeShape.Diamond -> {
                    val m = textMeasurer.measure(label, scopedFont(scope, labelFont), maxWidth = 120f)
                    Size((m.width + 44f).coerceAtLeast(92f), (m.height + 36f).coerceAtLeast(72f))
                }
                else -> {
                    val m = textMeasurer.measure(label, scopedFont(scope, labelFont), maxWidth = 200f)
                    Size((m.width + 28f).coerceAtLeast(132f), (m.height + 20f).coerceAtLeast(56f))
                }
            }
        }
    }

    private fun render(ir: GraphIR, laidOut: LaidOutDiagram, palette: ActivityPalette): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, out)
        for (node in ir.nodes) {
            val rect = laidOut.nodePositions[node.id] ?: continue
            val fill = node.style.fill?.let { Color(it.argb) } ?: colors.actionFill
            val stroke = node.style.stroke?.let { Color(it.argb) } ?: colors.actionStroke
            val text = node.style.textColor?.let { Color(it.argb) } ?: colors.actionText
            val scope = scopeFor(node, palette)
            val bodyFont = scopedFont(scope, labelFont)
            val barFont = scopedFont(scope, edgeLabelFont)
            when (node.shape) {
                NodeShape.StartCircle -> {
                    if (scope.shadowing == true) {
                        out += DrawCommand.FillRect(
                            PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                            shadowTint,
                            corner = rect.size.width / 2f,
                            z = 2,
                        )
                    }
                    out += DrawCommand.FillRect(rect, fill, corner = rect.size.width / 2f, z = 2)
                }
                NodeShape.EndCircle -> {
                    if (scope.shadowing == true) {
                        out += DrawCommand.FillRect(
                            PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                            shadowTint,
                            corner = rect.size.width / 2f,
                            z = 2,
                        )
                    }
                    out += DrawCommand.StrokeRect(rect, Stroke(width = node.style.strokeWidth ?: 1.5f), stroke, corner = rect.size.width / 2f, z = 2)
                    val inner = Rect.ltrb(rect.left + 4f, rect.top + 4f, rect.right - 4f, rect.bottom - 4f)
                    out += DrawCommand.FillRect(inner, stroke, corner = inner.size.width / 2f, z = 3)
                }
                NodeShape.ForkBar -> {
                    val label = labelTextOf(node)
                    if (label.isBlank()) {
                        if (scope.shadowing == true) {
                            out += DrawCommand.FillRect(
                                PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                                shadowTint,
                                corner = 2f,
                                z = 2,
                            )
                        }
                        out += DrawCommand.FillRect(rect, stroke, corner = 2f, z = 2)
                    } else {
                        val barTop = rect.bottom - 10f
                        val barRect = Rect.ltrb(rect.left, barTop, rect.right, rect.bottom)
                        if (scope.shadowing == true) {
                            out += DrawCommand.FillRect(
                                PlantUmlTreeRenderSupport.offsetRect(barRect, 4f, 4f),
                                shadowTint,
                                corner = 2f,
                                z = 2,
                            )
                        }
                        out += DrawCommand.FillRect(barRect, stroke, corner = 2f, z = 2)
                        out += DrawCommand.DrawText(
                            text = label,
                            origin = Point((rect.left + rect.right) / 2f, rect.top + 2f),
                            font = barFont,
                            color = stroke,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Top,
                            z = 4,
                        )
                    }
                }
                NodeShape.Diamond -> {
                    val cx = (rect.left + rect.right) / 2f
                    val cy = (rect.top + rect.bottom) / 2f
                    val path = PathCmd(
                        listOf(
                            PathOp.MoveTo(Point(cx, rect.top)),
                            PathOp.LineTo(Point(rect.right, cy)),
                            PathOp.LineTo(Point(cx, rect.bottom)),
                            PathOp.LineTo(Point(rect.left, cy)),
                            PathOp.Close,
                        ),
                    )
                    if (scope.shadowing == true) {
                        out += DrawCommand.FillPath(
                            PathCmd(
                                listOf(
                                    PathOp.MoveTo(Point(cx + 4f, rect.top + 4f)),
                                    PathOp.LineTo(Point(rect.right + 4f, cy + 4f)),
                                    PathOp.LineTo(Point(cx + 4f, rect.bottom + 4f)),
                                    PathOp.LineTo(Point(rect.left + 4f, cy + 4f)),
                                    PathOp.Close,
                                ),
                            ),
                            shadowTint,
                            z = 2,
                        )
                    }
                    out += DrawCommand.FillPath(path, fill, z = 2)
                    out += DrawCommand.StrokePath(path, Stroke(width = node.style.strokeWidth ?: 1.5f), stroke, z = 3)
                    drawCenteredText(labelTextOf(node), rect, text, bodyFont, out)
                }
                NodeShape.Note -> {
                    if (scope.shadowing == true) {
                        out += DrawCommand.FillRect(
                            PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                            shadowTint,
                            corner = 4f,
                            z = 2,
                        )
                    }
                    out += DrawCommand.FillRect(rect, fill, corner = 4f, z = 2)
                    out += DrawCommand.StrokeRect(rect, Stroke(width = node.style.strokeWidth ?: 1.5f), stroke, corner = 4f, z = 3)
                    drawCenteredText(labelTextOf(node), rect, text, bodyFont, out)
                }
                else -> {
                    if (scope.shadowing == true) {
                        out += DrawCommand.FillRect(
                            PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                            shadowTint,
                            corner = 10f,
                            z = 2,
                        )
                    }
                    out += DrawCommand.FillRect(rect, fill, corner = 10f, z = 2)
                    out += DrawCommand.StrokeRect(rect, Stroke(width = node.style.strokeWidth ?: 1.5f), stroke, corner = 10f, z = 3)
                    drawCenteredText(labelTextOf(node), rect, text, bodyFont, out)
                }
            }
        }
        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            val pts = route.points
            if (pts.size < 2) continue
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
            val color = edge.style.color?.let { Color(it.argb) } ?: colors.edge
            out += DrawCommand.StrokePath(PathCmd(ops), Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash), color, z = 1)
            out += openArrowHead(pts[pts.size - 2], pts.last(), color)
            val text = (edge.label as? RichLabel.Plain)?.text.orEmpty()
            if (text.isNotEmpty()) {
                val mid = pts[pts.size / 2]
                out += DrawCommand.DrawText(
                    text = text,
                    origin = Point(mid.x, mid.y - 4f),
                    font = edgeLabelFont,
                    color = colors.actionText,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Bottom,
                    z = 4,
                )
            }
        }
        return out.entities()
    }

    private fun drawCluster(cluster: Cluster, clusterRects: Map<NodeId, Rect>, out: MutableList<DrawCommand>) {
        val rect = clusterRects[cluster.id] ?: return
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.laneFill
        val stroke = cluster.style.stroke?.let { Color(it.argb) } ?: colors.laneStroke
        out += DrawCommand.FillRect(rect, fill, corner = 12f, z = 0)
        out += DrawCommand.StrokeRect(rect, Stroke(width = cluster.style.strokeWidth ?: 1f), stroke, corner = 12f, z = 0)
        val label = (cluster.label as? RichLabel.Plain)?.text.orEmpty()
        if (label.isNotEmpty()) {
            out += DrawCommand.DrawText(
                text = label,
                origin = Point(rect.left + 12f, rect.top + 10f),
                font = edgeLabelFont,
                color = colors.laneText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 1,
            )
        }
        for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, out)
    }

    private fun computeClusterRect(
        cluster: Cluster,
        nodePositions: Map<NodeId, Rect>,
        out: LinkedHashMap<NodeId, Rect>,
    ): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        childRects += cluster.nestedClusters.mapNotNull { computeClusterRect(it, nodePositions, out) }
        if (childRects.isEmpty()) return null
        val label = (cluster.label as? RichLabel.Plain)?.text.orEmpty().ifBlank { cluster.id.value }
        val titleMetrics = textMeasurer.measure(label, edgeLabelFont, maxWidth = 220f)
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
        if (rects.isEmpty()) return Rect(Point(0f, 0f), Size(0f, 0f))
        return Rect.ltrb(
            rects.minOf { it.left },
            rects.minOf { it.top },
            rects.maxOf { it.right },
            rects.maxOf { it.bottom },
        )
    }

    private fun drawCenteredText(text: String, rect: Rect, color: Color, font: FontSpec, out: MutableList<DrawCommand>) {
        if (text.isEmpty()) return
        out += DrawCommand.DrawText(
            text = text,
            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
            font = font,
            color = color,
            maxWidth = rect.size.width - 16f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 4,
        )
    }

    private fun openArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(to))),
            stroke = Stroke(width = 1f),
            color = color,
            z = 3,
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
            z = 3,
        )
    }

    private fun labelTextOf(node: Node): String =
        when (val label = node.label) {
            is RichLabel.Plain -> label.text
            is RichLabel.Markdown -> label.source
            is RichLabel.Html -> label.html
        }

    private fun buildLaneClusters(nodes: List<Node>): List<Cluster> =
        nodes.groupBy { it.payload[LANE_KEY].orEmpty() }
            .filterKeys { it.isNotEmpty() }
            .map { (lane, laneNodes) ->
                val fill = laneNodes.firstNotNullOfOrNull {
                    it.payload[LANE_FILL_KEY]
                        ?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)
                        ?.let { color -> ArgbColor(color.argb) }
                }
                Cluster(
                    id = NodeId("lane_${sanitizeId(lane)}"),
                    label = RichLabel.Plain(lane),
                    children = laneNodes.map { it.id },
                    style = ClusterStyle(
                        fill = fill ?: ArgbColor(colors.laneFill.argb),
                        stroke = fill?.let(::darkerStroke) ?: ArgbColor(colors.laneStroke.argb),
                        strokeWidth = 1f,
                    ),
                )
            }

    private fun asLaneMarker(block: ActivityBlock): LaneMarker? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.SWIMLANE_PREFIX)) return null
        val payload = text.removePrefix(PlantUmlActivityParser.SWIMLANE_PREFIX)
        val lane = payload.substringBefore("|||").trim()
        val fill = payload.substringAfter("|||", "")
            .takeIf { it.isNotBlank() }
            ?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)
            ?.let { ArgbColor(it.argb) }
        return LaneMarker(lane = lane, fill = fill)
    }

    private fun asEdgeLabelMarker(block: ActivityBlock): PendingEdgeLabel? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.EDGE_LABEL_PREFIX)) return null
        return PendingEdgeLabel(text.removePrefix(PlantUmlActivityParser.EDGE_LABEL_PREFIX))
    }

    private fun asActionStyleMarker(block: ActivityBlock): PendingActionStyle? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.ACTION_STYLE_PREFIX)) return null
        return PendingActionStyle(
            fill = PlantUmlTreeRenderSupport.parsePlantUmlColor(text.removePrefix(PlantUmlActivityParser.ACTION_STYLE_PREFIX))
                ?.let { ArgbColor(it.argb) },
        )
    }

    private fun asSyncBarMarker(block: ActivityBlock): SyncBarMarker? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.SYNC_BAR_PREFIX)) return null
        return SyncBarMarker(text.removePrefix(PlantUmlActivityParser.SYNC_BAR_PREFIX))
    }

    private fun asSourceRefMarker(block: ActivityBlock): PendingSourceRef? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.EDGE_SOURCE_PREFIX)) return null
        return PendingSourceRef(text.removePrefix(PlantUmlActivityParser.EDGE_SOURCE_PREFIX))
    }

    private fun asTargetRefMarker(block: ActivityBlock): PendingTargetRef? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.EDGE_TARGET_PREFIX)) return null
        return PendingTargetRef(text.removePrefix(PlantUmlActivityParser.EDGE_TARGET_PREFIX))
    }

    private fun asNodeRefMarker(block: ActivityBlock): PendingNodeRef? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.NODE_REF_PREFIX)) return null
        return PendingNodeRef(text.removePrefix(PlantUmlActivityParser.NODE_REF_PREFIX))
    }

    private fun asStopRefMarker(block: ActivityBlock): PendingStopRef? {
        val note = block as? ActivityBlock.Note ?: return null
        val text = (note.text as? RichLabel.Plain)?.text ?: return null
        if (!text.startsWith(PlantUmlActivityParser.EDGE_STOP_PREFIX)) return null
        return PendingStopRef(text.removePrefix(PlantUmlActivityParser.EDGE_STOP_PREFIX))
    }

    private fun lanePayload(lane: LaneState?): Map<String, String> =
        when {
            lane == null || lane.name.isEmpty() -> emptyMap()
            lane.fill == null -> mapOf(LANE_KEY to lane.name)
            else -> mapOf(LANE_KEY to lane.name, LANE_FILL_KEY to colorToken(lane.fill))
        }

    private fun isRepeatCond(label: RichLabel): Boolean =
        (label as? RichLabel.Plain)?.text?.startsWith(PlantUmlActivityParser.REPEAT_PREFIX) == true

    private fun visibleCond(label: RichLabel): RichLabel {
        val text = (label as? RichLabel.Plain)?.text ?: return label
        return if (text.startsWith(PlantUmlActivityParser.REPEAT_PREFIX)) {
            RichLabel.Plain(text.removePrefix(PlantUmlActivityParser.REPEAT_PREFIX))
        } else {
            label
        }
    }

    private fun sanitizeId(text: String): String =
        text.replace(Regex("[^A-Za-z0-9_.:-]+"), "_").trim('_').ifEmpty { "lane" }

    private fun colorToken(color: ArgbColor): String =
        "#" + color.argb.toUInt().toString(16).padStart(8, '0').uppercase()

    private fun darkerStroke(fill: ArgbColor): ArgbColor {
        val a = (fill.argb ushr 24) and 0xFF
        val r = ((fill.argb ushr 16) and 0xFF) * 0.72f
        val g = ((fill.argb ushr 8) and 0xFF) * 0.72f
        val b = (fill.argb and 0xFF) * 0.72f
        return ArgbColor((a shl 24) or (r.toInt().coerceIn(0, 255) shl 16) or (g.toInt().coerceIn(0, 255) shl 8) or b.toInt().coerceIn(0, 255))
    }

    private fun resolvePalette(ir: ActivityIR): ActivityPalette {
        val extras = ir.styleHints.extras
        if (cachedPaletteExtras != extras) {
            cachedPaletteExtras = extras.toMap()
            cachedPalette = paletteOf(cachedPaletteExtras)
        }
        return cachedPalette
    }

    private fun paletteOf(extras: Map<String, String>): ActivityPalette {
        fun c(key: String): ArgbColor? =
            extras[key]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) }
        fun f(key: String): Float? = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras[key])
        fun s(key: String): String? = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(extras[key])
        fun b(key: String): Boolean? = PlantUmlTreeRenderSupport.parsePlantUmlBoolean(extras[key])
        return ActivityPalette(
            actionFill = c(PlantUmlActivityParser.STYLE_ACTION_FILL_KEY),
            actionStroke = c(PlantUmlActivityParser.STYLE_ACTION_STROKE_KEY),
            actionText = c(PlantUmlActivityParser.STYLE_ACTION_TEXT_KEY),
            actionFontSize = f(PlantUmlActivityParser.STYLE_ACTION_FONT_SIZE_KEY),
            actionFontName = s(PlantUmlActivityParser.STYLE_ACTION_FONT_NAME_KEY),
            actionLineThickness = f(PlantUmlActivityParser.STYLE_ACTION_LINE_THICKNESS_KEY),
            actionShadowing = b(PlantUmlActivityParser.STYLE_ACTION_SHADOWING_KEY),
            decisionFill = c(PlantUmlActivityParser.STYLE_DECISION_FILL_KEY),
            decisionStroke = c(PlantUmlActivityParser.STYLE_DECISION_STROKE_KEY),
            decisionText = c(PlantUmlActivityParser.STYLE_DECISION_TEXT_KEY),
            decisionFontSize = f(PlantUmlActivityParser.STYLE_DECISION_FONT_SIZE_KEY),
            decisionFontName = s(PlantUmlActivityParser.STYLE_DECISION_FONT_NAME_KEY),
            decisionLineThickness = f(PlantUmlActivityParser.STYLE_DECISION_LINE_THICKNESS_KEY),
            decisionShadowing = b(PlantUmlActivityParser.STYLE_DECISION_SHADOWING_KEY),
            noteFill = c(PlantUmlActivityParser.STYLE_NOTE_FILL_KEY),
            noteStroke = c(PlantUmlActivityParser.STYLE_NOTE_STROKE_KEY),
            noteText = c(PlantUmlActivityParser.STYLE_NOTE_TEXT_KEY),
            noteFontSize = f(PlantUmlActivityParser.STYLE_NOTE_FONT_SIZE_KEY),
            noteFontName = s(PlantUmlActivityParser.STYLE_NOTE_FONT_NAME_KEY),
            noteLineThickness = f(PlantUmlActivityParser.STYLE_NOTE_LINE_THICKNESS_KEY),
            noteShadowing = b(PlantUmlActivityParser.STYLE_NOTE_SHADOWING_KEY),
            barFill = c(PlantUmlActivityParser.STYLE_BAR_FILL_KEY),
            barText = c(PlantUmlActivityParser.STYLE_BAR_TEXT_KEY),
            barFontSize = f(PlantUmlActivityParser.STYLE_BAR_FONT_SIZE_KEY),
            barFontName = s(PlantUmlActivityParser.STYLE_BAR_FONT_NAME_KEY),
            barLineThickness = f(PlantUmlActivityParser.STYLE_BAR_LINE_THICKNESS_KEY),
            barShadowing = b(PlantUmlActivityParser.STYLE_BAR_SHADOWING_KEY),
            startFill = c(PlantUmlActivityParser.STYLE_START_FILL_KEY),
            stopStroke = c(PlantUmlActivityParser.STYLE_STOP_STROKE_KEY),
            startLineThickness = f(PlantUmlActivityParser.STYLE_START_LINE_THICKNESS_KEY),
            stopLineThickness = f(PlantUmlActivityParser.STYLE_STOP_LINE_THICKNESS_KEY),
            startShadowing = b(PlantUmlActivityParser.STYLE_START_SHADOWING_KEY),
            stopShadowing = b(PlantUmlActivityParser.STYLE_STOP_SHADOWING_KEY),
            edgeColor = c(PlantUmlActivityParser.STYLE_EDGE_COLOR_KEY),
        )
    }

    private fun scopeFor(node: Node, palette: ActivityPalette): ActivityScope = when (node.shape) {
        NodeShape.StartCircle -> ActivityScope(palette.startShadowing, null, null)
        NodeShape.EndCircle -> ActivityScope(palette.stopShadowing, null, null)
        NodeShape.ForkBar -> ActivityScope(palette.barShadowing, palette.barFontName, palette.barFontSize)
        NodeShape.Note -> ActivityScope(palette.noteShadowing, palette.noteFontName, palette.noteFontSize)
        NodeShape.Diamond -> ActivityScope(palette.decisionShadowing, palette.decisionFontName, palette.decisionFontSize)
        else -> ActivityScope(palette.actionShadowing, palette.actionFontName, palette.actionFontSize)
    }

    private fun scopedFont(scope: ActivityScope, base: FontSpec): FontSpec =
        PlantUmlTreeRenderSupport.resolveFontSpec(base, scope.fontName, scope.fontSize?.toString())

    private data class ActivityScope(
        val shadowing: Boolean?,
        val fontName: String?,
        val fontSize: Float?,
    )
}
