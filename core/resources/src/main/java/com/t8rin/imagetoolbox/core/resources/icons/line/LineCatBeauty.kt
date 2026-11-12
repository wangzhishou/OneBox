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

val Icons.Outlined.LineCatBeauty: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineCatBeauty",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // 护肤品软管:瓶盖 + 管身,标签条镂空(even-odd)
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
            // 瓶盖
            moveTo(11f, 1.6f)
            horizontalLineTo(13f)
            curveTo(13.44f, 1.6f, 13.8f, 1.96f, 13.8f, 2.4f)
            verticalLineTo(4.2f)
            horizontalLineTo(10.2f)
            verticalLineTo(2.4f)
            curveTo(10.2f, 1.96f, 10.56f, 1.6f, 11f, 1.6f)
            close()
            // 管身
            moveTo(11f, 4.2f)
            horizontalLineTo(13f)
            curveTo(14.66f, 4.2f, 16f, 5.54f, 16f, 7.2f)
            verticalLineTo(19f)
            curveTo(16f, 20.66f, 14.66f, 22f, 13f, 22f)
            horizontalLineTo(11f)
            curveTo(9.34f, 22f, 8f, 20.66f, 8f, 19f)
            verticalLineTo(7.2f)
            curveTo(8f, 5.54f, 9.34f, 4.2f, 11f, 4.2f)
            close()
            // 标签条
            moveTo(11.2f, 8f)
            horizontalLineTo(12.8f)
            curveTo(12.8f, 8f, 12.8f, 8f, 12.8f, 8.8f)
            verticalLineTo(16.2f)
            curveTo(12.8f, 17f, 12.8f, 17f, 12.8f, 17f)
            horizontalLineTo(11.2f)
            curveTo(11.2f, 17f, 11.2f, 17f, 11.2f, 16.2f)
            verticalLineTo(8.8f)
            curveTo(11.2f, 8f, 11.2f, 8f, 11.2f, 8f)
            close()
        }
    }.build()
}
