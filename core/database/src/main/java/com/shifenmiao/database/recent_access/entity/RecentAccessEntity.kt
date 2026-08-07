package com.shifenmiao.database.recent_access.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 最近访问记录 — 文件/文件夹的统一持久化存储。
 *
 * 供文件浏览器"最近访问"、ActivityLog 中的"保存到 xxx"链接、
 * 以及任何需要"最近打开位置"列表的模块共用。
 *
 * @param uri URI 字符串（唯一键，同一 URI 多次访问会 REPLACE 旧记录）
 * @param displayName 显示名称（文件名或文件夹名）
 * @param accessType 访问类型："file" / "folder"
 * @param pathHint 路径提示（如 /storage/emulated/0/Downloads）
 * @param accessedAt 访问时间戳（毫秒）
 */
@Entity(
    tableName = "recent_access",
    indices = [
        Index(value = ["access_type", "accessed_at"]),
        Index(value = ["accessed_at"])
    ]
)
data class RecentAccessEntity(
    @PrimaryKey
    @ColumnInfo(name = "uri") val uri: String,

    @ColumnInfo(name = "display_name") val displayName: String,

    @ColumnInfo(name = "access_type") val accessType: String,

    @ColumnInfo(name = "path_hint") val pathHint: String? = null,

    @ColumnInfo(name = "accessed_at") val accessedAt: Long = System.currentTimeMillis(),
)
