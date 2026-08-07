package com.shifenmiao.model.wechat.common

interface MediaObject

/**
 * 分享的文本对象
 *
 * @property text
 */
data class TextObject(var text: String) : MediaObject

/**
 * 分享图片对象
 *
 * @property imageData
 * @property imgDataHash
 */
data class ImageObject(
    /**
     * 图片的二进制数据
     *
     * 内容大小不超过10MB
     */
    var imageData: ByteArray? = null,
    /**
     * 图片的本地路径	对应图片内容大小不超过 25MB，
     *
     * 仅支持Android
     */
    var imagePath: String = "",
    /**
     * 图片二进制数据的sha256
     */
    var imgDataHash: String? = null
) : MediaObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageObject

        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (imagePath != other.imagePath) return false
        if (imgDataHash != other.imgDataHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageData?.contentHashCode() ?: 0
        result = 31 * result + imagePath.hashCode()
        result = 31 * result + (imgDataHash?.hashCode() ?: 0)
        return result
    }

}

/**
 * 视频数据对象
 *
 * **注意：videoUrl和videoLowBandUrl不能同时为空**
 *
 * @property videoUrl
 * @property videoLowBandUrl
 */
data class VideoObject(
    /**
     * 视频链接	限制长度不超过10KB
     */
    var videoUrl: String = "",
    /**
     * 供低带宽的环境下使用的视频链接	限制长度不超过10KB
     */
    var videoLowBandUrl: String = ""
) : MediaObject

/**
 * 多媒体消息中包含的网页数据对象
 *
 * @property webpageUrl
 */
data class WebpageObject(
    /**
     * html链接	限制长度不超过10KB
     */
    val webpageUrl: String
) : MediaObject

/**
 * 多媒体消息中包含的小程序数据对象
 *
 * @property webpageUrl
 * @property userName
 * @property path
 * @property hdImageData
 * @property withShareTicket
 * @property miniprogramType
 */
data class MiniProgramObject(
    /**
     * 兼容低版本的网页链接	限制长度不超过10KB
     */
    var webpageUrl: String = "",
    /**
     * 小程序的userName	小程序原始ID获取方法：登录小程序管理后台-设置-基本设置-账号信息
     */
    var userName: String = "",
    /**
     * 小程序的页面路径	小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
     */
    var path: String? = "",

    /**
     * 小程序新版本的预览图二进制数据，6.5.9及以上版本微信客户端支持	限制大小不超过128KB，自定义图片建议长宽比是 5:4。
     */
    var hdImageData: ByteArray? = null,
    /**
     * 是否使用带shareTicket的分享	通常开发者希望分享出去的小程序被二次打开时可以获取到更多信息，例如群的标识。可以设置withShareTicket为true，当分享卡片在群聊中被其他用户打开时，可以获取到shareTicket，用于获取更多分享信息。详见小程序获取更多分享信息 ，最低客户端版本要求：6.5.13
     */
    var withShareTicket: Boolean = false,
    /**
     * 小程序的类型，默认正式版，1.8.1及以上版本开发者工具包支持分享开发版和体验版小程序
     * 正式版: WXMiniProgramTypeRelease;
     * 测试版: WXMiniProgramTypeTest;
     * 体验版: WXMiniProgramTypePreview;
     */
    var miniprogramType: MiniProgramType = MiniProgramType.Release
) : MediaObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MiniProgramObject

        if (webpageUrl != other.webpageUrl) return false
        if (userName != other.userName) return false
        if (path != other.path) return false
        if (hdImageData != null) {
            if (other.hdImageData == null) return false
            if (!hdImageData.contentEquals(other.hdImageData)) return false
        } else if (other.hdImageData != null) return false
        if (withShareTicket != other.withShareTicket) return false
        if (miniprogramType != other.miniprogramType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = webpageUrl.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (hdImageData?.contentHashCode() ?: 0)
        result = 31 * result + withShareTicket.hashCode()
        result = 31 * result + miniprogramType.hashCode()
        return result
    }
}

/**
 * 该类型需要OpenSDK 1.8.9以及以上版本
 *
 * WXMusicVideoObject 多媒体消息中包含的音乐视频数据对象
 *
 * @property musicUrl
 * @property musicDataUrl
 * @property singerName
 * @property duration
 * @property hdAlbumThumbData
 * @property albumName
 * @property songLyric
 * @property musicGenre
 * @property issueDate
 * @property identification
 * @property hdAlbumThumbFileHash
 */
data class MusicVideoObject(
    /**
     * 音频网页的URL地址	必填，限制长度不超过10KB
     */
    var musicUrl: String = "",

    /**
     * 音频数据的URL地址	必填，限制长度不超过10KB
     */
    var musicDataUrl: String = "",

    /**
     * 歌手名	必填，限制长度不超过1KB
     */
    var singerName: String = "",
    /**
     * 音乐时长，歌曲时间	必填，不能为0，单位：毫秒，
     */
    var duration: UInt = UInt.MIN_VALUE,

    /**
     * 高清专辑封面图	选填，大小不超过1MB
     */
    var hdAlbumThumbData: ByteArray? = null,
    /**
     * 高清专辑图本地文件路径	选填，文件限制长度不超过1MB
     *
     * 仅Android支持
     */
    var hdAlbumThumbFilePath: String = "",

    /**
     * 音乐专辑名称	选填
     */
    var albumName: String = "",

    /**
     * 歌词信息 LRC格式	选填，长度不能超过32K
     */
    var songLyric: String = "",

    /**
     * 音乐流派	选填
     */
    var musicGenre: String = "",

    /**
     * 发行时间Unix时间戳	选填，单位：秒
     */
    var issueDate: ULong = ULong.MIN_VALUE,

    /**
     * 音乐标识符	选填，建议填写，用户在微信音乐播放器内跳回应用时会携带该参数，可用于唯一标识一首歌，微信侧不理解（iOS、Android双平台要一致)
     */
    var identification: String = "",

    /**
     * 高清专辑封面图sha256	用于签名，详情可见 OpenSDK 分享签名开发手册
     */
    var hdAlbumThumbFileHash: String = ""
) : MediaObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MusicVideoObject

        if (musicUrl != other.musicUrl) return false
        if (musicDataUrl != other.musicDataUrl) return false
        if (singerName != other.singerName) return false
        if (duration != other.duration) return false
        if (hdAlbumThumbData != null) {
            if (other.hdAlbumThumbData == null) return false
            if (!hdAlbumThumbData.contentEquals(other.hdAlbumThumbData)) return false
        } else if (other.hdAlbumThumbData != null) return false
        if (hdAlbumThumbFilePath != other.hdAlbumThumbFilePath) return false
        if (albumName != other.albumName) return false
        if (songLyric != other.songLyric) return false
        if (musicGenre != other.musicGenre) return false
        if (issueDate != other.issueDate) return false
        if (identification != other.identification) return false
        if (hdAlbumThumbFileHash != other.hdAlbumThumbFileHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = musicUrl.hashCode()
        result = 31 * result + musicDataUrl.hashCode()
        result = 31 * result + singerName.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + (hdAlbumThumbData?.contentHashCode() ?: 0)
        result = 31 * result + hdAlbumThumbFilePath.hashCode()
        result = 31 * result + albumName.hashCode()
        result = 31 * result + songLyric.hashCode()
        result = 31 * result + musicGenre.hashCode()
        result = 31 * result + issueDate.hashCode()
        result = 31 * result + identification.hashCode()
        result = 31 * result + hdAlbumThumbFileHash.hashCode()
        return result
    }

}