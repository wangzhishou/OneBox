package com.shifenmiao.network.model.comment

import kotlinx.serialization.Serializable

/** 发表评论 / 回复请求体. */
@Serializable
data class CreateCommentRequest(
    val content: String,
    val images: List<Int> = emptyList(),
)

/** 管理员更新评论请求体 (屏蔽 / 取消屏蔽 / 设置屏蔽原因). */
@Serializable
data class UpdateCommentRequest(
    val blocked: Boolean? = null,
    val blockReason: String? = null,
)
