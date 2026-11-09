package com.halilibo.richtext.markdown

import com.shifenmiao.model.node.AstImage
import com.shifenmiao.model.node.AstNode

/** 按文档顺序收集 Markdown 图片地址，供画廊左右滑动。 */
fun collectMarkdownImageUrls(nodes: List<AstNode>): List<String> {
    if (nodes.isEmpty()) return emptyList()
    val urls = ArrayList<String>()
    nodes.forEach { collectMarkdownImageUrls(it, urls) }
    return urls
}

/** 按文档顺序收集 [root] 及其子孙里的图片地址。 */
fun collectMarkdownImageUrls(root: AstNode): List<String> {
    val urls = ArrayList<String>()
    collectMarkdownImageUrls(root, urls)
    return urls
}

private fun collectMarkdownImageUrls(node: AstNode, out: MutableList<String>) {
    val type = node.type
    if (type is AstImage && type.destination.isNotBlank()) {
        out.add(type.destination)
    }
    var child = node.links.firstChild
    while (child != null) {
        collectMarkdownImageUrls(child, out)
        child = child.links.next
    }
}
