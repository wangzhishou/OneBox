package com.wanbaohe.boardgame.ui

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 棋类页面里 M3 原生按钮的配色:一律走 Container 家族。
 *
 * M3 的 `Button` / `TextButton` / `OutlinedButton` 默认取 `primary` / `onPrimary`,
 * 与仓库「强调色只走 Container 家族」的口径冲突(实心 primary 按钮在浅色主题里尤其跳,
 * 文字按钮也会变成同一种强调蓝)。把口径收在这里,页面侧只写 `colors = accentTextButtonColors()`,
 * 后续调整口径只改这一个文件。
 */

/** 实心按钮:primaryContainer 底 + onPrimaryContainer 字 */
@Composable
fun accentFilledButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

/** 文字按钮:onPrimaryContainer 字 */
@Composable
fun accentTextButtonColors(): ButtonColors = ButtonDefaults.textButtonColors(
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

/** 危险动作(认输/删除)的文字按钮:onErrorContainer 字 */
@Composable
fun dangerTextButtonColors(): ButtonColors = ButtonDefaults.textButtonColors(
    contentColor = MaterialTheme.colorScheme.onErrorContainer,
)

/** 描边按钮:onPrimaryContainer 字(描边保持 M3 的 outline) */
@Composable
fun accentOutlinedButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)
