package com.shifenmiao.pay

import com.shifenmiao.model.pay.PayResult

interface PaymentResultCallback {
    fun onPaymentSuccess(payResult: PayResult)
    fun onPaymentFailure(error: String)
    fun onPaymentIncomplete()
}