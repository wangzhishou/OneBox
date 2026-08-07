package com.shifenmiao.model.wechat.common

interface WechatEventHandler {
    fun onReq(req: BaseReq)

    fun onResp(resp: BaseResp)
}