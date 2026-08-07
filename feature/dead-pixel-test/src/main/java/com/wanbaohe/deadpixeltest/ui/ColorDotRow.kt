package com.wanbaohe.deadpixeltest.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wanbaohe.deadpixeltest.component.DEAD_PIXEL_COLORS

/**
 * 颜色选择点行
 *
 * 显示所有测试颜色的小圆点，当前选中项有白色边框高亮。
 * 点击任意圆点直接跳转到对应颜色。
 */
@Composable
fun ColorDotRow(
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DEAD_PIXEL_COLORS.forEachIndexed { index, testColor ->
            val isSelected = index == currentIndex
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                animationSpec = tween(200),
                label = "dotBorder"
            )
            Box(
                modifier = Modifier
                    .size(if (isSelected) 20.dp else 14.dp)
                    .clip(CircleShape)
                    .background(testColor.color)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onSelect(index) }
            )
        }
    }
}

