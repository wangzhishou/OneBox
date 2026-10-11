package com.t8rin.imagetoolbox.core.domain.image

import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.saving.io.Writeable

data class GifAnimationFrame<Image>(
    val image: Image,
    val durationMillis: Int,
)

interface GifAnimationEncoder<Image> {
    /**
     * Encodes opaque, full-size frames with an infinite loop.
     * The provider transfers ownership of each frame to the encoder; Android frames must be
     * fresh software bitmaps and are recycled after use. The caller owns and closes [output].
     */
    suspend fun encode(
        size: IntegerSize,
        frameCount: Int,
        output: Writeable,
        frameProvider: suspend (index: Int) -> GifAnimationFrame<Image>,
        onProgress: suspend (completedFrames: Int) -> Unit = {},
    )
}
