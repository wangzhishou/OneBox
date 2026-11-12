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

package com.shifenmiao.common.components.comments

import com.shifenmiao.model.ListItemType
import com.shifenmiao.network.model.comment.Comment

/**
 * 评论入口的 Strapi UID 常量 — 与 go-proxy `GET /api/comments/:documentId?uid=` 对齐.
 *
 * 评论在 plugin_comments_comments 里的 related 字段是 `"<uid>:<documentId>"`,
 * uid 错了会查到空列表, 所以每个评论入口都必须传对.
 */
const val COMMENT_UID_BLOG = "api::blog.blog"
const val COMMENT_UID_ITEM_LIST = "api::item-list.item-list"

/** 列表条目类型 → 评论 UID (只有博客与条目两类, 其余一律按 item-list 处理). */
fun commentUidForListType(listTypeId: Int?): String =
    if (listTypeId == ListItemType.BLOG.id) COMMENT_UID_BLOG else COMMENT_UID_ITEM_LIST

/**
 * 没有本地条目时的兜底推断.
 *
 * go-proxy 的 `/api/comments/mine` 与 `/api/user-notifications` 只为 blog 来源回填
 * sourceTitle, 因此"有来源标题"≈ 评论挂在博客上; 拿不到标题就按条目处理.
 */
fun commentUidForSourceTitle(sourceTitle: String): String =
    if (sourceTitle.isNotBlank()) COMMENT_UID_BLOG else COMMENT_UID_ITEM_LIST

/**
 * 是否为需要定位/高亮的评论: 命中一级评论本身, 或命中它的最新一条回复
 * (消息中心的通知指向的可能是回复, 列表接口把它折叠在 recentReply 里).
 */
fun Comment.matchesFocus(focusCommentId: Int): Boolean =
    focusCommentId > 0 && (id == focusCommentId || recentReply?.id == focusCommentId)
