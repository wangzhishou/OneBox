/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.network.model.notification

import com.shifenmiao.model.common.Meta
import com.shifenmiao.model.common.Pagination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 消息中心单条通知 — 与 go-proxy `/api/user-notifications` 输出字段对齐。
 *
 * 字段含义:
 * - [id]: int 主键
 * - [documentId]: Strapi v5 文档级 cuid
 * - [type]: comment_reply=评论回复 / system=系统通知 / feedback_reply=反馈回复
 * - [relatedFeedbackId] / [relatedCommentId]: 关联实体 id,无关联为 0
 * - [read]: 已读标记
 * - [createdAt] / [updatedAt] / [publishedAt]: epoch millis,与项目内其他 model 时间约定一致
 */
@Serializable
data class UserNotification(
    val id: Int = 0,
    @SerialName("documentId") val documentId: String? = null,
    val title: String = "",
    val content: String = "",
    val userId: Int = 0,
    val type: String = TYPE_SYSTEM,
    val relatedFeedbackId: Int = 0,
    val relatedCommentId: Int = 0,
    val read: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val publishedAt: Long? = null,
    /** 回复人昵称(comment_reply 类型时由 go-proxy 追加) */
    val replierName: String = "",
    /** 回复人头像 URL(comment_reply 类型时由 go-proxy 追加) */
    val replierAvatar: String = "",
    /** 来源博客标题 */
    val sourceTitle: String = "",
    /** 来源博客 documentId */
    val sourceDocumentId: String? = null,
) {
    companion object {
        const val TYPE_COMMENT_REPLY = "comment_reply"
        const val TYPE_SYSTEM = "system"
        const val TYPE_FEEDBACK_REPLY = "feedback_reply"
    }
}

/**
 * 通知列表响应 — 复用项目通用的 [Meta] / [Pagination],
 * 与 go-proxy 返回的 `{ data: [...], meta: { pagination: {...} } }` 结构一致。
 */
@Serializable
data class UserNotificationListResponse(
    val data: List<UserNotification> = emptyList(),
    val meta: Meta = Meta(pagination = Pagination(page = 1, pageSize = 20, pageCount = 0, total = 0)),
)

/** 未读数响应 `{ "data": { "count": 3 } }` */
@Serializable
data class UnreadCountResponse(
    val data: UnreadCountData = UnreadCountData(),
)

@Serializable
data class UnreadCountData(
    val count: Int = 0,
)

/** FCM token 上报请求体(POST user/fcm-token) */
@Serializable
data class FcmTokenRequest(
    val platform: String = "android",
    val fcmToken: String,
)

/** FCM token 解绑请求体(DELETE user/fcm-token) */
@Serializable
data class FcmTokenDeleteRequest(
    val fcmToken: String,
)
