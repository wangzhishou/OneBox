package com.shifenmiao.database.agent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.Source

/**
 * AGENT 类型条目的资源行。
 * 通过 [com.shifenmiao.database.item.entity.ItemAgentLink] 与 item 建立 1:1 关联。
 */
@Entity(
    tableName = "item_agent",
    indices = [
        Index(value = ["source", "document_id"], unique = true),
    ]
)
data class ItemAgentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "remote_id") val remoteId: Int? = null,
    /**
     * Strapi v5 文档级 cuid，远端 agent 的同步去重主键；数字 [remoteId] 重发后会漂移，仅作遗留信息。
     * 本地用户 agent 没有该字段，保持 null。
     */
    @ColumnInfo(name = "document_id") val documentId: String? = null,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,
    val title: String = "",
    val description: String? = null,
    val header: String? = null,
    val body: String? = null,
    val prompt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
