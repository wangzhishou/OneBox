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
