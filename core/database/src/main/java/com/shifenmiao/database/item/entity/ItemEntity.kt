package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.Source

/**
 * 统一条目主表（瘦身后）：
 * - 内容数据：title / description / url / miniProgramId / placeholder / icon*
 * - 服务端标记：recommend / vipLevel / isHighlighted / isOnline / isAi
 * - 同步键：source + remoteId
 *
 * 不再持有的字段（已迁出）：
 * - isFavorited / isPinned / pinnedTime / canEdit → [ItemUserState]
 * - 大文本 data → [ItemDataEntity]（通过 [ItemDataLink]）
 * - agent 资源 → [ItemAgentEntity]（通过 [ItemAgentLink]）
 * - prompt 资源 → [PromptEntity]（通过 [ItemPromptLink]）
 *
 * 时间字段统一 epoch millis（Long），排序 / 范围查询都能走索引。
 */
@Entity(
    tableName = "item",
    indices = [
        Index(value = ["source", "remote_id"], unique = true),
        Index(value = ["list_type"]),
    ]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "remote_id") val remoteId: Int? = null,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,
    /** ListItemType.id，DB 存 Int 便于 JOIN / 索引 */
    @ColumnInfo(name = "list_type") val listType: Int = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "url") val url: String = "",
    @ColumnInfo(name = "mini_program_id") val miniProgramId: String = "",
    @ColumnInfo(name = "placeholder") val placeholder: String = "",
    @ColumnInfo(name = "icon_path") val iconPath: String? = null,
    @ColumnInfo(name = "icon_name") val iconName: String? = null,
    @ColumnInfo(name = "recommend", defaultValue = "0") val recommend: Boolean = false,
    @ColumnInfo(name = "vip_level", defaultValue = "0") val vipLevel: Int = 0,
    @ColumnInfo(name = "is_highlighted", defaultValue = "0") val isHighlighted: Boolean = false,
    @ColumnInfo(name = "is_online", defaultValue = "0") val isOnline: Boolean = false,
    @ColumnInfo(name = "is_ai", defaultValue = "0") val isAi: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "published_at") val publishedAt: Long? = null,
    /**
     * Strapi v5 文档级 cuid (24 字符字符串).
     *
     * 与 [remoteId] (Strapi v4 数字主键) 并存:
     * - [remoteId] 继续用于本地同步键 (Source + remoteId 唯一索引)
     * - [documentId] 用于评论 / 关联表等 v5 接口
     *
     * v4 时代的数据没有该字段, 保持 null 即可, UI 走降级路径.
     */
    @ColumnInfo(name = "document_id") val documentId: String? = null,
    /**
     * 评论总数 (一级 + 回复), 由 go-proxy 在 item-list 列表接口侧附加.
     * 旧数据 / 未启用评论插件时为 null, UI 不展示计数.
     */
    @ColumnInfo(name = "comment_count") val commentCount: Int? = null,
)
