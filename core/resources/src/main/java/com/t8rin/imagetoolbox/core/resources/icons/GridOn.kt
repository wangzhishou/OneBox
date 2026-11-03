/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.t8rin.imagetoolbox.core.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.GridOn: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Outlined.GridOn",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        group(translationY = 960f) {
            addPath(
                fill = SolidColor(Color.Black),
                pathData = addPathNodes(
                    "M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h133v-133H200v133Zm213 0h134v-133H413v133Zm214 0h133v-133H627v133ZM200-413h133v-134H200v134Zm213 0h134v-134H413v134Zm214 0h133v-134H627v134ZM200-627h133v-133H200v133Zm213 0h134v-133H413v133Zm214 0h133v-133H627v133Z"
                )
            )
        }
    }.build()
}
