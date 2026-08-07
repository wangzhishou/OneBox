
package com.t8rin.imagetoolbox.core.ui.utils.provider

import android.graphics.Bitmap
import androidx.compose.runtime.staticCompositionLocalOf
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider

/**
 * CompositionLocal providing [ImageShareProvider] for sharing images as bitmaps.
 * Must be provided at the app root (e.g. in ImageToolboxCompositionLocals).
 */
val LocalImageShareProvider =
    staticCompositionLocalOf<ImageShareProvider<Bitmap>> { error("ImageShareProvider not provided") }

