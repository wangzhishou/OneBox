package com.shifenmiao.database.item.entity

/**
 * item_data 的内容类型。决定渲染路径（HTML / Markdown / URL 重定向 / 富 JSON）。
 * - data / url 同时存时由 kind 决定主路径，另一字段作辅助。
 */
enum class ItemDataKind {
    HTML,
    MARKDOWN,
    URL,
    JSON
}
