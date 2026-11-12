/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.model.pay.alipay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class PayParams(
    @SerialName("subject") val subject: String,
    @SerialName("out_trade_no") val outTradeNo: String,
    @SerialName("total_amount") val totalAmount: String,
    @SerialName("product_code") val productCode: String,
    @SerialName("body") val body: String? = null,
    @SerialName("goods_detail") val goodsDetail: List<GoodsDetail>? = null,
    @SerialName("business_params") val businessParams: String? = null, // Consider changing to appropriate type
    @SerialName("disable_pay_channels") val disablePayChannels: String? = null,
    @SerialName("enable_pay_channels") val enablePayChannels: String? = null,
    @SerialName("specified_channel") val specifiedChannel: String? = null,
    @SerialName("extend_params") val extendParams: ExtendParams? = null,
//    @SerialName("agreement_sign_params") val agreementSignParams: SignParams?,
    @SerialName("goods_type") val goodsType: String? = null,
    @SerialName("invoice_info") val invoiceInfo: String? = null,
    @SerialName("passback_params") val passbackParams: String? = null,
    @SerialName("promo_params") val promoParams: String? = null,
    @SerialName("royalty_info") val royaltyInfo: String? = null,
    @SerialName("seller_id") val sellerId: String? = null,
    @SerialName("settle_info") val settleInfo: String? = null,
    @SerialName("store_id") val storeId: String? = null,
    @SerialName("sub_merchant") val subMerchant: String? = null,
    @SerialName("timeout_express") val timeoutExpress: String? = null,
    @SerialName("time_expire") val timeExpire: String? = null,
    @SerialName("merchant_order_no") val merchantOrderNo: String? = null,
    @SerialName("ext_user_info") val extUserInfo: ExtUserInfo? = null,
    @SerialName("query_options") val queryOptions: List<String>? = null
)

@Serializable
data class ExtUserInfo(
    @SerialName("name") val name: String?,
    @SerialName("mobile") val mobile: String?,
    @SerialName("cert_type") val certType: String?,
    @SerialName("cert_no") val certNo: String?,
    @SerialName("min_age") val minAge: String?,
    @SerialName("need_check_info") val needCheckInfo: String?,
    @SerialName("identity_hash") val identityHash: String?
)

@Serializable
data class GoodsDetail(
    @SerialName("goods_id") val goodsId: String,
    @SerialName("alipay_goods_id") val aliPayGoodsId: String?,
    @SerialName("goods_name") val goodsName: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price") @Serializable(with = BigDecimalSerializer::class) val price: BigDecimal,
    @SerialName("goods_category") val goodsCategory: String?,
    @SerialName("categories_tree") val categoriesTree: String?,
    @SerialName("body") val body: String?,
    @SerialName("show_url") val showURL: String?
)

@Serializable
data class ExtendParams(
    @SerialName("sys_service_provider_id") val sysServiceProviderId: String?,
    @SerialName("hb_fq_num") val hbFqNum: String?,
    @SerialName("hb_fq_seller_percent") val hbFqSellerPercent: String?,
    @SerialName("industry_reflux_info") val industryRefluxInfo: String?,
    @SerialName("card_type") val cardType: String?,
    @SerialName("specified_seller_name") val specifiedSellerName: String?,
    @SerialName("orig_total_amount") val origTotalAmount: String?
)

@Serializable
data class Merchant(
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("merchant_type") val merchantType: String?
)

@Serializable
data class SettleInfo(
    @SerialName("settle_detail_infos") val settleDetailInfos: List<SettleDetailInfo>,
    @SerialName("settle_period_time") val settlePeriodTime: String?
)

@Serializable
data class SettleDetailInfo(
    @SerialName("trans_in_type") val transInType: String,
    @SerialName("trans_in") val transIn: String,
    @SerialName("summary_dimension") val summaryDimension: String?,
    @SerialName("settle_entity_id") val settleEntityId: String?,
    @SerialName("settle_entity_type") val settleEntityType: String?,
    @SerialName("amount") val amount: String?
)

@Serializable
data class PreOrderResult(
    @SerialName("app_id") val appId: String,
    @SerialName("out_trade_no") val outTradeNo: String,
    @SerialName("success") val success: Boolean,
    @SerialName("result_code") val resultCode: String
)