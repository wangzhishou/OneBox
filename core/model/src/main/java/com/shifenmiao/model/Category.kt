package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Category(
    val id: Int,
    val name: String = "",
    val canEdit: Boolean = false,
    val source: Source = Source.REMOTE,
    val updatedAt: Long = 0L,
) : Parcelable
