/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
