package com.wanbaohe.markdown

import com.hrm.markdown.renderer.MarkdownHtml

object OneBoxMarkdownHtml {
    fun render(markdown: String): String = MarkdownHtml.render(markdown)
}
