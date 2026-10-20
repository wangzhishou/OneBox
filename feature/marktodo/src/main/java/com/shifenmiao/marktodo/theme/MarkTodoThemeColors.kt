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

/**
 * 主题强调色：自定义色优先（全透明为历史脏数据按未设置），否则按分类 ID 分配。
 */
fun categoryAccentColor(category: TodoCategory): Color =
    category.colorArgb?.let { Color(it) }?.takeIf { it.alpha > 0f }
        ?: categoryPaletteColorForId(category.id)

/** 同色系浅容器色（图标徽章/标签底色等） */
fun categoryAccentContainerColor(category: TodoCategory): Color =
    categoryAccentColor(category).copy(alpha = 0.14f)

/**
 * 卡片玻璃底色：把强调色向页面底色 blend 成粉彩色（16% 色相），再交给玻璃管线。
 * 管线的 tint overlay 层不随 containerAlpha 线性变化，直接传饱和色会过浓；
 * 预混合成 pastel 后色相保留（RGB 通道极差 > 12，不会被判成中性玻璃）、观感只剩淡淡一层。
 * 非玻璃降级路径下 Card 直接用这个粉彩色，观感一致。
 */
@Composable
fun categoryCardTintColor(category: TodoCategory): Color =
    categoryAccentColor(category).blend(MaterialTheme.colorScheme.surface, 0.84f)

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
