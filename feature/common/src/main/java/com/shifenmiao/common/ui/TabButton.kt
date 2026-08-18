package com.shifenmiao.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * 通用的 Tab 按钮组件
 * 用于图片处理模块中的 Tab 切换（如：已选图片 / 选择样式）
 *
 * 特点：
 * - 选中时有背景色，未选中时透明
 * - 底部无圆角，与下方内容区域无缝衔接
 * - 可复用于相机水印、证件照等模块
 *
 * @param selectedTextStyle 选中态文字样式,默认 titleSmall
 * @param unselectedTextStyle 未选中态文字样式,默认 labelMedium
 */
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTextStyle: TextStyle = MaterialTheme.typography.titleSmall,
    unselectedTextStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        Color.Transparent
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val shape = MaterialTheme.shapes.small.copy(
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp)
    )
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = shape
    ) {
        Text(
            text = text,
            style = if (isSelected) selectedTextStyle else unselectedTextStyle,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

