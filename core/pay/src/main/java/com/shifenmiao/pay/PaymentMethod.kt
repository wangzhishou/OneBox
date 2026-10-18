package com.shifenmiao.pay

import android.content.Context
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.alipay.PayPrice

interface PaymentMethod<T> {
    val id: Int
    val displayName: String

    /**
     * 图标是否自带品牌文案与底板(支付宝/微信的 120x52 品牌徽标)。
     * 为 true 时 UI 只渲染徽标本身, 不再重复展示 [displayName]; 默认 false(纯图标, 需要文字补全含义)。
     */
    val iconIncludesLabel: Boolean
        get() = false

    fun setPaymentResultCallback(callback: PaymentResultCallback)
    fun getIcon(): Int
    fun pay(context: Context, payPrice: PayPrice, prePayResult: T)
    fun onPaySuccess(payResult: PayResult)
    fun onPayFailure(error: String)
    fun onPayIncomplete()
}