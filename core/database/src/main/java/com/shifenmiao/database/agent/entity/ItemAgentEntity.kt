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
        Index(value = ["source", "remote_id"], unique = true),
    ]
)
data class ItemAgentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "remote_id") val remoteId: Int? = null,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,
    val title: String = "",
    val description: String? = null,
    val header: String? = null,
    val body: String? = null,
    val prompt: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
