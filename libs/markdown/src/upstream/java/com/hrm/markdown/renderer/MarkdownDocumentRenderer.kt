package com.hrm.markdown.renderer

import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.rememberTextMeasurer
import com.hrm.markdown.parser.ast.Document
import com.hrm.markdown.renderer.internal.LocalMarkdownInteractions
import com.hrm.markdown.renderer.internal.MarkdownEngineHost
import com.hrm.markdown.renderer.internal.MarkdownInteractionController
import com.hrm.markdown.renderer.internal.RendererFacadeState
import com.hrm.markdown.renderer.internal.compose.ComposeRenderEnvironment
import com.hrm.markdown.renderer.internal.layout.engine.EagerMarkdownLayoutSource
import com.hrm.markdown.renderer.internal.selection.LocalMarkdownSelectionController
import com.hrm.markdown.renderer.internal.selection.SelectionHandlesHost
import com.hrm.markdown.renderer.internal.selection.SelectionToolbarHost
import com.hrm.markdown.renderer.internal.selection.markdownSelectionGestures
import com.hrm.markdown.renderer.internal.selection.rememberMarkdownSelectionController
import com.hrm.markdown.renderer.internal.toCompileEnvironment
import com.hrm.markdown.runtime.MarkdownDirectiveRegistry
import com.hrm.markdown.runtime.MarkdownSourceMap

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun MarkdownDocumentRenderer(
    document: Document,
    modifier: Modifier = Modifier,
    theme: ResolvedMarkdownStyle = ResolvedMarkdownStyle(),
    config: MarkdownRenderOptions = MarkdownRenderOptions.Default,
    state: MarkdownState = rememberMarkdownState(),
    isStreaming: Boolean = false,
    enableScroll: Boolean = true,
    enableSelection: Boolean = true,
    directiveRegistry: MarkdownDirectiveRegistry = MarkdownDirectiveRegistry.Empty,
    sourceMap: MarkdownSourceMap = MarkdownSourceMap.Identity,
    components: MarkdownComponents = MarkdownComponents.Default,
    interactions: MarkdownInteractions = MarkdownInteractions.Default,
    textSearchState: MarkdownTextSearchState? = null,
    externalScrollState: ScrollState? = null,
) {
    DisposableEffect(state) { onDispose { state.clearSelectionAction = null; state.scrollEnabled = false } }
    val latexState = rememberMarkdownLatexState(config.latexConfig)
    val renderMode = remember(enableScroll) {
        resolveMarkdownRenderMode(enableScroll = enableScroll)
    }
    val lazyListState = state.lazyListState
    val scrollState = if (enableScroll) state.scrollState else externalScrollState ?: state.scrollState
    val header = components.header
    val footer = components.footer
    SideEffect {
        state.headerItems = if (header == null) 0 else 1
        state.footerItems = if (footer == null) 0 else 1
        state.scrollEnabled = enableScroll
    }
    // A parser commit is the only render input. Revision lookup is O(1); no semantic tree scan
    // runs during composition. Flavour transformations finish before this publication exists.
    val publication = requireNotNull(document.publication) {
        "Markdown requires a parser publication; construct custom syntax through MarkdownFlavour"
    }
    require(publication.isCurrent) { "Pass the latest MarkdownParser.document to Markdown" }
    ProvideMarkdownStyle(theme) {
        val engineHost = remember { MarkdownEngineHost() }
        val interactionController = remember { MarkdownInteractionController() }
        val uriHandler = LocalUriHandler.current
        val currentOnLinkClick = rememberUpdatedState(interactions.onLinkClick ?: uriHandler::openUri)
        val stableOnLinkClick: (String) -> Unit = remember {
            { target -> currentOnLinkClick.value.invoke(target) }
        }
        val facadeState = remember(
            theme,
            config,
            directiveRegistry,
            isStreaming,
        ) {
            RendererFacadeState(
                theme = theme,
                config = config,
                directiveRegistry = directiveRegistry,
                isStreaming = isStreaming,
            )
        }
        val compileEnvironment = facadeState.toCompileEnvironment()
        val internalRenderDocument = remember(
            engineHost,
            document,
            publication.lineageId,
            publication.revision,
            compileEnvironment,
        ) {
            engineHost.compile(
                document = document,
                facadeState = facadeState,
            )
        }
        val layoutRenderDocument = resolveInlineEndMarkers(internalRenderDocument, interactions.inlineDecoration)
        SideEffect { interactionController.update(internalRenderDocument, interactions) }
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val latexMeasurer = latexState.measurer
        val diagramHostRegistry = remember { DiagramHostRegistry() }
        BoxWithConstraints(modifier = modifier) {
            val viewportWidthPx = with(density) { maxWidth.toPx() }
            val blockSpacingPx = with(density) { theme.layout.blockSpacing.toPx() }
            val textMeasurer = rememberTextMeasurer()
            val selectionController = if (enableSelection || textSearchState != null) {
                val selectionScope = rememberCoroutineScope()
                rememberMarkdownSelectionController(selectionScope, textMeasurer)
            } else {
                null
            }
            selectionController?.bindClipboard(LocalClipboard.current)
            SideEffect {
                state.blockCount = internalRenderDocument.blocks.size
                state.clearSelectionAction = { selectionController?.clearSelection() }
            }
            val navigationController = rememberMarkdownNavigationController(
                renderMode = renderMode,
                enableScroll = enableScroll || externalScrollState != null,
                scrollState = scrollState,
                lazyListState = lazyListState,
                onLinkClick = stableOnLinkClick,
            )
            val layoutSource = remember(
                engineHost,
                layoutRenderDocument,
                facadeState,
                renderMode,
                viewportWidthPx,
                blockSpacingPx,
                density,
                layoutDirection,
                textMeasurer,
                latexMeasurer,
                diagramHostRegistry,
                components.codeBlockHeader?.height,
                components.tableToolbar?.height,
                components.inlineCode,
            ) {
                if (renderMode == MarkdownRenderMode.LazyColumn) {
                    engineHost.layoutSession(
                        renderDocument = layoutRenderDocument,
                        facadeState = facadeState,
                        viewportWidth = viewportWidthPx,
                        blockSpacing = blockSpacingPx,
                        onLinkClick = navigationController.linkClickDelegate,
                        onFootnoteClick = navigationController.onFootnoteClick,
                        density = density,
                        textMeasurer = textMeasurer,
                        latexMeasurer = latexMeasurer,
                        diagramHostRegistry = diagramHostRegistry,
                        layoutDirection = layoutDirection,
                        components = components,
                    )
                } else {
                    EagerMarkdownLayoutSource(
                        engineHost.layout(
                            renderDocument = layoutRenderDocument,
                            facadeState = facadeState,
                            viewportWidth = viewportWidthPx,
                            blockSpacing = blockSpacingPx,
                            onLinkClick = navigationController.linkClickDelegate,
                            onFootnoteClick = navigationController.onFootnoteClick,
                            density = density,
                            textMeasurer = textMeasurer,
                            latexMeasurer = latexMeasurer,
                            diagramHostRegistry = diagramHostRegistry,
                            layoutDirection = layoutDirection,
                            components = components,
                        )
                    )
                }
            }
            navigationController.footnoteDefinitionItemIndexes =
                layoutSource.metadata.footnoteDefinitionItemIndexes
            navigationController.documentStartIndex = if (header == null) 0 else 1
            if (selectionController != null) {
                val searchDocumentKey = remember(internalRenderDocument, publication) { Any() }
                SideEffect {
                    selectionController.updateDocument(internalRenderDocument.blocks, textSearchState != null)
                    textSearchState?.bind(selectionController, searchDocumentKey, publication.revision, state)
                }
                DisposableEffect(selectionController, textSearchState) {
                    onDispose { textSearchState?.unbind(selectionController) }
                }
            }
            Box(
                modifier = if (selectionController != null) {
                    Modifier
                        .onGloballyPositioned { selectionController.registry.setRoot(it) }
                        .then(if (enableSelection) Modifier.markdownSelectionGestures(selectionController) else Modifier)
                } else {
                    Modifier
                },
            ) {
                CompositionLocalProvider(
                    LocalBringIntoViewSpec provides if (enableScroll && textSearchState != null) {
                        rememberMarkdownTextSearchBringIntoViewSpec(textSearchState)
                    } else {
                        LocalBringIntoViewSpec.current
                    },
                    LocalMarkdownSelectionController provides selectionController,
                    LocalMarkdownTextSearchState provides textSearchState,
                    LocalMarkdownInteractions provides interactionController,
                    LocalMarkdownComponents provides components,
                    LocalMarkdownSourceMap provides sourceMap,
                    LocalMarkdownLatexState provides latexState,
                ) {
                    ProvideRendererContext(
                        document = document,
                        onLinkClick = stableOnLinkClick,
                        onFootnoteClick = navigationController.onFootnoteClick,
                        onFootnoteBackClick = navigationController.onFootnoteBackClick,
                        footnoteNavigationState = navigationController.footnoteNavigationState,
                        imageContent = components.image,
                        config = config,
                        isStreaming = isStreaming,
                        directiveRegistry = directiveRegistry,
                        diagramHostRegistry = diagramHostRegistry,
                    ) {
                        engineHost.composePainter.Paint(
                            document = layoutSource,
                            environment = ComposeRenderEnvironment(
                                modifier = Modifier.fillMaxWidth(),
                                renderMode = renderMode,
                                enableScroll = enableScroll,
                                scrollState = scrollState,
                                lazyListState = lazyListState,
                                header = header,
                                footer = footer,
                            ),
                        )
                    }
                }
                if (enableSelection && selectionController != null) {
                    SelectionHandlesHost(selectionController)
                    SelectionToolbarHost(selectionController, components.selectionToolbar)
                }
            }
        }
    }
}
