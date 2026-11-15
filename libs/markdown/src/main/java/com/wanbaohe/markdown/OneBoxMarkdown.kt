package com.wanbaohe.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.wanbaohe.markdown.internal.KmpMarkdownPluginAdapter
import com.wanbaohe.markdown.internal.KmpMarkdownRenderer

@Composable
fun OneBoxMarkdown(
    markdown: String,
    modifier: Modifier = Modifier,
    state: OneBoxMarkdownState = rememberOneBoxMarkdownState(),
    isStreaming: Boolean = false,
    enableScroll: Boolean = true,
    externalScrollState: ScrollState? = null,
    enableSelection: Boolean = true,
    onLinkClick: ((String) -> Unit)? = null,
    textStyle: TextStyle? = null,
    contentColor: Color? = null,
    blockSpacing: Dp? = null,
    components: OneBoxMarkdownComponents = OneBoxMarkdownComponents.Default,
    plugins: List<OneBoxMarkdownPlugin> = emptyList(),
    embeddedViewportPolicy: OneBoxMarkdownPerformancePolicy? = OneBoxMarkdownPerformancePolicy.Default,
) {
    val installedPlugins = LocalOneBoxMarkdownPlugins.current
    val allPlugins = remember(installedPlugins, plugins) { installedPlugins + plugins }
    val directivePlugins = remember(allPlugins) { allPlugins.map(::KmpMarkdownPluginAdapter) }
    val useEmbeddedViewport = !enableScroll &&
        embeddedViewportPolicy?.useEmbeddedViewport(markdown) == true
    LaunchedEffect(state, markdown, isStreaming, useEmbeddedViewport) {
        if (!isStreaming || !useEmbeddedViewport) return@LaunchedEffect
        snapshotFlow { state.contentBlockCount to state.isAtContentEnd }
            .collect { (_, atEnd) ->
                if (atEnd) state.scrollToEnd()
            }
    }

    ApplyOneBoxMarkdownPlugins(
        plugins = allPlugins,
        index = allPlugins.lastIndex,
    ) {
        KmpMarkdownRenderer(
            markdown = markdown,
            modifier = if (useEmbeddedViewport) {
                modifier.heightIn(max = embeddedViewportPolicy.embeddedViewportMaxHeight)
            } else {
                modifier
            },
            state = state,
            isStreaming = isStreaming,
            enableScroll = enableScroll || useEmbeddedViewport,
            externalScrollState = if (useEmbeddedViewport) null else externalScrollState,
            enableSelection = enableSelection,
            onLinkClick = onLinkClick,
            textStyle = textStyle,
            contentColor = contentColor,
            blockSpacing = blockSpacing,
            components = components,
            directivePlugins = directivePlugins,
        )
    }
}

@Composable
private fun ApplyOneBoxMarkdownPlugins(
    plugins: List<OneBoxMarkdownPlugin>,
    index: Int,
    content: @Composable () -> Unit,
) {
    if (index < 0) {
        content()
    } else {
        plugins[index].WrapDocument {
            ApplyOneBoxMarkdownPlugins(plugins, index - 1, content)
        }
    }
}
