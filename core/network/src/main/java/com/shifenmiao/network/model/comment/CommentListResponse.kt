package com.shifenmiao.network.model.comment

import com.shifenmiao.model.common.Meta
import com.shifenmiao.model.common.Pagination
import kotlinx.serialization.Serializable

/**
 * 评论列表响应 — 复用项目通用的 [com.shifenmiao.model.common.Meta] / Pagination,
 * 与 Strapi / go-proxy 返回的 `{ data: [...], meta: { pagination: {...} } }` 结构一致.
 *
 * 不实现 Parcelable: API 响应模型不参与 Bundle.
 */
@Serializable
data class CommentListResponse(
    val data: List<Comment> = emptyList(),
    val meta: Meta = Meta(pagination = Pagination(page = 1, pageSize = 20, pageCount = 0, total = 0)),
)
