package com.wanbaohe.a2ui.catalog.builtin.feedback

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class ModalRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Modal"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val visible = context.resolveBoolean(component.properties["visible"]) ?: false
        val title = context.resolveString(component.properties["title"])
        val dismissText = context.resolveString(component.properties["dismissText"])
            ?: stringResource(R.string.a2ui_modal_dismiss)
        val confirmText = context.resolveString(component.properties["confirmText"])

        val visiblePath = (component.properties["visible"] as? DynamicValue.Pointer)?.path
        val onDismiss: () -> Unit = {
            visiblePath?.let { context.updateDataModel(it, JsonPrimitive(false)) }
        }
        val onConfirm: () -> Unit = {
            onDismiss()
            component.action?.let { context.dispatchAction(sourceComponentId = component.id, action = it) }
        }

        EnhancedAlertDialog(
            visible = visible,
            onDismissRequest = onDismiss,
            title = title?.let { { Text(it) } },
            text = { children() },
            confirmButton = {
                if (confirmText != null) {
                    com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton(
                        onClick = onConfirm,
                        content = { Text(confirmText) },
                    )
                }
            },
            dismissButton = {
                com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton(
                    onClick = onDismiss,
                    content = { Text(dismissText) },
                )
            },
        )
    }
}
