package com.wanbaohe.markuplayers.domain.model

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Pixel-space remapping for the base bitmap's center flips, rotation, translated
 * bounding box, and crop, in that order (the same order as Bitmap.createBitmap).
 * No layer is discarded, including empty or out-of-frame layers.
 */
object LayerBaseRemap {

    /**
     * [cropRect] refers to the rotated bitmap's rounded integer bounding box.
     * Crop edges use Android's roundToInt/clamp rules, not ideal fractional sizes.
     *
     * [outputWidth]/[outputHeight] optionally supply the actual cropped bitmap
     * dimensions. They are normalization dimensions, NOT a request to resize;
     * omitted values use the calculated integer crop size. Supply them after
     * transforming/cropping the base to avoid accumulated one-pixel rounding drift.
     *
     * Text/shape intrinsic ratios are compensated once; only image/sticker
     * transforms need width-based scale compensation. Draw geometry is baked
     * into editable points, including its own flips/rotation/translation.
     */
    fun remap(
        layers: List<MarkupLayer>,
        oldWidth: Int,
        oldHeight: Int,
        rotationSteps: Int,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRect: NormalizedRect,
        freeRotation: Float = 0f,
        outputWidth: Int? = null,
        outputHeight: Int? = null,
    ): List<MarkupLayer> {
        val context = createContext(
            oldWidth = oldWidth,
            oldHeight = oldHeight,
            rotationSteps = rotationSteps,
            freeRotation = freeRotation,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical,
            cropRect = cropRect,
            outputWidth = outputWidth,
            outputHeight = outputHeight
        )
        return layers.map { it.remap(context) }
    }

    /**
     * Base-image mask remapping for [remap]'s same affine transform. The base mask
     * lives in untransformed content coordinates (identity layer transform), so
     * strokes map through [RemapContext.mapPoint] directly. As with draw-layer masks,
     * a rotated rect becomes a filled polygon and inversion folds into operations.
     */
    fun remapMask(
        mask: LayerMask,
        oldWidth: Int,
        oldHeight: Int,
        rotationSteps: Int,
        freeRotation: Float,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRect: NormalizedRect,
        outputWidth: Int? = null,
        outputHeight: Int? = null,
    ): LayerMask {
        val context = createContext(
            oldWidth = oldWidth,
            oldHeight = oldHeight,
            rotationSteps = rotationSteps,
            freeRotation = freeRotation,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical,
            cropRect = cropRect,
            outputWidth = outputWidth,
            outputHeight = outputHeight
        )
        fun mapContentPoint(point: StrokePoint): StrokePoint = context.mapPoint(
            point.x * oldWidth.toDouble(), point.y * oldHeight.toDouble()
        )

        fun polygon(rect: NormalizedRect, erase: Boolean = false) = MaskStroke(
            points = listOf(
                StrokePoint(rect.left, rect.top),
                StrokePoint(rect.right, rect.top),
                StrokePoint(rect.right, rect.bottom),
                StrokePoint(rect.left, rect.bottom)
            ).map(::mapContentPoint),
            widthRatio = 0f,
            erase = erase
        )

        val mappedStrokes = buildList {
            val rect = mask.rect
            if (rect != null) {
                if (mask.inverted) add(polygon(NormalizedRect.Full))
                add(polygon(rect, erase = mask.inverted))
            } else {
                val startsEmpty = mask.strokes.any { !it.erase }
                if (startsEmpty == mask.inverted) add(polygon(NormalizedRect.Full))
            }
            mask.strokes.forEach { stroke ->
                add(
                    stroke.copy(
                        points = stroke.points.map(::mapContentPoint),
                        widthRatio = stroke.widthRatio.scaled(context.widthCompensation),
                        erase = stroke.erase xor mask.inverted
                    )
                )
            }
        }
        return LayerMask(
            strokes = mappedStrokes,
            inverted = mappedStrokes.isEmpty(),
            feather = mask.feather.scaled(
                minOf(context.oldWidth, context.oldHeight).toDouble() /
                    minOf(context.newWidth, context.newHeight)
            )
        )
    }

    private fun createContext(
        oldWidth: Int,
        oldHeight: Int,
        rotationSteps: Int,
        freeRotation: Float,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRect: NormalizedRect,
        outputWidth: Int?,
        outputHeight: Int?,
    ): RemapContext {
        require(oldWidth > 0 && oldHeight > 0) { "Original canvas dimensions must be positive" }
        require(freeRotation.isFinite()) { "Base rotation must be finite" }
        require(
            cropRect.left.isFinite() && cropRect.top.isFinite() &&
                cropRect.right.isFinite() && cropRect.bottom.isFinite() &&
                cropRect.left in 0f..1f && cropRect.top in 0f..1f &&
                cropRect.right in 0f..1f && cropRect.bottom in 0f..1f &&
                cropRect.width > 0f && cropRect.height > 0f
        ) { "Crop must be a finite, positive normalized rectangle" }
        require(outputWidth == null || outputWidth > 0) { "Output width must be positive" }
        require(outputHeight == null || outputHeight > 0) { "Output height must be positive" }

        val steps = ((rotationSteps % 4) + 4) % 4
        val degrees = normalizedDegrees(steps * 90.0 + freeRotation)
        val rotation = rotation(degrees)
        val boundWidth = (abs(rotation.cos) * oldWidth + abs(rotation.sin) * oldHeight)
            .toFloat().toDouble()
        val boundHeight = (abs(rotation.sin) * oldWidth + abs(rotation.cos) * oldHeight)
            .toFloat().toDouble()
        require(boundWidth < Int.MAX_VALUE && boundHeight < Int.MAX_VALUE) {
            "Rotated canvas dimensions exceed integer range"
        }
        val rotatedWidth = boundWidth.roundToInt().coerceAtLeast(1)
        val rotatedHeight = boundHeight.roundToInt().coerceAtLeast(1)
        val left = (cropRect.left * rotatedWidth).roundToInt().coerceIn(0, rotatedWidth - 1)
        val top = (cropRect.top * rotatedHeight).roundToInt().coerceIn(0, rotatedHeight - 1)
        val right = (cropRect.right * rotatedWidth).roundToInt().coerceIn(left + 1, rotatedWidth)
        val bottom = (cropRect.bottom * rotatedHeight).roundToInt().coerceIn(top + 1, rotatedHeight)
        return RemapContext(
            oldWidth = oldWidth,
            oldHeight = oldHeight,
            newWidth = outputWidth ?: (right - left),
            newHeight = outputHeight ?: (bottom - top),
            degrees = degrees,
            rotation = rotation,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical,
            offsetX = boundWidth / 2.0 - left,
            offsetY = boundHeight / 2.0 - top
        )
    }

    private class RemapContext(
        val oldWidth: Int,
        val oldHeight: Int,
        val newWidth: Int,
        val newHeight: Int,
        val degrees: Double,
        val rotation: Rotation,
        val flipHorizontal: Boolean,
        val flipVertical: Boolean,
        val offsetX: Double,
        val offsetY: Double,
    ) {
        val widthCompensation: Double = oldWidth.toDouble() / newWidth
        val heightCompensation: Double = oldHeight.toDouble() / newHeight

        fun mapPoint(x: Double, y: Double): StrokePoint {
            val dx = (x - oldWidth / 2.0) * if (flipHorizontal) -1.0 else 1.0
            val dy = (y - oldHeight / 2.0) * if (flipVertical) -1.0 else 1.0
            return StrokePoint(
                ((rotation.cos * dx - rotation.sin * dy + offsetX) / newWidth).finiteFloat(),
                ((rotation.sin * dx + rotation.cos * dy + offsetY) / newHeight).finiteFloat()
            )
        }

        fun mapLocalPoint(point: StrokePoint, transform: LayerTransform): StrokePoint {
            require(point.x.isFinite() && point.y.isFinite()) { "Draw points must be finite" }
            val dx = (point.x * oldWidth.toDouble() - oldWidth / 2.0) *
                transform.scale * if (transform.flipHorizontal) -1.0 else 1.0
            val dy = (point.y * oldHeight.toDouble() - oldHeight / 2.0) *
                transform.scale * if (transform.flipVertical) -1.0 else 1.0
            val localRotation = LayerBaseRemap.rotation(transform.rotation.toDouble())
            return mapPoint(
                transform.centerX * oldWidth.toDouble() + localRotation.cos * dx - localRotation.sin * dy,
                transform.centerY * oldHeight.toDouble() + localRotation.sin * dx + localRotation.cos * dy
            )
        }
    }

    private fun MarkupLayer.remap(context: RemapContext): MarkupLayer {
        transform.requireFiniteGeometry()
        val content = type
        if (content is LayerType.Draw) {
            val widthCompensation = context.widthCompensation * transform.scale
            return copy(
                type = content.copy(
                    strokes = content.strokes.map { stroke ->
                        stroke.copy(
                            points = stroke.points.map { context.mapLocalPoint(it, transform) },
                            widthRatio = stroke.widthRatio.scaled(widthCompensation)
                        )
                    }
                ),
                mask = mask?.remapDrawMask(transform, context),
                transform = transform.copy(
                    centerX = 0.5f,
                    centerY = 0.5f,
                    scale = 1f,
                    rotation = 0f,
                    flipHorizontal = false,
                    flipVertical = false
                )
            )
        }

        val remappedType = when (content) {
            is LayerType.Text -> content.copy(
                fontSizeRatio = content.fontSizeRatio.scaled(context.widthCompensation),
                widthRatio = content.widthRatio?.scaled(context.widthCompensation),
                outline = content.outline?.let {
                    it.copy(width = it.width.scaled(context.widthCompensation))
                }
            )

            is LayerType.Shape -> content.copy(
                spec = content.spec.run {
                    copy(
                        widthRatio = widthRatio.scaled(context.widthCompensation),
                        heightRatio = heightRatio.scaled(context.heightCompensation),
                        cornerRadiusRatio = cornerRadiusRatio.scaled(context.widthCompensation),
                        strokeWidthRatio = strokeWidthRatio.scaled(context.widthCompensation)
                    )
                }
            )

            else -> content
        }
        val center = context.mapPoint(
            transform.centerX * context.oldWidth.toDouble(),
            transform.centerY * context.oldHeight.toDouble()
        )
        val isReflected = context.flipHorizontal xor context.flipVertical
        val imageScale = if (content is LayerType.Image || content is LayerType.Sticker) {
            context.widthCompensation
        } else 1.0
        return copy(
            type = remappedType,
            transform = transform.copy(
                centerX = center.x,
                centerY = center.y,
                rotation = normalizedDegrees(
                    context.degrees + if (isReflected) -transform.rotation.toDouble()
                    else transform.rotation.toDouble()
                ).finiteFloat(),
                scale = transform.scale.scaled(imageScale),
                flipHorizontal = transform.flipHorizontal xor context.flipHorizontal,
                flipVertical = transform.flipVertical xor context.flipVertical
            )
        )
    }

    /**
     * A rotated rectangle cannot be represented by a new axis-aligned rect.
     * Zero-width MaskStrokes are filled polygons, preserving the exact selection
     * geometry without rasterizing the draw layer or introducing persistence fields.
     * Inversion is moved into the seed/stroke operations, rather than accidentally
     * selecting the entire new canvas outside the old draw-content frame.
     */
    private fun LayerMask.remapDrawMask(
        transform: LayerTransform,
        context: RemapContext,
    ): LayerMask {
        fun polygon(rect: NormalizedRect, erase: Boolean = false) = MaskStroke(
            points = listOf(
                StrokePoint(rect.left, rect.top),
                StrokePoint(rect.right, rect.top),
                StrokePoint(rect.right, rect.bottom),
                StrokePoint(rect.left, rect.bottom)
            ).map { context.mapLocalPoint(it, transform) },
            widthRatio = 0f,
            erase = erase
        )

        val mappedStrokes = buildList {
            val rect = rect
            if (rect != null) {
                if (inverted) add(polygon(NormalizedRect.Full))
                add(polygon(rect, erase = inverted))
            } else {
                val startsEmpty = strokes.any { !it.erase }
                if (startsEmpty == inverted) add(polygon(NormalizedRect.Full))
            }
            strokes.forEach { stroke ->
                add(
                    stroke.copy(
                        points = stroke.points.map { context.mapLocalPoint(it, transform) },
                        widthRatio = stroke.widthRatio.scaled(context.widthCompensation * transform.scale),
                        erase = stroke.erase xor inverted
                    )
                )
            }
        }
        // No strokes denotes a full selection, so retain an inverted empty mask
        // when the transported selection is actually empty.
        return LayerMask(
            strokes = mappedStrokes,
            inverted = mappedStrokes.isEmpty(),
            feather = feather.scaled(
                transform.scale * minOf(context.oldWidth, context.oldHeight).toDouble() /
                    minOf(context.newWidth, context.newHeight)
            )
        )
    }

    private fun LayerTransform.requireFiniteGeometry() {
        require(
            centerX.isFinite() && centerY.isFinite() && rotation.isFinite() &&
                scale.isFinite() && scale > 0f && alpha.isFinite()
        ) { "Layer geometry must be finite with positive scale" }
    }

    private data class Rotation(val cos: Double, val sin: Double)

    private fun rotation(degrees: Double): Rotation = when (val angle = normalizedDegrees(degrees)) {
        0.0 -> Rotation(1.0, 0.0)
        90.0 -> Rotation(0.0, 1.0)
        180.0 -> Rotation(-1.0, 0.0)
        270.0 -> Rotation(0.0, -1.0)
        else -> Math.toRadians(angle).let {
            Rotation(cos(it).toFloat().toDouble(), sin(it).toFloat().toDouble())
        }
    }

    private fun normalizedDegrees(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

    private fun Float.scaled(factor: Double): Float = (this * factor).finiteFloat()

    private fun Double.finiteFloat(): Float = toFloat().also {
        require(it.isFinite()) { "Remapped layer geometry exceeds finite coordinate range" }
    }
}
