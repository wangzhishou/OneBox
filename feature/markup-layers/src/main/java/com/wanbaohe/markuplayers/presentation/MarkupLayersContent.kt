package com.wanbaohe.markuplayers.presentation

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.markuplayers.presentation.editor.EditorScaffold
import com.wanbaohe.markuplayers.presentation.home.MarkupHomeContent
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

@Composable
fun MarkupLayersContent(
    component: MarkupLayersComponent
) {
    if (component.hasImage) {
        EditorScaffold(component = component)
    } else {
        MarkupHomeContent(component = component)
    }

    LoadingDialog(
        visible = component.isSaving || component.isImageLoading || component.isAiProcessing,
        onCancelLoading = component::cancelAllImageTasks,
        canCancel = component.isSaving || component.isImageLoading || component.isAiProcessing,
        content = {
            component.taskLabel?.let { label ->
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}
