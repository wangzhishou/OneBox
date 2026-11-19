package com.wanbaohe.markdown

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import com.hrm.markdown.renderer.internal.selection.MarkdownSelectionController

/**
 * 同一屏上的多个 Markdown 文档共享的一组选区控制器。
 *
 * 每条 AI 消息都是一个独立的 Markdown 渲染器，各自持有一份选区控制器；不协调时，
 * 长按第二条消息会再弹一个选区工具条，而上一条的选区与工具条不会消失（"浮动层不是唯一一个"）。
 * 宿主在消息列表外层 provide 一个 group，文档开始选区时向 group 声明自己，group 负责清掉
 * 其它文档的选区，保证同一时刻只有一份 Markdown 选区与一个工具条。
 *
 * 同一屏里可能还并存 Foundation 的 `SelectionContainer` 选区（`TextSelectionMenuContainer`，
 * 全文 Markdown 弹层里也会再套一个）。通过 [registerExternalSelection] 把它们登记进来，
 * 任一 Markdown 文档开始选区时也会一并收起，避免两套选区/工具条同时出现。
 */
@Stable
class MarkdownSelectionGroup {
    private val controllers = mutableListOf<MarkdownSelectionController>()
    private val externalSelectionClearers = mutableListOf<() -> Unit>()

    internal fun register(controller: MarkdownSelectionController) {
        if (controllers.none { it === controller }) controllers += controller
    }

    internal fun unregister(controller: MarkdownSelectionController) {
        controllers.removeAll { it === controller }
    }

    /**
     * 登记一套外部选区（Foundation `SelectionContainer`）的清理动作。
     * 任一 Markdown 文档开始选区时，group 会调用它收起外部选区。
     * 同一个 lambda 实例重复登记只保留一份，[unregisterExternalSelection] 用同一实例注销。
     */
    fun registerExternalSelection(clear: () -> Unit) {
        if (externalSelectionClearers.none { it === clear }) externalSelectionClearers += clear
    }

    fun unregisterExternalSelection(clear: () -> Unit) {
        externalSelectionClearers.removeAll { it === clear }
    }

    /** 某文档开始选区：收起其它文档与外部选区的选区。 */
    internal fun claim(controller: MarkdownSelectionController) {
        externalSelectionClearers.toList().forEach { clear -> clear() }
        controllers.toList().forEach { member ->
            if (member !== controller) member.clearSelection()
        }
    }

    /** 外部选区（Foundation 的 SelectionContainer）开始时清空所有 Markdown 文档的选区。 */
    fun clearAll() {
        controllers.toList().forEach { it.clearSelection() }
    }
}

/** 当前屏幕共享的 Markdown 选区组；为 null 时各文档的选区互不协调。 */
val LocalMarkdownSelectionGroup = compositionLocalOf<MarkdownSelectionGroup?> { null }
