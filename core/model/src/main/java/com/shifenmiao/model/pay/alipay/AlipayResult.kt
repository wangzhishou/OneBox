package com.shifenmiao.model.pay.alipay

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.shifenmiao.model.pay.PayResult
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * result字符串返回码 含义
 * 9000 订单支付成功。
 * 8000 正在处理中，支付结果未知（有可能已经支付成功），请查询商家订单列表中订单的支付状态。
 * 4000 订单支付失败。
 * 5000 重复请求。
 * 6001 用户中途取消。
 * 6002 网络连接出错。
 * 6004 支付结果未知（有可能已经支付成功），请查询商家订单列表中订单的支付状态。
 * 其它 其它支付错误。
 * 切记这个字符串中 result 不是对象，是字符串
 */
@Serializable
@Parcelize
data class AlipayResult(
    @SerializedName("memo") val memo: String,
    @SerializedName("result") val result: String,
    @SerializedName("resultStatus") val resultStatus: String
) : Parcelable, PayResult

@Serializable
@Parcelize
data class Result(
    @SerializedName("alipay_trade_app_pay_response") val alipayTradeAppPayResponse: AlipayTradeAppPayResponse,
    @SerializedName("sign") val sign: String,
    @SerializedName("sign_type") val signType: String
) : Parcelable

@Serializable
@Parcelize
data class AlipayTradeAppPayResponse(
    @SerializedName("code") val code: String,
    @SerializedName("msg") val msg: String,
    @SerializedName("app_id") val appId: String,
    @SerializedName("out_trade_no") val outTradeNo: String,
    @SerializedName("trade_no") val tradeNo: String,
    @SerializedName("total_amount") val totalAmount: String,
    @SerializedName("seller_id") val sellerId: String,
    @SerializedName("charset") val charset: String,
    @SerializedName("timestamp") val timestamp: String
) : Parcelable