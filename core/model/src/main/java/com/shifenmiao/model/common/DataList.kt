package com.shifenmiao.model.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DataList<T : Parcelable>(
    val data: List<T>,
    val meta: Meta
) : Parcelable

@Parcelize
@Serializable
data class DataObject<T : Parcelable>(
    val data: T,
    val meta: Meta
) : Parcelable

@Parcelize
@Serializable
data class Meta(
    val pagination: Pagination,
    val serverTime: String? = null,
) : Parcelable

/**
 * page: 当前页码，从 1 开始。
 * pageSize: 每页显示的数据条数。
 * pageCount: 总页数。
 * total: 数据的总条数。
 */
@Parcelize
@Serializable
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val pageCount: Int,
    val total: Int
) : Parcelable