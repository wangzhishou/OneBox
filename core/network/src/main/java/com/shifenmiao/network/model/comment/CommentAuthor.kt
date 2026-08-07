package com.shifenmiao.network.model.comment

import kotlinx.serialization.Serializable

/**
 * 评论作者 — 与 go-proxy CommentAuthor 字段对齐.
 *
 * 字段全部可空 / 带默认值: 后端 author_user_lnk 可能没记录 (老评论、孤儿数据等),
 * 客户端展示 fallback 为 "匿名".
 *
 * 不实现 Parcelable: 仅 API 响应层使用, 无需进 Bundle.
 */
@Serializable
data class CommentAuthor(
    val id: Int = 0,
    val nickname: String = "",
    val avatar: String = "",
)
