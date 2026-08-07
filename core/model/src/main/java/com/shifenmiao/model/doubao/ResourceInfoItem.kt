package com.shifenmiao.model.doubao

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class ResourceInfoItem(
    val duration: Int = 86400,
    val resourceType: String = "endpoint",
    val resourceIds: List<String> = emptyList()
) : Parcelable
