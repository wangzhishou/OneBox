package com.shifenmiao.model.pay.alipay

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class PayParams(
    @SerializedName("subject") val subject: String,
    @SerializedName("out_trade_no") val outTradeNo: String,
    @SerializedName("total_amount") val totalAmount: String,
    @SerializedName("product_code") val productCode: String,
    @SerializedName("body") val body: String? = null,
    @SerializedName("goods_detail") val goodsDetail: List<GoodsDetail>? = null,
    @SerializedName("business_params") val businessParams: String? = null, // Consider changing to appropriate type
    @SerializedName("disable_pay_channels") val disablePayChannels: String? = null,
    @SerializedName("enable_pay_channels") val enablePayChannels: String? = null,
    @SerializedName("specified_channel") val specifiedChannel: String? = null,
    @SerializedName("extend_params") val extendParams: ExtendParams? = null,
//    @SerializedName("agreement_sign_params") val agreementSignParams: SignParams?,
    @SerializedName("goods_type") val goodsType: String? = null,
    @SerializedName("invoice_info") val invoiceInfo: String? = null,
    @SerializedName("passback_params") val passbackParams: String? = null,
    @SerializedName("promo_params") val promoParams: String? = null,
    @SerializedName("royalty_info") val royaltyInfo: String? = null,
    @SerializedName("seller_id") val sellerId: String? = null,
    @SerializedName("settle_info") val settleInfo: String? = null,
    @SerializedName("store_id") val storeId: String? = null,
    @SerializedName("sub_merchant") val subMerchant: String? = null,
    @SerializedName("timeout_express") val timeoutExpress: String? = null,
    @SerializedName("time_expire") val timeExpire: String? = null,
    @SerializedName("merchant_order_no") val merchantOrderNo: String? = null,
    @SerializedName("ext_user_info") val extUserInfo: ExtUserInfo? = null,
    @SerializedName("query_options") val queryOptions: List<String>? = null
)

@Serializable
data class ExtUserInfo(
    @SerializedName("name") val name: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("cert_type") val certType: String?,
    @SerializedName("cert_no") val certNo: String?,
    @SerializedName("min_age") val minAge: String?,
    @SerializedName("need_check_info") val needCheckInfo: String?,
    @SerializedName("identity_hash") val identityHash: String?
)

@Serializable
data class GoodsDetail(
    @SerializedName("goods_id") val goodsId: String,
    @SerializedName("alipay_goods_id") val aliPayGoodsId: String?,
    @SerializedName("goods_name") val goodsName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") @Serializable(with = BigDecimalSerializer::class) val price: BigDecimal,
    @SerializedName("goods_category") val goodsCategory: String?,
    @SerializedName("categories_tree") val categoriesTree: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("show_url") val showURL: String?
)

@Serializable
data class ExtendParams(
    @SerializedName("sys_service_provider_id") val sysServiceProviderId: String?,
    @SerializedName("hb_fq_num") val hbFqNum: String?,
    @SerializedName("hb_fq_seller_percent") val hbFqSellerPercent: String?,
    @SerializedName("industry_reflux_info") val industryRefluxInfo: String?,
    @SerializedName("card_type") val cardType: String?,
    @SerializedName("specified_seller_name") val specifiedSellerName: String?,
    @SerializedName("orig_total_amount") val origTotalAmount: String?
)

data class Merchant(
    @SerializedName("merchant_id") val merchantId: String,
    @SerializedName("merchant_type") val merchantType: String?
)

data class SettleInfo(
    @SerializedName("settle_detail_infos") val settleDetailInfos: List<SettleDetailInfo>,
    @SerializedName("settle_period_time") val settlePeriodTime: String?
)

data class SettleDetailInfo(
    @SerializedName("trans_in_type") val transInType: String,
    @SerializedName("trans_in") val transIn: String,
    @SerializedName("summary_dimension") val summaryDimension: String?,
    @SerializedName("settle_entity_id") val settleEntityId: String?,
    @SerializedName("settle_entity_type") val settleEntityType: String?,
    @SerializedName("amount") val amount: String?
)

data class PreOrderResult(
    @SerializedName("app_id") val appId: String,
    @SerializedName("out_trade_no") val outTradeNo: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("result_code") val resultCode: String
)