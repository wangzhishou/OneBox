package com.shifenmiao.model.blog

import android.os.Parcelable
import com.shifenmiao.model.StrapiImage
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class BlogItem(
    val id: Int = 0,
    val title: String = "",
    val content: String? = null,
    val summary: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val publishedAt: Long? = null,
    val fixed: Boolean = false,
    val type: Int = 1,
    val picture: List<StrapiImage>? = null,
    val author: Author? = null,
    val tags: List<Tag> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class Author(
    val avatar: String? = null,
    val id: Int = 0,
    val nickname: String? = null,
    val username: String? = null
) : Parcelable

@Parcelize
@Serializable
data class Tag(
    val desc: String = "",
    val id: Int = 0,
    val name: String = ""
) : Parcelable


@Serializable
data class FeedbackRequest(
    val title: String,
    val content: String,
    val pictureIds: List<Int>,
    val tagIds: List<Int>,
    val blogType: Int = 1
)