package com.shifenmiao.model.favorite

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Favorite(
    val ids: Set<Int> = emptySet()
) : Parcelable
