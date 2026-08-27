package com.t8rin.imagetoolbox.core.ui.widget.editor

/**
 * 贴纸来源(自 markup-layers 上移 core/ui,贴纸选择面板跨模块共用)。
 * 素材路径相对 assets(如 stickers/decor/flower.svg),emoji 沿用 core/resources/emoji 的矢量图。
 */
sealed interface StickerSource {

    /** assets 内置素材,path 为 assets 内相对路径 */
    data class Asset(val path: String) : StickerSource

    /** emoji,[emojiIndex] 对应 core emoji 表中的下标 */
    data class Emoji(val emojiIndex: Int) : StickerSource

    /** AI 生成贴纸,path 为生成结果的本地文件绝对路径 */
    data class Generated(val path: String) : StickerSource
}
