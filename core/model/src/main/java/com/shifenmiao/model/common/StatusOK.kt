package com.shifenmiao.model.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Status(
    val status: Int,
    val message: String
) : Parcelable
