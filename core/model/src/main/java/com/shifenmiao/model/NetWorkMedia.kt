package com.shifenmiao.model

import kotlinx.serialization.Serializable

@Serializable
data class NetWorkMedia(
    var id: Int = 0,
    val url: String = "",
    val mime: String = "",
    val ext: String = "",
    val width: Int = 0,
    val height: Int = 0
)