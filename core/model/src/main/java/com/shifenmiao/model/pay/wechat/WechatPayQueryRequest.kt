package com.shifenmiao.model.pay.wechat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class WechatPayQueryRequest(
    val outTradeNo: String? = null,
    var transactionId: String? = null,
    var openId: String? = null,
) : Parcelable
