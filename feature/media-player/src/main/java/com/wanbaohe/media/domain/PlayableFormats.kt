package com.wanbaohe.media.domain

/**
 * 内置播放器(media3/ExoPlayer)真正解得开的格式。
 *
 * 为什么要单独列一份:MediaStore 会把系统索引到的"音频/视频"全都给我们,
 * 但解码能力取决于 ExoPlayer 的 extractor + 系统 MediaCodec —— AIFF、WMA
 * 这类 MediaStore 认、ExoPlayer 不认的文件如果照常入库,就会出现"列表里有、
 * 点了没反应/被静默跳过",还顺带污染"最近播放"。
 *
 * 类型判断(内容是不是音频/视频)仍用 core:domain 的 `ContentType`,这里只管
 * "能不能播",两者别混用。
 */

/** media3 有 extractor、且平台普遍带对应解码器的音频容器 */
private val PLAYABLE_AUDIO_EXTENSIONS = setOf(
    "mp3", "m4a", "m4b", "m4r", "aac", "flac", "ogg", "oga", "opus",
    "wav", "mka", "amr", "awb", "ac3", "eac3", "mp4", "3gp", "3g2",
)

/** 同上;WMV/RealMedia 系编码平台没有解码器,视频只留常见容器 */
private val PLAYABLE_VIDEO_EXTENSIONS = setOf(
    "mp4", "m4v", "mkv", "webm", "mov", "3gp", "3g2", "ts", "m2ts", "mts",
    "mpg", "mpeg", "flv", "avi", "ogv", "f4v",
)

/** 明确解不开的后缀:即使 MediaStore 归了类也不入库(比白名单更稳,便于报错时判断) */
private val UNSUPPORTED_EXTENSIONS = setOf(
    "aiff", "aif", "aifc", "wma", "asf", "au", "snd", "ra", "ram", "ape",
    "wv", "dsf", "dff", "mid", "midi", "wmv", "rm", "rmvb", "divx", "vob",
    "amv", "mxf",
)

/**
 * 这个文件名能不能用内置播放器播。
 *
 * 没有后缀(MediaStore 的 `content://.../audio/media/86` 只记了数字 id)时无法判断,
 * 按"能播"处理 —— 宁可让它在播放失败时给一次提示,也不要把正常的媒体藏起来。
 */
fun MediaKind.canPlay(fileName: String): Boolean {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    if (extension.isBlank()) return true
    if (extension in UNSUPPORTED_EXTENSIONS) return false
    return extension in when (this) {
        MediaKind.AUDIO -> PLAYABLE_AUDIO_EXTENSIONS
        MediaKind.VIDEO -> PLAYABLE_VIDEO_EXTENSIONS
    }
}

/** 已知解不开的格式:用于区分"格式不支持"和"文件坏了"两种失败文案 */
fun MediaKind.isKnownUnsupported(fileName: String): Boolean {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return extension.isNotBlank() && extension in UNSUPPORTED_EXTENSIONS
}
