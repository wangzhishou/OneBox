package com.shifenmiao.model.channel

/**
 * 首启隐私协议弹窗是否弹出（按渠道配置）。
 *
 * 国内应用市场审核要求首启弹隐私协议、登录前确认《用户协议》。
 * foss 渠道(F-Droid)同样弹窗: 同意前不发起任何网络请求(远程配置/条目同步/更新检查),
 * 满足 F-Droid 对首启自动联网的合规要求, 弹窗只出现一次。
 * google 渠道无此要求, 首启不弹窗、登录也不弹"服务协议及隐私保护"确认弹窗,
 * 隐私政策链接保留在登录页/设置页即可。
 */
enum class NeedPrivacyPolicyDialog(val need: Boolean) {
    REQUIRED(true),
    NOT_REQUIRED(false);

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): NeedPrivacyPolicyDialog {
            return when (flavorType) {
                FlavorType.GOOGLE -> NOT_REQUIRED
                else -> REQUIRED
            }
        }
    }
}
