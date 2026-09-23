/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.resources.icons.line

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LineChess: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineChess",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // 王冠轮廓
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(4.5f, 8.5f)
            lineTo(8.3f, 13.2f)
            lineTo(12f, 5.8f)
            lineTo(15.7f, 13.2f)
            lineTo(19.5f, 8.5f)
            lineTo(18.4f, 18.2f)
            lineTo(5.6f, 18.2f)
            close()
        }
        // 冠带装饰线
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(6.1f, 15.6f)
            lineTo(17.9f, 15.6f)
        }
        // 冠顶尖珠(实心)
        path(fill = SolidColor(Color.Black)) {
            moveTo(4.5f, 7.35f)
            curveTo(5.14f, 7.35f, 5.65f, 7.86f, 5.65f, 8.5f)
            curveTo(5.65f, 9.14f, 5.14f, 9.65f, 4.5f, 9.65f)
            curveTo(3.86f, 9.65f, 3.35f, 9.14f, 3.35f, 8.5f)
            curveTo(3.35f, 7.86f, 3.86f, 7.35f, 4.5f, 7.35f)
            close()
        }
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 4.65f)
            curveTo(12.64f, 4.65f, 13.15f, 5.16f, 13.15f, 5.8f)
            curveTo(13.15f, 6.44f, 12.64f, 6.95f, 12f, 6.95f)
            curveTo(11.36f, 6.95f, 10.85f, 6.44f, 10.85f, 5.8f)
            curveTo(10.85f, 5.16f, 11.36f, 4.65f, 12f, 4.65f)
            close()
        }
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.5f, 7.35f)
            curveTo(20.14f, 7.35f, 20.65f, 7.86f, 20.65f, 8.5f)
            curveTo(20.65f, 9.14f, 20.14f, 9.65f, 19.5f, 9.65f)
            curveTo(18.86f, 9.65f, 18.35f, 9.14f, 18.35f, 8.5f)
            curveTo(18.35f, 7.86f, 18.86f, 7.35f, 19.5f, 7.35f)
            close()
        }
    }.build()
}
