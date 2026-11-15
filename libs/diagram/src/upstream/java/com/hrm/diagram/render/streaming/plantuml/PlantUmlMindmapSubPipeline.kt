package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.TreeIR
import com.hrm.diagram.core.ir.TreeNode
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.tree.MindmapLayout
import com.hrm.diagram.parser.plantuml.PlantUmlMindmapParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver

internal class PlantUmlMindmapSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val shadowTint = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private val palette = ThemeResolver.resolvePlantUmlMindmap(theme).let {
        PlantUmlTreeNodePalette(
            defaultNodeFill = it.nodeFill,
            defaultNodeStroke = it.nodeStroke,
            rootFill = it.rootFill,
            rootStroke = it.rootStroke,
            textColor = it.nodeText,
            edgeColor = it.edge,
        )
    }
    private val chrome = PlantUmlTreeNodeChrome(
        rootCornerRadius = 14f,
        childCornerRadius = 6f,
        rootStrokeWidth = 2f,
        childStrokeWidth = 1.5f,
    )

    private val parser = PlantUmlMindmapParser()
    private val inverseText = theme.colors.surface
    private var cachedStyleExtras: Map<String, String> = emptyMap()
    private var styleCache: StyleCache = StyleCache.empty()
    private val layout = MindmapLayout(textMeasurer)
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val font = FontSpec(family = "sans-serif", sizeSp = 12f)
    private val stereotypeGap = 2f

    override fun acceptLine(line: String) = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean) = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun render(ir: TreeIR, laid: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laid)
        val cache = resolveStyleCache(ir)
        val boxless = cache.boxless
        val inlineColors = cache.inlineColors
        val styleColors = cache.styleColors
        val styleLineColors = cache.styleLineColors
        val styleFontColors = cache.styleFontColors
        val styleFontNames = cache.styleFontNames
        val styleFontSizes = cache.styleFontSizes
        val styleFontStyles = cache.styleFontStyles
        val styleLineThickness = cache.styleLineThickness
        val styleRoundCorners = cache.styleRoundCorners
        val styleShadowing = cache.styleShadowing
        val stereotypes = cache.stereotypes
        val leadingVisuals = cache.leadingVisuals

        fun drawEdges(parent: TreeNode) {
            val pr = laid.nodePositions[parent.id] ?: return
            for (c in parent.children) {
                val cr = laid.nodePositions[c.id] ?: continue
                PlantUmlTreeRenderSupport.appendCubicConnector(
                    out = out,
                    parentRect = pr,
                    childRect = cr,
                    color = styleLineColors[c.id] ?: palette.edgeColor,
                    strokeWidth = styleLineThickness[c.id] ?: 1.5f,
                )
                drawEdges(c)
            }
        }

        fun drawNode(n: TreeNode, isRoot: Boolean) {
            val r = laid.nodePositions[n.id] ?: return
            val nodeBoxless = n.id in boxless
            val inlineColor = inlineColors[n.id]
            val styleColor = styleColors[n.id]
            val styleLineColor = styleLineColors[n.id]
            val styleFontColor = styleFontColors[n.id]
            val styleRoundCorner = styleRoundCorners[n.id]
            val shadowing = styleShadowing[n.id] == true
            val fill = inlineColor ?: styleColor ?: if (isRoot) palette.rootFill else palette.defaultNodeFill
            val strokeColor = styleLineColor
                ?: (inlineColor ?: styleColor)?.let { PlantUmlTreeRenderSupport.darken(it, 0.18f) }
                ?: if (isRoot) palette.rootStroke else palette.defaultNodeStroke
            if (shadowing && !nodeBoxless) {
                out += DrawCommand.FillRect(
                    rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                    color = shadowTint,
                    corner = styleRoundCorner ?: if (isRoot) chrome.rootCornerRadius else chrome.childCornerRadius,
                    z = 0,
                )
            }
            PlantUmlTreeRenderSupport.appendNodeChrome(
                out = out,
                rect = r,
                isRoot = isRoot,
                boxless = nodeBoxless,
                fill = fill,
                strokeColor = strokeColor,
                chrome = chrome,
                shadowColor = shadowTint,
                cornerRadiusOverride = styleRoundCorner,
                strokeWidthOverride = styleLineThickness[n.id],
            )

            val label = (n.label as? RichLabel.Plain)?.text ?: ""
            val stereotype = stereotypes[n.id]
            val leadingVisual = leadingVisuals[n.id]
            val bodyFont = styledFont(
                base = if (isRoot) font.copy(weight = 600) else font,
                family = styleFontNames[n.id],
                size = styleFontSizes[n.id],
                style = styleFontStyles[n.id],
            )
            val effectiveTextColor = if (nodeBoxless) {
                styleFontColor ?: (inlineColor ?: styleColor)?.let { PlantUmlTreeRenderSupport.darken(it, 0.45f) } ?: palette.textColor
            } else {
                styleFontColor ?: if (PlantUmlTreeRenderSupport.isDark(fill)) inverseText else palette.textColor
            }
            PlantUmlTreeRenderSupport.appendCenteredNodeText(
                out = out,
                textMeasurer = textMeasurer,
                rect = r,
                label = label,
                stereotype = stereotype,
                leadingVisual = leadingVisual,
                bodyFont = bodyFont,
                color = effectiveTextColor,
                stereotypeGap = stereotypeGap,
            )
            n.children.forEach { drawNode(it, false) }
        }

        drawEdges(ir.root)
        drawNode(ir.root, true)
        return out.entities()
    }

    private fun parseBoxless(ir: TreeIR): Set<NodeId> =
        ir.styleHints.extras[PlantUmlMindmapParser.BOXLESS_KEY]
            .orEmpty()
            .split("||")
            .filter { it.isNotEmpty() }
            .map { NodeId(it) }
            .toSet()

    private fun resolveStyleCache(ir: TreeIR): StyleCache {
        val extras = ir.styleHints.extras
        if (cachedStyleExtras != extras) {
            cachedStyleExtras = extras.toMap()
            styleCache = StyleCache(
                boxless = parseBoxless(ir),
                inlineColors = PlantUmlTreeRenderSupport.parseNodeColorMap(extras[PlantUmlMindmapParser.INLINE_COLOR_KEY].orEmpty()),
                styleColors = PlantUmlTreeRenderSupport.parseNodeColorMap(extras[PlantUmlMindmapParser.STYLE_COLOR_KEY].orEmpty()),
                styleLineColors = PlantUmlTreeRenderSupport.parseNodeColorMap(extras[PlantUmlMindmapParser.STYLE_LINE_COLOR_KEY].orEmpty()),
                styleFontColors = PlantUmlTreeRenderSupport.parseNodeColorMap(extras[PlantUmlMindmapParser.STYLE_FONT_COLOR_KEY].orEmpty()),
                styleFontNames = PlantUmlTreeRenderSupport.parseNodeStringMap(extras[PlantUmlMindmapParser.STYLE_FONT_NAME_KEY].orEmpty()),
                styleFontSizes = PlantUmlTreeRenderSupport.parseNodeFloatMap(extras[PlantUmlMindmapParser.STYLE_FONT_SIZE_KEY].orEmpty()),
                styleFontStyles = PlantUmlTreeRenderSupport.parseNodeStringMap(extras[PlantUmlMindmapParser.STYLE_FONT_STYLE_KEY].orEmpty()),
                styleLineThickness = PlantUmlTreeRenderSupport.parseNodeFloatMap(extras[PlantUmlMindmapParser.STYLE_LINE_THICKNESS_KEY].orEmpty()),
                styleRoundCorners = PlantUmlTreeRenderSupport.parseNodeFloatMap(extras[PlantUmlMindmapParser.STYLE_ROUND_CORNER_KEY].orEmpty()),
                styleShadowing = PlantUmlTreeRenderSupport.parseNodeBooleanMap(extras[PlantUmlMindmapParser.STYLE_SHADOWING_KEY].orEmpty()),
                stereotypes = PlantUmlTreeRenderSupport.parseNodeStringMap(extras[PlantUmlMindmapParser.STEREOTYPE_KEY].orEmpty()),
                leadingVisuals = PlantUmlTreeRenderSupport.parseNodeLeadingVisualMap(extras[PlantUmlMindmapParser.LEADING_VISUAL_KEY].orEmpty()),
            )
        }
        return styleCache
    }

    private data class StyleCache(
        val boxless: Set<NodeId>,
        val inlineColors: Map<NodeId, Color>,
        val styleColors: Map<NodeId, Color>,
        val styleLineColors: Map<NodeId, Color>,
        val styleFontColors: Map<NodeId, Color>,
        val styleFontNames: Map<NodeId, String>,
        val styleFontSizes: Map<NodeId, Float>,
        val styleFontStyles: Map<NodeId, String>,
        val styleLineThickness: Map<NodeId, Float>,
        val styleRoundCorners: Map<NodeId, Float>,
        val styleShadowing: Map<NodeId, Boolean>,
        val stereotypes: Map<NodeId, String>,
        val leadingVisuals: Map<NodeId, PlantUmlTreeLeadingVisualSpec>,
    ) {
        companion object {
            fun empty(): StyleCache = StyleCache(
                boxless = emptySet(),
                inlineColors = emptyMap(),
                styleColors = emptyMap(),
                styleLineColors = emptyMap(),
                styleFontColors = emptyMap(),
                styleFontNames = emptyMap(),
                styleFontSizes = emptyMap(),
                styleFontStyles = emptyMap(),
                styleLineThickness = emptyMap(),
                styleRoundCorners = emptyMap(),
                styleShadowing = emptyMap(),
                stereotypes = emptyMap(),
                leadingVisuals = emptyMap<NodeId, PlantUmlTreeLeadingVisualSpec>(),
            )
        }
    }

    private fun styledFont(base: FontSpec, family: String?, size: Float?, style: String?): FontSpec {
        val normalizedStyle = style.orEmpty().lowercase()
        val bold = "bold" in normalizedStyle
        val italic = "italic" in normalizedStyle
        return base.copy(
            family = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(family) ?: base.family,
            sizeSp = size ?: base.sizeSp,
            weight = if (bold) 700 else base.weight,
            italic = italic || base.italic,
        )
    }
}
