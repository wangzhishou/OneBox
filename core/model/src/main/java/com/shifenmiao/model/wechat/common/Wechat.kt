package com.shifenmiao.model.wechat.common

interface Wechat {

    val wechatEventHandlers: MutableSet<WechatEventHandler>

    /**
     * 增加回调监听
     *
     * @param wechatEventHandler
     */
    fun addEventHandler(wechatEventHandler: WechatEventHandler)

    /**
     * 移除监听
     *
     * @param wechatEventHandler
     */
    fun removeEventHandler(wechatEventHandler: WechatEventHandler)

    /**
     * 分享
     *
     * @param mediaMessage [MediaMessage]和官方属性是一一对应的WXMediaMessage，其中mediaObject也是和官方一一对应的
     * @param scene [WXScene]分享场景
     */
    fun share(mediaMessage: MediaMessage, scene: WXScene)

    /**
     * 拉起支付
     *
     * @param partnerId
     * @param prepayId
     * @param packageStr
     * @param nonceStr
     * @param timeStamp
     * @param sign
     */
    fun pay(
        appId: String,
        partnerId: String,
        prepayId: String,
        packageStr: String,
        nonceStr: String,
        timeStamp: UInt,
        sign: String,
    )

    /**
     *
     *
     * @param state 用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止 csrf 攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加 session 进行校验。在state传递的过程中会将该参数作为url的一部分进行处理，因此建议对该参数进行url encode操作，防止其中含有影响url解析的特殊字符（如'#'、'&'等）导致该参数无法正确回传。
     *
     */
    fun auth(state: String? = "")

    /**
     * 拉起小程序
     *
     * @param userName 拉起的小程序的username
     * @param miniProgramType 拉起小程序的类型
     * @param path 拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
     */
    fun launchMiniProgram(
        userName: String,
        miniProgramType: MiniProgramType = MiniProgramType.Release,
        path: String? = ""
    )


    /**
     * 拉起微信客服
     *
     * @param corpId 企业ID
     * @param url 客服URL
     */
    fun launchCustomerService(corpId: String, url: String)

    /**
     * 唤起微信客户端
     *
     */
    fun launch(): Boolean
}