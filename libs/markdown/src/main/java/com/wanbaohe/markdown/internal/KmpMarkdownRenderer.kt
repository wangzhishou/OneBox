package com.wanbaohe.markdown.internal

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.hrm.codehigh.renderer.CodeLayout
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.latex.renderer.model.LatexTheme
import com.hrm.markdown.renderer.Markdown
import com.hrm.markdown.renderer.MarkdownBlockSlot
import com.hrm.markdown.renderer.MarkdownComponents
import com.hrm.markdown.renderer.MarkdownDefaults
import com.hrm.markdown.renderer.MarkdownInteractions
import com.hrm.markdown.runtime.MarkdownDirectivePlugin
import com.wanbaohe.core.ui.widget.image.ImageGalleryViewer
import com.wanbaohe.diagram.oneBoxDiagramTheme
import com.wanbaohe.markdown.LocalOneBoxMarkdownCodeStyle
import com.wanbaohe.markdown.OneBoxMarkdownCodeBlockInfo
import com.wanbaohe.markdown.OneBoxMarkdownComponents
import com.wanbaohe.markdown.OneBoxMarkdownSelectionActions
import com.wanbaohe.markdown.OneBoxMarkdownSelectionInfo
import com.wanbaohe.markdown.OneBoxMarkdownState
import com.wanbaohe.markdown.oneBoxMarkdownCodeStyle
import com.wanbaohe.markdown.toEngineColors

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
            blockSurface = { _, content -> OneBoxMarkdownBlockSurface(content) },
            inlineCode = { info -> OneBoxMarkdownInlineCode(info) },
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
    val formulaColor = contentColor ?: LocalContentColor.current
    val formulaTheme = remember(formulaColor) {
        LatexTheme.light(color = formulaColor, backgroundColor = Color.Transparent)
    }
    val latexConfig = remember(textStyle, formulaTheme) {
        LatexConfig(
            fontSize = textStyle?.fontSize?.takeIf { it.isSpecified } ?: 20.sp,
            theme = formulaTheme,
            accessibilityEnabled = true,
        )
    }
    val colorScheme = MaterialTheme.colorScheme
    val markdownColors = remember(defaultColors, contentColor, colorScheme) {
        defaultColors.copy(
            text = contentColor ?: defaultColors.text,
            heading = contentColor ?: defaultColors.heading,
            blockQuoteText = contentColor ?: defaultColors.blockQuoteText,
            listBullet = contentColor ?: defaultColors.listBullet,
            tableHeaderBackground = Color.Transparent,
            containerBackground = Color.Transparent,
            containerHeaderBackground = Color.Transparent,
            admonitions = defaultColors.admonitions.mapValues { (_, colors) ->
                colors.copy(backgroundColor = Color.Transparent)
            },
        )
    }
    val markdownLayout = remember(defaultLayout, blockSpacing) {
        defaultLayout.copy(
            blockSpacing = blockSpacing ?: defaultLayout.blockSpacing,
            blockCornerRadius = 12.dp,
        )
    }
    val diagramTheme = remember(colorScheme) { oneBoxDiagramTheme(colorScheme) }
    val codeStyle = remember(colorScheme) {
        oneBoxMarkdownCodeStyle(isDark = colorScheme.surfaceContainer.luminance() < 0.5f)
            .copy(surface = colorScheme.surfaceContainer)
    }
    val codeColors = remember(codeStyle) { codeStyle.toEngineColors() }
    val codeShape = MaterialTheme.shapes.medium
    val inlineCodeShape = MaterialTheme.shapes.small
    val codeLayout = remember(codeShape, inlineCodeShape) {
        CodeLayout(shape = codeShape, inlineShape = inlineCodeShape)
    }
    val defaultCodeTypography = MarkdownDefaults.codeTypography()
    val bodyTextStyle = textStyle ?: MaterialTheme.typography.bodyLarge
    val codeLineHeight = bodyTextStyle.lineHeight.takeIf { it.isSpecified } ?: defaultCodeTypography.text.lineHeight
    val codeTypography = remember(defaultCodeTypography, codeLineHeight) {
        defaultCodeTypography.copy(
            text = defaultCodeTypography.text.copy(lineHeight = codeLineHeight),
            inlineCode = defaultCodeTypography.inlineCode.copy(lineHeight = codeLineHeight),
        )
    }

    CompositionLocalProvider(LocalOneBoxMarkdownCodeStyle provides codeStyle) {
        Markdown(
            markdown = markdown,
            modifier = modifier,
            colors = markdownColors,
            typography = defaultTypography.copy(text = bodyTextStyle),
            layout = markdownLayout,
            latexConfig = latexConfig,
            diagramTheme = diagramTheme,
            codeColors = codeColors,
            codeTypography = codeTypography,
            codeLayout = codeLayout,
            state = state.engineState,
            isStreaming = isStreaming,
            enableScroll = enableScroll,
            externalScrollState = externalScrollState,
            enableSelection = enableSelection,
            interactions = interactions,
            components = engineComponents,
            directivePlugins = directivePlugins,
        )
    }

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
