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

package com.t8rin.imagetoolbox.core.resources.icons.line

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LineCatClothing: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineCatClothing",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // T 恤剪影 + 领口镂空(even-odd)
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
            moveTo(16f, 21f)
            horizontalLineTo(8f)
            curveTo(7.45f, 21f, 7f, 20.55f, 7f, 20f)
            verticalLineTo(12.07f)
            lineTo(5.7f, 13.12f)
            curveTo(5.31f, 13.5f, 4.68f, 13.5f, 4.29f, 13.12f)
            lineTo(1.82f, 10.64f)
            curveTo(1.43f, 10.25f, 1.43f, 9.62f, 1.82f, 9.23f)
            lineTo(7.23f, 3.82f)
            curveTo(7.43f, 3.62f, 7.7f, 3.5f, 8f, 3.5f)
            horizontalLineTo(16f)
            curveTo(16.3f, 3.5f, 16.57f, 3.62f, 16.77f, 3.82f)
            lineTo(22.18f, 9.23f)
            curveTo(22.57f, 9.62f, 22.57f, 10.25f, 22.18f, 10.64f)
            lineTo(19.71f, 13.12f)
            curveTo(19.32f, 13.5f, 18.69f, 13.5f, 18.3f, 13.12f)
            lineTo(17f, 12.07f)
            verticalLineTo(20f)
            curveTo(17f, 20.55f, 16.55f, 21f, 16f, 21f)
            close()
            // 领口
            moveTo(9.5f, 3.6f)
            curveTo(9.5f, 5f, 10.6f, 6.1f, 12f, 6.1f)
            curveTo(13.4f, 6.1f, 14.5f, 5f, 14.5f, 3.6f)
            close()
        }
    }.build()
}
