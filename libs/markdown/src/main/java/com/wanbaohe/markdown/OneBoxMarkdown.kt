package com.wanbaohe.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    components: OneBoxMarkdownComponents = OneBoxMarkdownComponents.Default,
    plugins: List<OneBoxMarkdownPlugin> = emptyList(),
) {
    val installedPlugins = LocalOneBoxMarkdownPlugins.current
    val allPlugins = remember(installedPlugins, plugins) { installedPlugins + plugins }
    val directivePlugins = remember(allPlugins) { allPlugins.map(::KmpMarkdownPluginAdapter) }

    ApplyOneBoxMarkdownPlugins(
        plugins = allPlugins,
        index = allPlugins.lastIndex,
    ) {
        KmpMarkdownRenderer(
            markdown = markdown,
            modifier = modifier,
            state = state,
            isStreaming = isStreaming,
            enableScroll = enableScroll,
            externalScrollState = externalScrollState,
            enableSelection = enableSelection,
            onLinkClick = onLinkClick,
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
