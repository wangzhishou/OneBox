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

package com.shifenmiao.model.wechat

import android.content.Context
import com.shifenmiao.model.wechat.common.MediaMessage
import com.shifenmiao.model.wechat.common.WXScene
import com.shifenmiao.model.wechat.common.WechatEventHandler

/**
 * foss (F-Droid) 渠道 stub: 不打包微信 OpenSDK, 所有方法 no-op。
 * foss 的 ENABLE_WECHAT=false, 业务 UI 入口已由渠道开关隐藏, 这里是编译期兜底。
 * 签名必须与 src/nonfoss 的真实实现保持一致(appId/corpId 为公开标识符, 保留同值)。
 */
object Wechat {

    const val appId: String = "wx597f8df475291a20"
    const val corpId: String = "ww14868bcfd293a6f1"

    @Volatile
    var isEnabled: Boolean = true
        private set

    fun applyChannelEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun register(context: Context, appId: String) = Unit

    fun share(mediaMessage: MediaMessage, scene: WXScene) = Unit

    fun pay(
        appId: String,
        partnerId: String,
        prepayId: String,
        packageStr: String,
        nonceStr: String,
        timeStamp: String,
        sign: String
    ) = Unit

    fun auth(state: String?): Boolean = false

    fun launchMiniProgram(
        userName: String,
        path: String = ""
    ) = Unit

    fun launchCustomerService(corpId: String, url: String) = Unit

    fun launchCustomerService() = Unit

    fun addEventHandler(wechatEventHandler: WechatEventHandler) = Unit

    fun removeEventHandler(wechatEventHandler: WechatEventHandler) = Unit

    val wechatEventHandlers: MutableSet<WechatEventHandler>
        get() = mutableSetOf()

    fun launch(): Boolean = false

    fun isInstalled(): Boolean = false
}
