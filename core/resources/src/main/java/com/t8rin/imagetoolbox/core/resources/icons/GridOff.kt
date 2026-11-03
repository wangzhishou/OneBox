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

val Icons.Outlined.GridOff: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Outlined.GridOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        group(translationY = 960f) {
            addPath(
                fill = SolidColor(Color.Black),
                pathData = addPathNodes(
                    "M333-200v-133H200v133h133Zm214 0v-100l-33-33H413v133h134Zm80 0Zm116-133Zm-410-80v-101l-33-33H200v134h133Zm80 0Zm347 0v-134H627v99l35 35h98ZM529-547Zm-329-80Zm347 0v-133H413v98l35 35h99Zm213 0v-133H627v133h133ZM316-760Zm524 525L235-840h525q33 0 56.5 23.5T840-760v525ZM200-120q-33 0-56.5-23.5T120-200v-640l720 720H200Zm619 92L28-820l56-56L876-84l-57 56Z"
                )
            )
        }
    }.build()
}
