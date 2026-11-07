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
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * 微信 SDK 在没注册成功 / intent 不合法时会直接抛异常。
     * 这里是登录链路的唯一回传入口，异常跑出去就再也没有任何结果回给登录页了，
     * 用户看到的就是「一直 loading」——所以统一兜住。
     */
    private fun handleIntent(intent: Intent?) {
        val target = intent ?: return finish()
        val handled = runCatching {
            Wechat.api.handleIntent(target, this)
        }.getOrElse {
            Log.e("WXEntryActivity", "handleIntent failed", it)
            false
        }
        // 不是微信发来的 intent（或根本没解析出回调）：立刻收掉这个透明页，
        // 否则它会一直盖在 App 上面，视觉上同样是「卡住」。
        if (!handled) finish()
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
        runCatching {
            when (p0?.type) {
                ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM -> {
                    extMsg = (p0 as? WXLaunchMiniProgram.Resp)?.extMsg.orEmpty()
                }

                ConstantsAPI.COMMAND_SENDAUTH -> {
                    // 用户取消/拒绝时微信回包里的 code 是 null，原先直接赋给非空 String
                    // 会抛 NPE，事件就再也发不出去：登录 loading 永远收不掉。
                    code = (p0 as? SendAuth.Resp)?.code.orEmpty()
                }

                else -> {}
            }
        }.onFailure {
            Log.e("WXEntryActivity.onResp", "parse resp failed", it)
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
        runCatching {
            Wechat.wechatEventHandlers.forEach { it.onResp(resp) }
        }
        finish()
    }
}
