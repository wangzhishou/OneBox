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

package com.wanbaohe.core.ui.widget.editor

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.shifenmiao.model.editor.ShapeGeometry
import com.shifenmiao.model.editor.ShapeKind
import com.shifenmiao.model.editor.ShapeSpec
import kotlin.math.min

fun DrawScope.drawEditorShape(
    spec: ShapeSpec,
    strokeWidthPx: Float,
    cornerRadiusPx: Float,
) {
    val color = Color(spec.color)
    val stroke = Stroke(width = strokeWidthPx)
    when (spec.kind) {
        ShapeKind.Rectangle -> {
            val radius = cornerRadiusPx.coerceIn(0f, min(size.width, size.height) / 2f)
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(radius, radius),
                style = if (spec.filled) Fill else stroke
            )
        }
        ShapeKind.Circle -> {
            val diameter = min(size.width, size.height)
            drawOval(
                color = color,
                topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f),
                size = Size(diameter, diameter),
                style = if (spec.filled) Fill else stroke
            )
        }
        ShapeKind.Line -> {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )
        }
        ShapeKind.Triangle, ShapeKind.Arrow, ShapeKind.Star -> {
            val path = Path().apply {
                ShapeGeometry.polygonPoints(spec.kind, size.width, size.height)
                    .forEachIndexed { index, (x, y) ->
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                close()
            }
            drawPath(path = path, color = color, style = if (spec.filled) Fill else stroke)
        }
    }
}

fun drawEditorShape(
    canvas: android.graphics.Canvas,
    spec: ShapeSpec,
    imageWidth: Float,
    imageHeight: Float,
) {
    if (imageWidth <= 0f || imageHeight <= 0f) return
    val width = spec.widthRatio * imageWidth
    val height = spec.heightRatio * imageHeight
    if (width <= 0f || height <= 0f) return
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = spec.color
        style = if (spec.filled) Paint.Style.FILL else Paint.Style.STROKE
        strokeWidth = (spec.strokeWidthRatio * imageWidth).coerceAtLeast(1f)
    }
    when (spec.kind) {
        ShapeKind.Rectangle -> {
            val radius = (spec.cornerRadiusRatio * imageWidth).coerceIn(0f, min(width, height) / 2f)
            canvas.drawRoundRect(
                RectF(-width / 2f, -height / 2f, width / 2f, height / 2f),
                radius, radius, paint
            )
        }
        ShapeKind.Circle -> {
            val diameter = min(width, height)
            canvas.drawOval(RectF(-diameter / 2f, -diameter / 2f, diameter / 2f, diameter / 2f), paint)
        }
        ShapeKind.Line -> {
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            canvas.drawLine(-width / 2f, 0f, width / 2f, 0f, paint)
        }
        ShapeKind.Triangle, ShapeKind.Arrow, ShapeKind.Star -> {
            val path = android.graphics.Path().apply {
                ShapeGeometry.polygonPoints(spec.kind, width, height)
                    .forEachIndexed { index, (x, y) ->
                        val px = x - width / 2f
                        val py = y - height / 2f
                        if (index == 0) moveTo(px, py) else lineTo(px, py)
                    }
                close()
            }
            canvas.drawPath(path, paint)
        }
    }
}
