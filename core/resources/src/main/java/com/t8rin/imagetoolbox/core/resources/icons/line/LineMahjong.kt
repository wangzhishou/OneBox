/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LineMahjong: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineMahjong",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(7f, 2.8f)
            lineTo(17f, 2.8f)
            curveTo(18.2f, 2.8f, 19f, 3.8f, 19f, 5f)
            lineTo(19f, 19f)
            curveTo(19f, 20.2f, 18.2f, 21.2f, 17f, 21.2f)
            lineTo(7f, 21.2f)
            curveTo(5.8f, 21.2f, 5f, 20.2f, 5f, 19f)
            lineTo(5f, 5f)
            curveTo(5f, 3.8f, 5.8f, 2.8f, 7f, 2.8f)
            close()
            moveTo(9f, 8f)
            lineTo(15f, 8f)
            lineTo(15f, 15.5f)
            lineTo(9f, 15.5f)
            close()
            moveTo(12f, 5.2f)
            lineTo(12f, 18.8f)
            moveTo(9f, 11.8f)
            lineTo(15f, 11.8f)
        }
    }.build()
}
