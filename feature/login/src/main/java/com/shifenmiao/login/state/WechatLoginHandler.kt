package com.shifenmiao.login.state

import com.shifenmiao.model.wechat.common.BaseResp

interface WechatLoginHandler {
    fun onSuccess(resp: BaseResp)

    fun onFail(resp: BaseResp)
}