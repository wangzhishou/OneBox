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

package com.wanbaohe.textcard.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.shifenmiao.model.editor.LayerTransform
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.wanbaohe.core.ui.widget.editor.drawEditorShape
import com.wanbaohe.core.ui.widget.editor.renderEditorStrokesBitmap
import com.wanbaohe.core.ui.widget.modifier.PointData
import com.wanbaohe.core.ui.widget.modifier.drawMeshGradient
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.ElementTransform
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardAnnotation
import com.wanbaohe.textcard.domain.model.TextCardAnnotationType
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.ceil

class AndroidTextCardExportRenderer @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val imageGetter: ImageGetter<Bitmap>,
) : TextCardExportRenderer {

    override suspend fun render(state: TextCardRenderState): Bitmap {
        val width = state.canvas.width
        val height = state.canvas.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(CardLayout.CARD_BASE_COLOR.toInt())
        if (state.backgroundVisible) drawBackground(canvas, state, width, height)
        state.textBlocks.forEach { block -> drawTextBlock(canvas, block, width, height) }
        state.annotations.forEach { annotation -> drawAnnotation(canvas, annotation, width, height) }
        return bitmap
    }

    private fun drawBackground(
        canvas: Canvas,
        state: TextCardRenderState,
        width: Int,
        height: Int,
    ) {
        val alpha = (state.backgroundOpacity.coerceIn(0f, 1f) * 255).toInt()
        if (alpha == 0) return
        canvas.saveLayerAlpha(0f, 0f, width.toFloat(), height.toFloat(), alpha)
        when (val background = state.background) {
            BackgroundSpec.None -> Unit
            is BackgroundSpec.Paper -> PaperTexturePainter.draw(
                canvas = canvas,
                kind = background.kind,
                width = width.toFloat(),
                height = height.toFloat()
            )
            is BackgroundSpec.Gradient -> drawGradient(canvas, background, width, height)
            is BackgroundSpec.Image -> drawImageBackground(canvas, background, width, height)
        }
        canvas.restore()
    }

    private fun drawGradient(
        canvas: Canvas,
        gradient: BackgroundSpec.Gradient,
        width: Int,
        height: Int,
    ) {
        val imageBitmap = ImageBitmap(width, height)
        val composeCanvas = androidx.compose.ui.graphics.Canvas(imageBitmap)
        composeCanvas.drawMeshGradient(
            pointData = PointData(
                points = gradient.toPointPairs(),
                stepsX = MESH_RESOLUTION,
                stepsY = MESH_RESOLUTION
            ),
            size = Size(width.toFloat(), height.toFloat()),
            alpha = 1f
        )
        canvas.drawBitmap(
            imageBitmap.asAndroidBitmap(),
            0f,
            0f,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }

    private fun drawImageBackground(
        canvas: Canvas,
        image: BackgroundSpec.Image,
        width: Int,
        height: Int,
    ) {
        val source = runBlocking {
            imageGetter.getImage(data = image.uri, size = maxOf(width, height))
        } ?: return
        val scale = maxOf(
            width.toFloat() / source.width,
            height.toFloat() / source.height
        ) * CardLayout.BACKGROUND_IMAGE_OVERSCAN
        val drawWidth = source.width * scale
        val drawHeight = source.height * scale
        val shiftX = image.offsetX * width
        val shiftY = image.offsetY * height
        val dest = RectF(
            (width - drawWidth) / 2f + shiftX,
            (height - drawHeight) / 2f + shiftY,
            (width + drawWidth) / 2f + shiftX,
            (height + drawHeight) / 2f + shiftY
        )
        canvas.drawBitmap(source, null, dest, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    }

    private fun drawTextBlock(
        canvas: Canvas,
        block: TextBlock,
        width: Int,
        height: Int,
    ) {
        if (block.content.isBlank()) return
        val padding = width * CardLayout.CONTENT_PADDING_RATIO
        val maxContentWidth = (width * block.widthRatio).toInt().coerceAtLeast(1)
        val left = padding + block.offsetX * width
        val top = width * block.baseTopRatio + block.offsetY * height
        val paint = block.createTextPaint(width)
        var layoutWidthUsed = maxContentWidth
        var layout = block.createTextLayout(width, layoutWidthUsed, paint)
        val contentWidth = if (block.widthManuallySet) {
            maxContentWidth
        } else {
            val maxLineWidth = (0 until layout.lineCount).maxOf { layout.getLineWidth(it) }
            val wrappedWidth = ceil(maxLineWidth).toInt().coerceAtLeast(1)
            if (wrappedWidth < maxContentWidth) {
                layoutWidthUsed = wrappedWidth
                layout = block.createTextLayout(width, layoutWidthUsed, paint)
            }
            wrappedWidth
        }
        val boxHeight = maxOf(layout.height.toFloat(), block.heightRatio * height)
        canvas.save()
        canvas.translate(left, top)
        canvas.applyElementTransform(block, contentWidth.toFloat(), boxHeight)
        if (block.backgroundColor != 0L) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = block.backgroundColor.toInt()
            }
            for (line in 0 until layout.lineCount) {
                canvas.drawRect(
                    layout.getLineLeft(line),
                    layout.getLineTop(line).toFloat(),
                    layout.getLineRight(line),
                    layout.getLineBottom(line).toFloat(),
                    bgPaint
                )
            }
        }
        if (block.outlineWidthRatio > 0f) {
            val strokePaint = TextPaint(paint).apply {
                color = block.outlineColor.toInt()
                style = Paint.Style.STROKE
                strokeWidth = block.outlineWidthRatio * width
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            block.createTextLayout(width, layoutWidthUsed, strokePaint).draw(canvas)
        }
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawAnnotation(
        canvas: Canvas,
        annotation: TextCardAnnotation,
        width: Int,
        height: Int,
    ) {
        if (!annotation.transform.visible) return
        val checkpoint = canvas.saveLayerAlpha(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            (annotation.transform.alpha.coerceIn(0f, 1f) * 255).toInt()
        )
        canvas.applyAnnotationTransform(annotation.transform, width, height)
        when (val type = annotation.type) {
            is TextCardAnnotationType.Text -> {
                val block = type.block
                val contentWidth = width * block.widthRatio
                val layoutWidth = contentWidth.toInt().coerceAtLeast(1)
                val layout = block.createTextLayout(width, layoutWidth)
                canvas.translate(-contentWidth / 2f, -layout.height / 2f)
                if (block.backgroundColor != 0L) {
                    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = block.backgroundColor.toInt()
                    }
                    for (line in 0 until layout.lineCount) {
                        canvas.drawRect(
                            layout.getLineLeft(line),
                            layout.getLineTop(line).toFloat(),
                            layout.getLineRight(line),
                            layout.getLineBottom(line).toFloat(),
                            backgroundPaint,
                        )
                    }
                }
                if (block.outlineWidthRatio > 0f) {
                    val outlinePaint = TextPaint(layout.paint).apply {
                        color = block.outlineColor.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = block.outlineWidthRatio * width
                        strokeJoin = Paint.Join.ROUND
                        strokeCap = Paint.Cap.ROUND
                    }
                    block.createTextLayout(width, layoutWidth, outlinePaint).draw(canvas)
                }
                layout.draw(canvas)
            }
            is TextCardAnnotationType.Draw -> {
                val layerBitmap = renderEditorStrokesBitmap(type.strokes, width, height)
                canvas.drawBitmap(
                    layerBitmap,
                    null,
                    RectF(-width / 2f, -height / 2f, width / 2f, height / 2f),
                    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                )
            }
            is TextCardAnnotationType.Shape -> {
                drawEditorShape(canvas, type.spec, width.toFloat(), height.toFloat())
            }
        }
        canvas.restoreToCount(checkpoint)
    }

    private fun Canvas.applyAnnotationTransform(
        transform: LayerTransform,
        width: Int,
        height: Int,
    ) {
        translate(transform.centerX * width, transform.centerY * height)
        if (transform.rotation != 0f) rotate(transform.rotation)
        if (transform.scale != 1f) scale(transform.scale, transform.scale)
        if (transform.flipHorizontal) scale(-1f, 1f)
        if (transform.flipVertical) scale(1f, -1f)
    }

    private fun Canvas.applyElementTransform(
        transform: ElementTransform,
        contentWidth: Float,
        contentHeight: Float,
    ) {
        if (transform.rotation == 0f && transform.scale == 1f) return
        val centerX = contentWidth / 2f
        val centerY = contentHeight / 2f
        translate(centerX, centerY)
        if (transform.rotation != 0f) rotate(transform.rotation)
        if (transform.scale != 1f) scale(transform.scale, transform.scale)
        translate(-centerX, -centerY)
    }
}
