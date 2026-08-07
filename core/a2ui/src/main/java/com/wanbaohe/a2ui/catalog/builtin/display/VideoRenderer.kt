package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class VideoRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Video"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val src = context.resolveString(component.properties["src"])
        val height = context.resolveInt(component.properties["height"])?.dp ?: 200.dp

        GlassCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = if (src.isNullOrEmpty()) "[Video]" else "Video: $src")
            }
        }
    }
}
