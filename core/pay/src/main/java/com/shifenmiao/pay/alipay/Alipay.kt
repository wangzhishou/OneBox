package com.shifenmiao.pay.alipay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import com.alipay.sdk.app.PayTask
import com.google.gson.Gson
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.AlipayResult
import com.shifenmiao.model.pay.alipay.PayEncodeParamResult
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.pay.PaymentMethod
import com.shifenmiao.pay.PaymentResultCallback

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

    private fun consumeCallback(): PaymentResultCallback? {
        val callback = paymentResultCallback
        paymentResultCallback = null
        return callback
    }

    override fun pay(
        context: Context,
        payPrice: PayPrice,
        prePayResult: PrePayResponse
    ) {
        val payEncodeParamResult = prePayResult as PayEncodeParamResult
        // 实现支付宝支付逻辑
        val orderInfo = payEncodeParamResult.payEncodeParam
        if (orderInfo != null) {
            if (orderInfo.isNotEmpty()) {
                toAliPay(context, orderInfo)
            } else {
                prePayResult.error?.let { onPayFailure(it) }
                return
            }
        } else {
            onPayFailure("支付信息订单生成错误")
            return
        }
    }

    override fun onPaySuccess(payResult: PayResult) {
        val alipayResult = payResult as AlipayResult
        // 处理支付成功逻辑，只消费一次
        consumeCallback()?.onPaymentSuccess(alipayResult)
    }

    override fun onPayFailure(error: String) {
        // 处理支付失败逻辑，只消费一次
        consumeCallback()?.onPaymentFailure(error)
    }

    override fun onPayIncomplete() {
        // 处理支付未完成逻辑，只消费一次
        consumeCallback()?.onPaymentIncomplete()
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SDK_PAY_FLAG -> {
                    try {
                        val gson = Gson()
                        @Suppress("UNCHECKED_CAST")
                        val json = gson.toJson(msg.obj as Map<String?, String?>)
                        val alipayResult = gson.fromJson(json, AlipayResult::class.java)
                        if (alipayResult != null) {
                            val resultStatus = alipayResult.resultStatus
                            when {
                                TextUtils.equals(resultStatus, "9000") -> {
                                    onPaySuccess(alipayResult)
                                    ActionUtils.showToast("付款成功")
                                }

                                TextUtils.equals(resultStatus, "6001") -> {
                                    onPayIncomplete()
                                    ActionUtils.showError("你未完成付款")
                                }

                                else -> {
                                    ActionUtils.showError("付款失败")
                                    onPayFailure(alipayResult.toString())
                                }
                            }
                        }
                    } catch (e: Exception) {
                        onPayFailure(AppContext.getString(R.string.pay_error))
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private val SDK_PAY_FLAG = 1
    private fun toAliPay(
        context: Context,
        orderInfo: String
    ) {
        val payRunnable = Runnable {
            val alipay = PayTask(context as Activity)
            val result = alipay.payV2(orderInfo, true)
            val msg = Message().apply {
                what = SDK_PAY_FLAG
                obj = result
            }
            mHandler.sendMessage(msg)
        }
        Thread(payRunnable).start()
    }
}