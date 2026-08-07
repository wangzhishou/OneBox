package com.halilibo.richtext.ui.material3

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.t8rin.imagetoolbox.core.ui.widget.image.ImageGalleryViewer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.MarkdownImageConfig
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.BasicRichText
import com.halilibo.richtext.ui.LocalInternalContentColor
import com.halilibo.richtext.ui.LocalInternalTextStyle
import com.halilibo.richtext.ui.ListStyle
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.RichTextThemeProvider
import com.halilibo.richtext.ui.merge
import com.halilibo.richtext.ui.resolveDefaults
import com.halilibo.richtext.ui.string.RichTextStringStyle
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstNodeLinks
import com.shifenmiao.model.node.AstText
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RichMarkdown(
    modifier: Modifier = Modifier,
    content: String = "",
    contentBlock: (@Composable RichTextScope.() -> Unit)? = null,
    codeBlockClickListener: CodeBlockClickListener? = null
) {
    val markdownParseOptions by remember { mutableStateOf(CommonMarkdownParseOptions.Default) }
    val context = LocalContext.current

    var astNode by remember {
        mutableStateOf(
            AstNode(
                type = AstText(
                    literal = ""
                ),
                links = AstNodeLinks(
                    parent = null,
                    previous = null,
                )
            )
        )
    }

    LaunchedEffect(content) {
        withContext(Dispatchers.IO) {
            if (content.isNotEmpty()) {
                val parser = MarkdownAstNodeParser(context, markdownParseOptions)
                astNode = parser.parse(content)
            }
        }
    }

    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    val imageConfig = remember {
        MarkdownImageConfig(
            onImageClick = { url, _ ->
                previewImageUrl = url
            }
        )
    }

    RichText(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .then(modifier),
    ) {
        if (contentBlock != null) {
            contentBlock()
        } else {
            BasicMarkdown(
                astNode = astNode,
                codeBlockClickListener = codeBlockClickListener,
                imageConfig = imageConfig
            )
        }
    }

    previewImageUrl?.let { url ->
        ImageGalleryViewer(
            images = listOf(url),
            onDismiss = { previewImageUrl = null }
        )
    }
}

/**
 * Material 3风格的RichText实现
 */
@Composable
fun RichText(
    modifier: Modifier = Modifier,
    style: RichTextStyle? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    children: @Composable RichTextScope.() -> Unit,
) {
    val defaultStyle = RichTextStyle(
        paragraphSpacing = 12.sp,
        headingStyle = { level, defaultTextStyle ->
            when (level) {
                0 -> TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold
                )

                1 -> TextStyle(
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                2 -> TextStyle(
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = defaultTextStyle.color.copy(alpha = 0.8f)
                )

                3 -> TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                4 -> TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = defaultTextStyle.color.copy(alpha = 0.8f)
                )

                5 -> TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = defaultTextStyle.color.copy(alpha = 0.6f)
                )

                else -> defaultTextStyle
            }
        },
        listStyle = ListStyle(
            itemSpacing = 8.sp,
            markerIndent = 12.sp,
            contentsIndent = 8.sp
        ),
        stringStyle = RichTextStringStyle(
            linkStyle = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ),
            codeStyle = SpanStyle(
                background = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    )

    // 直接使用Material 3的文本样式和颜色
    RichTextThemeProvider(
        textStyleProvider = { textStyle },
        contentColorProvider = { contentColor },
        textStyleBackProvider = { textStyle, content ->
            ProvideTextStyle(textStyle, content)
        },
        contentColorBackProvider = { _, content ->
            content()
        }
    ) {
        BasicRichText(
            style = style?.merge(defaultStyle)?.resolveDefaults() ?: defaultStyle.resolveDefaults(),
            modifier = modifier,
            children = children
        )
    }
}
