package com.shifenmiao.base.channel

/**
 * 渠道配置: 由 :app 模块从 BuildConfig 注入, 业务模块通过 Hilt 获取.
 *
 * 业务模块不应直接 import com.shifenmiao.app.BuildConfig (会形成反向依赖),
 * 而是通过注入 ChannelConfig 来判断当前渠道特性, 实现"切渠道不影响"的隔离.
 *
 * 字段含义与 app/build.gradle.kts 中 google flavor 的 buildConfigField 一一对应.
 */
data class ChannelConfig(
    val enableWechat: Boolean,
    val enableAlipay: Boolean,
    val enableHms: Boolean,
    val enableGms: Boolean,
    val apiBaseUrl: String,
    val webBaseUrl: String,
    val privacyPolicyUrl: String,
    val userAgreementUrl: String,
    /** 是否展示语言切换入口; 只有打包了多语言资源的渠道才应开启 */
    val showLanguageSetting: Boolean = false,
) {
    /** 是否有可用的应用内支付渠道; Google Play 渠道全关(数字商品须走 Play Billing) */
    val enablePayment: Boolean
        get() = enableWechat || enableAlipay
}
