package com.wanbaohe.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

interface OneBoxMarkdownPlugin {
    val id: String
    val inputTransformers: List<OneBoxMarkdownInputTransformer> get() = emptyList()
    val blockRenderers: Map<String, OneBoxMarkdownBlockRenderer> get() = emptyMap()
    val inlineRenderers: Map<String, OneBoxMarkdownInlineRenderer> get() = emptyMap()

    @Composable
    fun WrapDocument(content: @Composable () -> Unit) {
        content()
    }
}

interface OneBoxMarkdownInputTransformer {
    val id: String
    val streamingSupport: OneBoxMarkdownStreamingSupport
        get() = OneBoxMarkdownStreamingSupport.RestartOnRewrite

    fun transform(input: String): String
}

enum class OneBoxMarkdownStreamingSupport {
    RestartOnRewrite,
    AppendSafe,
    FullRebuild,
}

@Immutable
data class OneBoxMarkdownDirective(
    val tagName: String,
    val args: Map<String, String>,
    val hasContent: Boolean,
)

data class OneBoxMarkdownBlockScope(
    val directive: OneBoxMarkdownDirective,
    val content: (@Composable () -> Unit)?,
)

data class OneBoxMarkdownInlineScope(
    val directive: OneBoxMarkdownDirective,
    val alternateText: String,
)

typealias OneBoxMarkdownBlockRenderer = @Composable (OneBoxMarkdownBlockScope) -> Unit
typealias OneBoxMarkdownInlineRenderer = @Composable (OneBoxMarkdownInlineScope) -> Unit

val LocalOneBoxMarkdownPlugins = compositionLocalOf<List<OneBoxMarkdownPlugin>> { emptyList() }

@Composable
fun ProvideOneBoxMarkdownPlugins(
    plugins: List<OneBoxMarkdownPlugin>,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalOneBoxMarkdownPlugins provides plugins, content = content)
}
