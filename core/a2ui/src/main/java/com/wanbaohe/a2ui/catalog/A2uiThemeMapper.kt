package com.wanbaohe.a2ui.catalog

import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A2uiThemeMapper @Inject constructor() {

    @Composable
    fun mapGlassStyle(style: String?): GlassStyle = when (style?.lowercase()?.trim()) {
        "transparent" -> GlassStyle.Transparent
        "thin" -> GlassStyle.Thin
        "regular" -> GlassStyle.Regular
        "medium" -> GlassStyle.Medium
        "thick" -> GlassStyle.Thick
        "dense" -> GlassStyle.Dense
        "none" -> GlassStyle.None
        null, "" -> GlassStyle.Regular
        else -> GlassStyle.Regular
    }
}
