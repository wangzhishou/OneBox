package com.shifenmiao.network.model.comment

import com.shifenmiao.model.StrapiImage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 单条评论 — 与 go-proxy 任务三输出的 CommentResponse 字段对齐.
 *
 * 不实现 Parcelable: 这是 API 响应模型, 不参与 Bundle 序列化;
 * 之前用 @RawValue 包裹 recentReply 绕开 Parcelize 的递归限制,
 * 但 @RawValue 在跨进程 IPC / SavedStateHandle 持久化时容易崩溃.
 *
 * 字段含义:
 * - [id]: int 主键 (Strapi v5 在 plugin_comments_comments 上是 int unsigned)
 * - [documentId]: Strapi v5 文档级 cuid, 与 id 一一对应, 跨内容类型稳定
 * - [content]: 评论正文
 * - [createdAt] / [updatedAt]: epoch millis, 与项目内其他 model 时间约定一致
 * - [blocked] / [removed]: 屏蔽 / 软删标记, 列表接口默认过滤掉
 * - [author]: 冗余作者信息 (admin panel 直接展示用, 客户端仅展示 nickname/avatar)
 * - [replyCount]: 该评论下的回复数 (聚合查询拿到的)
 * - [recentReply]: 最近一条回复 (用于楼中楼预览), 仅一级评论有
 * - [threadOf]: 父评论 id, 仅回复时有值
 * - [images]: 评论附图, 通过 Strapi files_related_morphs 表与 files 关联,
 *   关联 related_type="plugin::comments.comment" field="image".
 */
@Serializable
data class Comment(
    val id: Int = 0,
    @SerialName("documentId") val documentId: String? = null,
    val content: String = "",
    val author: CommentAuthor = CommentAuthor(),
    val replyCount: Long = 0,
    val recentReply: Comment? = null,
    @SerialName("threadOf") val threadOf: Int? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val blocked: Boolean = false,
    val blockedThread: Boolean = false,
    val blockReason: String? = null,
    val removed: Boolean = false,
    val approvalStatus: String? = null,
    val images: List<StrapiImage> = emptyList(),
)
