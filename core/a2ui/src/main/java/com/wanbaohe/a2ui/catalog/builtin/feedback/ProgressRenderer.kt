package com.wanbaohe.a2ui.catalog.builtin.feedback

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCircularProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class ProgressRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Progress"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val type = context.resolveString(component.properties["type"]) ?: "circular"
        val progress = context.resolveFloat(component.properties["progress"])
        val normalizedProgress = progress?.let { (it / 100f).coerceIn(0f, 1f) }

        when (type.lowercase()) {
            "linear" -> {
                if (normalizedProgress != null) {
                    GlassLinearProgressIndicator(progress = { normalizedProgress })
                } else {
                    GlassLinearProgressIndicator()
                }
            }

            else -> {
                if (normalizedProgress != null) {
                    GlassCircularProgressIndicator(
                        progress = { normalizedProgress },
                        modifier = Modifier.size(36.dp),
                    )
                } else {
                    GlassCircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}
