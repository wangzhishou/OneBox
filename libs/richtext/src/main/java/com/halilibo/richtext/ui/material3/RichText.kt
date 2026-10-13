package com.halilibo.richtext.ui.material3

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
        // 字号梯度向 HTML 默认渲染靠齐(body 16sp):
        // h1=2em, h2=1.5em, h3=1.17em, h4=1em, h5=0.83em, h6=0.67em
        headingStyle = { level, _ ->
            when (level) {
                0 -> TextStyle(
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold
                )

                1 -> TextStyle(
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                2 -> TextStyle(
                    fontSize = 18.7.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                3 -> TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                4 -> TextStyle(
                    fontSize = 13.3.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                5 -> TextStyle(
                    fontSize = 10.7.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                else -> TextStyle(fontWeight = FontWeight.Bold)
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textDecoration = TextDecoration.Underline
                )
            ),
            codeStyle = SpanStyle(
                background = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    )

    // 样式的读写必须走同一个 Local 形成闭环:Heading/CodeBlock 等通过
    // textStyleBackProvider 写入新样式,下游再通过 textStyleProvider 读到它。
    RichTextThemeProvider(
        textStyleProvider = { LocalInternalTextStyle.current },
        contentColorProvider = { LocalInternalContentColor.current },
        textStyleBackProvider = { newTextStyle, content ->
            CompositionLocalProvider(LocalInternalTextStyle provides newTextStyle) {
                content()
            }
        },
        contentColorBackProvider = { newColor, content ->
            CompositionLocalProvider(LocalInternalContentColor provides newColor) {
                content()
            }
        }
    ) {
        CompositionLocalProvider(
            LocalInternalTextStyle provides textStyle,
            LocalInternalContentColor provides contentColor
        ) {
            BasicRichText(
                style = style?.merge(defaultStyle)?.resolveDefaults() ?: defaultStyle.resolveDefaults(),
                modifier = modifier,
                children = children
            )
        }
    }
}
