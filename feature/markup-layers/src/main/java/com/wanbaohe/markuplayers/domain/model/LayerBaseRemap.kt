package com.wanbaohe.markuplayers.domain.model

import kotlin.math.cos
import kotlin.math.sin

/**
 * 底图基础变换(90° 步进旋转 / 翻转 / 裁剪)后的图层重映射。
 *
 * 图层参数分两类,按同一仿射换算到新底图坐标系:
 * - 归一化坐标(变换中心点 / 笔画采样点):先翻转、再逐步 90° 旋转、
 *   最后做裁剪的平移缩放。归一化空间内旋转公式与像素空间同构(宽高比在
 *   比例参数的补偿里恢复)。
 * - 相对底图宽/高的比例(scale 基数、字号、形状宽高等):乘以新旧底图
 *   宽/高之比,保证图层的绝对像素尺寸在变换前后一致。
 *
 * 纯函数,不处理任意角度旋转(自由旋转由调用方先烘焙图层再清空)。
 */
object LayerBaseRemap {

    fun remap(
        layers: List<MarkupLayer>,
        oldWidth: Int,
        oldHeight: Int,
        rotationSteps: Int,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRect: NormalizedRect,
    ): List<MarkupLayer> {
        val steps = ((rotationSteps % 4) + 4) % 4
        val rotatedWidth = if (steps % 2 == 1) oldHeight else oldWidth
        val rotatedHeight = if (steps % 2 == 1) oldWidth else oldHeight
        val newWidth = rotatedWidth * cropRect.width
        val newHeight = rotatedHeight * cropRect.height
        if (oldWidth <= 0 || oldHeight <= 0 || newWidth <= 0f || newHeight <= 0f) return layers

        val context = RemapContext(
            steps = steps,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical,
            cropRect = cropRect,
            oldWidth = oldWidth,
            oldHeight = oldHeight,
            widthCompensation = oldWidth / newWidth,
            heightCompensation = oldHeight / newHeight
        )
        return layers.map { it.remap(context) }
    }

    private class RemapContext(
        val steps: Int,
        val flipHorizontal: Boolean,
        val flipVertical: Boolean,
        val cropRect: NormalizedRect,
        val oldWidth: Int,
        val oldHeight: Int,
        /** 相对底图宽的比例参数补偿系数 = 旧宽 / 新宽 */
        val widthCompensation: Float,
        /** 相对底图高的比例参数补偿系数 = 旧高 / 新高 */
        val heightCompensation: Float,
    )

    private fun MarkupLayer.remap(context: RemapContext): MarkupLayer {
        val type = type
        if (type is LayerType.Draw) return remapDrawLayer(type, context)
        val newType = when (type) {
            is LayerType.Text -> type.copy(
                fontSizeRatio = type.fontSizeRatio * context.widthCompensation
            )

            is LayerType.Shape -> type.copy(
                spec = type.spec.run {
                    copy(
                        widthRatio = widthRatio * context.widthCompensation,
                        heightRatio = heightRatio * context.heightCompensation,
                        cornerRadiusRatio = cornerRadiusRatio * context.widthCompensation,
                        strokeWidthRatio = strokeWidthRatio * context.widthCompensation
                    )
                }
            )

            // 贴纸/图片的基础尺寸 = 比例 × 底图宽,经 transform.scale 补偿即可
            else -> type
        }
        return copy(type = newType, transform = transform.remap(context))
    }

    /**
     * 画笔图层特殊处理:笔画采样点在画布坐标系(非图层局部坐标),
     * 若只重映射 transform 会造成双重变换。这里把图层自身 transform
     * 与底图变换一并烘焙进采样点,transform 几何量归零(保留 alpha 等标志)。
     */
    private fun MarkupLayer.remapDrawLayer(
        type: LayerType.Draw,
        context: RemapContext,
    ): MarkupLayer {
        val t = transform
        val hasGeoTransform = t.centerX != 0.5f || t.centerY != 0.5f ||
            t.scale != 1f || t.rotation != 0f
        val widthScale = context.widthCompensation * if (hasGeoTransform) t.scale else 1f
        val strokes = type.strokes.map { stroke ->
            stroke.copy(
                points = stroke.points.map { point ->
                    val canvasPoint = if (hasGeoTransform) {
                        point.applyLayerTransform(t, context.oldWidth, context.oldHeight)
                    } else point
                    canvasPoint.remapPoint(context)
                },
                widthRatio = stroke.widthRatio * widthScale
            )
        }
        return copy(
            type = LayerType.Draw(strokes),
            transform = t.copy(centerX = 0.5f, centerY = 0.5f, scale = 1f, rotation = 0f)
        )
    }

    /** 把图层自身 transform 换算进采样点(像素空间:缩→旋→平移到中心) */
    private fun StrokePoint.applyLayerTransform(
        transform: LayerTransform,
        width: Int,
        height: Int,
    ): StrokePoint {
        val centerX = transform.centerX * width
        val centerY = transform.centerY * height
        val dx = x * width - centerX
        val dy = y * height - centerY
        val radians = Math.toRadians(transform.rotation.toDouble())
        val cos = cos(radians).toFloat() * transform.scale
        val sin = sin(radians).toFloat() * transform.scale
        return StrokePoint(
            x = (centerX + dx * cos - dy * sin) / width,
            y = (centerY + dx * sin + dy * cos) / height
        )
    }

    private fun LayerTransform.remap(context: RemapContext): LayerTransform {
        var x = centerX
        var y = centerY
        var newRotation = rotation
        // 镜像会翻转旋转方向(与 Matrix 先 scale 后 rotate 的顺序一致)
        if (context.flipHorizontal) {
            x = 1f - x
            newRotation = 180f - newRotation
        }
        if (context.flipVertical) {
            y = 1f - y
            newRotation = -newRotation
        }
        repeat(context.steps) {
            val rotatedX = 1f - y
            y = x
            x = rotatedX
            newRotation += 90f
        }
        return copy(
            centerX = (x - context.cropRect.left) / context.cropRect.width,
            centerY = (y - context.cropRect.top) / context.cropRect.height,
            rotation = newRotation,
            scale = scale * context.widthCompensation
        )
    }

    private fun StrokePoint.remapPoint(context: RemapContext): StrokePoint {
        var x = x
        var y = y
        if (context.flipHorizontal) x = 1f - x
        if (context.flipVertical) y = 1f - y
        repeat(context.steps) {
            val rotatedX = 1f - y
            y = x
            x = rotatedX
        }
        return StrokePoint(
            x = (x - context.cropRect.left) / context.cropRect.width,
            y = (y - context.cropRect.top) / context.cropRect.height
        )
    }
}
