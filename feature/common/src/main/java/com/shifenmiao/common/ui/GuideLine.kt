package com.shifenmiao.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 导视线 — 播放页屏幕上的半透明指示线（带渐变光晕）
 *
 * 可用于提词器、字幕滚动等场景。
 */
@Composable
fun GuideLine(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6C63FF),
    thickness: Dp = 2.dp,
    glowHeight: Dp = 24.dp,
) {
    Box(modifier = modifier) {
        // 上部渐变光晕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(glowHeight)
                .offset(y = -glowHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, color.copy(alpha = 0.15f))
                    )
                )
        )
        // 主线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .background(color.copy(alpha = 0.7f))
        )
        // 下部渐变光晕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(glowHeight)
                .offset(y = thickness)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
    }
}

