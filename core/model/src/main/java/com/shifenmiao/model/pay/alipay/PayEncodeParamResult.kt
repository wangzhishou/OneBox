package com.shifenmiao.model.pay.alipay

import com.shifenmiao.model.pay.PrePayResponse
import kotlinx.serialization.Serializable

@Serializable
data class PayEncodeParamResult(
    val payEncodeParam: String? = null,
    val error: String? = null
) : PrePayResponse
