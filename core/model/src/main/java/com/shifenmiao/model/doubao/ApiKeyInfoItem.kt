package com.shifenmiao.model.doubao

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class ApiKeyInfoItem(
    val apiKey: String = "",
    val expiredTime: Long = 0,
    val expiresIn: Int = 10000,
    val resourceIds: List<String> = emptyList()
) : Parcelable
