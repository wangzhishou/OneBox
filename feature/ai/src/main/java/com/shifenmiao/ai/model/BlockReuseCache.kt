package com.shifenmiao.ai.model

import com.shifenmiao.model.node.AstFencedCodeBlock
import com.shifenmiao.model.node.AstHeading
import com.shifenmiao.model.node.AstHtmlBlock
import com.shifenmiao.model.node.AstHtmlInline
import com.shifenmiao.model.node.AstIndentedCodeBlock
import com.shifenmiao.model.node.AstJLatexBlockMath
import com.shifenmiao.model.node.AstJLatexNodeMath
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstText

/**
 * 流式渲染期间，按 "messageKey + blockIndex + contentSignature" 复用上一轮 parse 出来的 [AstNode]
 * 引用，让下游 `MarkdownRichText` 的 `remember(astNode, ...)` 能真正命中缓存。
 *
 * 背景（详见 AIChatComponent 注释）：
 * - 每个 SSE chunk 都会触发一次 `parser.parse(整段 answer)`，产出全新的 AstNode 树。
 * - `AstNodeLinks.equals` 用 === 引用比较，新树中所有节点引用都不同 → AstNode.equals 永远返回
 *   false → `remember(astNode)` 永远 cache miss → `computeRichTextString` 在 Main 线程上
 *   对每个段落每个 chunk 都重算一次。长消息下 Main 线程被打爆，表现为"前几个字显示之后就停住，
 *   流结束时再一次性把剩下的内容刷出来"。
 *
 * 复用逻辑：
 *   1. `addRobotContent` 把整段 answer 拆成块 (`MarkdownBlock`)；
 *   2. 对每个块计算"字面文本签名"（递归收集 AstText / 普通代码块 literal / latex 等可读文本）；
 *   3. 与上一次 render 同一 (messageKey, blockIndex) 的签名比对：
 *      - 命中 → 把新 block 的 `node` 字段替换为缓存的 AstNode 引用（同一个对象）；
 *      - 未命中 → 把新的 (sig, node) 写回缓存。
 *
 * 仅在流式状态下使用；NORMAL 状态消息每次 render 通过 `cachedMessageUiModels` 整体复用，
 * 不需要二次签名比对。线程安全要求与 `AIChatComponent.cachedMessageUiModels` 一致：
 * 仅在 doRenderMessage 调用链上使用，Mutex 化由调用方保证（这里实际上只在
 * componentScope 的单一 IO 线程上读写，无并发）。
 */
class BlockReuseCache {
    private data class Slot(val signature: String, val node: AstNode)

    private val perMessage = HashMap<String, HashMap<Int, Slot>>()

    /**
     * 尝试用缓存里"内容签名一致"的旧 AstNode 替换 [newNode]。
     * @return 复用的旧节点（或 newNode 本身，如果签名不一致 / 无缓存）。
     *         调用方应把返回值写回 MarkdownBlock.node 字段。
     */
    fun reuseOrPut(messageKey: String, blockIndex: Int, newNode: AstNode): AstNode {
        val sig = computeSignature(newNode)
        val slots = perMessage.getOrPut(messageKey) { HashMap() }
        val existing = slots[blockIndex]
        return if (existing != null && existing.signature == sig) {
            existing.node
        } else {
            slots[blockIndex] = Slot(sig, newNode)
            newNode
        }
    }

    /**
     * 删除 [messageKey] 下索引超出 [validIndexExclusive] 的旧条目。
     * 流式期间块数量可能减少（极少见，例如"重写中段落合并"），避免缓存膨胀。
     */
    fun trim(messageKey: String, validIndexExclusive: Int) {
        val slots = perMessage[messageKey] ?: return
        val it = slots.keys.iterator()
        while (it.hasNext()) {
            if (it.next() >= validIndexExclusive) it.remove()
        }
    }

    fun clear(messageKey: String? = null) {
        if (messageKey == null) perMessage.clear()
        else perMessage.remove(messageKey)
    }

    /**
     * 计算块字面内容的稳定签名。
     * 仅收集"会被实际渲染出来的可见文本"——文本节点、代码块 literal、latex、html literal、
     * heading level；忽略所有引用类字段（AstNodeLinks 引用、parent 等）。
     * 用 StringBuilder 拼接 + 类型 tag 分隔，避免 "abc" + "def" 与 "ab" + "cdef" 撞 hash。
     */
    private fun computeSignature(node: AstNode): String {
        val sb = StringBuilder(64)
        appendNodeSignature(sb, node)
        return sb.toString()
    }

    private fun appendNodeSignature(sb: StringBuilder, node: AstNode?) {
        node ?: return
        when (val t = node.type) {
            is AstText -> sb.append("|t:").append(t.literal)
            is AstFencedCodeBlock -> {
                val info = t.info.trim().lowercase()
                // `a2ui` 在流式阶段只展示稳定骨架，不需要把持续增长的 JSON literal
                // 纳入签名；否则每个 token 都会让节点复用失效，引起聊天气泡频繁重组抖动。
                // "uijson" 为旧标记，向后兼容。
                if (info == "a2ui" || info == "uijson") {
                    sb.append("|fc:a2ui")
                } else {
                    sb.append("|fc:").append(t.info).append(":").append(t.literal)
                }
            }
            is AstIndentedCodeBlock -> sb.append("|ic:").append(t.literal)
            is AstHtmlBlock -> sb.append("|hb:").append(t.literal)
            is AstHtmlInline -> sb.append("|hi:").append(t.literal)
            is AstJLatexBlockMath -> sb.append("|lb:").append(t.latex.orEmpty())
            is AstJLatexNodeMath -> sb.append("|li:").append(t.latex.orEmpty())
            is AstHeading -> sb.append("|h:").append(t.level)
            else -> sb.append("|n:").append(t::class.java.simpleName)
        }
        // 递归子节点；不依赖 parent 引用，避免引用噪声进入签名
        var child = node.links.firstChild
        sb.append('[')
        while (child != null) {
            appendNodeSignature(sb, child)
            child = child.links.next
        }
        sb.append(']')
    }
}

