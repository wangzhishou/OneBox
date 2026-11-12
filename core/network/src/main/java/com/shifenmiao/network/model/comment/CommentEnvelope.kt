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
