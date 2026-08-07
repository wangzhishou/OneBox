package com.shifenmiao.model.pay.wechat

import com.google.gson.annotations.SerializedName
import com.shifenmiao.model.pay.PrePayResponse
import kotlinx.serialization.Serializable

@Serializable
data class WechatPrepayResponse(
    val appId: String,
    val partnerId: String,
    val prepayId: String,
    @SerializedName("package") val packageStr: String,
    val nonceStr: String,
    val timeStamp: String,
    val sign: String
) : PrePayResponse