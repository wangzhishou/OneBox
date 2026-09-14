package com.wanbaohe.a2ui.catalog

import androidx.compose.runtime.compositionLocalOf

/**
 * 当 A2UI 表单渲染在 `placeAboveAll = true` 的弹层(如 ask_user 问询对话框)内部时,
 * 内嵌的 picker 弹窗(日期 / 时间等)需要同步 `placeAboveAll`,
 * 否则会被外层弹层的 z-index 处理盖住。
 */
val LocalA2uiPlaceAboveAll = compositionLocalOf { false }
