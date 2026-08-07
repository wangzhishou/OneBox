package com.shifenmiao.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopRoundedBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = MaterialTheme.shapes.medium.copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
    )
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = shape
            )
            .fillMaxWidth()
            .wrapContentHeight() // 使用wrapContentHeight替代height(20.dp)
    ) {
        content()
    }
}
