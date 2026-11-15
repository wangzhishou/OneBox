package com.hrm.markdown.renderer.internal.layout

import androidx.compose.ui.unit.dp

/**
 * Shared geometry tokens consumed by both the pure layout engine and Compose painters.
 * Keeping these values here prevents the measurement and paint paths from drifting apart.
 */
internal object LayoutTokens {
    val ColumnsSpacing = 8.dp
    val ListTightItemSpacing = 2.dp
    val TabContentPadding = 12.dp
    val TabTitleHorizontalPadding = 12.dp
    val TabTitleVerticalPadding = 8.dp
    val ContainerStartPadding = 16.dp
    val ContainerVerticalPadding = 12.dp
    val ContainerEndPadding = 12.dp
    val ContainerContentSpacing = 8.dp
    val DirectivePadding = 12.dp
    val DefinitionIndent = 24.dp
    val DefinitionSpacing = 4.dp
    val FootnoteTopPadding = 4.dp
    val FootnoteHorizontalSpacing = 8.dp
    val TocIndentPerLevel = 16.dp
    val TocSpacing = 2.dp
    val MinimumInlineMeasureWidth = 16.dp
    val InlineImageFallbackWidth = 200.dp
    val InlineImageFallbackHeight = 150.dp
    val MinimumTocMeasureWidth = 24.dp
    val FigureCaptionSpacing = 4.dp
    val FigureCaptionHorizontalPadding = 16.dp
    val FigureFallbackHeight = 220.dp
    val HeadingDividerSpacing = 4.dp
    val BibliographyPadding = 12.dp
    val BibliographyTitleSpacing = 8.dp
    val BibliographyEntryVerticalPadding = 2.dp
    val PageBreakVerticalPadding = 8.dp
}
