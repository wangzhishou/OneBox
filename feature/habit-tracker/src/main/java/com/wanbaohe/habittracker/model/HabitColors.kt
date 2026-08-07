package com.wanbaohe.habittracker.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.t8rin.imagetoolbox.core.ui.theme.blend
import kotlin.math.abs

/**
 * 习惯配色板 — 全部派生自 MaterialTheme.colorScheme,
 * 深色/浅色、动态色下均可自适应(参考 bookkeeping 的 chartPalette)。
 */
@Composable
fun habitPalette(): List<Color> {
    val s = MaterialTheme.colorScheme
    return remember(s) {
        listOf(
            s.primary,
            s.secondary,
            s.tertiary,
            s.error,
            s.primary.blend(s.tertiary, 0.5f),
            s.tertiary.blend(s.error, 0.5f),
            s.secondary.blend(s.error, 0.5f),
            s.inversePrimary,
        )
    }
}

/**
 * 解析习惯展示色:
 * - 用户指定色 → 夜间模式做提亮,保证深色背景下可辨识
 * - 未指定(null=自动)→ 按 habitId 哈希在配色板上稳定取色
 */
@Composable
fun resolveHabitColor(colorArgb: Long?, habitId: String): Color {
    val palette = habitPalette()
    val isDark = isSystemInDarkTheme()
    return remember(colorArgb, habitId, palette, isDark) {
        val base = if (colorArgb != null) {
            Color(colorArgb)
        } else {
            palette[abs(habitId.hashCode()) % palette.size]
        }
        if (isDark && colorArgb != null) {
            // 夜间提亮:向白色混合 35%
            base.blend(Color.White, 0.35f)
        } else {
            base
        }
    }
}
