package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
@Parcelize
@Serializable
data class NetworkPagination(
    val page: Int,
    val pageSize: Int,
    val pageCount: Int,
    val total: Int
) : Parcelable
