package com.hrm.markdown.renderer.internal.layout.inline

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.markdown.renderer.MarkdownRenderOptions
import com.hrm.markdown.renderer.ResolvedMarkdownStyle
import com.hrm.markdown.renderer.inline.InlineRenderResult
import com.hrm.markdown.renderer.inline.buildInlineRenderResultFromModel
import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity
import com.hrm.markdown.renderer.internal.core.model.InlineModel
import com.hrm.markdown.runtime.MarkdownDirectiveRegistry
import kotlin.math.ceil

internal class InlineLayoutRuntime {
    private val renderResultCache = InlineRenderResultCache()
    private val flowLayoutCache = InlineFlowLayoutCache()
    private val metrics = InlineLayoutMetrics()

    fun metricsSnapshot(): InlineLayoutMetricsSnapshot = metrics.snapshot(
        renderResultCache = renderResultCache.metricsSnapshot(),
        flowLayoutCache = flowLayoutCache.metricsSnapshot(),
    )

    fun resetMetrics() {
        metrics.reset()
        renderResultCache.resetStatistics()
        flowLayoutCache.resetStatistics()
    }

    fun renderResult(
        model: InlineModel,
        style: TextStyle,
        epoch: InlineLayoutEpoch,
        theme: ResolvedMarkdownStyle,
        directiveRegistry: MarkdownDirectiveRegistry,
        onLinkClick: ((String) -> Unit)?,
        onFootnoteClick: ((String) -> Unit)?,
        latexMeasurer: LatexMeasurerState,
        density: Density,
        textMeasurer: TextMeasurer,
    ): InlineRenderResult {
        metrics.renderResultRequests++
        return renderResultCache.getOrPut(
            epoch = epoch,
            stableId = model.identity.stableId,
            contentRevision = model.identity.contentRevision,
            style = style,
        ) {
            metrics.renderResultComputations++
            val result = buildInlineRenderResultFromModel(
                model = model,
                theme = theme,
                hostTextStyle = style,
                directiveRegistry = directiveRegistry,
                onLinkClick = onLinkClick,
                onFootnoteClick = onFootnoteClick,
                latexMeasurer = latexMeasurer,
                density = density,
                textMeasurer = textMeasurer,
            )
            metrics.inlineMathBuildRequests += result.inlineMathBuildRequests
            result
        }
    }

    fun flowLayout(
        identity: RenderIdentity,
        inlineResult: InlineRenderResult,
        style: TextStyle,
        epoch: InlineLayoutEpoch,
        density: Density,
        textMeasurer: TextMeasurer,
        widthPx: Float,
        maxLines: Int,
    ): InlineFlowLayout {
        metrics.flowLayoutRequests++
        return flowLayoutCache.getOrPut(
            epoch = epoch,
            stableId = identity.stableId,
            layoutRevision = identity.layoutRevision,
            widthPx = widthPx,
            maxLines = maxLines,
            style = style,
        ) {
            metrics.flowLayoutComputations++
            computeInlineFlowLayout(
                input = inlineResult.flowInput,
                style = style,
                density = density,
                textMeasurer = textMeasurer,
                maxWidthPx = widthPx,
                maxLines = maxLines,
            )
        }
    }

    fun intrinsicHeightPx(
        identity: RenderIdentity,
        inlineResult: InlineRenderResult,
        style: TextStyle,
        epoch: InlineLayoutEpoch,
        density: Density,
        textMeasurer: TextMeasurer,
        maxLines: Int,
        widthPx: Int,
    ): Int {
        val targetWidth = if (widthPx == Constraints.Infinity || widthPx <= 0) {
            computeMaxIntrinsicWidthPx(
                input = inlineResult.flowInput,
                style = style,
                textMeasurer = textMeasurer,
            ).coerceAtLeast(1)
        } else {
            widthPx
        }
        return ceil(
            flowLayout(
                identity = identity,
                inlineResult = inlineResult,
                style = style,
                epoch = epoch,
                density = density,
                textMeasurer = textMeasurer,
                widthPx = targetWidth.toFloat(),
                maxLines = maxLines,
            ).heightPx
        ).toInt()
    }
}

internal fun inlineLayoutEpoch(
    theme: ResolvedMarkdownStyle,
    directiveRegistry: MarkdownDirectiveRegistry,
    config: MarkdownRenderOptions?,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    density: Density,
    textMeasurer: TextMeasurer,
    latexMeasurer: LatexMeasurerState,
): InlineLayoutEpoch = InlineLayoutEpoch(
    themeIdentity = StructuralIdentity.of(theme),
    directiveRegistryIdentity = ReferentialIdentity.of(directiveRegistry),
    configIdentity = StructuralIdentity.of(config),
    densityBits = density.density.toBits(),
    fontScaleBits = density.fontScale.toBits(),
    textMeasurerIdentity = ReferentialIdentity.of(textMeasurer),
    latexMeasurerIdentity = ReferentialIdentity.of(latexMeasurer),
    onLinkClickIdentity = ReferentialIdentity.of(onLinkClick),
    onFootnoteClickIdentity = ReferentialIdentity.of(onFootnoteClick),
)
