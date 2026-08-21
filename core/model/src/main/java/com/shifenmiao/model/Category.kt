package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Category(
    val id: Int,
    val name: String = "",
    val canEdit: Boolean = false,
    val source: Source = Source.REMOTE,
    val updatedAt: Long = 0L,
    /** Strapi v5 文档级 cuid；服务端分类定位（chip 过滤等）统一用它，数字 id 重发后会漂移。 */
    @SerialName("documentId")
    val documentId: String? = null,
) : Parcelable
