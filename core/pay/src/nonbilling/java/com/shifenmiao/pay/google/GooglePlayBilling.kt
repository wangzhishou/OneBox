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

package com.shifenmiao.pay.google

import android.content.Context
import com.shifenmiao.core.R
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.pay.PaymentMethod
import com.shifenmiao.pay.PaymentResultCallback

/**
 * 非 google 渠道 (国内 + foss) stub: 不打包 Play Billing SDK, 所有方法空实现。
 * 这些渠道 ENABLE_PLAY_BILLING=false, PayComponent 不会把本类加入支付方式列表,
 * 这里仅保证 main 源集对 GooglePlayBilling 的类型引用可编译。
 * 签名必须与 src/google 的真实实现保持一致。
 */
class GooglePlayBilling : PaymentMethod<PrePayResponse> {

    override val id: Int
        get() = 2
    override val displayName: String
        get() = "Google Play"

    override fun getIcon(): Int {
        return R.drawable.google_play
    }

    override fun setPaymentResultCallback(callback: PaymentResultCallback) = Unit

    override fun pay(context: Context, payPrice: PayPrice, prePayResult: PrePayResponse) = Unit

    override fun onPaySuccess(payResult: PayResult) = Unit

    override fun onPayFailure(error: String) = Unit

    override fun onPayIncomplete() = Unit

    suspend fun queryPlayProducts(productIds: List<String>): List<PlayProductInfo> = emptyList()

    suspend fun queryOwnedPurchases(): List<com.shifenmiao.model.pay.google.GooglePlayPayResult> =
        emptyList()

    fun lastErrorMessage(): String = ""

    suspend fun consumePurchase(purchaseToken: String): Boolean = false
}
