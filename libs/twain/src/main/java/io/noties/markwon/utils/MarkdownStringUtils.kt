package io.noties.markwon.utils

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup

object MarkdownStringUtils {

    /**
     * 将 Android 原生的 Spanned 转换为 Compose 的 AnnotatedString
     *
     * @param spanned 要转换的 Spanned 对象
     * @return 转换后的 AnnotatedString
     */
    fun spannedToAnnotatedString(spanned: Spanned): AnnotatedString {
        return buildAnnotatedString {
            append(spanned.toString())

            // 处理前景色样式
            spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java).forEach { span ->
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                addStyle(
                    SpanStyle(color = Color(span.foregroundColor)),
                    start,
                    end
                )
            }

            // 处理背景色样式
            spanned.getSpans(0, spanned.length, BackgroundColorSpan::class.java).forEach { span ->
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                addStyle(
                    SpanStyle(background = Color(span.backgroundColor)),
                    start,
                    end
                )
            }

            // 处理字体样式（粗体、斜体）
            spanned.getSpans(0, spanned.length, StyleSpan::class.java).forEach { span ->
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                val spanStyle = when (span.style) {
                    Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                    Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                    Typeface.BOLD_ITALIC -> SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )

                    else -> SpanStyle()
                }
                addStyle(spanStyle, start, end)
            }

            // 处理下划线样式
            spanned.getSpans(0, spanned.length, UnderlineSpan::class.java).forEach { span ->
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                addStyle(
                    SpanStyle(textDecoration = TextDecoration.Underline),
                    start,
                    end
                )
            }
        }
    }

    /**
     * 从 HTML 内容中提取图片链接
     *
     * @param htmlContent 要解析的 HTML 内容
     * @return 提取的图片链接列表
     */
    fun extractImageLinksFromHtml(htmlContent: String): List<String> {
        val imageLinks = mutableListOf<String>()

        // Parse the HTML content using Jsoup
        val doc = Jsoup.parse(htmlContent)

        // Select all <img> tags
        val imgTags = doc.select("img")

        // Extract src attributes from <img> tags
        for (imgTag in imgTags) {
            val imageUrl = imgTag.attr("src")
            imageLinks.add(imageUrl)
        }

        return imageLinks
    }

    fun convertMarkdownToHtml(markdownText: String): String {
        val parser = Parser.builder()
            .extensions(GFM_EXTENSIONS)
            .build()
        val renderer = HtmlRenderer.builder()
            .extensions(GFM_EXTENSIONS)
            .build()
        val document = parser.parse(markdownText)
        return renderer.render(document)
    }

    fun convertMarkdownToHtmlGfm(markdownText: String): String = convertMarkdownToHtml(markdownText)

    private val GFM_EXTENSIONS = listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
    )
}
