package com.shifenmiao.model.login

import com.shifenmiao.model.channel.FlavorType

/**
 * 渠道登录能力配置。
 *
 * 国内渠道 [DOMESTIC]: 手机号验证码 + 公众号验证码 + 邮箱登录, 支持绑定手机。
 * 海外渠道 [OVERSEAS](google 及以后新增的外国渠道): 只保留邮箱登录,
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
                FlavorType.GOOGLE -> OVERSEAS
                else -> DOMESTIC
            }
        }
    }
}
