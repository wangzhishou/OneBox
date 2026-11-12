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

package com.shifenmiao.model.pay.wechat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class WechatPrepayRequest(
    @SerialName("appid") val appid: String? = null,
    @SerialName("mchid") val mchid: String? = null,
    @SerialName("description") val description: String,
    @SerialName("out_trade_no") val outTradeNo: String,
    @SerialName("time_expire") val timeExpire: String? = null,
    @SerialName("attach") val attach: String? = null,
    @SerialName("notify_url") val notifyUrl: String? = null,
    @SerialName("goods_tag") val goodsTag: String? = null,
    @SerialName("limit_pay") val limitPay: List<String>? = null,
    @SerialName("support_fapiao") val supportFapiao: Boolean? = null,
    @SerialName("amount") val amount: Amount,
    @SerialName("detail") val detail: Detail? = null,
    @SerialName("scene_info") val sceneInfo: SceneInfo? = null,
    @SerialName("settle_info") val settleInfo: SettleInfo? = null
) : Parcelable {

    @Serializable
    @Parcelize
    data class Amount(
        @SerialName("total") val total: Long,
        @SerialName("currency") val currency: String? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class Detail(
        @SerialName("cost_price") val costPrice: Long? = null,
        @SerialName("invoice_id") val invoiceId: String? = null,
        @SerialName("goods_detail") val goodsDetail: List<GoodsDetail>? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class GoodsDetail(
        @SerialName("merchant_goods_id") val merchantGoodsId: String,
        @SerialName("wechatpay_goods_id") val wechatpayGoodsId: String? = null,
        @SerialName("goods_name") val goodsName: String? = null,
        @SerialName("quantity") val quantity: Long,
        @SerialName("unit_price") val unitPrice: Long
    ) : Parcelable

    @Serializable
    @Parcelize
    data class SceneInfo(
        @SerialName("payer_client_ip") val payerClientIp: String,
        @SerialName("device_id") val deviceId: String? = null,
        @SerialName("store_info") val storeInfo: StoreInfo? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class StoreInfo(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String? = null,
        @SerialName("area_code") val areaCode: String? = null,
        @SerialName("address") val address: String? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class SettleInfo(
        @SerialName("profit_sharing") val profitSharing: Boolean? = null
    ) : Parcelable
}