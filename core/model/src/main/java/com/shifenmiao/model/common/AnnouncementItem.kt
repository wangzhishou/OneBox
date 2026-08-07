package com.shifenmiao.model.common

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class AnnouncementItem(
    val title: String = "",
    val iconName: String = "Notifications",
    val link: String? = null,
    val displayTimeMs: Long = 5000
) : Parcelable