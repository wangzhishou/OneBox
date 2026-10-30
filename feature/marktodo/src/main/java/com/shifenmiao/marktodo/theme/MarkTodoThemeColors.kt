package com.shifenmiao.marktodo.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.marktodo.model.TodoCategory
import com.wanbaohe.core.ui.theme.blend

/** 分组可选色板（新增分组页）。 */
val CategoryColorPalette: List<Color> = listOf(
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFF5B8DEF),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
)

val CategoryColorPaletteArgb: List<Int> = CategoryColorPalette.map { it.toArgb() }

@Composable
fun categoryPaletteWithThemeColors(): List<Color> {
    val scheme = MaterialTheme.colorScheme
    return (listOf(scheme.primary, scheme.secondary, scheme.tertiary) + CategoryColorPalette)
        .distinctBy { it.toArgb() }
}

/** 与 FeaturedComponents 相同的四色循环，按列表位置分配主题色。 */
enum class MarkTodoSectionTheme { PRIMARY, SECONDARY, TERTIARY, SURFACE }

fun markTodoSectionThemeForIndex(index: Int): MarkTodoSectionTheme =
    when ((index % 4 + 4) % 4) {
        0 -> MarkTodoSectionTheme.PRIMARY
        1 -> MarkTodoSectionTheme.SECONDARY
        2 -> MarkTodoSectionTheme.TERTIARY
        else -> MarkTodoSectionTheme.SURFACE
    }

@Composable
private fun sectionContainerColor(theme: MarkTodoSectionTheme): Color = when (theme) {
    MarkTodoSectionTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    MarkTodoSectionTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    MarkTodoSectionTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    MarkTodoSectionTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer
}

@Composable
private fun sectionContentColor(theme: MarkTodoSectionTheme): Color = when (theme) {
    MarkTodoSectionTheme.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    MarkTodoSectionTheme.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    MarkTodoSectionTheme.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    MarkTodoSectionTheme.SURFACE -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun themedAccentColor(color: Color): Color =
    color.blend(MaterialTheme.colorScheme.primary, 0.24f)

private fun hasCustomColor(colorArgb: Int?): Boolean =
    colorArgb != null && colorArgb !in CategoryColorPaletteArgb && Color(colorArgb).alpha > 0f

@Composable
fun categoryAccentColor(category: TodoCategory, themeIndex: Int): Color =
    if (hasCustomColor(category.colorArgb)) {
        themedAccentColor(Color(category.colorArgb!!))
    } else {
        sectionContentColor(markTodoSectionThemeForIndex(themeIndex))
    }

@Composable
fun categoryAccentContainerColor(category: TodoCategory, themeIndex: Int): Color =
    if (hasCustomColor(category.colorArgb)) {
        categoryAccentColor(category, themeIndex)
            .blend(MaterialTheme.colorScheme.primaryContainer, 0.55f)
    } else {
        sectionContainerColor(markTodoSectionThemeForIndex(themeIndex))
    }

@Composable
fun categoryCardTintColor(category: TodoCategory, themeIndex: Int): Color =
    if (hasCustomColor(category.colorArgb)) {
        categoryAccentColor(category, themeIndex)
            .blend(MaterialTheme.colorScheme.surfaceContainer, 0.82f)
    } else {
        sectionContainerColor(markTodoSectionThemeForIndex(themeIndex))
    }

@Composable
fun tagAccentColor(colorArgb: Int?, themeIndex: Int): Color =
    if (hasCustomColor(colorArgb)) {
        themedAccentColor(Color(colorArgb!!))
    } else {
        sectionContentColor(markTodoSectionThemeForIndex(themeIndex))
    }

@Composable
fun tagContainerColor(colorArgb: Int?, themeIndex: Int): Color =
    if (hasCustomColor(colorArgb)) {
        tagAccentColor(colorArgb, themeIndex)
            .blend(MaterialTheme.colorScheme.surfaceContainer, 0.86f)
    } else {
        sectionContainerColor(markTodoSectionThemeForIndex(themeIndex))
    }

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
