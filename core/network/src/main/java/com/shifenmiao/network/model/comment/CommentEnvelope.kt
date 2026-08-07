package com.shifenmiao.network.model.comment

import kotlinx.serialization.Serializable

/**
 * 评论接口的"包装响应" — 与 go-proxy `service.JsonResponse` 一一对应.
 *
 * 后端格式: `{ "data": <Comment>, "meta": {...} }`.
 *
 * 不实现 Parcelable: API 响应模型, 不进 Bundle.
 * 用 [Serializable] 而非 [com.shifenmiao.model.common.JsonResponse] 的原因是后者
 * 的 T 受 Parcelable 约束, 而我们把 [Comment] 去掉了 Parcelable 以避开 @RawValue 风险.
 */
@Serializable
data class CommentEnvelope(
    val data: Comment? = null,
    val meta: CommentMeta = CommentMeta(),
)

/**
 * 评论接口返回的最小 meta — 兼容现有 list 接口返回的 Pagination 结构.
 *
 * 后端 admin 接口只返回空 meta, 这里用默认值兜底;
 * list 接口则填真实分页数据.
 */
@Serializable
data class CommentMeta(
    val pagination: CommentPagination = CommentPagination(),
    val serverTime: String? = null,
)

@Serializable
data class CommentPagination(
    val page: Int = 0,
    val pageSize: Int = 0,
    val pageCount: Int = 0,
    val total: Int = 0,
)
