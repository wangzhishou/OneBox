/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.wanbaohe.richtext.editor.R

@Composable
fun rememberRichTextStyleScheme(): RichTextStyleScheme {
    val codeBackground = MaterialTheme.colorScheme.tertiaryContainer
    val bold = stringResource(R.string.rich_text_bold)
    val italic = stringResource(R.string.rich_text_italic)
    val underline = stringResource(R.string.rich_text_underline)
    val strikethrough = stringResource(R.string.rich_text_strikethrough)
    val inlineCode = stringResource(R.string.rich_text_inline_code)
    val body = stringResource(R.string.rich_text_body)
    return remember(codeBackground, bold, italic, underline, strikethrough, inlineCode, body) {
        RichTextStyleScheme(
            inlineStyles = listOf(
                RichTextInlineStyle.bold(bold),
                RichTextInlineStyle.italic(italic),
                RichTextInlineStyle.underline(underline),
                RichTextInlineStyle.strikethrough(strikethrough),
                RichTextInlineStyle.inlineCode(codeBackground, inlineCode),
            ),
            blockStyles = listOf(
                RichTextBlockStyle.body(body),
                RichTextBlockStyle.heading1(fontSize = 30.sp, lineHeight = 38.sp),
                RichTextBlockStyle.heading2(fontSize = 24.sp, lineHeight = 32.sp),
            ),
            listStyle = RichTextListStyle(),
        )
    }
}
