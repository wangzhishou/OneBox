package com.shifenmiao.database.item.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity

/**
 * item + 关联表（categories / agent / prompt / data / userState）。
 *
 * 所有资源都通过 Junction + @Relation 自动加载，调用方不再需要手动 fill。
 */
data class ItemWithRelation(
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
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ItemAgentLink::class,
            parentColumn = "item_id",
            entityColumn = "agent_id"
        )
    )
    var agent: ItemAgentEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ItemPromptLink::class,
            parentColumn = "item_id",
            entityColumn = "prompt_id"
        )
    )
    var prompt: PromptEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ItemDataLink::class,
            parentColumn = "item_id",
            entityColumn = "data_id"
        )
    )
    var data: ItemDataEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "item_id"
    )
    var userState: ItemUserState? = null,
)
