/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.domain.image.model

import androidx.annotation.IntRange

sealed interface Quality {
    val qualityValue: Int

    fun coerceIn(
        imageFormat: ImageFormat
    ): Quality {
        return when (imageFormat) {
            is ImageFormat.Png.Lossy -> {
                val value = this as? PngLossy
                    ?: return PngLossy()
                value.copy(
                    maxColors = value.maxColors.coerceIn(2..1024),
                    compressionLevel = compressionLevel.coerceIn(0..9)
                )
            }

            is ImageFormat.Tif,
            is ImageFormat.Tiff -> {
                val value = this as? Tiff
                    ?: return Tiff()
                value.copy(
                    compressionScheme = value.compressionScheme.coerceIn(0..9)
                )
            }

            is ImageFormat.Jpeg2000 -> Base(qualityValue.coerceIn(20..100))

            else -> {
                Base(qualityValue.coerceIn(0..100))
            }
        }
    }

    fun isNonAlpha(): Boolean = false

    fun isDefault(): Boolean = when (this) {
        is Base -> this == Base()
        is PngLossy -> this == PngLossy()
        is Tiff -> this == Tiff()
    }

    data class PngLossy(
        @IntRange(from = 2, to = 1024)
        val maxColors: Int = 512,
        @IntRange(from = 0, to = 9)
        val compressionLevel: Int = 7,
    ) : Quality {
        override val qualityValue: Int = compressionLevel
    }

    data class Tiff(
        val compressionScheme: Int = 5
    ) : Quality {
        override val qualityValue: Int = compressionScheme
    }

    data class Base(
        override val qualityValue: Int = 100
    ) : Quality
}
