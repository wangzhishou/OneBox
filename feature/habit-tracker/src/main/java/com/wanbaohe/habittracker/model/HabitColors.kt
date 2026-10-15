package com.wanbaohe.habittracker.model

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.t8rin.imagetoolbox.core.ui.theme.blend
import com.t8rin.imagetoolbox.core.ui.widget.glass.tintedGlassContentColor
import kotlin.math.abs

/**
 * 习惯展示色 = 容器色 + 内容色一对。
 *
 * 按仓库约定, 身份/分类色只走 container 家族: [container] 只负责色调(经玻璃管线铺成底),
 * 图标、勾选这类前景一律用 [content](onXContainer)——直接拿容器色当图标色会糊成一片。
 * 做法对齐 功能卡片(TypeSelectionCard / GenericTonalCard 的 icon chip)。
 */
data class HabitColorPair(
    val container: Color,
    val content: Color,
)

/**
 * 习惯配色板 —— 全部派生自 MaterialTheme.colorScheme 的 container 家族,
 * 深色/浅色、动态色下均可自适应。
 */
@Composable
fun habitColorPalette(): List<HabitColorPair> {
    val s = MaterialTheme.colorScheme
    return remember(s) {
        listOf(
            HabitColorPair(s.primaryContainer, s.onPrimaryContainer),
            HabitColorPair(s.secondaryContainer, s.onSecondaryContainer),
            HabitColorPair(s.tertiaryContainer, s.onTertiaryContainer),
            HabitColorPair(s.errorContainer, s.onErrorContainer),
            HabitColorPair(
                s.primaryContainer.blend(s.tertiaryContainer, 0.5f),
                s.onPrimaryContainer,
            ),
            HabitColorPair(
                s.tertiaryContainer.blend(s.errorContainer, 0.5f),
                s.onTertiaryContainer,
            ),
            HabitColorPair(
                s.secondaryContainer.blend(s.errorContainer, 0.5f),
                s.onSecondaryContainer,
            ),
            HabitColorPair(
                s.primaryContainer.blend(s.secondaryContainer, 0.5f),
                s.onPrimaryContainer,
            ),
        )
    }
}

/** 色板里的容器色列表(编辑页选色、图表序列用);实例稳定, 可直接当 remember 的 key。 */
@Composable
fun habitPalette(): List<Color> {
    val pairs = habitColorPalette()
    return remember(pairs) { pairs.map { it.container } }
}

/**
 * 解析习惯展示色:
 * - 用户指定色 → 命中色板就用色板配好的内容色, 否则按容器色亮度兜底算一个可读前景
 * - 未指定(null=自动)→ 按 habitId 哈希在配色板上稳定取色
 */
@Composable
fun resolveHabitColor(colorArgb: Long?, habitId: String): HabitColorPair {
    val palette = habitColorPalette()
    val colorScheme = MaterialTheme.colorScheme
    val container = remember(colorArgb, habitId, palette, colorScheme) {
        if (colorArgb != null) {
            Color(colorArgb).toContainerColor(colorScheme)
        } else {
            palette[abs(habitId.hashCode()) % palette.size].container
        }
    }

    palette.firstOrNull { it.container == container }?.let { return it }
    // 用户自定义/历史遗留色没有配对的 on 色, 按玻璃混合后的有效底色推一个可读前景
    return HabitColorPair(container, tintedGlassContentColor(container))
}

/** 库里存的色值 → 实际渲染用的容器色;编辑页拿它跟色板比对, 老数据才会显示为"选中"。 */
@Composable
fun habitContainerColor(colorArgb: Long): Color {
    val colorScheme = MaterialTheme.colorScheme
    return remember(colorArgb, colorScheme) {
        Color(colorArgb).toContainerColor(colorScheme)
    }
}

/**
 * 旧色板存的是裸 primary/secondary/tertiary/error/inversePrimary, 这里归一到对应容器色,
 * 免得历史习惯游离在新配色之外(不动库里的值, 只改渲染口径)。
 */
private fun Color.toContainerColor(colorScheme: ColorScheme): Color = when (this) {
    colorScheme.primary -> colorScheme.primaryContainer
    colorScheme.secondary -> colorScheme.secondaryContainer
    colorScheme.tertiary -> colorScheme.tertiaryContainer
    colorScheme.error -> colorScheme.errorContainer
    colorScheme.inversePrimary -> colorScheme.primaryContainer
    else -> this
}
