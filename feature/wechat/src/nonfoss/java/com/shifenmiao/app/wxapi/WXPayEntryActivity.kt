/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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