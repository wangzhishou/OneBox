package com.shifenmiao.model.channel

/**
 * 启动时是否需要弹隐私协议弹窗（按渠道配置）。
 *
 * 国内应用市场审核要求首启弹隐私协议; 海外渠道(google / foss)无此要求, 不弹窗,
 * 隐私政策链接保留在登录页/设置页即可。
 */
enum class NeedPrivacyPolicyDialog(val need: Boolean) {
    REQUIRED(true),
    NOT_REQUIRED(false);

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): NeedPrivacyPolicyDialog {
            return when (flavorType) {
                FlavorType.GOOGLE, FlavorType.FOSS -> NOT_REQUIRED
                else -> REQUIRED
            }
        }
    }
}
