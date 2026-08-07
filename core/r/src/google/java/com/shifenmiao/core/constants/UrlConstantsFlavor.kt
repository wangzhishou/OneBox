package com.shifenmiao.core.constants

import com.shifenmiao.core.BuildConfig

/**
 * Google Play 渠道配置。海外域名统一使用 oneboxable.com。
 *
 * 注意：该类在各 flavor sourceSet 中签名必须保持一致，
 * 否则 src/main 中的 UrlConstants 无法引用。
 */
internal object UrlConstantsFlavor {
    // 域名与 token 由 keystore.properties 注入 BuildConfig (见 build-logic convention 插件),
    // 不入库; 未配置时(如开源构建)为空串, 不会触达生产服务器
    val RELEASE_URL: String = BuildConfig.ApiBaseUrlGoogle
    val DEBUG_URL: String = BuildConfig.ApiBaseUrlGoogle
    const val USER_AGREEMENT_URL = "https://www.oneboxable.com/agreement/global.html"
    const val PRIVACY_POLICY_URL = "https://www.oneboxable.com/privacy/global.html"

    val ACCESS_TOKEN: String = BuildConfig.GuestAccessToken

    /**
     * RemoteConfig.accessToken 的完整默认 token，google 渠道专用，与国内渠道不同。
     */
    val REMOTE_CONFIG_ACCESS_TOKEN: String = BuildConfig.RemoteConfigAccessTokenGoogle

    const val OFFICIAL_WEBSITE = "https://www.oneboxable.com"
    const val WECHAT_CUSTOMER_SERVICE = "https://work.weixin.qq.com/kfid/kfc3880f5e004ce5a8b"

    /** 联系邮箱: google 渠道使用海外域名邮箱 */
    const val EMAIL = "support@oneboxable.com"

    val BAIDU_OCR_BASE_URL: String = RELEASE_URL

    /** 是否显示备案号/备案查询入口（仅国内渠道需要，海外渠道无此监管要求） */
    const val SHOW_BEI_AN_ENTRY = false

    const val BEI_AN_NUMBER = "京ICP备2023031661号-3A"
    const val BEI_AN_QUERY: String = "https://beian.miit.gov.cn/"
    const val BEI_AN_AI_QUERY: String = "https://beian.cac.gov.cn/#/index"
    const val BEI_AN_BAIDU_NUMBER = "网信算备110108645502801230035号"
    const val BEI_AN_ALI_NUMBER = "网信算备330110507206401240101号"
    const val BEI_AN_KIMI_NUMBER = "网信算备110108896786101240023号"
    const val BEI_AN_DOUBAO_NUMBER = "网信算备110108823483901230065号"
    const val BEI_AN_TENCENT_NUMBER = "网信算备440305295988701230071号"
    const val BEI_AN_ONEBOX_NUMBER = "网信算备110114085306301240013号"
    const val BEI_AN_DEEPSEEK_NUMBER = "网信算备330105747635301240017号"
}
