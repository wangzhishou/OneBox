package com.shifenmiao.model.pay.wechat

import com.shifenmiao.model.pay.PayResult

data class WechatPayResult(
    var code: String? = null,
    var transaction: String? = null,
    var openId: String? = null,
    val outTradeNo: String? = null,
): PayResult
