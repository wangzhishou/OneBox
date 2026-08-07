package com.shifenmiao.model.wechat.common

/**
 * 分享的消息体
 *
 * @property title
 * @property description
 * @property thumbData
 * @property mediaObject
 * @property messageExt
 * @property thumbDataHash
 * @property msgSignature
 */
data class MediaMessage(
    /**
     * sdk 版本号
     *
     * 仅支持Android
     */
    var sdkVer: Int? = null,
    /**
     * 消息标题，限制长度不超过512Bytes
     */
    var title: String = "",
    /**
     * 描述内容，限制长度不超过1KB
     */
    var description: String = "",

    /**
     * 缩略图的二进制数据，限制内容大小不超过32KB
     */
    var thumbData: ByteArray? = null,
    /**
     * 多媒体数据对象,可以为WXImageObject、WXMusicVideoObject、WXVideoObject、WXWebpageObject等
     */
    var mediaObject: MediaObject? = null,
    /**
     * 额外信息
     *
     * mediaObject为WXMusicVideoObject时，从微信音乐播放器内跳回，会携带该参数，长度不能超过2k。（iOS、Android双平台要一致）
     */
    var messageExt: String? = null,

    /**
     * 消息缩略图数据的sha256
     *
     * 用于签名，详情可见 [OpenSDK 分享签名开发手册](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/OpenSDK_Signature.html)
     */
    var thumbDataHash: String? = null,
    /**
     * 消息签名
     *
     * 用于签名，详情可见 [OpenSDK 分享签名开发手册](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Share_and_Favorites/OpenSDK_Signature.html)
     */
    var msgSignature: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MediaMessage

        if (title != other.title) return false
        if (description != other.description) return false
        if (thumbData != null) {
            if (other.thumbData == null) return false
            if (!thumbData.contentEquals(other.thumbData)) return false
        } else if (other.thumbData != null) return false
        if (mediaObject != other.mediaObject) return false
        if (messageExt != other.messageExt) return false
        if (thumbDataHash != other.thumbDataHash) return false
        if (msgSignature != other.msgSignature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (thumbData?.contentHashCode() ?: 0)
        result = 31 * result + (mediaObject?.hashCode() ?: 0)
        result = 31 * result + (messageExt?.hashCode() ?: 0)
        result = 31 * result + (thumbDataHash?.hashCode() ?: 0)
        result = 31 * result + (msgSignature?.hashCode() ?: 0)
        return result
    }
}