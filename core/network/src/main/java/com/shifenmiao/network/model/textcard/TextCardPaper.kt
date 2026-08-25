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
