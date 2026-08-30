package com.shifenmiao.app.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.model.wechat.event.WechatEvent
import com.t8rin.logger.makeLog
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.shifenmiao.model.event.AppEventBus

class WXPayEntryActivity : Activity(), IWXAPIEventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Wechat.api.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        Wechat.api.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq?) {
        makeLog {
            "onReq: $baseReq"
        }
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

    override fun onResp(baseResp: BaseResp?) {
        var extMsg = ""
        var code = ""
        when (baseResp?.type) {
            ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM -> {
                extMsg = (baseResp as WXLaunchMiniProgram.Resp).extMsg
            }

            ConstantsAPI.COMMAND_SENDAUTH -> {
                code = (baseResp as SendAuth.Resp).code
            }

            else -> {}
        }
        val resp = com.shifenmiao.model.wechat.common.BaseResp(
            code,
            extMsg,
            baseResp?.errCode,
            baseResp?.errStr,
            baseResp?.transaction,
            baseResp?.openId,
            baseResp?.type,
            baseResp?.checkArgs()
        )
        AppEventBus.emit(WechatEvent(resp))
    }
}