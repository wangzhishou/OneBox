/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.core.data.coil

import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Size
import coil3.size.pxOrElse
import coil3.svg.SvgDecoder
import coil3.svg.isSvg

internal class UpscaleSvgDecoder(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult = SvgDecoder(
        source = source,
        options = options.copy(
            size = options.size.coerceAtLeast(2048)
        )
    ).decode()

    private fun Size.coerceAtLeast(size: Int): Size = Size(
        width = width.pxOrElse { 0 }.coerceAtLeast(size),
        height = height.pxOrElse { 0 }.coerceAtLeast(size)
    )


    class Factory : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            if (!isApplicable(result)) return null
            return UpscaleSvgDecoder(
                source = result.source,
                options = options
            )
        }

        private fun isApplicable(result: SourceFetchResult): Boolean {
            return result.mimeType == "image/svg+xml" || DecodeUtils.isSvg(result.source.source())
        }
    }

}