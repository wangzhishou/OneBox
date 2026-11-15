package com.wanbaohe.markdown.internal

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrm.markdown.renderer.Markdown
import com.hrm.markdown.renderer.MarkdownBlockSlot
import com.hrm.markdown.renderer.MarkdownComponents
import com.hrm.markdown.renderer.MarkdownDefaults
import com.hrm.markdown.renderer.MarkdownInteractions
import com.hrm.markdown.runtime.MarkdownDirectivePlugin
import com.wanbaohe.core.ui.widget.image.ImageGalleryViewer
import com.wanbaohe.markdown.OneBoxMarkdownCodeBlockInfo
import com.wanbaohe.markdown.OneBoxMarkdownComponents
import com.wanbaohe.markdown.OneBoxMarkdownSelectionActions
import com.wanbaohe.markdown.OneBoxMarkdownSelectionInfo
import com.wanbaohe.markdown.OneBoxMarkdownState

@Composable
internal fun KmpMarkdownRenderer(
    markdown: String,
    modifier: Modifier,
    state: OneBoxMarkdownState,
    isStreaming: Boolean,
    enableScroll: Boolean,
    externalScrollState: ScrollState?,
    enableSelection: Boolean,
    onLinkClick: ((String) -> Unit)?,
    textStyle: TextStyle?,
    contentColor: Color?,
    blockSpacing: Dp?,
    components: OneBoxMarkdownComponents,
    directivePlugins: List<MarkdownDirectivePlugin>,
) {
    var imagePreview by remember { mutableStateOf<ImagePreview?>(null) }
    val interactions = remember(onLinkClick) {
        MarkdownInteractions(
            onLinkClick = onLinkClick,
            onImageClick = { event ->
                val gallery = event.gallery
                val images = gallery.images.map { it.image.url }.ifEmpty { listOf(event.image.url) }
                imagePreview = ImagePreview(
                    images = images,
                    index = gallery.initialIndex.coerceIn(0, images.lastIndex),
                )
            },
        )
    }
    val engineComponents = remember(components) {
        MarkdownComponents(
            header = components.header,
            footer = components.footer,
            codeBlockHeader = components.codeBlockHeader?.let { header ->
                MarkdownBlockSlot(CODE_BLOCK_HEADER_HEIGHT) { code ->
                    header(
                        OneBoxMarkdownCodeBlockInfo(
                            code = code.code,
                            language = code.language,
                            title = code.title,
                        )
                    )
                }
            },
            selectionToolbar = components.selectionToolbar?.let { toolbar ->
                { info, actions ->
                    toolbar(
                        OneBoxMarkdownSelectionInfo(
                            text = info.text,
                            boundsInWindow = info.boundsInWindow,
                        ),
                        OneBoxMarkdownSelectionActions(
                            onCopy = actions::copySelection,
                            onSelectAll = actions::selectAll,
                            onDismiss = actions::dismiss,
                        ),
                    )
                }
            },
        )
    }
    val defaultTypography = MarkdownDefaults.typography()
    val defaultColors = MarkdownDefaults.material3Colors()
    val defaultLayout = MarkdownDefaults.layout()

    Markdown(
        markdown = markdown,
        modifier = modifier,
        colors = if (contentColor == null) defaultColors else defaultColors.copy(
            text = contentColor,
            heading = contentColor,
            blockQuoteText = contentColor,
            listBullet = contentColor,
        ),
        typography = if (textStyle == null) defaultTypography else defaultTypography.copy(text = textStyle),
        layout = if (blockSpacing == null) defaultLayout else defaultLayout.copy(blockSpacing = blockSpacing),
        state = state.engineState,
        isStreaming = isStreaming,
        enableScroll = enableScroll,
        externalScrollState = externalScrollState,
        enableSelection = enableSelection,
        interactions = interactions,
        components = engineComponents,
        directivePlugins = directivePlugins,
    )

    imagePreview?.let { preview ->
        ImageGalleryViewer(
            images = preview.images,
            initialIndex = preview.index,
            onDismiss = { imagePreview = null },
        )
    }
}

private data class ImagePreview(
    val images: List<String>,
    val index: Int,
)

private val CODE_BLOCK_HEADER_HEIGHT = 48.dp
