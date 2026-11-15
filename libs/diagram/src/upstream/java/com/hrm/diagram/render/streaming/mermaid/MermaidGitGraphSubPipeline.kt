package com.hrm.diagram.render.streaming.mermaid

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
import com.hrm.diagram.core.ir.GitCommitType
import com.hrm.diagram.core.ir.GitGraphIR
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.gitgraph.GitGraphLayout
import com.hrm.diagram.parser.mermaid.MermaidGitGraphParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver

internal class MermaidGitGraphSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidGitGraphParser()
    private val colors = ThemeResolver.resolveGitGraph(theme)
    private val layout = GitGraphLayout(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val branchFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 10f)
    private val tagFont = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: GitGraphIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val bounds = laid.bounds
        val branchColors = ir.branches.mapIndexed { index, name -> name to palette(index) }.toMap()

        out += DrawCommand.FillRect(Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)), colors.background, z = 0)
        val titleRect = laid.nodePositions[NodeId("gitgraph:title")]
        if (titleRect != null && !ir.title.isNullOrBlank()) {
            out += DrawCommand.DrawText(ir.title!!, Point(titleRect.left, titleRect.top), titleFont, colors.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
        }

        for (branch in ir.branches) {
            val rect = laid.nodePositions[NodeId("gitgraph:branch:$branch")] ?: continue
            val y = rect.top + rect.size.height / 2f
            out += DrawCommand.StrokePath(
                path = PathCmd(listOf(PathOp.MoveTo(Point(90f, y)), PathOp.LineTo(Point(bounds.right - 20f, y)))),
                stroke = Stroke(width = 1f),
                color = colors.lane,
                z = 1,
            )
            out += DrawCommand.DrawText(branch, Point(rect.left, rect.top), branchFont, colors.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
        }

        for (route in laid.edgeRoutes) {
            val child = ir.commits.firstOrNull { it.id == route.to } ?: continue
            val color = branchColors[child.branch] ?: colors.border
            out += DrawCommand.StrokePath(
                path = PathCmd(route.points.mapIndexed { index, point ->
                    if (index == 0) PathOp.MoveTo(point) else PathOp.LineTo(point)
                }),
                stroke = Stroke(width = if (child.parents.size > 1) 2f else 1.5f),
                color = color,
                z = 2,
            )
        }

        for (commit in ir.commits) {
            val rect = laid.nodePositions[NodeId("gitgraph:commit:${commit.id.value}")] ?: continue
            val fill = branchColors[commit.branch] ?: colors.border
            when (commit.type) {
                GitCommitType.Highlight -> {
                    out += DrawCommand.FillRect(rect, fill, corner = 4f, z = 4)
                    out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.border, corner = 4f, z = 5)
                }
                GitCommitType.Reverse -> {
                    out += DrawCommand.StrokeRect(rect, Stroke(width = 2f), fill, corner = rect.size.width / 2f, z = 5)
                    out += DrawCommand.StrokePath(
                        path = PathCmd(
                            listOf(
                                PathOp.MoveTo(Point(rect.left + 3f, rect.top + 3f)),
                                PathOp.LineTo(Point(rect.right - 3f, rect.bottom - 3f)),
                                PathOp.MoveTo(Point(rect.left + 3f, rect.bottom - 3f)),
                                PathOp.LineTo(Point(rect.right - 3f, rect.top + 3f)),
                            ),
                        ),
                        stroke = Stroke(width = 1.5f),
                        color = fill,
                        z = 6,
                    )
                }
                GitCommitType.Merge -> {
                    out += DrawCommand.FillRect(rect, fill, corner = rect.size.width / 2f, z = 4)
                    out += DrawCommand.StrokeRect(rect, Stroke(width = 2f), colors.border, corner = rect.size.width / 2f, z = 5)
                    val inner = Rect.ltrb(rect.left + 4f, rect.top + 4f, rect.right - 4f, rect.bottom - 4f)
                    out += DrawCommand.StrokeRect(inner, Stroke(width = 1f), colors.background, corner = inner.size.width / 2f, z = 6)
                }
                GitCommitType.CherryPick -> {
                    out += DrawCommand.FillRect(rect, fill, corner = rect.size.width / 2f, z = 4)
                    out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.border, corner = rect.size.width / 2f, z = 5)
                    val source = (commit.label as? RichLabel.Plain)?.text.orEmpty()
                    if (source.isNotBlank()) {
                        out += DrawCommand.DrawText("pick:$source", Point((rect.left + rect.right) / 2f, rect.bottom + 10f), labelFont, colors.text, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Top, z = 10)
                    }
                }
                else -> {
                    out += DrawCommand.FillRect(rect, fill, corner = rect.size.width / 2f, z = 4)
                    out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.border, corner = rect.size.width / 2f, z = 5)
                }
            }

            val labelRect = laid.nodePositions[NodeId("gitgraph:label:${commit.id.value}")]
            val label = (commit.label as? RichLabel.Plain)?.text.orEmpty()
            if (labelRect != null && label.isNotBlank()) {
                out += DrawCommand.DrawText(label, Point(labelRect.left, labelRect.top), labelFont, colors.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
            }
            val tagRect = laid.nodePositions[NodeId("gitgraph:tag:${commit.id.value}")]
            if (tagRect != null && !commit.tag.isNullOrBlank()) {
                out += DrawCommand.FillRect(tagRect, colors.tagFill, corner = 10f, z = 7)
                out += DrawCommand.StrokeRect(tagRect, Stroke(width = 1f), colors.border, corner = 10f, z = 8)
                out += DrawCommand.DrawText(commit.tag!!, Point((tagRect.left + tagRect.right) / 2f, tagRect.top + 3f), tagFont, colors.tagText, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Top, z = 10)
            }
        }
        return out.entities()
    }

    private fun palette(index: Int): Color =
        colors.branchPalette[index % colors.branchPalette.size.coerceAtLeast(1)]

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }
}
