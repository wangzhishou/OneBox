package com.wanbaohe.markdown.internal

import com.hrm.markdown.runtime.DirectiveBlockRenderScope
import com.hrm.markdown.runtime.DirectiveInlineRenderScope
import com.hrm.markdown.runtime.MarkdownBlockDirectiveRenderer
import com.hrm.markdown.runtime.MarkdownDirectivePlugin
import com.hrm.markdown.runtime.MarkdownInlineDirectiveRenderer
import com.hrm.markdown.runtime.MarkdownInputTransformer
import com.hrm.markdown.runtime.MarkdownTransformResult
import com.hrm.markdown.runtime.MarkdownTransformerStreamingSupport
import com.wanbaohe.markdown.OneBoxMarkdownDirective
import com.wanbaohe.markdown.OneBoxMarkdownInputTransformer
import com.wanbaohe.markdown.OneBoxMarkdownPlugin
import com.wanbaohe.markdown.OneBoxMarkdownStreamingSupport

internal class KmpMarkdownPluginAdapter(
    private val plugin: OneBoxMarkdownPlugin,
) : MarkdownDirectivePlugin {
    override val id: String = plugin.id

    override val inputTransformers: List<MarkdownInputTransformer> by lazy(LazyThreadSafetyMode.NONE) {
        plugin.inputTransformers.map(::KmpInputTransformerAdapter)
    }

    override val blockDirectiveRenderers: Map<String, MarkdownBlockDirectiveRenderer> by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        plugin.blockRenderers.mapValues { (_, renderer) ->
            { scope: DirectiveBlockRenderScope ->
                renderer(
                    com.wanbaohe.markdown.OneBoxMarkdownBlockScope(
                        directive = scope.directive.toOneBoxDirective(),
                        content = scope.content,
                    )
                )
            }
        }
    }

    override val inlineDirectiveRenderers: Map<String, MarkdownInlineDirectiveRenderer> by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        plugin.inlineRenderers.mapValues { (_, renderer) ->
            { scope: DirectiveInlineRenderScope ->
                renderer(
                    com.wanbaohe.markdown.OneBoxMarkdownInlineScope(
                        directive = scope.directive.toOneBoxDirective(),
                        alternateText = scope.directive.alternateText,
                    )
                )
            }
        }
    }
}

private class KmpInputTransformerAdapter(
    private val transformer: OneBoxMarkdownInputTransformer,
) : MarkdownInputTransformer {
    override val id: String = transformer.id
    override val streamingSupport: MarkdownTransformerStreamingSupport
        get() = when (transformer.streamingSupport) {
            OneBoxMarkdownStreamingSupport.RestartOnRewrite ->
                MarkdownTransformerStreamingSupport.RestartOnRewrite

            OneBoxMarkdownStreamingSupport.AppendSafe -> MarkdownTransformerStreamingSupport.AppendSafe
            OneBoxMarkdownStreamingSupport.FullRebuild -> MarkdownTransformerStreamingSupport.Unsupported
        }

    override fun transform(input: String): MarkdownTransformResult = MarkdownTransformResult(
        markdown = transformer.transform(input)
    )
}

private fun com.hrm.markdown.runtime.DirectiveBlockSnapshot.toOneBoxDirective() = OneBoxMarkdownDirective(
    tagName = tagName,
    args = args,
    hasContent = hasContent,
)

private fun com.hrm.markdown.runtime.DirectiveInlineSnapshot.toOneBoxDirective() = OneBoxMarkdownDirective(
    tagName = tagName,
    args = args,
    hasContent = false,
)
