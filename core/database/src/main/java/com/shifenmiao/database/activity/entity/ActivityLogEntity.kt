package com.shifenmiao.database.activity.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 活动日志 Room 实体。
 *
 * 相比旧的 `OperationHistoryEntity`：
 * - 移除 AI 特有字段（conversationId / completionId / beforeId / afterId）
 * - 用 [payload] JSON 承载各类型专属数据，扩展零成本
 * - 用 [dedupKey] + UNIQUE index 实现 upsert 语义（同一活动只保留最新）
 * - 用 [screenRoute] 保存跳转目标的 Screen.id，导航自包含
 */
@Entity(
    tableName = "activity_log",
    indices = [
        Index(value = ["dedup_key"], unique = true),
        Index(value = ["category"]),
        Index(value = ["created_at"])
    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 活动分类名，对应 [com.shifenmiao.model.activity.ActivityCategory.name] */
    val category: String = "",

    /** 功能标题 */
    @ColumnInfo(name = "app_title")
    val appTitle: String = "",

    /** 简短标题 */
    val title: String = "",

    /** 可含 HTML 的描述 */
    val description: String = "",

    /** 跳转目标 Screen.id */
    @ColumnInfo(name = "screen_route")
    val screenRoute: String = "",

    /** 各类型专属 JSON 数据 */
    val payload: String = "",

    /** 可选缩略图 */
    @ColumnInfo(name = "thumbnail_uri")
    val thumbnailUri: String? = null,

    /**
     * 去重键。
     * - AI 对话：conversationId
     * - 笔记/HTML：itemId
     * - 图片编辑：每次保存唯一（timestamp）
     *
     * INSERT OR REPLACE on this unique index → 相同 dedupKey 自动替换旧记录。
     */
    @ColumnInfo(name = "dedup_key")
    val dedupKey: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)

