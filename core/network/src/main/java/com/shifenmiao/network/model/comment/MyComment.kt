package com.shifenmiao.network.model.comment

import com.shifenmiao.model.common.Meta
import com.shifenmiao.model.common.Pagination
import kotlinx.serialization.Serializable

/**
 * 我发表的评论 — 与 go-proxy `GET /api/comments/mine` 输出字段对齐.
 *
 * 字段含义:
 * - [id]: int 主键
 * - [documentId]: 评论的 Strapi v5 文档级 cuid
 * - [content]: 评论正文
 * - [createdAt]: epoch millis
 * - [sourceDocumentId] / [sourceTitle]: 评论来源博客的 documentId / 标题
 */
@Serializable
data class MyComment(
    val id: Int = 0,
    val documentId: String? = null,
    val content: String = "",
    val createdAt: Long = 0,
    val sourceDocumentId: String? = null,
    val sourceTitle: String = "",
)

/** 我发表的评论列表响应 — 复用项目通用的 [Meta] / [Pagination] */
@Serializable
data class MyCommentListResponse(
    val data: List<MyComment> = emptyList(),
    val meta: Meta = Meta(pagination = Pagination(page = 1, pageSize = 20, pageCount = 0, total = 0)),
)
