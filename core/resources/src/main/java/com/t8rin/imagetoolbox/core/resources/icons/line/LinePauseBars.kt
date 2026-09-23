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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LinePauseBars: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LinePauseBars",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // 纯两条竖线的暂停键，**不带外圈**：与 LinePlay 一样，外层按钮本身就是
            // 圆形玻璃底，再套圆形图标会变成"圆套圆"。原 LinePause 是"圆圈+双竖线"
            // 且铺满 24 视口，看起来比相邻图标大一圈。
            //
            // 尺寸刻意与 LinePlay 的三角占位对齐（三角 x[6.6,18.9] y[4.9,18.64]）：
            // 图标按 ContentScale.Fit 缩放到同一 size，若暂停只占很小一坨，会被放大得
            // 比播放键粗很多。这里取 13.2 宽 × 12.5 高，与三角的视觉重量相当。
            moveTo(5.4f, 5.75f)
            lineTo(9.0f, 5.75f)
            lineTo(9.0f, 18.25f)
            lineTo(5.4f, 18.25f)
            close()
            moveTo(15.0f, 5.75f)
            lineTo(18.6f, 5.75f)
            lineTo(18.6f, 18.25f)
            lineTo(15.0f, 18.25f)
            close()
        }
    }.build()
}
