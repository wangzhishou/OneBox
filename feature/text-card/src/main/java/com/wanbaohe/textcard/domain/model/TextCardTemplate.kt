package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R

data class TextCardTemplate(
    val id: String,
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int,
    val applyStyle: TextCardDocument.() -> TextCardDocument,
) {
    fun applyTo(document: TextCardDocument): TextCardDocument =
        document.applyStyle().copy(templateId = id)

    companion object {
        val all: List<TextCardTemplate> = listOf(
            TextCardTemplate(
                id = "quote_gradient",
                titleRes = R.string.textcard_template_quote,
                descRes = R.string.textcard_template_quote_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Portrait,
                        background = GradientPresets.default,
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.078f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.15f
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.034f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.4f
                        )
                    )
                }
            ),
            TextCardTemplate(
                id = "lined_notes",
                titleRes = R.string.textcard_template_notes,
                descRes = R.string.textcard_template_notes_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Portrait,
                        background = BackgroundSpec.Paper(PaperKind.Lined),
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.068f,
                            alignment = CardTextAlignment.Left
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.034f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.55f
                        )
                    )
                }
            ),
            TextCardTemplate(
                id = "kraft_excerpt",
                titleRes = R.string.textcard_template_excerpt,
                descRes = R.string.textcard_template_excerpt_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Portrait,
                        background = BackgroundSpec.Paper(PaperKind.Kraft),
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.072f,
                            alignment = CardTextAlignment.Center
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.036f,
                            alignment = CardTextAlignment.Justify,
                            lineSpacingMultiplier = 1.5f
                        ),
                        author = author.copy(alignment = CardTextAlignment.Right),
                        source = source.copy(alignment = CardTextAlignment.Right)
                    )
                }
            ),
            TextCardTemplate(
                id = "simple_notice",
                titleRes = R.string.textcard_template_notice,
                descRes = R.string.textcard_template_notice_desc,
                applyStyle = {
                    copy(
                        canvas = CanvasSpec.Square,
                        background = BackgroundSpec.None,
                        backgroundOpacity = 1f,
                        title = title.copy(
                            baseSizeRatio = 0.072f,
                            alignment = CardTextAlignment.Left
                        ),
                        body = body.copy(
                            baseSizeRatio = 0.036f,
                            alignment = CardTextAlignment.Left,
                            lineSpacingMultiplier = 1.4f
                        )
                    )
                }
            )
        )

        fun byId(id: String): TextCardTemplate = all.firstOrNull { it.id == id } ?: all.first()
    }
}
