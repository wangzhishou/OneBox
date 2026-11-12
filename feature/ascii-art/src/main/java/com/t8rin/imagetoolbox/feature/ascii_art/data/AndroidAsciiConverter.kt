/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.ascii_art.data

import android.graphics.Bitmap
import com.t8rin.ascii.ASCIIConverter
import com.t8rin.ascii.Gradient
import com.t8rin.ascii.toMapper
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.feature.ascii_art.domain.AsciiConverter
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidAsciiConverter @Inject constructor(
    dispatchersHolder: DispatchersHolder
) : AsciiConverter<Bitmap>, DispatchersHolder by dispatchersHolder {

    override suspend fun imageToAscii(
        image: Bitmap,
        fontSize: Float,
        gradient: String
    ): String = withContext(defaultDispatcher) {
        ASCIIConverter(
            fontSize = fontSize,
            mapper = Gradient(gradient).toMapper()
        ).convertToAscii(image)
    }

}