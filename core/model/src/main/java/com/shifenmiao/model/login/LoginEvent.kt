package com.shifenmiao.model.login

import com.shifenmiao.model.user.Login
import retrofit2.Response

/**
 * EventBus.getDefault().post(LoginEvent("sourceName", onSuccessCallback, onFailureCallback))
 * EventBus.getDefault().post(LoginEvent())
 * 一开始就约束下调用来源的设定，方便扩展
 */
class LoginEvent(
    val source: String = "",
    val onSuccess: (response: Response<Login>) -> Unit = {},
    val onFailure: (response: Response<Login>) -> Unit = {}
)