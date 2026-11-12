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

package com.t8rin.imagetoolbox.feature.filters.data.model

import android.graphics.Bitmap
import com.t8rin.imagetoolbox.core.domain.model.ImageModel
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.transformation.Transformation
import com.t8rin.imagetoolbox.core.filters.domain.model.Filter
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.SpotHealMode
import com.t8rin.imagetoolbox.core.ksp.annotations.FilterInject
import com.t8rin.imagetoolbox.feature.filters.data.utils.image.loadBitmap
import com.t8rin.opencv_tools.spot_heal.SpotHealer
import com.t8rin.opencv_tools.spot_heal.model.HealType

@FilterInject
internal class SpotHealFilter(
    override val value: Pair<ImageModel, SpotHealMode>,
) : Transformation<Bitmap>, Filter.SpotHeal {

    override val cacheKey: String
        get() = value.hashCode().toString()

    override suspend fun transform(
        input: Bitmap,
        size: IntegerSize
    ): Bitmap {
        val mask = value.first.data.loadBitmap() ?: return input

        return openCV(
            input = input,
            mask = mask
        )
    }

    private fun openCV(
        input: Bitmap,
        mask: Bitmap
    ) = SpotHealer.heal(
        image = input,
        mask = mask,
        radius = 3f,
        type = HealType.TELEA
    )

}