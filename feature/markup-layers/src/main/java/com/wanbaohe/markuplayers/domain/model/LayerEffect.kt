package com.wanbaohe.markuplayers.domain.model

import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter

/** An ordered, non-destructive filter. Strength interpolates premultiplied RGBA. */
data class LayerEffect(
    val filter: UiFilter<*>,
    val strength: Float = 1f,
) {
    init {
        require(strength.isFinite() && strength in 0f..1f) {
            "Effect strength must be finite and between 0 and 1"
        }
    }
}
