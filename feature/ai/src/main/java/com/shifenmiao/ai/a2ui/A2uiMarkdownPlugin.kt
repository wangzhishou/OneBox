package com.shifenmiao.ai.a2ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.wanbaohe.markdown.OneBoxMarkdownBlockRenderer
import com.wanbaohe.markdown.OneBoxMarkdownCodeActions
import com.wanbaohe.markdown.OneBoxMarkdownCodeFenceTransformer
import com.wanbaohe.markdown.OneBoxMarkdownPlugin

val LocalA2uiRenderProvider = compositionLocalOf<A2uiRenderProvider?> { null }
val LocalA2uiSubmitHandler = compositionLocalOf<((String) -> Unit)?> { null }

@Composable
fun rememberA2uiMarkdownPlugin(
    isStreaming: Boolean,
    codeActions: OneBoxMarkdownCodeActions,
): OneBoxMarkdownPlugin {
    val renderProvider = LocalA2uiRenderProvider.current
    val submitHandler = LocalA2uiSubmitHandler.current
    val currentStreaming by rememberUpdatedState(isStreaming)
    val currentCodeActions by rememberUpdatedState(codeActions)
    return remember(renderProvider, submitHandler) {
        object : OneBoxMarkdownPlugin {
            override val id: String = "a2ui"
            override val inputTransformers = listOf(
                OneBoxMarkdownCodeFenceTransformer(
                    id = "a2ui-fence",
                    directiveName = "a2ui",
                    languages = setOf("a2ui", "uijson"),
                )
            )
            override val blockRenderers: Map<String, OneBoxMarkdownBlockRenderer> = mapOf(
                "a2ui" to { scope ->
                    A2uiMarkdownCard(
                        source = decodeA2uiSource(scope.directive.args["payload"].orEmpty()),
                        renderProvider = renderProvider,
                        isStreaming = currentStreaming,
                        onCopy = { currentCodeActions.copy(it) },
                        onSubmit = { submitHandler?.invoke(it) },
                    )
                }
            )
        }
    }
}

private fun decodeA2uiSource(payload: String): String = runCatching {
    String(android.util.Base64.decode(payload, android.util.Base64.DEFAULT), Charsets.UTF_8)
}.getOrDefault("")
