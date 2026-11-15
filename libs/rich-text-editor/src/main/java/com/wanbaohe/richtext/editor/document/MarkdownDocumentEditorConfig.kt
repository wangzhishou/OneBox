package com.wanbaohe.richtext.editor.document

import com.wanbaohe.richtext.editor.ui.RichTextEditorBehaviorConfig
import com.wanbaohe.richtext.editor.ui.RichTextEditorVisualConfig

data class MarkdownDocumentEditorConfig(
    val registry: MarkdownBlockRegistry = MarkdownBlockRegistry.Default,
    val richTextBehavior: RichTextEditorBehaviorConfig = RichTextEditorBehaviorConfig(),
    val richTextVisual: RichTextEditorVisualConfig = RichTextEditorVisualConfig(),
)
