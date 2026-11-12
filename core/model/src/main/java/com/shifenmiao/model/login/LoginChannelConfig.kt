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

package com.shifenmiao.model.login

import com.shifenmiao.model.channel.FlavorType

/**
 * 渠道登录能力配置。
 *
 * 国内渠道 [DOMESTIC]: 手机号验证码 + 公众号验证码 + 邮箱登录, 支持绑定手机。
 * 海外渠道 [OVERSEAS](google / foss 及以后新增的外国渠道): 只保留邮箱登录,
 * 第三方登录按钮(如 Google 登录)由 ChannelConfig.enableGms 等 buildConfigField 控制,
 * 不提供绑定手机功能(强制检测 / 远端 forceBindPhone 强推 / 资料页手动入口全部关闭)。
 *
 * 新增海外渠道时, 只需在 [getConfigByFlavor] 中把新渠道映射到 [OVERSEAS]。
 */
enum class LoginChannelConfig(
    val loginTypes: List<LoginType>,
    val bindPhoneSupported: Boolean,
) {
    DOMESTIC(
        loginTypes = listOf(
            LoginType.PHONE_LOGIN,
            LoginType.CODE_LOGIN,
            LoginType.EMAIL_LOGIN
        ),
        bindPhoneSupported = true,
    ),
    OVERSEAS(
        loginTypes = listOf(LoginType.EMAIL_LOGIN),
        bindPhoneSupported = false,
    );

    val defaultLoginType: LoginType
        get() = loginTypes.first()

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): LoginChannelConfig {
            return when (flavorType) {
                FlavorType.GOOGLE, FlavorType.FOSS -> OVERSEAS
                else -> DOMESTIC
            }
        }
    }
}
