package com.shifenmiao.model.wechat

import android.content.Context
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.wechat.common.ImageObject
import com.shifenmiao.model.wechat.common.MediaMessage
import com.shifenmiao.model.wechat.common.MiniProgramObject
import com.shifenmiao.model.wechat.common.MusicVideoObject
import com.shifenmiao.model.wechat.common.TextObject
import com.shifenmiao.model.wechat.common.VideoObject
import com.shifenmiao.model.wechat.common.WXScene
import com.shifenmiao.model.wechat.common.WebpageObject
import com.shifenmiao.model.wechat.common.WechatEventHandler
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelbiz.WXOpenCustomerServiceChat
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject
import com.tencent.mm.opensdk.modelmsg.WXMusicVideoObject
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.tencent.mm.opensdk.modelmsg.WXVideoObject
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

object Wechat {

    const val appId: String = "wx597f8df475291a20"
    const val corpId: String = "ww14868bcfd293a6f1"

    /**
     * 渠道开关: 在 AppApplication.onCreate 早期根据 BuildConfig.ENABLE_WECHAT 注入.
     * Google Play 渠道设为 false 后, 所有 Wechat.* 调用都会安全 no-op,
     * 不会触发 Tencent SDK, 也不会展示微信相关 UI.
     *
     * 默认 true (国内渠道) 保持向后兼容, 旧调用方零修改.
     */
    @Volatile
    var isEnabled: Boolean = true
        private set

    /**
     * 由 AppApplication 在初始化早期根据渠道 BuildConfig 调用一次.
     * core/model 没有 app 的 BuildConfig 引用, 必须由 app 模块注入.
     */
    fun applyChannelEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    lateinit var api: IWXAPI

    /**
     * 注册APP到微信
     *
     * @param context
     * @param appId
     */
    fun register(context: Context, appId: String) {
        if (!isEnabled) return
        api = WXAPIFactory.createWXAPI(context, appId, false)
        api.registerApp(appId)
    }


    /**
     * 分享
     *
     * @param mediaMessage
     * @param scene
     */
    fun share(mediaMessage: MediaMessage, scene: WXScene) {
        if (!isEnabled) return
        val msg = WXMediaMessage()
        mediaMessage.sdkVer?.let {
            msg.sdkVer = it
        }
        msg.title = mediaMessage.title
        msg.description = mediaMessage.description
        msg.thumbData = mediaMessage.thumbData
        msg.messageExt = mediaMessage.messageExt
        msg.thumbDataHash = mediaMessage.thumbDataHash
        msg.msgSignature = mediaMessage.msgSignature
        val req = SendMessageToWX.Req()
        req.transaction = System.currentTimeMillis().toString()

        when (val mediaObject = mediaMessage.mediaObject) {
            is TextObject -> {
                val obj = WXTextObject()
                obj.text = mediaObject.text
                msg.mediaObject = obj
            }

            is ImageObject -> {
                val obj = WXImageObject()
                obj.imageData = mediaObject.imageData
                obj.imgDataHash = mediaObject.imgDataHash
                if (mediaObject.imagePath.isNotEmpty()) {
                    obj.imagePath = mediaObject.imagePath
                }

                msg.mediaObject = obj
            }

            is VideoObject -> {
                val obj = WXVideoObject()
                obj.videoUrl = mediaObject.videoUrl
                obj.videoLowBandUrl = mediaObject.videoLowBandUrl
                msg.mediaObject = obj
            }

            is WebpageObject -> {
                val webpageObject = WXWebpageObject()
                webpageObject.webpageUrl = mediaObject.webpageUrl
                msg.mediaObject = webpageObject
            }

            is MiniProgramObject -> {
                val miniObject = WXMiniProgramObject()
                miniObject.webpageUrl = mediaObject.webpageUrl
                miniObject.userName = mediaObject.userName
                miniObject.path = mediaObject.path
                miniObject.withShareTicket = mediaObject.withShareTicket
                miniObject.miniprogramType = mediaObject.miniprogramType.ordinal

                msg.mediaObject = miniObject
            }

            is MusicVideoObject -> {
                val mvObject = WXMusicVideoObject()
                mvObject.musicUrl = mediaObject.musicUrl
                mvObject.musicDataUrl = mediaObject.musicDataUrl
                mvObject.singerName = mediaObject.singerName
                mvObject.duration = mediaObject.duration.toInt() //音乐时长，毫秒

                mvObject.albumName = mediaObject.albumName
                mvObject.songLyric = mediaObject.songLyric
                mvObject.musicGenre = mediaObject.musicGenre
                mvObject.issueDate = mediaObject.issueDate.toLong() //发行时间Unix时间戳
                mvObject.identification = mediaObject.identification //音乐标识符
                mvObject.hdAlbumThumbFileHash = mediaObject.hdAlbumThumbFileHash
                mvObject.hdAlbumThumbFilePath = mediaObject.hdAlbumThumbFilePath
            }

            else -> {}
        }
        req.message = msg
        req.scene = scene.ordinal

        api.sendReq(req)

    }

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
        timeStamp: String,
        sign: String
    ) {
        if (!isEnabled) return
        val req = PayReq()
        req.appId = appId
        req.partnerId = partnerId
        req.prepayId = prepayId
        req.packageValue = packageStr
        req.nonceStr = nonceStr
        req.timeStamp = timeStamp
        req.sign = sign
        api.sendReq(req)
    }

    /**
     *
     *

     * @param state 用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止 csrf 攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加 session 进行校验。在state传递的过程中会将该参数作为url的一部分进行处理，因此建议对该参数进行url encode操作，防止其中含有影响url解析的特殊字符（如'#'、'&'等）导致该参数无法正确回传。
     *
     */
    fun auth(state: String?) {
        if (!isEnabled) return
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = state
        api.sendReq(req)
    }

    /**
     * 拉起小程序
     *
     * @param userName 拉起的小程序的username
     * @param path 拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
     */
    fun launchMiniProgram(
        userName: String,
        path: String = ""
    ) {
        if (!isEnabled) return
        val req = WXLaunchMiniProgram.Req()
        req.userName = userName
        if (path.isNotEmpty()) {
            req.path = path
        }
        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE

        api.sendReq(req)
    }

    /**
     * 拉起微信客服
     *
     * @param corpId 企业ID
     * @param url 客服URL
     */
    fun launchCustomerService(corpId: String, url: String) {
        if (!isEnabled) return
        if (api.wxAppSupportAPI >= Build.SUPPORT_OPEN_CUSTOMER_SERVICE_CHAT) {
            val req = WXOpenCustomerServiceChat.Req()
            req.corpId = corpId
            req.url = url
            api.sendReq(req)
        }
    }

    fun launchCustomerService() {
        launchCustomerService(corpId, UrlConstants.WECHAT_CUSTOMER_SERVICE)
    }

    fun addEventHandler(wechatEventHandler: WechatEventHandler) {
        wechatEventHandlers.add(wechatEventHandler)
    }

    /**
     * 移除监听
     *
     * @param wechatEventHandler
     */
    fun removeEventHandler(wechatEventHandler: WechatEventHandler) {
        wechatEventHandlers.remove(wechatEventHandler)
    }

    val wechatEventHandlers: MutableSet<WechatEventHandler>
        get() = mutableSetOf()

    /**
     * 唤起微信客户端
     *
     */
    fun launch(): Boolean {
        if (!isEnabled) return false
        return api.openWXApp()
    }

    /**
     * 检查微信是否安装
     *
     */
    fun isInstalled(): Boolean {
        if (!isEnabled) return false
        return api.isWXAppInstalled
    }
}