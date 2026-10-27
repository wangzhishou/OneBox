package com.wanbaohe.textcard.presentation

import androidx.compose.runtime.Composable
import com.wanbaohe.textcard.presentation.editor.TextCardEditorScreen
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import com.wanbaohe.textcard.presentation.selection.CanvasSelectContent

@Composable
fun TextCardContent(
    component: TextCardComponent,
) {
    component.AttachLifecycle()

    if (component.isEditing) {
        TextCardEditorScreen(component = component)
    } else {
        CanvasSelectContent(component = component)
    }
}
