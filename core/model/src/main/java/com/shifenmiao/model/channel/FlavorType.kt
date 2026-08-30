package com.shifenmiao.model.channel

import com.shifenmiao.model.BuildConfig

enum class FlavorType(val flavorName: String) {
    ONEBOX("onebox"),
    XIAOMI("xiaomi"),
    YYB("yyb"),
    OPPO("oppo"),
    VIVO("vivo"),
    GOOGLE("google"),
    HUAWEI("huawei"),
    // F-Droid 渠道: 海外功能形态(对齐 GOOGLE), 但不含任何 Google 系专有 SDK
    FOSS("foss");

    /** 海外形态渠道(google / foss): 邮箱登录、无国内合规文案、AI 引擎自带 token 直连等 */
    val isOverseas: Boolean
        get() = this == GOOGLE || this == FOSS

    companion object {
        // 未显式登记的渠道回退为 ONEBOX, 即国内功能形态
        fun fromName(name: String = BuildConfig.FLAVOR): FlavorType {
            return entries.find { it.flavorName == name } ?: ONEBOX
        }
    }
}
