package com.wanbaohe.markuplayers.domain.model

/**
 * 贴纸来源。素材路径相对 assets(如 stickers/emoji/xxx.png),
 * emoji 沿用 core/resources/emoji 的矢量图。
 */
sealed interface StickerSource {

    /** assets 内置素材,path 为 assets 内相对路径 */
    data class Asset(val path: String) : StickerSource

    /** emoji,[emojiIndex] 对应 core emoji 表中的下标 */
    data class Emoji(val emojiIndex: Int) : StickerSource
}
