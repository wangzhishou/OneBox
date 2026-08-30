package com.shifenmiao.pay.google

/**
 * Play 商品详情的 SDK-free 视图, 供 main 源集 (PayComponent 等) 在不依赖
 * Play Billing SDK 的情况下消费商品信息。
 * 仅 google 渠道的真实 GooglePlayBilling 会填充非空列表。
 */
data class PlayProductInfo(
    val productId: String,
    val name: String,
    val formattedPrice: String,
)
