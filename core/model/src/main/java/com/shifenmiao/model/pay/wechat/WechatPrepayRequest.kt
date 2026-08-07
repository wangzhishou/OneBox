package com.shifenmiao.model.pay.wechat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class WechatPrepayRequest(
    @SerializedName("appid") val appid: String? = null,
    @SerializedName("mchid") val mchid: String? = null,
    @SerializedName("description") val description: String,
    @SerializedName("out_trade_no") val outTradeNo: String,
    @SerializedName("time_expire") val timeExpire: String? = null,
    @SerializedName("attach") val attach: String? = null,
    @SerializedName("notify_url") val notifyUrl: String? = null,
    @SerializedName("goods_tag") val goodsTag: String? = null,
    @SerializedName("limit_pay") val limitPay: List<String>? = null,
    @SerializedName("support_fapiao") val supportFapiao: Boolean? = null,
    @SerializedName("amount") val amount: Amount,
    @SerializedName("detail") val detail: Detail? = null,
    @SerializedName("scene_info") val sceneInfo: SceneInfo? = null,
    @SerializedName("settle_info") val settleInfo: SettleInfo? = null
) : Parcelable {

    @Serializable
    @Parcelize
    data class Amount(
        @SerializedName("total") val total: Long,
        @SerializedName("currency") val currency: String? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class Detail(
        @SerializedName("cost_price") val costPrice: Long? = null,
        @SerializedName("invoice_id") val invoiceId: String? = null,
        @SerializedName("goods_detail") val goodsDetail: List<GoodsDetail>? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class GoodsDetail(
        @SerializedName("merchant_goods_id") val merchantGoodsId: String,
        @SerializedName("wechatpay_goods_id") val wechatpayGoodsId: String? = null,
        @SerializedName("goods_name") val goodsName: String? = null,
        @SerializedName("quantity") val quantity: Long,
        @SerializedName("unit_price") val unitPrice: Long
    ) : Parcelable

    @Serializable
    @Parcelize
    data class SceneInfo(
        @SerializedName("payer_client_ip") val payerClientIp: String,
        @SerializedName("device_id") val deviceId: String? = null,
        @SerializedName("store_info") val storeInfo: StoreInfo? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class StoreInfo(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String? = null,
        @SerializedName("area_code") val areaCode: String? = null,
        @SerializedName("address") val address: String? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class SettleInfo(
        @SerializedName("profit_sharing") val profitSharing: Boolean? = null
    ) : Parcelable
}