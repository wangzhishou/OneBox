package com.shifenmiao.database.item.entity

import androidx.room.Embedded
import androidx.room.Relation
import java.util.Date

/**
 * 列表用：item + categories + clickStat + userState。
 *
 * 不再携带 agent / prompt / data 大资源，详情页才走 ItemWithRelation。
 */
data class ItemWithCategoriesAndStats(
    @Embedded var item: ItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
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
    var clickStat: ItemClickStatEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    var userState: ItemUserState? = null,
) {
    val clickCount: Int
        get() = clickStat?.clickCount ?: 0

    val clickTime: Date
        get() = clickStat?.clickTimeAsDate ?: EPOCH_TIME

    val isFavorited: Boolean
        get() = userState?.isFavorited == true

    val isPinned: Boolean
        get() = userState?.isPinned == true

    val requiresAuth: Boolean
        get() = userState?.requiresAuth == true

    fun toItemWithCategories(): ItemWithCategories = ItemWithCategories(
        item = item,
        categories = categories,
        userState = userState,
    )

    private companion object {
        val EPOCH_TIME = Date(0)
    }
}
