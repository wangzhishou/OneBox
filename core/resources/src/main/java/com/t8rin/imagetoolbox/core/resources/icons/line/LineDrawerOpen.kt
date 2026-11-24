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
 * 「打开抽屉」图标（来源：用户提供的设计稿，按 24dp 视口重建）。
 *
 * 圆角外框 + 靠左的竖向分隔线 + 右侧左指箭头（`<`）：抽屉从右侧滑出，
 * 箭头朝左 = 把面板拉进来，所以关闭态顶栏用它。
 *
 * 描边 1.1（设计稿更细，24dp 下会糊成灰线），stroke 渲染保证任何尺寸都干净。
 */

val Icons.Outlined.LineDrawerOpen: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineDrawerOpen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(
            "M 4.4 1.8 L 19.6 1.8 C 21.04 1.8 22.2 2.96 22.2 4.4 L 22.2 19.6 C 22.2 21.04 21.04 22.2 19.6 22.2 L 4.4 22.2 C 2.96 22.2 1.8 21.04 1.8 19.6 L 1.8 4.4 C 1.8 2.96 2.96 1.8 4.4 1.8 Z M 7.3 2.9 L 7.3 21.2 M 16.4 8.4 L 12.6 12 L 16.4 15.6"
        ),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.1f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
}
