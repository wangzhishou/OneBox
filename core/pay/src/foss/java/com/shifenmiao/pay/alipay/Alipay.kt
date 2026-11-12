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

package com.shifenmiao.pay.alipay

import android.content.Context
import com.shifenmiao.core.R
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.pay.PaymentMethod
import com.shifenmiao.pay.PaymentResultCallback

/**
 * foss (F-Droid) 渠道 stub: 不打包支付宝 SDK, 拉起支付直接失败回调。
 * foss 的 ENABLE_ALIPAY=false, 支付入口已由渠道开关隐藏, 这里是编译期兜底
 * (PayComponent 中存在 `?: Alipay()` 的默认值引用)。
 * 签名必须与 src/nonfoss 的真实实现保持一致。
 */
class Alipay : PaymentMethod<PrePayResponse> {

    private var paymentResultCallback: PaymentResultCallback? = null

    override val id: Int
        get() = 0
    override val displayName: String
        get() = "支付宝"

    override val iconIncludesLabel: Boolean
        get() = true

    override fun getIcon(): Int {
        return R.drawable.alipay
    }

    override fun setPaymentResultCallback(callback: PaymentResultCallback) {
        this.paymentResultCallback = callback
    }

    override fun pay(
        context: Context,
        payPrice: PayPrice,
        prePayResult: PrePayResponse
    ) {
        val callback = paymentResultCallback
        paymentResultCallback = null
        callback?.onPaymentFailure("Alipay SDK not available in foss build")
    }

    override fun onPaySuccess(payResult: PayResult) = Unit

    override fun onPayFailure(error: String) = Unit

    override fun onPayIncomplete() = Unit
}
