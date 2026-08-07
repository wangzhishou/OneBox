package com.shifenmiao.base.ui.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme

@Composable
fun ClickChip(
    text: String,
    icon: ImageVector?,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.extraSmall
    Row(
        modifier = Modifier
            .clip(shape)
            .clickable {
                onClick.invoke()
            }
            .background(
                color = colors.containerColor,
                shape = shape
            )
            .padding(
                horizontal = AppTheme.dimens.paddingSmall,
                vertical = 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "Icon",
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = AppTheme.dimens.paddingTooSmall),
                tint = colors.contentColor
            )
        }
        Text(
            modifier = Modifier.wrapContentWidth(),
            text = text,
            color = colors.contentColor,
            maxLines = 1,
            minLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp
            ),
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
        )
    }
}