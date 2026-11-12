/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
    val annotations: List<TextCardAnnotation> = emptyList(),
) {
    val textBlocks: List<TextBlock>
        get() = listOf(title, body, author, source)

    val hasContent: Boolean
        get() = textBlocks.any { it.content.isNotBlank() } || annotations.isNotEmpty()

    fun block(role: TextCardRole): TextBlock = when (role) {
        TextCardRole.Title -> title
        TextCardRole.Body -> body
        TextCardRole.Author -> author
        TextCardRole.Source -> source
    }

    fun updateBlock(
        role: TextCardRole,
        transform: (TextBlock) -> TextBlock,
    ): TextCardDocument {
        val previous = block(role)
        val updated = transform(previous)
        val document = when (role) {
            TextCardRole.Title -> copy(title = updated)
            TextCardRole.Body -> copy(body = updated)
            TextCardRole.Author -> copy(author = updated)
            TextCardRole.Source -> copy(source = updated)
        }
        return document.copy(
            annotations = document.adjustAnnotationAnchors(role, previous.content, updated.content)
        )
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
        const val MANUAL_PAGE_BREAK = '\u000C'
        const val MAX_LONG_IMAGE_SIDE = 16384
        const val MAX_LONG_IMAGE_PIXELS = 24_000_000L
    }
}
