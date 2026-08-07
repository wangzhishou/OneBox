package com.shifenmiao.base.ui.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun VerticalText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    style: TextStyle = LocalTextStyle.current,
    spacing: Dp = 0.dp
) {
    val characters = remember(text) { text.toList() }

    Column(
        modifier = modifier,
        horizontalAlignment = when (textAlign) {
            TextAlign.Left -> Alignment.Start
            TextAlign.Right -> Alignment.End
            TextAlign.Center -> Alignment.CenterHorizontally
            else -> Alignment.CenterHorizontally
        }
    ) {
        characters.forEachIndexed { index, char ->
            Text(
                text = char.toString(),
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                letterSpacing = letterSpacing,
                textAlign = textAlign,
                lineHeight = lineHeight,
                maxLines = 1,
                overflow = overflow,
                softWrap = softWrap,
                style = style
            )
            if (index < characters.lastIndex) {
                Spacer(modifier = Modifier.height(spacing))
            }
        }
    }
}