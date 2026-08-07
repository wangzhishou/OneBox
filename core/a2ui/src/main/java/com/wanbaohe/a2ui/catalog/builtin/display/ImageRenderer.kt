package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class ImageRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Image"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val src = context.resolveString(component.properties["src"])
        val height = context.resolveInt(component.properties["height"])?.dp ?: 200.dp
        val scale = context.resolveString(component.properties["scale"])

        GlassCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                contentAlignment = Alignment.Center,
            ) {
                if (src.isNullOrEmpty()) {
                    Text(text = "[Image]")
                } else {
                    AsyncImage(
                        model = src,
                        contentDescription = context.resolveString(component.properties["description"]),
                        contentScale = mapContentScale(scale),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    private fun mapContentScale(scale: String?): ContentScale = when (scale) {
        "fit" -> ContentScale.Fit
        "crop" -> ContentScale.Crop
        "fill" -> ContentScale.FillBounds
        "inside" -> ContentScale.Inside
        else -> ContentScale.Crop
    }
}
