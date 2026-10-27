package com.wanbaohe.textcard.domain.model

import com.wanbaohe.textcard.domain.render.CardLayout

enum class TextCardRole {
    Title, Body, Author, Source
}

enum class TextCardOutputMode {
    Pages, LongImage
}

enum class TextCardExportFormat {
    Jpg, Png, Webp
}

data class TextCardDocument(
    val title: TextBlock = TextBlock(
        id = ROLE_TITLE,
        content = "",
        baseSizeRatio = CardLayout.TITLE_BASE_SIZE_RATIO,
        baseTopRatio = CardLayout.CONTENT_PADDING_RATIO,
        isBold = true
    ),
    val body: TextBlock = TextBlock(
        id = ROLE_BODY,
        content = "",
        baseSizeRatio = CardLayout.BODY_BASE_SIZE_RATIO,
        baseTopRatio = CardLayout.BODY_BASE_TOP_RATIO,
        lineSpacingMultiplier = 1.35f
    ),
    val author: TextBlock = TextBlock(
        id = ROLE_AUTHOR,
        content = "",
        baseSizeRatio = 0.028f,
        baseTopRatio = 0f,
        alignment = CardTextAlignment.Right
    ),
    val source: TextBlock = TextBlock(
        id = ROLE_SOURCE,
        content = "",
        baseSizeRatio = 0.023f,
        baseTopRatio = 0f,
        alignment = CardTextAlignment.Right,
        alpha = 0.72f
    ),
    val canvas: CanvasSpec = CanvasSpec.Portrait,
    val background: BackgroundSpec = GradientPresets.default,
    val backgroundOpacity: Float = 1f,
    val templateId: String = "quote_gradient",
    val showPageNumber: Boolean = true,
    val outputMode: TextCardOutputMode = TextCardOutputMode.Pages,
    val exportFormat: TextCardExportFormat = TextCardExportFormat.Png,
    val exportQuality: Int = 90,
    val shareAfterSave: Boolean = false,
) {
    val textBlocks: List<TextBlock>
        get() = listOf(title, body, author, source)

    val hasContent: Boolean
        get() = textBlocks.any { it.content.isNotBlank() }

    fun block(role: TextCardRole): TextBlock = when (role) {
        TextCardRole.Title -> title
        TextCardRole.Body -> body
        TextCardRole.Author -> author
        TextCardRole.Source -> source
    }

    fun updateBlock(
        role: TextCardRole,
        transform: (TextBlock) -> TextBlock,
    ): TextCardDocument = when (role) {
        TextCardRole.Title -> copy(title = transform(title))
        TextCardRole.Body -> copy(body = transform(body))
        TextCardRole.Author -> copy(author = transform(author))
        TextCardRole.Source -> copy(source = transform(source))
    }

    fun displayName(): String = title.content.lineSequence().firstOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: body.content.lineSequence().firstOrNull()?.takeIf { it.isNotBlank() }
        ?: "Text card"

    companion object {
        const val ROLE_TITLE = "title"
        const val ROLE_BODY = "body"
        const val ROLE_AUTHOR = "author"
        const val ROLE_SOURCE = "source"
        const val ROLE_PAGE_NUMBER = "page_number"
        const val MAX_LONG_IMAGE_SIDE = 16384
        const val MAX_LONG_IMAGE_PIXELS = 24_000_000L
    }
}
