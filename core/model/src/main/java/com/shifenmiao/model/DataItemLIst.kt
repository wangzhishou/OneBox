package com.shifenmiao.model

import android.os.Parcelable
import com.shifenmiao.model.common.Meta
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 网络请求参数拼接
 * @see 「https://docs.strapi.io/dev-docs/api/rest/interactive-query-builder」
 */
/**
 * val jsonMap = mapOf(
 *     "sort" to listOf("title:asc"),
 *     "filters" to mapOf(
 *         "title" to mapOf("\$eq" to "hello")
 *     ),
 *     "populate" to mapOf(
 *         "author" to mapOf(
 *             "fields" to listOf("firstName", "lastName")
 *         )
 *     ),
 *     "fields" to listOf("title"),
 *     "pagination" to mapOf(
 *         "pageSize" to 10,
 *         "page" to 1
 *     ),
 *     "publicationState" to "live",
 *     "locale" to listOf("en")
 * )
 */


@Parcelize
@Serializable
data class DataItemLIst(
    val data: List<DataItem>,
    val meta: Meta
) : Parcelable