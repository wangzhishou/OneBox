package com.shifenmiao.base.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme

@Composable
fun GrayHorizontalDivider(
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
    thickness: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        color = color,
        thickness = thickness,
        modifier = modifier
    )
}

@Composable
fun BoxHorizontalDivider(
    modifier: Modifier = Modifier.padding(vertical = AppTheme.dimens.paddingLarge),
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    backgroundColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.background(backgroundColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = color,
        )
        content()
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = color,
        )
    }
}