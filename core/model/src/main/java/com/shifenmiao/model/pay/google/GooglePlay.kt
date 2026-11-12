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

package com.shifenmiao.model.pay.google

import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.PayPrice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 后端 GET /google/pay/products 返回的商品目录项: productId 与积分的映射 */
@Serializable
data class GooglePlayProduct(
    @SerialName("product_id") val productId: String = "",
    val points: Int = 0,
)

/** POST /google/pay/verify 请求体: 服务端验单并幂等发放积分 */
@Serializable
data class GooglePayVerifyRequest(
    @SerialName("product_id") val productId: String = "",
    @SerialName("purchase_token") val purchaseToken: String = "",
)

/** Play Billing 支付成功结果(只携带验单所需字段, 不泄露 billing 类型到 model 层) */
@Serializable
data class GooglePlayPayResult(
    val productId: String = "",
    val purchaseToken: String = "",
) : PayResult

/** Google Play "下单"结果: 客户端无需后端下单, 仅携带待购买的 productId */
@Serializable
data class GooglePlayOrder(
    val productId: String = "",
) : PrePayResponse

/** 商品目录 + Play 本地化价格合并后的 UI 展示模型 */
@Serializable
data class PlayProduct(
    val productId: String = "",
    val points: Int = 0,
    val title: String = "",
    val formattedPrice: String = "",
)

/**
 * Google Play 商品档位 → 国内同款打赏形象(图标 + 俏皮文案),
 * 按各商品的人民币等价(rechargeFen)就近映射国内档位;
 * 服务端新增未知档位时映射缺失, UI 回退展示 Play 商品名
 */
val PlayProductTierMap: Map<String, PayPrice> = mapOf(
    "points_1000" to PayPrice.JuicePrice,     // ≈¥7
    "points_3000" to PayPrice.BeerPrice,      // ≈¥21
    "points_5000" to PayPrice.MoviePrice,     // ≈¥35
    "points_10000" to PayPrice.MilkPrice,     // ≈¥70
    "points_20000" to PayPrice.MedicinePrice, // ≈¥140
    "points_50000" to PayPrice.SaunaPrice,    // ≈¥350
)
