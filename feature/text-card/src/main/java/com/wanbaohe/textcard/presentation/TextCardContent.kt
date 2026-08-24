package com.wanbaohe.textcard.presentation

import androidx.compose.runtime.Composable
import com.wanbaohe.textcard.presentation.editor.TextCardEditorScreen
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import com.wanbaohe.textcard.presentation.selection.CanvasSelectContent

/**
 * 文字卡片入口:canvas 为 null 显示选择画布页,否则进入编辑页
 * (单 Component 双页面,同 markup-layers 的 hasImage 模式)。
 */
@Composable
fun TextCardContent(
    component: TextCardComponent,
) {
    component.AttachLifecycle()

    if (component.canvas != null) {
        TextCardEditorScreen(component = component)
    } else {
        CanvasSelectContent(component = component)
    }
}
