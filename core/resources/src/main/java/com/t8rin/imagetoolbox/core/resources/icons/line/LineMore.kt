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
 * 「更多」图标：三个竖排空心圆（来源：用户提供的设计稿，按 24dp 视口手工重建）。
 *
 * 全项目取这个图标的地方（笔记/提示词卡片的右上角、浏览器底栏、WebView 顶栏、
 * 物品分类图标，以及图标注册表里 "MoreVert" 这个 key）都写的是
 * `Icons.Outlined.LineMore`，所以这里名字与签名保持不变、只换里面的矢量内容：
 *
 * - 圆心在 x=12，纵向 4.2 / 12 / 19.8，半径 2.25，描边 1.15（空心圈不是实心点）；
 * - 设计稿是较大尺寸下的 3 圆圈，这里保持同样的「圈径 : 间距 ≈ 1 : 0.7」比例，
 *   只是把笔画提到 1.15，24dp 下不会糊成三个灰点；
 * - 之前用的是 Material 的实心三点 `MoreVert`；全项目的「更多」入口（笔记/提示词
 *   卡片、发现页卡片、待办、棋盘工具条、通知、代码编辑器、云存储等）已统一改引
 *   这里，所以别再往调用点写 `MoreVert`。
 *
 * 用 stroke 而不是填充轮廓：圈是标准几何形，描边渲染在任何尺寸下都不会出现
 * 矢量化描摹（vtracer）在低分辨率源上留下的毛边。
 */
val Icons.Outlined.LineMore: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineMore",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(
            "M 12 1.95 C 13.24 1.95 14.25 2.96 14.25 4.2 C 14.25 5.44 13.24 6.45 12 6.45 " +
                "C 10.76 6.45 9.75 5.44 9.75 4.2 C 9.75 2.96 10.76 1.95 12 1.95 Z " +
                "M 12 9.75 C 13.24 9.75 14.25 10.76 14.25 12 C 14.25 13.24 13.24 14.25 12 14.25 " +
                "C 10.76 14.25 9.75 13.24 9.75 12 C 9.75 10.76 10.76 9.75 12 9.75 Z " +
                "M 12 17.55 C 13.24 17.55 14.25 18.56 14.25 19.8 C 14.25 21.04 13.24 22.05 12 22.05 " +
                "C 10.76 22.05 9.75 21.04 9.75 19.8 C 9.75 18.56 10.76 17.55 12 17.55 Z"
        ),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.15f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
}
