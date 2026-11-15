package com.hrm.markdown.renderer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
internal class MarkdownNavigationController(
    private val coroutineScope: CoroutineScope,
) {
    val footnoteNavigationState = FootnoteNavigationState()
    var renderMode: MarkdownRenderMode = MarkdownRenderMode.StaticColumn
    var enableScroll: Boolean = true
    var scrollState: ScrollState? = null
    var lazyListState: LazyListState? = null
    var footnoteDefinitionItemIndexes: Map<String, Int> = emptyMap()
    var documentStartIndex: Int = 0
    var onLinkClick: ((String) -> Unit)? = null

    val linkClickDelegate: (String) -> Unit = { target: String ->
        onLinkClick?.invoke(target)
    }

    val onFootnoteClick: (String) -> Unit = { label: String ->
        coroutineScope.launch {
            when (renderMode) {
                MarkdownRenderMode.LazyColumn -> {
                    val lazyState = lazyListState ?: return@launch
                    footnoteNavigationState.rememberLazyListPosition(
                        label = label,
                        index = lazyState.firstVisibleItemIndex,
                        offset = lazyState.firstVisibleItemScrollOffset,
                    )
                    val targetIndex = footnoteDefinitionItemIndexes[label]
                    if (enableScroll && targetIndex != null) {
                        lazyState.animateScrollToItem(
                            resolveLazyDocumentItemIndex(targetIndex, documentStartIndex)
                        )
                        withFrameNanos { }
                    }
                    if (!footnoteNavigationState.bringDefinitionIntoView(label)) {
                        onLinkClick?.invoke("#fn-$label")
                    }
                }

                else -> {
                    val currentScrollState = scrollState ?: return@launch
                    footnoteNavigationState.rememberReturnPosition(label, currentScrollState.value)

                    if (!footnoteNavigationState.bringDefinitionIntoView(label)) {
                        onLinkClick?.invoke("#fn-$label")
                    }
                }
            }
        }
    }

    val onFootnoteBackClick: (String) -> Unit = { label: String ->
        coroutineScope.launch {
            val returnPosition = footnoteNavigationState.getReturnPosition(label)
            if (returnPosition != null && enableScroll) {
                when (returnPosition) {
                    is FootnoteReturnPosition.Scroll -> {
                        val state = scrollState ?: return@launch
                        state.animateScrollTo(returnPosition.value)
                        return@launch
                    }

                    is FootnoteReturnPosition.LazyList ->
                        lazyListState?.animateScrollToItem(
                            returnPosition.index,
                            returnPosition.offset
                        )
                }
            }
        }
    }
}

internal fun resolveLazyDocumentItemIndex(documentIndex: Int, documentStartIndex: Int): Int =
    documentIndex + documentStartIndex

@Composable
internal fun rememberMarkdownNavigationController(
    renderMode: MarkdownRenderMode,
    enableScroll: Boolean,
    scrollState: ScrollState,
    lazyListState: LazyListState,
    onLinkClick: ((String) -> Unit)?,
): MarkdownNavigationController {
    val coroutineScope = rememberCoroutineScope()
    val controller = remember(coroutineScope) { MarkdownNavigationController(coroutineScope) }
    controller.renderMode = renderMode
    controller.enableScroll = enableScroll
    controller.scrollState = scrollState
    controller.lazyListState = lazyListState
    controller.onLinkClick = onLinkClick
    return controller
}
