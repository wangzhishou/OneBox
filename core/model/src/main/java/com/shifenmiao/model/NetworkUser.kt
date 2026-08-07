package com.shifenmiao.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkUser(
    val id: Int = 0,
    val name: String = "",
    val url: String? = null
)