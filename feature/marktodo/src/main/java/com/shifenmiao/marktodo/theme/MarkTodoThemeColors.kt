package com.shifenmiao.marktodo.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 待办模块系统主题色辅助函数。
 *
 * 所有分类/任务卡片不再使用存储的自定义颜色，
 * 而是根据在列表中的索引位置循环使用 Material 3 系统主题色。
 *
 * 参考 FeaturedComponents.kt 中的 sectionThemeForIndex 模式。
 */

enum class CategoryTheme {
    PRIMARY, SECONDARY, TERTIARY, SURFACE
}

/** 根据索引（或任何稳定整数）获取循环主题色类型 */
fun categoryThemeForIndex(index: Int): CategoryTheme = when (index % 4) {
    0 -> CategoryTheme.PRIMARY
    1 -> CategoryTheme.SECONDARY
    2 -> CategoryTheme.TERTIARY
    else -> CategoryTheme.SURFACE
}

/** 卡片/容器背景色 */
@Composable
fun categoryContainerColor(theme: CategoryTheme): Color = when (theme) {
    CategoryTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    CategoryTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    CategoryTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
    CategoryTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
}

/** 头部/强调背景色 */
@Composable
fun categoryHeaderColor(theme: CategoryTheme): Color = when (theme) {
    CategoryTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    CategoryTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    CategoryTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    CategoryTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer
}

/** 在深色背景上的内容文字/图标色 */
@Composable
fun categoryContentColor(theme: CategoryTheme): Color = when (theme) {
    CategoryTheme.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    CategoryTheme.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    CategoryTheme.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    CategoryTheme.SURFACE -> MaterialTheme.colorScheme.onSurface
}

/** 图标容器背景色 */
@Composable
fun categoryIconContainerColor(theme: CategoryTheme): Color = when (theme) {
    CategoryTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    CategoryTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    CategoryTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
    CategoryTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
}
