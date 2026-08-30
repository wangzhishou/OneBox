package com.shifenmiao.core.constants

/**
 * F-Droid (foss) 渠道配置: 海外功能形态(对齐 google 渠道), 域名统一 oneboxable.com。
 *
 * F-Droid 构建环境没有 keystore.properties, 后端域名是公开信息, 直接硬编码;
 * 游客 token / RemoteConfig token 硬编码与 google 渠道相同的海外低权限值(有意公开),
 * 游客接口与登录/注册/内容拉取均可用。
 * AI 厂商密钥 / 和风凭据等仍注入为空串, 对应功能静默降级。
 *
 * 注意：该类在各 flavor sourceSet 中签名必须保持一致，
 * 否则 src/main 中的 UrlConstants 无法引用。
 */
internal object UrlConstantsFlavor {
    // 硬编码海外生产域名(公开信息), 不依赖 keystore.properties 注入
    const val RELEASE_URL: String = "https://api.oneboxable.com"
    const val DEBUG_URL: String = "https://api.oneboxable.com"
    const val USER_AGREEMENT_URL = "https://www.oneboxable.com/agreement/global.html"
    const val PRIVACY_POLICY_URL = "https://www.oneboxable.com/privacy/global.html"

    // 公开低权限游客 token(与 google 渠道相同, 有意公开): 硬编码以便 F-Droid 构建(无 keystore.properties)也可用
    const val ACCESS_TOKEN: String = "f09ed9c1163256813a9730c0ee4cebce576f7db6b44a37c7906d18a"

    /**
     * RemoteConfig.accessToken 的完整默认 token，硬编码海外(google 渠道同款)低权限值。
     */
    const val REMOTE_CONFIG_ACCESS_TOKEN: String = "0749593e1fcb796b3194d9a2bf4c31da24c3ff3c462d6863ecb88cfea358b53288e7b809e816aa44178006fd3d95a8a7200dd519c8ef25645eb307bde837901a36b237648d2c48f003356db7664c9b38ff589d13400b94f314483d664addb1bf9cad106b81b614c92ae3482a7a68417e3c364da473c8c7c215cd7ad342caff9e"

    const val OFFICIAL_WEBSITE = "https://www.oneboxable.com"
    const val WECHAT_CUSTOMER_SERVICE = "https://work.weixin.qq.com/kfid/kfc3880f5e004ce5a8b"

    /** 联系邮箱: foss 与 google 渠道一致使用海外域名邮箱 */
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
