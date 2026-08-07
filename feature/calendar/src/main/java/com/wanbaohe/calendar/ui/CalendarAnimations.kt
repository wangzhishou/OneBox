package com.wanbaohe.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground


/**
 * 日历 Tab 骨架屏加载占位
 *
 * 纯扁平色块，颜色跟随 MaterialTheme 自适应亮/暗主题
 */
@Composable
fun CalendarTabSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // 模拟 SelectedDayCard
        SkeletonBlock(height = 250, shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(12.dp))
        // 模拟月导航栏
        SkeletonBlock(height = 304, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(8.dp))
        // 模拟星期标题行
        Spacer(modifier = Modifier.height(4.dp))
        // 模拟宜忌双卡
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBlock(
                height = 120,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
            SkeletonBlock(
                height = 120,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 八字 Tab 骨架屏
 */
@Composable
fun BaZiTabSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBlock(height = 44, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBlock(height = 52, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))
        SkeletonBlock(height = 200, shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBlock(height = 160, shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBlock(height = 140, shape = RoundedCornerShape(16.dp))
    }
}

/**
 * 扁平色块占位 — 纯单色，无渐变/shimmer
 *
 * 使用 [MaterialTheme.colorScheme.surfaceContainerLowest] 自动适配亮暗主题
 */
@Composable
private fun SkeletonBlock(
    height: Int,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .glassBackground(
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
            )
    )
}
