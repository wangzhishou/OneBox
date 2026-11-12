/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.settings.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ShapeType {
    val ordinal: Int get() = entries.indexOf(this)

    val strength: Float

    fun copy(strength: Float): ShapeType = when (this) {
        is Cut -> Cut(strength = strength)
        is Rounded -> Rounded(strength = strength)
        is Squircle -> Squircle(strength = strength)
        is Smooth -> Smooth(strength = strength)
    }

    @SerialName("Rounded")
    @Serializable
    class Rounded(
        override val strength: Float = 1f
    ) : ShapeType

    @SerialName("Cut")
    @Serializable
    class Cut(
        override val strength: Float = 1f
    ) : ShapeType

    @SerialName("Squircle")
    @Serializable
    class Squircle(
        override val strength: Float = 1f
    ) : ShapeType

    @SerialName("Smooth")
    @Serializable
    class Smooth(
        override val strength: Float = 1f
    ) : ShapeType

    companion object {
        val entries by lazy {
            listOf(
                Rounded(), Cut(), Squircle(), Smooth()
            )
        }
    }
}