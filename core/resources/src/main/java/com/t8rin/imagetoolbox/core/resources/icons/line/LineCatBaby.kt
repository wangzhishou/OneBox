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

val Icons.Outlined.LineCatBaby: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineCatBaby",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // 奶瓶:奶嘴 + 瓶盖 + 瓶身,刻度线镂空(even-odd)
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
            // 奶嘴
            moveTo(12f, 1.5f)
            curveTo(12.72f, 1.5f, 13.3f, 2.08f, 13.3f, 2.8f)
            curveTo(13.3f, 3.52f, 12.72f, 4.1f, 12f, 4.1f)
            curveTo(11.28f, 4.1f, 10.7f, 3.52f, 10.7f, 2.8f)
            curveTo(10.7f, 2.08f, 11.28f, 1.5f, 12f, 1.5f)
            close()
            // 瓶盖
            moveTo(9.05f, 4.7f)
            horizontalLineTo(14.95f)
            curveTo(15.42f, 4.7f, 15.8f, 5.08f, 15.8f, 5.55f)
            curveTo(15.8f, 6.02f, 15.42f, 6.4f, 14.95f, 6.4f)
            horizontalLineTo(9.05f)
            curveTo(8.58f, 6.4f, 8.2f, 6.02f, 8.2f, 5.55f)
            curveTo(8.2f, 5.08f, 8.58f, 4.7f, 9.05f, 4.7f)
            close()
            // 瓶身
            moveTo(9f, 6.4f)
            horizontalLineTo(15f)
            curveTo(16.38f, 6.4f, 17.5f, 7.52f, 17.5f, 8.9f)
            verticalLineTo(19f)
            curveTo(17.5f, 20.38f, 16.38f, 21.5f, 15f, 21.5f)
            horizontalLineTo(9f)
            curveTo(7.62f, 21.5f, 6.5f, 20.38f, 6.5f, 19f)
            verticalLineTo(8.9f)
            curveTo(6.5f, 7.52f, 7.62f, 6.4f, 9f, 6.4f)
            close()
            // 刻度线
            moveTo(8.1f, 10f)
            horizontalLineTo(9.9f)
            verticalLineTo(11.1f)
            horizontalLineTo(8.1f)
            close()
            moveTo(8.1f, 13f)
            horizontalLineTo(9.9f)
            verticalLineTo(14.1f)
            horizontalLineTo(8.1f)
            close()
            moveTo(8.1f, 16f)
            horizontalLineTo(9.9f)
            verticalLineTo(17.1f)
            horizontalLineTo(8.1f)
            close()
        }
    }.build()
}
