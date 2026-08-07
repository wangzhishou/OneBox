package com.shifenmiao.model.wechat.common

data class BaseReq(
    val transaction: String? = null,
    val openId: String? = null,
    val type: Int? = null,
    val checkArgs: Boolean? = null
)

data class BaseResp(
    /**
     * 微信登录请求回来的code，使用该值请求access_token接口
     */
    val code: String = "",
    val extMsg: String = "",
    val errCode: Int? = 0,
    val errStr: String? = null,
    val transaction: String? = null,
    val openId: String? = null,
    val type: Int? = null,
    val checkArgs: Boolean? = null
)