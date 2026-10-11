package com.wanbaohe.markuplayers.domain.model

/**
 * Alpha selection in normalized, untransformed bitmap-content coordinates.
 *
 * A rect seeds the selection. Without a rect, additive strokes start on an empty
 * selection; an empty or erase-only stroke list starts on the full selection.
 * Strokes are applied in order, then inversion, then feathering. Pixels outside
 * the selection become transparent, including when this mask is used on a background.
 * Coordinates outside the content are clipped, not moved onto its edges.
 *
 * [feather] is a ratio of the shorter content dimension, bounded to
 * [MaxFeather] by the processor. It is a linear alpha blur, not a color effect.
 */
data class LayerMask(
    val rect: NormalizedRect? = null,
    val strokes: List<MaskStroke> = emptyList(),
    val inverted: Boolean = false,
    val feather: Float = 0f,
) {
    init {
        require(feather.isFinite() && feather >= 0f) { "Mask feather must be finite and nonnegative" }
        rect?.let {
            require(
                it.left.isFinite() && it.top.isFinite() &&
                    it.right.isFinite() && it.bottom.isFinite() &&
                    it.width.isFinite() && it.height.isFinite() &&
                    it.width > 0f && it.height > 0f
            ) { "Mask rect must have finite coordinates and positive dimensions" }
        }
    }

    companion object {
        const val MaxFeather: Float = 0.25f
    }
}

/**
 * Round brush geometry; width is relative to the untransformed content width.
 * A zero width with at least three points denotes a closed filled polygon,
 * used to preserve rectangular selections through arbitrary affine remapping.
 */
data class MaskStroke(
    val points: List<StrokePoint>,
    val widthRatio: Float,
    val erase: Boolean = false,
) {
    init {
        require(widthRatio.isFinite() && widthRatio >= 0f) {
            "Mask stroke width must be finite and nonnegative"
        }
        require(widthRatio != 0f || points.size >= 3) {
            "A filled mask polygon requires at least three points"
        }
        require(points.all { it.x.isFinite() && it.y.isFinite() }) {
            "Mask stroke points must be finite"
        }
    }
}
