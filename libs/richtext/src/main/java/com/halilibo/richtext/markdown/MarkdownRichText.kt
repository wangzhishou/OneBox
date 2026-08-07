package com.halilibo.richtext.markdown

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.shifenmiao.model.node.AstBlockQuote
import com.shifenmiao.model.node.AstCode
import com.shifenmiao.model.node.AstEmphasis
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.shifenmiao.model.node.AstHardLineBreak
import com.shifenmiao.model.node.AstHeading
import com.shifenmiao.model.node.AstImage
import com.shifenmiao.model.node.AstIndentedCodeBlock
import com.shifenmiao.model.node.AstLink
import com.shifenmiao.model.node.AstLinkReferenceDefinition
import com.shifenmiao.model.node.AstListItem
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstParagraph
import com.shifenmiao.model.node.AstSoftLineBreak
import com.shifenmiao.model.node.AstStrikethrough
import com.shifenmiao.model.node.AstStrongEmphasis
import com.shifenmiao.model.node.AstText
import com.shifenmiao.model.node.AstHtmlInline
import com.shifenmiao.model.node.AstJLatexNodeMath
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.InlineContent
import com.halilibo.richtext.ui.string.RichTextString
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.withFormat
import io.noties.markwon.MarkdownJLatexInlineMath
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAppShortcut

/**
 * Only render the text content that exists below [astNode]. All the content blocks
 * like [AstBlockQuote] or [AstFencedCodeBlock] are ignored. This composable is
 * suited for [AstHeading] and [AstParagraph] since they are strictly text blocks.
 *
 * Some notes about commonmark and in general Markdown parsing.
 *
 * - Paragraph and Heading are the only RichTextString containers in base implementation.
 *   - RichTextString is build by traversing the children of Heading or Paragraph.
 *   - RichTextString can include;
 *     - Emphasis
 *     - StrongEmphasis
 *     - Image
 *     - Link
 *     - Code
 * - Code blocks should not have any children. Their whole content must reside in
 * [AstIndentedCodeBlock.literal] or [AstFencedCodeBlock.literal].
 * - Blocks like [BlockQuote], [FormattedList], [AstListItem] must have an [AstParagraph]
 * as a child to include any further RichText.
 * - CustomNode and CustomBlock can have their own scope, no idea about that.
 *
 * @param astNode Root node to accept as Text Content container.
 */
@Composable
internal fun RichTextScope.MarkdownRichText(
  astNode: AstNode,
  modifier: Modifier = Modifier,
  imageConfig: MarkdownImageConfig = MarkdownImageConfig(),
  showCursor: Boolean = false,
) {
  val density = LocalDensity.current
  val textStyle = LocalTextStyle.current
  val fontSize = if (textStyle.fontSize.isSpecified) textStyle.fontSize else 16.sp
  val lineHeight = if (textStyle.lineHeight.isSpecified) textStyle.lineHeight else fontSize

  // Calculate placeholder size once based on current font size
  val inlineMathPlaceholderSize = remember(fontSize, density) {
    with(density) {
      val fontPx = fontSize.toPx()
      val widthPx = (fontPx * 3f).toInt()
      val heightPx = (fontPx * 1.2f).toInt()
      IntSize(widthPx, heightPx)
    }
  }

  // Cursor placeholder size: width follows font size, height follows line height so that
  // the cursor spans the whole line and aligns with surrounding text.
  val cursorSize = remember(fontSize, lineHeight, density, showCursor) {
    if (!showCursor) null else with(density) {
      val widthPx = (fontSize * 0.5f).roundToPx()
      val heightPx = lineHeight.roundToPx()
      IntSize(widthPx, heightPx)
    }
  }

  // Assume that only RichText nodes reside below this level.
  val richText = remember(astNode, imageConfig, inlineMathPlaceholderSize, cursorSize) {
    computeRichTextString(astNode, imageConfig, inlineMathPlaceholderSize, fontSize, cursorSize)
  }

  Text(text = richText, modifier = modifier)
}

private fun computeRichTextString(
  astNode: AstNode,
  imageConfig: MarkdownImageConfig,
  inlineMathPlaceholderSize: IntSize,
  fontSize: TextUnit,
  cursorSize: IntSize? = null,
): RichTextString {
  val richTextStringBuilder = RichTextString.Builder()

  // Modified pre-order traversal with pushFormat, popFormat support.
  var iteratorStack = listOf(
    AstNodeTraversalEntry(
      astNode = astNode,
      isVisited = false,
      formatIndex = null
    )
  )

  while (iteratorStack.isNotEmpty()) {
    val (currentNode, isVisited, formatIndex) = iteratorStack.first().copy()
    iteratorStack = iteratorStack.drop(1)

    if (!isVisited) {
      val newFormatIndex = when (val currentNodeType = currentNode.type) {
        is AstCode -> {
          richTextStringBuilder.withFormat(RichTextString.Format.Code) {
            append(currentNodeType.literal)
          }
          null
        }
        is AstEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Italic)
        is AstStrikethrough -> richTextStringBuilder.pushFormat(
          RichTextString.Format.Strikethrough
        )

        is AstJLatexNodeMath -> {
          currentNodeType.latex?.let { latexContent ->
            richTextStringBuilder.appendInlineContent(
              content = InlineContent(
                initialSize = {
                    inlineMathPlaceholderSize
                },
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
              ) {
                MarkdownJLatexInlineMath(
                  latex = latexContent
                )
              }
            )
          }
          null
        }

        is AstHtmlInline -> {
          // Strip tags and render as plain text so raw markup is not shown.
          richTextStringBuilder.append(stripHtmlTags(currentNodeType.literal))
          null
        }

        is AstImage -> {
          richTextStringBuilder.appendInlineContent(
            content = InlineContent(
              initialSize = {
                IntSize(128.dp.roundToPx(), 128.dp.roundToPx())
              },
              placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            ) {
              MarkdownImage(
                url = currentNodeType.destination,
                title = currentNodeType.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = imageConfig.contentScale,
                onClick = imageConfig.onImageClick?.let { cb ->
                  { cb(currentNodeType.destination, currentNodeType.title) }
                },
                onLongClick = imageConfig.onImageLongClick?.let { cb ->
                  { cb(currentNodeType.destination, currentNodeType.title) }
                },
                loading = imageConfig.loading?.let { loadingContent ->
                  { loadingContent() }
                },
                error = imageConfig.error?.let { errorContent ->
                  { errorContent() }
                }
              )
            }
          )
          null
        }
        is AstLink -> richTextStringBuilder.pushFormat(RichTextString.Format.Link(
          destination = currentNodeType.destination
        ))
        is AstSoftLineBreak -> {
          // CommonMark: soft line breaks render as a single space.
          richTextStringBuilder.append(" ")
          null
        }
        is AstHardLineBreak -> {
          richTextStringBuilder.append("\n")
          null
        }
        is AstStrongEmphasis -> richTextStringBuilder.pushFormat(RichTextString.Format.Bold)
        is AstText -> {
          richTextStringBuilder.append(currentNodeType.literal)
          null
        }
        is AstLinkReferenceDefinition -> richTextStringBuilder.pushFormat(
          RichTextString.Format.Link(destination = currentNodeType.destination))
        else -> null
      }

      iteratorStack = iteratorStack.addFirst(
        AstNodeTraversalEntry(
          astNode = currentNode,
          isVisited = true,
          formatIndex = newFormatIndex
        )
      )

      // Do not visit children of terminals such as Text, Image, etc.
      if (!currentNode.isRichTextTerminal()) {
        currentNode.childrenSequence(reverse = true).forEach {
          iteratorStack = iteratorStack.addFirst(
            AstNodeTraversalEntry(
              astNode = it,
              isVisited = false,
              formatIndex = null
            )
          )
        }
      }
    }

    // 在 Link format 范围内追加“可跳转”提示图标，图标和链接文本在同一行。
    if (isVisited) {
      val linkDestination = when (val nodeType = currentNode.type) {
        is AstLink -> nodeType.destination
        is AstLinkReferenceDefinition -> nodeType.destination
        else -> null
      }
      if (linkDestination != null) {
        richTextStringBuilder.appendLinkIcon(linkDestination, fontSize)
      }
    }

    if (formatIndex != null) {
      richTextStringBuilder.pop(formatIndex)
    }
  }

  if (cursorSize != null) {
    richTextStringBuilder.appendInlineContent(
      content = InlineContent(
        initialSize = { cursorSize },
        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
      ) {
        val infiniteTransition = rememberInfiniteTransition(label = "cursor")
        val alpha by infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 0f,
          animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
          ),
          label = "cursor_blink",
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
        )
      }
    )
  }

  return richTextStringBuilder.toRichTextString()
}

/**
 * 在链接文本后追加一个 inline 跳转图标，图标与链接文本在同一行。
 *
 * 图标被追加在当前 Link format 的作用域内，因此点击图标也会触发链接跳转。
 * 尺寸跟随当前字体大小，颜色跟随链接颜色（[LocalContentColor]）。
 *
 * @param destination 链接目标地址。http/https 使用外部打开图标，其它 scheme（deepLink）使用通用链接图标。
 */
private fun RichTextString.Builder.appendLinkIcon(
  destination: String,
  fontSize: TextUnit
) {
  val isWebUrl = destination.startsWith("http://", ignoreCase = true)
    || destination.startsWith("https://", ignoreCase = true)

  // 链接文本与图标之间保留一个窄空格，避免紧贴
  append("\u2009")
  appendInlineContent(
    alternateText = " ",
    content = InlineContent(
      initialSize = {
        IntSize((fontSize.value * 0.7f).toInt(), fontSize.roundToPx())
      },
      placeholderVerticalAlign = PlaceholderVerticalAlign.Center
    ) {
      val icon = if (isWebUrl) {
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew
      } else {
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAppShortcut
      }
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size((fontSize.value * 0.7f).dp),
        tint = LocalContentColor.current
      )
    }
  )
}

/** Removes HTML tags from inline HTML literals, keeping text content. */
private fun stripHtmlTags(literal: String): String {
  if (literal.isEmpty() || literal.indexOf('<') < 0) return literal
  return literal.replace(HtmlTagRegex, "")
}

private val HtmlTagRegex = Regex("<[^>]*>")

private data class AstNodeTraversalEntry(
  val astNode: AstNode,
  val isVisited: Boolean,
  val formatIndex: Int?
)

private inline fun <reified T> List<T>.addFirst(item: T): List<T> {
  return listOf(item) + this
}
