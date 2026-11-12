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
