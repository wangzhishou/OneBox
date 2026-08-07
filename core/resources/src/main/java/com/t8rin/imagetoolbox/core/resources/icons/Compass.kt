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

package com.t8rin.imagetoolbox.core.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.Compass: ImageVector by lazy {
    ImageVector.Builder(
        name = "Compass",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(14.19f, 14.19f)
            lineTo(6f, 18f)
            lineTo(9.81f, 9.81f)
            lineTo(18f, 6f)
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, false, false, 2f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 22f)
            arcTo(10f, 10f, 0f, false, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            moveTo(12f, 10.9f)
            arcTo(1.1f, 1.1f, 0f, false, false, 10.9f, 12f)
            arcTo(1.1f, 1.1f, 0f, false, false, 12f, 13.1f)
            arcTo(1.1f, 1.1f, 0f, false, false, 13.1f, 12f)
            arcTo(1.1f, 1.1f, 0f, false, false, 12f, 10.9f)
            close()
        }
    }.build()
}

val Icons.TwoTone.Compass: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "TwoTone.Compass",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 0.3f,
            strokeAlpha = 0.3f
        ) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, false, false, 2f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 22f)
            arcTo(10f, 10f, 0f, false, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            close()
        }
        path(fill = SolidColor(Color.Black)) {
            moveTo(14.19f, 14.19f)
            lineTo(6f, 18f)
            lineTo(9.81f, 9.81f)
            lineTo(18f, 6f)
            moveTo(12f, 10.9f)
            arcTo(1.1f, 1.1f, 0f, false, false, 10.9f, 12f)
            arcTo(1.1f, 1.1f, 0f, false, false, 12f, 13.1f)
            arcTo(1.1f, 1.1f, 0f, false, false, 13.1f, 12f)
            arcTo(1.1f, 1.1f, 0f, false, false, 12f, 10.9f)
            close()
        }
    }.build()
}
