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

val Icons.Outlined.LineGomoku: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineGomoku",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // 棋盘网格
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(3.75f, 3.75f)
            lineTo(20.25f, 3.75f)
            lineTo(20.25f, 20.25f)
            lineTo(3.75f, 20.25f)
            close()
            moveTo(9.25f, 3.75f)
            lineTo(9.25f, 20.25f)
            moveTo(14.75f, 3.75f)
            lineTo(14.75f, 20.25f)
            moveTo(3.75f, 9.25f)
            lineTo(20.25f, 9.25f)
            moveTo(3.75f, 14.75f)
            lineTo(20.25f, 14.75f)
        }
        // 黑子(实心)
        path(fill = SolidColor(Color.Black)) {
            moveTo(9.25f, 6.95f)
            curveTo(10.52f, 6.95f, 11.55f, 7.98f, 11.55f, 9.25f)
            curveTo(11.55f, 10.52f, 10.52f, 11.55f, 9.25f, 11.55f)
            curveTo(7.98f, 11.55f, 6.95f, 10.52f, 6.95f, 9.25f)
            curveTo(6.95f, 7.98f, 7.98f, 6.95f, 9.25f, 6.95f)
            close()
        }
        // 白子(空心)
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
        ) {
            moveTo(14.75f, 12.45f)
            curveTo(16.02f, 12.45f, 17.05f, 13.48f, 17.05f, 14.75f)
            curveTo(17.05f, 16.02f, 16.02f, 17.05f, 14.75f, 17.05f)
            curveTo(13.48f, 17.05f, 12.45f, 16.02f, 12.45f, 14.75f)
            curveTo(12.45f, 13.48f, 13.48f, 12.45f, 14.75f, 12.45f)
            close()
        }
    }.build()
}
