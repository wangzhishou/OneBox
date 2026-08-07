package com.shifenmiao.base.ui.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import com.shifenmiao.base.ui.painter.TextPainter
import com.shifenmiao.base.ui.painter.TextPainterNoBg
import com.shifenmiao.theme.AppTheme

@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color,
    text: String,
    size: Dp = AppTheme.dimens.cardIconSize,
    shape: Shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
) {
    val textMeasurer = rememberTextMeasurer()
    val avatarText = remember(backgroundColor, text, textColor) {
        TextPainter(
            backgroundColor = backgroundColor,
            textMeasurer = textMeasurer,
            text = text,
            textColor = textColor,
            size = Size(size.value, size.value)
        )
    }
    Image(
        modifier = modifier
            .size(size)
            .clip(shape),
        painter = avatarText,
        contentDescription = null
    )
}

@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    textColor: Color,
    text: String,
    alpha: Float = DefaultAlpha,
    textSize: Dp = AppTheme.dimens.cardIconSize,
    shape: Shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
) {
    val textMeasurer = rememberTextMeasurer()
    val avatarText = remember(text, textColor) {
        TextPainterNoBg(
            textMeasurer = textMeasurer,
            text = text,
            textColor = textColor,
            size = Size(textSize.value, textSize.value)
        )
    }
    Image(
        modifier = modifier.clip(shape),
        painter = avatarText,
        alpha = alpha,
        contentDescription = null
    )
}