package com.shifenmiao.model.image

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ImageViewerInfo(
    val images: List<String>,
    val initialIndex: Int
) : Parcelable