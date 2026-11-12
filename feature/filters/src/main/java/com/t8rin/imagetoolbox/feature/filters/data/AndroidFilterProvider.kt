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

package com.t8rin.imagetoolbox.feature.filters.data

import android.graphics.Bitmap
import com.shifenmiao.common.di.OpenCVInitializer
import com.t8rin.imagetoolbox.core.domain.model.ifVisible
import com.t8rin.imagetoolbox.core.domain.transformation.EmptyTransformation
import com.t8rin.imagetoolbox.core.domain.transformation.Transformation
import com.t8rin.imagetoolbox.core.filters.domain.FilterProvider
import com.t8rin.imagetoolbox.core.filters.domain.model.Filter
import com.t8rin.imagetoolbox.generated.mapFilter
import javax.inject.Inject

internal class AndroidFilterProvider @Inject constructor(
    private val openCVInitializer: OpenCVInitializer
) : FilterProvider<Bitmap> {

    override fun filterToTransformation(
        filter: Filter<*>,
    ): Transformation<Bitmap> = filter.ifVisible(::mapFilter) ?: EmptyTransformation()

}