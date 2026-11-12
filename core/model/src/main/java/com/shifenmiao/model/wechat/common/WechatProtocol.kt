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

package com.shifenmiao.model.wechat.common

/**
 * 微信 SDK 协议常量, 取值与 com.tencent.mm.opensdk.constants.ConstantsAPI /
 * com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode 中的稳定协议值一致
 * (这些值由微信客户端协议定义, 不会随 SDK 版本变化)。
 *
 * 抽到 SDK-free 的 core/model, 是为了让 foss (F-Droid) 构建在不含微信 SDK 时
 * 也能编译 WechatEvent 的消费方 (LoginComponent / AppComponent / PayComponent)。
 */
object WechatProtocol {

    /** ConstantsAPI.COMMAND_SENDAUTH */
    const val COMMAND_SENDAUTH = 1

    /** ConstantsAPI.COMMAND_PAY_BY_WX */
    const val COMMAND_PAY_BY_WX = 5

    /** ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM */
    const val COMMAND_LAUNCH_WX_MINIPROGRAM = 19

    /** BaseResp.ErrCode.ERR_OK (用户同意) */
    const val ERR_OK = 0

    /** BaseResp.ErrCode.ERR_USER_CANCEL (用户取消) */
    const val ERR_USER_CANCEL = -2

    /** BaseResp.ErrCode.ERR_AUTH_DENIED (用户拒绝授权) */
    const val ERR_AUTH_DENIED = -4
}
