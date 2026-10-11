package com.wanbaohe.markuplayers.presentation.render

import android.os.Build
import androidx.compose.ui.graphics.BlendMode
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode

/**
 * Compose has no source-over Multiply on API 24..28 (Modulate is not equivalent).
 * Such previews must use a bounded bitmap composition from MarkupLayersApplier,
 * with the same processed-content map, rather than drawing this mode as SrcOver.
 */
val LayerBlendMode.requiresSoftwarePreview: Boolean
    get() = this == LayerBlendMode.Multiply && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

fun LayerBlendMode.toComposeBlendMode(): BlendMode = when (this) {
    LayerBlendMode.Normal -> BlendMode.SrcOver
    LayerBlendMode.Multiply -> BlendMode.Multiply
    LayerBlendMode.Screen -> BlendMode.Screen
    LayerBlendMode.Overlay -> BlendMode.Overlay
    LayerBlendMode.Darken -> BlendMode.Darken
    LayerBlendMode.Lighten -> BlendMode.Lighten
}
