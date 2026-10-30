package com.shifenmiao.marktodo.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.marktodo.model.TodoCategory
import com.wanbaohe.core.ui.theme.blend
import kotlin.math.abs

/**
 * 待办模块主题色辅助。
 *
 * 配色参考 FeaturedComponents.kt 的 SectionTheme 体系，但分配维度从「列表索引」
 * 换成「分类 ID」：每个主题按 id 稳定分到色板色，过滤/排序后颜色不漂移。
 * 分类可用 colorArgb 自定义颜色（全透明的无效色按未设置处理），未设置时按 ID 分配。
 * 卡片内元素统一用同色系：图标徽章/进度条/勾选框用强调色，卡片玻璃底色用同色系浅染。
 */

/** 主题可选色板（新增主题页） */
val CategoryColorPalette: List<Color> = listOf(
    Color(0xFF8B5CF6), // 紫
    Color(0xFFEC4899), // 粉
    Color(0xFF5B8DEF), // 蓝
    Color(0xFF10B981), // 绿
    Color(0xFFF59E0B), // 橙
)

/** 色板的 ARGB int 形式（持久化/默认值用） */
val CategoryColorPaletteArgb: List<Int> = CategoryColorPalette.map { it.toArgb() }

/** 快捷色板：当前主题三主色（primary/secondary/tertiary）前置 + 固定色板，按 ARGB 去重 */
@Composable
fun categoryPaletteWithThemeColors(): List<Color> {
    val scheme = MaterialTheme.colorScheme
    return (listOf(scheme.primary, scheme.secondary, scheme.tertiary) + CategoryColorPalette)
        .distinctBy { it.toArgb() }
}

/** 按分类 ID 稳定分配色板色 */
fun categoryPaletteColorForId(categoryId: String): Color =
    CategoryColorPalette[abs(categoryId.hashCode()) % CategoryColorPalette.size]

@Composable
private fun themedAccentColor(color: Color): Color =
    color.blend(MaterialTheme.colorScheme.primary, 0.24f)

/** 分组强调色：保留自定义色相，并随当前主题调整明暗与饱和度。 */
@Composable
fun categoryAccentColor(category: TodoCategory): Color {
    val rawColor = category.colorArgb?.let { Color(it) }?.takeIf { it.alpha > 0f }
        ?: categoryPaletteColorForId(category.id)
    return themedAccentColor(rawColor)
}

/** 与当前主题容器色协调的分组徽章底色。 */
@Composable
fun categoryAccentContainerColor(category: TodoCategory): Color =
    categoryAccentColor(category).blend(MaterialTheme.colorScheme.primaryContainer, 0.55f)

/** 分组卡片底色：保留分组色相，并适配当前主题的明暗与玻璃管线。 */
@Composable
fun categoryCardTintColor(category: TodoCategory): Color =
    categoryAccentColor(category).blend(MaterialTheme.colorScheme.surfaceContainer, 0.82f)

/** 标签强调色：使用标签原始色相，并跟随主题配色。 */
@Composable
fun tagAccentColor(colorArgb: Int?): Color =
    themedAccentColor(colorArgb?.let(::Color) ?: MaterialTheme.colorScheme.onPrimaryContainer)

/** 标签容器色：在浅色和深色主题下均保持可读性。 */
@Composable
fun tagContainerColor(colorArgb: Int?): Color =
    tagAccentColor(colorArgb).blend(MaterialTheme.colorScheme.surfaceContainer, 0.86f)

/** 预置标签 id → 颜色（与 FeatureDatabase 种子及 service PRESET_TAG_COLORS 一致） */
val PresetTagColors: Map<String, Color> = mapOf(
    "preset_work" to Color(0xFF5B8DEF),
    "preset_study" to Color(0xFF8B5CF6),
    "preset_life" to Color(0xFFEC4899),
    "preset_health" to Color(0xFF10B981),
    "preset_travel" to Color(0xFFF59E0B),
)

/** 优先级配色：高=粉，中=橙，低=绿 */
fun priorityColor(priority: Int): Color = when (priority) {
    2 -> Color(0xFFEC4899)
    0 -> Color(0xFF10B981)
    else -> Color(0xFFF59E0B)
}
