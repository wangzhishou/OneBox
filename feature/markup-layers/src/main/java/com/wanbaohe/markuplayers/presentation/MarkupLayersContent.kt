package com.wanbaohe.markuplayers.presentation

import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.utils.AutoContentBasedColors
import com.wanbaohe.markuplayers.presentation.editor.EditorScaffold
import com.wanbaohe.markuplayers.presentation.home.MarkupHomeContent
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

@Composable
fun MarkupLayersContent(
    component: MarkupLayersComponent
) {
    AutoContentBasedColors(component.bitmap)

    if (component.hasImage) {
        EditorScaffold(component = component)
    } else {
        MarkupHomeContent(component = component)
    }

    LoadingDialog(
        visible = component.isSaving || component.isImageLoading || component.isAiProcessing,
        onCancelLoading = {
            component.cancelSaving()
            component.cancelAiProcessing()
        },
        canCancel = component.isSaving || component.isAiProcessing
    )
}
