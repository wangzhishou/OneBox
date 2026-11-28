/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.shifenmiao.common.ui.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 模型选择器专用图标:列表、网格,以及选中角标上的对勾。
 * 线宽按 24 视口约 1.8,圆角描边,和精选卡那套线图标同一视觉重量。
 */
internal object ModelLayoutIcons {
    val List: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        Builder(
            name = "ModelLayoutList",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                roundRect(3f, 3.4f, 7.6f, 8f, 1.4f)
                roundRect(9.6f, 4.2f, 21f, 7.2f, 1.5f)
                roundRect(3f, 9.7f, 7.6f, 14.3f, 1.4f)
                roundRect(9.6f, 10.5f, 21f, 13.5f, 1.5f)
                roundRect(3f, 16f, 7.6f, 20.6f, 1.4f)
                roundRect(9.6f, 16.8f, 21f, 19.8f, 1.5f)
            }
        }.build()
    }

    val Grid: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        Builder(
            name = "ModelLayoutGrid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                roundRect(3f, 3f, 10.4f, 10.4f, 2f)
                roundRect(13.6f, 3f, 21f, 10.4f, 2f)
                roundRect(3f, 13.6f, 10.4f, 21f, 2f)
                roundRect(13.6f, 13.6f, 21f, 21f, 2f)
            }
        }.build()
    }

    val Check: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
        Builder(
            name = "ModelLayoutCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5.5f, 12.5f)
                lineTo(10f, 17f)
                lineTo(18.5f, 7.5f)
            }
        }.build()
    }
}

private fun PathBuilder.roundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float,
) {
    val rad = radius.coerceAtMost(minOf(right - left, bottom - top) / 2f)
    moveTo(left + rad, top)
    lineTo(right - rad, top)
    arcTo(rad, rad, 0f, false, true, right, top + rad)
    lineTo(right, bottom - rad)
    arcTo(rad, rad, 0f, false, true, right - rad, bottom)
    lineTo(left + rad, bottom)
    arcTo(rad, rad, 0f, false, true, left, bottom - rad)
    lineTo(left, top + rad)
    arcTo(rad, rad, 0f, false, true, left + rad, top)
    close()
}
