package com.shifenmiao.database.item.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Lightweight version of ItemWithRelation for list display.
 * Does NOT include agent and prompt relations to reduce memory usage.
 * Use ItemWithRelation only when navigating to detail pages where agent/prompt data is needed.
 *
 * 携带 userState 是为了让 [com.shifenmiao.common.handle.HandleEvent] 等中间层
 * 能直接读 requiresAuth / isFavorited 等本地用户态字段,避免每个调用点重复 DAO 查询。
 */
data class ItemWithCategories(
    @Embedded var item: ItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemCategoryCrossRef::class,
            parentColumn = "item_id",
            entityColumn = "category_id"
        )
    )
    var categories: List<Category> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    var userState: ItemUserState? = null,
)

val ItemWithCategories.requiresAuth: Boolean
    get() = userState?.requiresAuth == true

