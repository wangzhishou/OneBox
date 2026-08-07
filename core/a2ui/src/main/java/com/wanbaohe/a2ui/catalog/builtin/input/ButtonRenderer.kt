package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class ButtonRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Button"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val label = context.resolveString(component.properties["label"])
            ?: context.resolveString(component.properties["text"])
        val variant = context.resolveString(component.properties["variant"]) ?: "filled"
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true

        val onClick: () -> Unit = {
            component.action?.let { action ->
                context.dispatchAction(sourceComponentId = component.id, action = action)
            }
        }

        val content: @Composable RowScope.() -> Unit = {
            if (label != null) {
                Text(text = label)
            } else {
                children()
            }
        }

        when (variant.lowercase()) {
            "outlined" -> GlassOutlinedButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )

            "text", "ghost" -> GlassTextButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )

            "tonal" -> GlassTonalButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )

            else -> GlassButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )
        }
    }
}
