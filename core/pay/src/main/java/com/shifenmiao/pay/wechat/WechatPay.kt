package com.shifenmiao.pay.wechat

import android.content.Context
import com.shifenmiao.core.R
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.model.pay.wechat.WechatPayResult
import com.shifenmiao.model.pay.wechat.WechatPrepayResponse
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.pay.PaymentMethod
import com.shifenmiao.pay.PaymentResultCallback

class WechatPay : PaymentMethod<PrePayResponse> {

    private var paymentResultCallback: PaymentResultCallback? = null

    override val id: Int
        get() = 1
    override val displayName: String
        get() = "微信"

    override fun getIcon(): Int {
        return R.drawable.wechatpay
    }

    override fun setPaymentResultCallback(callback: PaymentResultCallback) {
        this.paymentResultCallback = callback
    }

    private fun consumeCallback(): PaymentResultCallback? {
        val callback = paymentResultCallback
        paymentResultCallback = null
        return callback
    }

    override fun pay(context: Context, payPrice: PayPrice, prePayResult: PrePayResponse) {
        val wechatPrePayResponse = prePayResult as WechatPrepayResponse
        Wechat.pay(
            appId = wechatPrePayResponse.appId,
            partnerId = wechatPrePayResponse.partnerId,
            prepayId = wechatPrePayResponse.prepayId,
            nonceStr = wechatPrePayResponse.nonceStr,
            timeStamp = wechatPrePayResponse.timeStamp,
            sign = wechatPrePayResponse.sign,
            packageStr = wechatPrePayResponse.packageStr,
        )
    }

    override fun onPaySuccess(payResult: PayResult) {
        val wechatPayResult = payResult as WechatPayResult
        // 处理支付成功逻辑，只消费一次
        consumeCallback()?.onPaymentSuccess(wechatPayResult)
    }

    override fun onPayFailure(error: String) {
        // 处理支付失败逻辑，只消费一次
        consumeCallback()?.onPaymentFailure(error)
    }

    override fun onPayIncomplete() {
        // 处理支付未完成逻辑，只消费一次
        consumeCallback()?.onPaymentIncomplete()
    }
}