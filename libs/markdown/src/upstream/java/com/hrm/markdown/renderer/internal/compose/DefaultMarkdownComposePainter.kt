package com.hrm.markdown.renderer.internal.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.hrm.markdown.renderer.LocalMarkdownComponents
import com.hrm.markdown.renderer.LocalMarkdownDirectiveRegistry
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.LocalMarkdownTextSearchState
import com.hrm.markdown.renderer.MarkdownBlockSurfaceKind
import com.hrm.markdown.renderer.MarkdownRenderMode
import com.hrm.markdown.renderer.block.FencedCodeBlockRenderer
import com.hrm.markdown.renderer.block.MathBlockRenderer
import com.hrm.markdown.renderer.block.PageBreakRenderer
import com.hrm.markdown.renderer.block.RenderAdmonitionBlockModel
import com.hrm.markdown.renderer.block.RenderBibliographyBlockModel
import com.hrm.markdown.renderer.block.RenderBibliographyLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderBlockQuoteBlockModel
import com.hrm.markdown.renderer.block.RenderColumnsLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderColumnsLayoutGroupModel
import com.hrm.markdown.renderer.block.RenderCustomContainerBlockModel
import com.hrm.markdown.renderer.block.RenderDefinitionListBlockModel
import com.hrm.markdown.renderer.block.RenderDefinitionListLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderDiagramBlockWidgetModel
import com.hrm.markdown.renderer.block.RenderDirectiveFallbackBlockModel
import com.hrm.markdown.renderer.block.RenderFigureBlockModel
import com.hrm.markdown.renderer.block.RenderFigureLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderFootnoteDefinitionBlockModel
import com.hrm.markdown.renderer.block.RenderFootnoteLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderHtmlBlockModel
import com.hrm.markdown.renderer.block.RenderListBlockModel
import com.hrm.markdown.renderer.block.RenderListLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderTabBlockModel
import com.hrm.markdown.renderer.block.RenderTabLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderTableBlockModel
import com.hrm.markdown.renderer.block.RenderTableLayoutBlockModel
import com.hrm.markdown.renderer.block.RenderTocLayoutBlockModel
import com.hrm.markdown.renderer.block.ThematicBreakRenderer
import com.hrm.markdown.renderer.internal.adapter.createDirectiveBlockRenderScope
import com.hrm.markdown.renderer.internal.core.model.AdmonitionBlockModel
import com.hrm.markdown.renderer.internal.core.model.BibliographyDefinitionBlockModel
import com.hrm.markdown.renderer.internal.core.model.BlockQuoteBlockModel
import com.hrm.markdown.renderer.internal.core.model.CodeBlockModel
import com.hrm.markdown.renderer.internal.core.model.CodeBlockWidgetModel
import com.hrm.markdown.renderer.internal.core.model.ColumnsLayoutBlockModel
import com.hrm.markdown.renderer.internal.core.model.CustomContainerBlockModel
import com.hrm.markdown.renderer.internal.core.model.DefinitionListBlockModel
import com.hrm.markdown.renderer.internal.core.model.DiagramBlockModel
import com.hrm.markdown.renderer.internal.core.model.DiagramBlockWidgetModel
import com.hrm.markdown.renderer.internal.core.model.DirectiveBlockModel
import com.hrm.markdown.renderer.internal.core.model.FallbackContainerBlockModel
import com.hrm.markdown.renderer.internal.core.model.FallbackLeafBlockModel
import com.hrm.markdown.renderer.internal.core.model.FigureBlockModel
import com.hrm.markdown.renderer.internal.core.model.FootnoteDefinitionBlockModel
import com.hrm.markdown.renderer.internal.core.model.HeadingBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlContainerBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.core.model.ListBlockModel
import com.hrm.markdown.renderer.internal.core.model.MathBlockModel
import com.hrm.markdown.renderer.internal.core.model.MathBlockWidgetModel
import com.hrm.markdown.renderer.internal.core.model.PageBreakBlockModel
import com.hrm.markdown.renderer.internal.core.model.ParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.TabBlockModel
import com.hrm.markdown.renderer.internal.core.model.TableBlockModel
import com.hrm.markdown.renderer.internal.core.model.ThematicBreakBlockModel
import com.hrm.markdown.renderer.internal.core.model.TocBlockModel
import com.hrm.markdown.renderer.internal.layout.LayoutTokens
import com.hrm.markdown.renderer.internal.layout.engine.MarkdownLayoutSource
import com.hrm.markdown.renderer.internal.layout.inline.LayoutInlineRunPlacement
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutBibliographyBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutColumnsBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutDefinitionListBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutFigureBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutFootnoteBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutInlineBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutListBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutRect
import com.hrm.markdown.renderer.internal.layout.model.LayoutRenderBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTabBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTableBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTocBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutWidgetBlockModel
import com.hrm.markdown.renderer.internal.selection.LocalMarkdownSelectionController
import com.hrm.markdown.renderer.internal.selection.selectableBlock
import kotlinx.coroutines.flow.distinctUntilChanged

internal object DefaultMarkdownComposePainter : MarkdownComposePainter {
    @Composable
    override fun Paint(
        document: MarkdownLayoutSource,
        environment: ComposeRenderEnvironment,
    ) {
        when (environment.renderMode) {
            MarkdownRenderMode.LazyColumn -> {
                val lazyListState = environment.lazyListState ?: rememberLazyListState()
                val documentStartIndex = if (environment.header == null) 0 else 1
                PrefetchNextMarkdownBlock(
                    document = document,
                    state = lazyListState,
                    documentStartIndex = documentStartIndex,
                )
                LazyColumn(
                    state = lazyListState,
                    modifier = environment.modifier.graphicsLayer { },
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                ) {
                    environment.header?.let { header ->
                        item(key = "markdown_header") { header() }
                    }
                    items(
                        count = document.blockCount,
                        key = document::stableIdAt,
                    ) { index ->
                        PaintBlock(document.blockAt(index))
                    }
                    environment.footer?.let { footer ->
                        item(key = "markdown_footer") { footer() }
                    }
                }
            }

            MarkdownRenderMode.StaticColumn -> {
                val body: @Composable () -> Unit = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                    ) {
                        for (index in 0 until document.blockCount) {
                            key(document.stableIdAt(index)) {
                                PaintBlock(document.blockAt(index))
                            }
                        }
                    }
                }
                Column(
                    modifier = environment.modifier
                        .then(
                            if (environment.enableScroll && environment.scrollState != null) {
                                Modifier.verticalScroll(environment.scrollState)
                            } else {
                                Modifier
                            }
                        )
                        .graphicsLayer { },
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                ) {
                    environment.header?.invoke()
                    body()
                    environment.footer?.invoke()
                }
            }
        }
    }
}

@Composable
private fun PrefetchNextMarkdownBlock(
    document: MarkdownLayoutSource,
    state: LazyListState,
    documentStartIndex: Int,
) {
    LaunchedEffect(document, state, documentStartIndex) {
        snapshotFlow {
            state.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
                ?.minus(documentStartIndex)
        }
            .distinctUntilChanged()
            .collect { lastVisibleDocumentIndex ->
                if (lastVisibleDocumentIndex != null) {
                    document.prefetch(lastVisibleDocumentIndex + 1)
                }
            }
    }
}

@Composable
private fun PaintBlock(block: InternalLayoutBlockModel) {
    val kind = block.surfaceKind()
    val surface = LocalMarkdownComponents.current.blockSurface
    if (kind == null || surface == null) {
        PaintBlockContent(block)
    } else {
        surface(kind) { PaintBlockContent(block) }
    }
}

private fun InternalLayoutBlockModel.surfaceKind(): MarkdownBlockSurfaceKind? = when (this) {
    is LayoutWidgetBlockModel -> when (block) {
        is CodeBlockModel -> MarkdownBlockSurfaceKind.Code
        is DiagramBlockModel -> MarkdownBlockSurfaceKind.Diagram
        else -> null
    }
    is LayoutRenderBlockModel -> block.surfaceKind()
    is LayoutTableBlockModel -> MarkdownBlockSurfaceKind.Table
    is LayoutTabBlockModel -> MarkdownBlockSurfaceKind.Tabs
    is LayoutBibliographyBlockModel -> MarkdownBlockSurfaceKind.Bibliography
    else -> null
}

private fun InternalRenderBlockModel.surfaceKind(): MarkdownBlockSurfaceKind? = when (this) {
    is CodeBlockModel -> MarkdownBlockSurfaceKind.Code
    is DiagramBlockModel -> MarkdownBlockSurfaceKind.Diagram
    is BlockQuoteBlockModel -> MarkdownBlockSurfaceKind.Quote
    is AdmonitionBlockModel -> MarkdownBlockSurfaceKind.Admonition
    is TableBlockModel -> MarkdownBlockSurfaceKind.Table
    is CustomContainerBlockModel -> MarkdownBlockSurfaceKind.CustomContainer
    is TabBlockModel -> MarkdownBlockSurfaceKind.Tabs
    is BibliographyDefinitionBlockModel -> MarkdownBlockSurfaceKind.Bibliography
    else -> null
}

@Composable
private fun PaintBlockContent(block: InternalLayoutBlockModel) {
    when (block) {
        is LayoutRenderBlockModel -> PaintRenderBlock(block)
        is LayoutListBlockModel -> RenderListLayoutBlockModel(
            model = block,
            modifier = Modifier.fillMaxWidth(),
            renderChildren = ::PaintLayoutBlockChildren,
        )

        is LayoutColumnsBlockModel -> RenderColumnsLayoutGroupModel(
            model = block,
            modifier = Modifier.fillMaxWidth(),
            renderChildren = ::PaintLayoutBlockColumn,
        )

        is LayoutTableBlockModel -> RenderTableLayoutBlockModel(
            model = block,
            modifier = Modifier
                .fillMaxWidth()
                .selectableBlock(block, LocalMarkdownSelectionController.current),
        )

        is LayoutDefinitionListBlockModel -> RenderDefinitionListLayoutBlockModel(
            model = block,
            modifier = Modifier.fillMaxWidth(),
            renderTerm = ::PaintBlock,
            renderChildren = ::PaintLayoutBlockChildren,
        )

        is LayoutFigureBlockModel -> RenderFigureLayoutBlockModel(
            model = block,
            modifier = Modifier
                .fillMaxWidth()
                .selectableBlock(block, LocalMarkdownSelectionController.current),
        )

        is LayoutTocBlockModel -> RenderTocLayoutBlockModel(
            model = block,
            modifier = Modifier
                .fillMaxWidth()
                .selectableBlock(block, LocalMarkdownSelectionController.current),
        )

        is LayoutBibliographyBlockModel -> RenderBibliographyLayoutBlockModel(
            model = block,
            modifier = Modifier
                .fillMaxWidth()
                .selectableBlock(block, LocalMarkdownSelectionController.current),
        )

        is LayoutTabBlockModel -> RenderTabLayoutBlockModel(
            model = block,
            modifier = Modifier.fillMaxWidth(),
            renderChildren = ::PaintLayoutBlockColumn,
        )

        is LayoutFootnoteBlockModel -> RenderFootnoteLayoutBlockModel(
            model = block,
            modifier = Modifier.fillMaxWidth(),
            renderLeadChild = { child ->
                if (child != null) {
                    Box {
                        PaintBlock(child)
                    }
                }
            },
            renderTrailingChildren = { children ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                ) {
                    PaintLayoutBlockChildren(children)
                }
            },
        )

        is LayoutWidgetBlockModel -> PaintWidgetBlock(
            block,
            modifier = Modifier
                .fillMaxWidth()
                .selectableBlock(block, LocalMarkdownSelectionController.current),
        )

        is LayoutInlineBlockModel -> PaintInlineBlock(block)
    }
}

@Composable
private fun PaintRenderBlock(block: LayoutRenderBlockModel) {
    when (val renderBlock = block.block) {
        is CodeBlockModel -> FencedCodeBlockRenderer(
            text = renderBlock.code,
            language = renderBlock.language,
            title = renderBlock.title,
            showLineNumbers = renderBlock.showLineNumbers,
            startLine = renderBlock.startLine,
            highlightedLines = renderBlock.highlightedLines,
            modifier = Modifier.fillMaxWidth(),
        )

        is MathBlockModel -> MathBlockRenderer(
            latex = renderBlock.latex,
            modifier = Modifier.fillMaxWidth(),
        )

        is BlockQuoteBlockModel -> RenderBlockQuoteBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
        ) {
            PaintLayoutBlockColumn(block.children)
        }

        is ListBlockModel -> RenderListBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
            renderChildren = ::PaintRenderBlockChildren
        )

        is TableBlockModel -> RenderTableBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
        )

        is AdmonitionBlockModel -> RenderAdmonitionBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (renderBlock.children.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                ) {
                    PaintLayoutBlockChildren(block.children)
                }
            }
        }

        is HtmlBlockModel -> RenderHtmlBlockModel(
            model = renderBlock,
            modifier = Modifier
                .fillMaxWidth()
                .selectableBlock(block, LocalMarkdownSelectionController.current),
        )

        is CustomContainerBlockModel -> RenderCustomContainerBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (block.children.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                ) {
                    PaintLayoutBlockChildren(block.children)
                }
            }
        }

        is ColumnsLayoutBlockModel -> RenderColumnsLayoutBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
            renderChildren = ::PaintRenderBlockColumn
        )

        is DefinitionListBlockModel -> RenderDefinitionListBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
            renderChildren = ::PaintRenderBlockChildren,
        )

        is FootnoteDefinitionBlockModel -> RenderFootnoteDefinitionBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
            renderLeadContent = {},
            renderTrailingContent = {},
        )

        is TocBlockModel -> {
            if (renderBlock.entries.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing / 2),
                ) {
                    renderBlock.entries.forEach { entry ->
                        Text(
                            text = "${"  ".repeat((entry.level - 1).coerceAtLeast(0))}• ${entry.text}",
                            style = LocalMarkdownStyle.current.bodyStyle,
                            color = LocalMarkdownStyle.current.colors.link,
                        )
                    }
                }
            }
        }

        is PageBreakBlockModel -> PageBreakRenderer(
            modifier = Modifier.fillMaxWidth(),
        )

        is DirectiveBlockModel -> {
            val renderer = LocalMarkdownDirectiveRegistry.current
                .findBlockDirectiveRenderer(renderBlock.tagName)
            if (renderer != null) {
                renderer(
                    createDirectiveBlockRenderScope(
                        tagName = renderBlock.tagName,
                        args = renderBlock.args,
                        content = if (block.children.isNotEmpty()) {
                            { PaintLayoutBlockColumn(block.children) }
                        } else {
                            null
                        },
                    )
                )
            } else {
                RenderDirectiveFallbackBlockModel(
                    tagName = renderBlock.tagName,
                    args = renderBlock.args,
                    modifier = Modifier.fillMaxWidth(),
                    renderChildren = if (block.children.isNotEmpty()) {
                        { PaintLayoutBlockChildren(block.children) }
                    } else {
                        null
                    },
                )
            }
        }

        is TabBlockModel -> RenderTabBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
            renderChildren = ::PaintRenderBlockColumn
        )

        is BibliographyDefinitionBlockModel -> RenderBibliographyBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
        )

        is FigureBlockModel -> RenderFigureBlockModel(
            model = renderBlock,
            modifier = Modifier.fillMaxWidth(),
        )

        is DiagramBlockModel -> RenderDiagramBlockWidgetModel(
            model = renderBlock.widget as DiagramBlockWidgetModel,
            modifier = Modifier.fillMaxWidth(),
        )

        is ThematicBreakBlockModel -> ThematicBreakRenderer(
            modifier = Modifier.fillMaxWidth(),
        )

        is FallbackContainerBlockModel -> {
            if (block.children.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                ) {
                    PaintLayoutBlockChildren(block.children)
                }
            }
        }

        is HtmlContainerBlockModel -> {
            if (block.children.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
                ) {
                    PaintLayoutBlockChildren(block.children)
                }
            }
        }

        is FallbackLeafBlockModel -> Unit
        is ParagraphBlockModel,
        is HtmlParagraphBlockModel,
        is HeadingBlockModel -> Unit
    }
}

@Composable
private fun PaintInlineBlock(block: LayoutInlineBlockModel) {
    val selectionController = LocalMarkdownSelectionController.current
    // 普通路径不注册回调；启用搜索时复用 BasicText 的真实布局，避免匹配行定位再次测量。
    val onTextItemLayout = if (selectionController == null ||
        (!selectionController.hasSelection && LocalMarkdownTextSearchState.current == null)
    ) {
        null
    } else {
        remember(block.identity.stableId, selectionController) {
            { placements: List<LayoutInlineRunPlacement>, result: TextLayoutResult ->
                selectionController.registerTextLayout(block.identity.stableId, placements, result)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableBlock(block, selectionController)
    ) {
        PaintInlineLayoutContent(
            block = block,
            modifier = Modifier.fillMaxWidth(),
            onTextItemLayout = onTextItemLayout,
        )
        if (block.showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = LayoutTokens.HeadingDividerSpacing),
                thickness = LocalMarkdownStyle.current.layout.dividerThickness,
                color = LocalMarkdownStyle.current.colors.divider,
            )
        }
    }
}

@Composable
private fun PaintRenderBlockColumn(
    blocks: List<InternalRenderBlockModel>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
    ) {
        PaintRenderBlockChildren(blocks)
    }
}

@Composable
private fun PaintRenderBlockChildren(
    blocks: List<InternalRenderBlockModel>,
) {
    for (block in blocks) {
        key(block.identity.stableId) {
            PaintCompiledBlock(block)
        }
    }
}

@Composable
private fun PaintLayoutBlockColumn(
    blocks: List<InternalLayoutBlockModel>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LocalMarkdownStyle.current.layout.blockSpacing),
    ) {
        PaintLayoutBlockChildren(blocks)
    }
}

@Composable
private fun PaintLayoutBlockChildren(
    blocks: List<InternalLayoutBlockModel>,
) {
    for (block in blocks) {
        key(block.identity.stableId) {
            PaintBlock(block)
        }
    }
}

@Composable
private fun PaintCompiledBlock(block: InternalRenderBlockModel) {
    PaintBlock(
        LayoutRenderBlockModel(
            identity = block.identity,
            frame = LayoutRect(
                left = 0f,
                top = 0f,
                width = 0f,
                height = 0f,
            ),
            contentFrame = LayoutRect(
                left = 0f,
                top = 0f,
                width = 0f,
                height = 0f,
            ),
            block = block,
        )
    )
}

@Composable
private fun PaintWidgetBlock(
    block: LayoutWidgetBlockModel,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    when (val widget = block.widget) {
        is CodeBlockWidgetModel -> FencedCodeBlockRenderer(
            text = widget.code,
            language = widget.language,
            title = widget.title,
            showLineNumbers = (block.block as? CodeBlockModel)?.showLineNumbers ?: true,
            startLine = (block.block as? CodeBlockModel)?.startLine ?: 1,
            highlightedLines = (block.block as? CodeBlockModel)?.highlightedLines ?: emptySet(),
            modifier = modifier,
        )

        is MathBlockWidgetModel -> MathBlockRenderer(
            latex = widget.latex,
            modifier = modifier,
        )

        is DiagramBlockWidgetModel -> RenderDiagramBlockWidgetModel(
            model = widget,
            modifier = modifier,
        )
    }
}
