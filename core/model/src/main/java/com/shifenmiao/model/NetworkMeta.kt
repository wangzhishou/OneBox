package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
@Parcelize
@Serializable
data class NetworkMeta(
    val networkPagination: NetworkPagination,
) : Parcelable