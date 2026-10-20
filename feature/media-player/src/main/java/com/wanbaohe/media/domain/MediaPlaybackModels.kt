package com.wanbaohe.media.domain

/**
 * 播放器覆盖的两类媒体。
 *
 * 类型只由打开入口决定（音频/视频后缀或 MIME），播放页面的两套布局也按它分叉。
 */
enum class MediaKind {
    VIDEO,
    AUDIO,
}

/** 按文件名后缀判断媒体类型（收藏等只有名字、没有 MIME 的地方用）。 */
fun MediaKind.matchesFileName(fileName: String): Boolean {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    if (extension.isBlank()) return false
    return extension in when (this) {
        MediaKind.VIDEO -> com.t8rin.imagetoolbox.core.domain.content.ContentType.VIDEO_EXTENSIONS
        MediaKind.AUDIO -> com.t8rin.imagetoolbox.core.domain.content.ContentType.AUDIO_EXTENSIONS
    }
}

/**
 * 一个可播放的媒体文件。
 *
 * 播放队列、最近列表、收藏都只认 URI —— 标题显示用文件名去掉后缀，
 * 真正的作者/时长/封面交给 [MediaMetadataSource] 按需读取（读一次缓存一次）。
 *
 * @param uri 媒体 URI（content:// 或 file://），同时作为队列身份和收藏键
 * @param displayName 带后缀的显示名
 * @param accessedAt 最近访问时间（毫秒），只用于"最近播放"排序
 */
data class MediaEntry(
    val uri: String,
    val displayName: String,
    /** 最近打开时间；本机媒体库里的条目没有访问记录时为 0 */
    val accessedAt: Long = 0L,
    /** 拍摄/创建时间（毫秒）；取不到时为 0，按拍摄时间排序/分组用 */
    val capturedAt: Long = 0L,
    /** MediaStore 里的标题（可能为空；最近打开的文件只有文件名） */
    val mediaTitle: String? = null,
    val artist: String? = null,
    val album: String? = null,
    /** MediaStore 里的时长，0 表示未知（还需要 MediaMetadataRetriever 兜底） */
    val durationMs: Long = 0L,
    /** 可读的位置信息（文件路径或目录），没有就是 null */
    val location: String? = null,
    /**
     * 同一份内容的规范化 URI（文档 URI → MediaStore URI）。
     *
     * 只用于去重/识别，不用于播放：用户可能只授予了文档 URI 的临时权限。
     */
    val identityUri: String? = null,
) {
    /** 标题：去掉后缀的文件名；没有后缀时原样返回 */
    val title: String
        get() = displayName.substringBeforeLast('.', displayName).ifBlank { displayName }

    val extension: String
        get() = displayName.substringAfterLast('.', "").lowercase()

    /** 界面优先显示媒体库标题，其次才是文件名 */
    val bestTitle: String
        get() = mediaTitle?.takeIf { it.isNotBlank() } ?: title

    /**
     * 列表排序/按天分组用的时间。
     *
     * 拍摄时间优先；老文件或文档 URI 没有拍摄时间时退回最近打开时间，
     * 都没有就落到 0（归到"未知日期"）。
     */
    val sortTimeMillis: Long
        get() = capturedAt.takeIf { it > 0L } ?: accessedAt

    /**
     * 列表行的副标题提示：只认作者。
     *
     * 不用专辑兜底：没有标签的文件 MediaStore 会把专辑记成所在目录名（例如 "Download"），
     * 显示成作者会很误导。
     */
    val subtitleHint: String?
        get() = artist?.takeIf { it.isNotBlank() }
}

/**
 * 媒体文件的可选元信息。
 *
 * 这些字段都来自文件本身（MediaMetadataRetriever），拿不到的保持默认值，
 * 界面用文件名/默认文案兜底，不因为缺元数据就报错。
 */
data class MediaMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L,
) {
    val hasDimensions: Boolean get() = width > 0 && height > 0
}

/**
 * 播放模式，与原型里"播放模式"一栏的文案一一对应。
 *
 * 映射到 ExoPlayer 时：顺序播放 = repeatMode OFF（列表放完即停），
 * 单曲循环 = repeatMode ONE，随机播放 = repeatMode ALL + shuffleModeEnabled。
 */
enum class PlaybackMode {
    SEQUENTIAL,
    REPEAT_ONE,
    SHUFFLE,
}

/**
 * 一次播放失败。
 *
 * [unsupported] 表示"内置播放器本来就不认这个格式"（解码器/extractor 缺失），
 * 与文件损坏、权限丢失等运行时错误区分开，页面文案不一样。
 */
data class PlaybackFailure(
    val entry: MediaEntry?,
    val kind: MediaKind,
    val unsupported: Boolean,
    /** 引擎是否已经自动跳到下一项：跳过的才需要一次性提示，否则页面自己会显示错误 */
    val skipped: Boolean = false,
    val message: String? = null,
)

/**
 * 播放器的可观察状态。
 *
 * 位置/时长都由播放内核推送，页面只负责渲染；切歌时整个状态一起换，
 * 不把"第几个"拆成多个字段，避免进度条和标题出现在不同歌上。
 */
data class PlaybackState(
    val mediaUri: String? = null,
    /** 当前播放会话属于音频还是视频；null 表示播放器空闲 */
    val kind: MediaKind? = null,
    /** 播放器的真实队列，页面重进后靠它还原队列面板 */
    val queue: List<MediaEntry> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val mode: PlaybackMode = PlaybackMode.SEQUENTIAL,
    val queueSize: Int = 0,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val hasVideo: Boolean = false,
    val hasError: Boolean = false,
    /** 队列自然播完（STATE_ENDED）：前台服务据此收尾，避免通知常驻 */
    val isEnded: Boolean = false,
) {
    /** 进度（0f..1f）；时长未知时返回 0，避免进度条乱跳 */
    val progress: Float
        get() = if (durationMs <= 0L) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
}
