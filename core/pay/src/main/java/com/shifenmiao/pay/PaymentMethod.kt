package com.shifenmiao.pay

import android.content.Context
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.alipay.PayPrice

interface PaymentMethod<T> {
    val id: Int
    val displayName: String
    fun setPaymentResultCallback(callback: PaymentResultCallback)
    fun getIcon(): Int
    fun pay(context: Context, payPrice: PayPrice, prePayResult: T)
    fun onPaySuccess(payResult: PayResult)
    fun onPayFailure(error: String)
    fun onPayIncomplete()
}