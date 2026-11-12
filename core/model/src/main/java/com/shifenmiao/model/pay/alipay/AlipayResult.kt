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

import android.os.Parcelable
import com.shifenmiao.model.pay.PayResult
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
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
    @SerialName("memo") val memo: String,
    @SerialName("result") val result: String,
    @SerialName("resultStatus") val resultStatus: String
) : Parcelable, PayResult

@Serializable
@Parcelize
data class Result(
    @SerialName("alipay_trade_app_pay_response") val alipayTradeAppPayResponse: AlipayTradeAppPayResponse,
    @SerialName("sign") val sign: String,
    @SerialName("sign_type") val signType: String
) : Parcelable

@Serializable
@Parcelize
data class AlipayTradeAppPayResponse(
    @SerialName("code") val code: String,
    @SerialName("msg") val msg: String,
    @SerialName("app_id") val appId: String,
    @SerialName("out_trade_no") val outTradeNo: String,
    @SerialName("trade_no") val tradeNo: String,
    @SerialName("total_amount") val totalAmount: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("charset") val charset: String,
    @SerialName("timestamp") val timestamp: String
) : Parcelable