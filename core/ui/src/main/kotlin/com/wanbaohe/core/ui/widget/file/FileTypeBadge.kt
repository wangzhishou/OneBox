package com.wanbaohe.core.ui.widget.file

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.utils.file.FileTypeIconProvider
import com.wanbaohe.core.ui.utils.file.FileVisualCategory

/**
 * 文件类型图标块 —— 列表行左侧统一的长相：
 * 主题容器色圆角方块 + 该类型的线性图标。
 */
@Composable
fun FileTypeBadge(
    category: FileVisualCategory,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    contentDescription: String? = null
) {
    val style = FileTypeIconProvider.styleFor(category)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(style.containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = contentDescription,
            tint = style.contentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
