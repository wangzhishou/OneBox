package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType

internal fun buildMarkdownLink(label: String, deeplink: String): String {
    return "[${sanitizeMarkdownText(label)}]($deeplink)"
}

internal fun centsToYuanText(amountCents: Long): String = "%.2f".format(amountCents / 100.0)

internal fun BookkeepingRecordType.displayName(textProvider: AgentToolTextProvider): String = when (this) {
    BookkeepingRecordType.EXPENSE -> textProvider.string(R.string.agent_tool_bookkeeping_type_expense)
    BookkeepingRecordType.INCOME -> textProvider.string(R.string.agent_tool_bookkeeping_type_income)
    BookkeepingRecordType.EXCLUDED -> textProvider.string(R.string.agent_tool_bookkeeping_type_excluded)
}

internal fun categoryNameOrDefault(
    categoryName: String?,
    textProvider: AgentToolTextProvider,
): String {
    return sanitizeMarkdownText(
        categoryName ?: textProvider.string(R.string.agent_tool_bookkeeping_uncategorized)
    )
}

internal fun sanitizeMarkdownText(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\r\n", " ")
        .replace("\n", " ")
        .replace("[", "\\[")
        .replace("]", "\\]")
}

internal fun StringBuilder.appendMarkdownSection(title: String, block: StringBuilder.() -> Unit) {
    appendLine()
    appendLine("## ${sanitizeMarkdownText(title)}")
    block()
}

internal fun StringBuilder.appendMarkdownBullet(label: String, value: String) {
    appendLine("- ${sanitizeMarkdownText(label)}：${sanitizeMarkdownText(value)}")
}

