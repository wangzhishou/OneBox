package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSheetHeader

/**
 * 底部弹层共享头部 — 已统一到 [EnhancedSheetHeader](样式基准:本模块「编辑文字」面板)。
 * 保留本包装以免各调用点立刻改 import;新代码请直接用 [EnhancedSheetHeader]。
 *
 * [showDragHandle] 默认 true:标题上方内嵌紧凑拖动条,宿主 Sheet 必须传
 * `dragHandle = {}` 抑制默认条。
 */
@Composable
fun SheetHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    closeEnabled: Boolean = true,
    showDragHandle: Boolean = true,
) {
    EnhancedSheetHeader(
        title = title,
        onClose = onClose,
        modifier = modifier,
        closeEnabled = closeEnabled,
        showDragHandle = showDragHandle
    )
}
