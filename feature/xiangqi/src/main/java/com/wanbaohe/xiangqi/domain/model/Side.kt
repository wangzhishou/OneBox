package com.wanbaohe.xiangqi.domain.model

enum class Side {
    RED,
    BLACK;

    fun opposite(): Side = if (this == RED) BLACK else RED
}
