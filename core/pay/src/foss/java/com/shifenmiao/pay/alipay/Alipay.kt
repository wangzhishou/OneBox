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
