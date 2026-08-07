package com.halilibo.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markwon.JLatexMathStyle
import com.halilibo.richtext.markwon.resolveDefaults
import com.halilibo.richtext.ui.string.RichTextStringStyle

internal val LocalRichTextStyle = compositionLocalOf { RichTextStyle.Default }
internal val DefaultParagraphSpacing: TextUnit = 8.sp

/**
 * 配置所有用于绘制富文本的格式属性
 */
@Immutable
data class RichTextStyle(
  val paragraphSpacing: TextUnit? = null,
  val headingStyle: HeadingStyle? = null,
  val listStyle: ListStyle? = null,
  val blockQuoteGutter: BlockQuoteGutter? = null,
  val codeBlockStyle: CodeBlockStyle? = null,
  val tableStyle: TableStyle? = null,
  val infoPanelStyle: InfoPanelStyle? = null,
  val stringStyle: RichTextStringStyle? = null,
  val jLatexMathStyle: JLatexMathStyle = JLatexMathStyle.Default
) {
  companion object {
    val Default: RichTextStyle = RichTextStyle()
  }
}

fun RichTextStyle.merge(otherStyle: RichTextStyle?): RichTextStyle = RichTextStyle(
  paragraphSpacing = otherStyle?.paragraphSpacing ?: paragraphSpacing,
  headingStyle = otherStyle?.headingStyle ?: headingStyle,
  listStyle = otherStyle?.listStyle ?: listStyle,
  blockQuoteGutter = otherStyle?.blockQuoteGutter ?: blockQuoteGutter,
  codeBlockStyle = otherStyle?.codeBlockStyle ?: codeBlockStyle,
  tableStyle = otherStyle?.tableStyle ?: tableStyle,
  infoPanelStyle = otherStyle?.infoPanelStyle ?: infoPanelStyle,
  stringStyle = stringStyle?.merge(otherStyle?.stringStyle) ?: otherStyle?.stringStyle,
  jLatexMathStyle = otherStyle?.jLatexMathStyle ?: jLatexMathStyle
)

fun RichTextStyle.resolveDefaults(): RichTextStyle = RichTextStyle(
  paragraphSpacing = paragraphSpacing ?: DefaultParagraphSpacing,
  headingStyle = headingStyle ?: DefaultHeadingStyle,
  listStyle = (listStyle ?: ListStyle.Default).resolveDefaults(),
  blockQuoteGutter = blockQuoteGutter ?: DefaultBlockQuoteGutter,
  codeBlockStyle = (codeBlockStyle ?: CodeBlockStyle.Default).resolveDefaults(),
  tableStyle = (tableStyle ?: TableStyle.Default).resolveDefaults(),
  infoPanelStyle = (infoPanelStyle ?: InfoPanelStyle.Default).resolveDefaults(),
  stringStyle = (stringStyle ?: RichTextStringStyle.Default).resolveDefaults(),
  jLatexMathStyle = jLatexMathStyle.resolveDefaults()
)

/**
 * 当前的RichTextStyle
 */
val currentRichTextStyle: RichTextStyle
  @Composable get() = LocalRichTextStyle.current

/**
 * 为子元素设置RichTextStyle
 */
@Composable
fun RichTextScope.WithStyle(
  style: RichTextStyle?,
  children: @Composable RichTextScope.() -> Unit
) {
  if (style == null) {
    children()
  } else {
    val mergedStyle = LocalRichTextStyle.current.merge(style)
    CompositionLocalProvider(LocalRichTextStyle provides mergedStyle) {
      children()
    }
  }
}
