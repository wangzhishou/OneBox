package com.shifenmiao.common.components.category

import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.Source

/**
 * Wrapper for Category to implement ManageableItem
 * - 排序语义已删除；拖拽重排后续若需要可接 fractional indexing / 服务端字段。
 */
data class CategoryItem(
    val category: Category
) : ManageableItem {
    override val id: Int get() = category.id
    override val name: String get() = category.name
    override val order: Int get() = 0
    override val canEdit: Boolean? get() = category.canEdit
    override val source: Source get() = category.source
}

fun Category.toManageableItem(): CategoryItem = CategoryItem(this)
fun CategoryItem.toCategory(): Category = category
