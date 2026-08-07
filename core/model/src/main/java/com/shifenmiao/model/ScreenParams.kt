package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ScreenParams(
    val id: Int = -1,
    val title: String = "",
    val description: String = "",
    val url: String? = null,
    val isScreen: Boolean = true,
    val blogType: Int? = null,
) : Parcelable