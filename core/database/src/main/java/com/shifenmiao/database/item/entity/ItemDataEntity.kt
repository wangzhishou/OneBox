package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.Source

/**
 * item 的大文本内容（HTML / Markdown / URL / JSON）。
 * 列表查询只走 item 表，详情页按需加载此表，避免大字段拖慢分页。
 *
 * 通过 [ItemDataLink] 与 item 建立 1:1 关联；可独立存在（picker 选择、远程同步）。
 *
 * 字段约定：
 * - title：独立展示用（如在 picker 列表里）
 * - kind：决定渲染路径，data / url 取其一
 * - sizeBytes：列表排序 / 性能监控
 */
@Entity(
    tableName = "item_data",
    indices = [
        Index(value = ["source", "document_id"], unique = true),
        Index(value = ["kind"]),
    ]
)
data class ItemDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "remote_id") val remoteId: Int? = null,
    /** 跟随所属 item 的 Strapi v5 documentId，同步去重主键；数字 [remoteId] 仅作遗留信息。 */
    @ColumnInfo(name = "document_id") val documentId: String? = null,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,

    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "kind") val kind: ItemDataKind = ItemDataKind.HTML,

    @ColumnInfo(name = "data") val data: String? = null,
    @ColumnInfo(name = "url") val url: String? = null,
    @ColumnInfo(name = "extra") val extra: String? = null,

    @ColumnInfo(name = "size_bytes", defaultValue = "0") val sizeBytes: Long = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
