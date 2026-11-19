/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.editor

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.shifenmiao.model.editor.BrushType
import com.shifenmiao.model.editor.DrawStroke
import com.shifenmiao.model.editor.DrawStrokeGeometry
import com.shifenmiao.model.editor.StrokePoint
import kotlin.math.roundToInt

internal fun DrawScope.drawLegacyEditorStroke(
    stroke: DrawStroke,
    canvasWidthPx: Float = size.width,
    canvasHeightPx: Float = size.height,
) {
    val points = stroke.points
    if (points.isEmpty()) return
    val widthPx = (stroke.widthRatio * canvasWidthPx).coerceAtLeast(1f)
    val isEraser = stroke.brush == BrushType.Eraser
    val blendMode = if (isEraser) BlendMode.Clear else BlendMode.SrcOver
    val color = if (isEraser) Color.Black else Color(stroke.color).copy(alpha = stroke.alpha)

    fun StrokePoint.toOffset() = Offset(x * canvasWidthPx, y * canvasHeightPx)

    if (points.size == 1) {
        drawCircle(
            color = color,
            radius = widthPx / 2,
            center = points.first().toOffset(),
            blendMode = blendMode
        )
        return
    }
    if (stroke.brush == BrushType.Brush) {
        val smoothed = DrawStrokeGeometry.smoothed(points)
        val lastIndex = smoothed.size - 1
        for (i in 0 until lastIndex) {
            drawLine(
                color = color,
                start = smoothed[i].toOffset(),
                end = smoothed[i + 1].toOffset(),
                strokeWidth = widthPx * DrawStrokeGeometry.taperFactor(i, lastIndex),
                cap = StrokeCap.Round,
                blendMode = blendMode
            )
        }
        return
    }
    val path = Path().apply {
        moveTo(points.first().toOffset().x, points.first().toOffset().y)
        for (i in 1 until points.size - 1) {
            val control = points[i].toOffset()
            val end = Offset(
                (points[i].x + points[i + 1].x) / 2f * canvasWidthPx,
                (points[i].y + points[i + 1].y) / 2f * canvasHeightPx
            )
            quadraticTo(control.x, control.y, end.x, end.y)
        }
        lineTo(points.last().toOffset().x, points.last().toOffset().y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = widthPx,
            cap = if (stroke.brush == BrushType.Marker) StrokeCap.Square else StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        blendMode = blendMode
    )
}

fun renderEditorStrokesBitmap(
    strokes: List<DrawStroke>,
    imageWidth: Int,
    imageHeight: Int,
): Bitmap {
    require(imageWidth > 0 && imageHeight > 0) { "Draw content dimensions must be positive" }
    val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val renderer = EditorInkStrokeRenderer()
    val transform = Matrix()
    strokes.forEach { stroke ->
        val inkStroke = stroke.toInkStrokeOrNull()
        val snapshot = stroke.ink
        if (inkStroke != null && snapshot != null) {
            transform.setScale(
                imageWidth / snapshot.canvasWidth,
                imageHeight / snapshot.canvasHeight,
            )
            renderer.draw(
                canvas = canvas,
                stroke = inkStroke,
                transform = transform,
                width = imageWidth.toFloat(),
                height = imageHeight.toFloat(),
            )
        } else {
            drawLegacyEditorStroke(canvas, stroke, imageWidth.toFloat(), imageHeight.toFloat())
        }
    }
    return bitmap
}

private fun drawLegacyEditorStroke(
    canvas: android.graphics.Canvas,
    stroke: DrawStroke,
    imageWidth: Float,
    imageHeight: Float,
) {
    val points = stroke.points
    if (points.isEmpty()) return
    val widthPx = (stroke.widthRatio * imageWidth).coerceAtLeast(1f)

    fun StrokePoint.toX() = x * imageWidth
    fun StrokePoint.toY() = y * imageHeight

    if (points.size == 1) {
        canvas.drawCircle(
            points.first().toX(), points.first().toY(), widthPx / 2,
            editorStrokePaint(stroke, widthPx).apply { style = Paint.Style.FILL }
        )
        return
    }
    val paint = editorStrokePaint(stroke, widthPx)
    if (stroke.brush == BrushType.Brush) {
        val smoothed = DrawStrokeGeometry.smoothed(points)
        val lastIndex = smoothed.size - 1
        for (i in 0 until lastIndex) {
            paint.strokeWidth = widthPx * DrawStrokeGeometry.taperFactor(i, lastIndex)
            canvas.drawLine(
                smoothed[i].toX(), smoothed[i].toY(),
                smoothed[i + 1].toX(), smoothed[i + 1].toY(), paint
            )
        }
        return
    }
    val path = android.graphics.Path().apply {
        moveTo(points.first().toX(), points.first().toY())
        for (i in 1 until points.size - 1) {
            val midX = (points[i].x + points[i + 1].x) / 2f * imageWidth
            val midY = (points[i].y + points[i + 1].y) / 2f * imageHeight
            quadTo(points[i].toX(), points[i].toY(), midX, midY)
        }
        lineTo(points.last().toX(), points.last().toY())
    }
    canvas.drawPath(path, paint)
}

private fun editorStrokePaint(stroke: DrawStroke, widthPx: Float) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = widthPx
        strokeJoin = Paint.Join.ROUND
        strokeCap = if (stroke.brush == BrushType.Marker) Paint.Cap.SQUARE else Paint.Cap.ROUND
        if (stroke.brush == BrushType.Eraser) {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            color = stroke.color
            alpha = (stroke.alpha.coerceIn(0f, 1f) * 255).roundToInt()
        }
    }
