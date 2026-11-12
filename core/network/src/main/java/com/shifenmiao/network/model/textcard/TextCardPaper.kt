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

package com.shifenmiao.network.model.textcard

import com.shifenmiao.model.StrapiImage
import com.shifenmiao.model.common.Meta
import com.shifenmiao.model.common.Pagination
import kotlinx.serialization.Serializable

/**
 * 图文卡片远程纸张(text-card-paper)— Strapi v5 扁平响应:
 * data[].title / data[].sort / data[].image.url(populate=image 后为完整 files 对象,
 * url 可能是相对路径 /uploads/...,调用方需拼 baseUrl)。
 *
 * 不实现 Parcelable: API 响应模型不参与 Bundle(同 model/comment 约定)。
 */
@Serializable
data class TextCardPaper(
    val id: Int = 0,
    val title: String = "",
    val sort: Int = 0,
    val image: StrapiImage? = null,
)

@Serializable
data class TextCardPaperListResponse(
    val data: List<TextCardPaper> = emptyList(),
    val meta: Meta = Meta(pagination = Pagination(page = 1, pageSize = 25, pageCount = 0, total = 0)),
)
