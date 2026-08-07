package com.shifenmiao.database.data_draft.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 通用草稿实体，统一存储所有类型的编辑草稿。
 *
 * @param id              自增主键
 * @param title           标题
 * @param description     描述
 * @param url             URL（HTML类型使用）
 * @param data            大文本内容（HTML/Markdown/rawJson等）
 * @param selectedCategoryIds  选中分类ID的JSON数组字符串，如 "[1,2,3]"
 * @param draftType       草稿类型，对应 [com.shifenmiao.model.ListItemType.id]
 *                        HTML=2, PROMPT=4, AGENT=6, NOTE=8
 * @param status          草稿状态: 0=草稿 1=生成成功 2=生成失败
 * @param itemId          关联的 ItemEntity.id（编辑已有条目时非空）
 * @param relatedEntityId 关联的业务实体ID（如 agentId / promptId），成功保存后回填
 * @param updateTime      最后更新时间（毫秒时间戳）
 */
@Entity(tableName = "data_draft")
data class DataDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val data: String = "",
    @ColumnInfo(name = "selected_category_ids")
    val selectedCategoryIds: String = "[]",
    @ColumnInfo(name = "draft_type")
    val draftType: Int = 0,
    val status: Int = STATUS_DRAFT,
    @ColumnInfo(name = "item_id")
    val itemId: Int? = null,
    @ColumnInfo(name = "related_entity_id")
    val relatedEntityId: Int? = null,
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis(),
) {
    companion object {
        const val STATUS_DRAFT = 0
        const val STATUS_SUCCESS = 1
        const val STATUS_FAILED = 2
    }
}

