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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

/**
 * 「分享」图标（来源：用户提供的图标集，24dp 视口按几何重建）。
 *
 * 三个空心圆由两条连线串起来（share-2 形状）；描边 1.2，与全站线框图标同一档。
 * 全项目的分享入口（分享按钮、图片查看器、导航图标、卡片动作）都走这里。
 */

val Icons.Outlined.LineShare: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineShare",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(
            "M 18 2 C 19.66 2 21 3.34 21 5 C 21 6.66 19.66 8 18 8 C 16.34 8 15 6.66 15 5 C 15 3.34 16.34 2 18 2 Z M 6 9 C 7.66 9 9 10.34 9 12 C 9 13.66 7.66 15 6 15 C 4.34 15 3 13.66 3 12 C 3 10.34 4.34 9 6 9 Z M 18 16 C 19.66 16 21 17.34 21 19 C 21 20.66 19.66 22 18 22 C 16.34 22 15 20.66 15 19 C 15 17.34 16.34 16 18 16 Z M 8.59 13.51 L 15.42 17.49 M 15.41 6.51 L 8.59 10.49"
        ),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
}
