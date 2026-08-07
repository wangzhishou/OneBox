package com.shifenmiao.app.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.model.wechat.event.WechatEvent
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.shifenmiao.model.event.AppEventBus

class WXEntryActivity : Activity(), IWXAPIEventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Wechat.api.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        Wechat.api.handleIntent(intent, this)
    }

    override fun onReq(p0: BaseReq?) {
        Log.e("WXEntryActivity.onReq", p0.toString())
        val req = com.shifenmiao.model.wechat.common.BaseReq(
            p0?.transaction,
            p0?.openId,
            p0?.type,
            p0?.checkArgs()
        )
        Wechat.wechatEventHandlers.forEach {
            it.onReq(
                req
            )
        }
    }

    override fun onResp(p0: BaseResp?) {
        Log.e("WXEntryActivity.onResp", p0?.errCode.toString() + ":" + p0?.errStr)
        var extMsg = ""
        var code = ""
        when (p0?.type) {
            ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM -> {
                extMsg = (p0 as WXLaunchMiniProgram.Resp).extMsg
            }

            ConstantsAPI.COMMAND_SENDAUTH -> {
                code = (p0 as SendAuth.Resp).code
            }

            else -> {}
        }

        val resp = com.shifenmiao.model.wechat.common.BaseResp(
            code,
            extMsg,
            p0?.errCode,
            p0?.errStr,
            p0?.transaction,
            p0?.openId,
            p0?.type,
            p0?.checkArgs()
        )
        val event = WechatEvent(resp)
        AppEventBus.emit(event)
        if (p0?.type == ConstantsAPI.COMMAND_SENDAUTH) {
            AppEventBus.emitWechatLogin(event)
        }
        Wechat.wechatEventHandlers.forEach { it.onResp(resp) }
        finish()
    }
}