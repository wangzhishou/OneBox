package com.wanbaohe.markuplayers.domain.model

import com.t8rin.imagetoolbox.core.domain.model.Outline
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiSepiaFilter
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LayerRenderingGeometryTest {

    @Test
    fun legacyFilterIsUsedOnlyWhenTheExplicitStackIsEmpty() {
        val filter = UiSepiaFilter()
        val legacy = MarkupLayer(type = LayerType.Image("image"), filter = filter)
        assertEquals(listOf(LayerEffect(filter)), legacy.effectiveEffects)
        val explicit = listOf(LayerEffect(UiSepiaFilter(), 0.25f))
        assertEquals(explicit, legacy.copy(effects = explicit).effectiveEffects)
        assertTrue(legacy.requiresBitmapProcessing)
        assertTrue(legacy.copy(filter = null, mask = LayerMask()).requiresBitmapProcessing)
        assertFalse(legacy.copy(filter = null).requiresBitmapProcessing)
    }

    @Test
    fun allSixAdjustmentChannelsParticipateInNeutrality() {
        assertTrue(BaseAdjustments().isNeutral)
        assertFalse(BaseAdjustments(exposure = 1).isNeutral)
        assertFalse(BaseAdjustments(temperature = 1).isNeutral)
        assertFalse(BaseAdjustments(tint = 1).isNeutral)
        assertFalse(BaseAdjustments(brightness = 1).isNeutral)
        assertFalse(BaseAdjustments(contrast = 1).isNeutral)
        assertFalse(BaseAdjustments(saturation = 1).isNeutral)
    }

    @Test
    fun invalidGeometryNeverFallsBackToTheUnchangedLayers() {
        assertThrows(IllegalArgumentException::class.java) {
            remap(emptyList(), width = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            remap(emptyList(), freeRotation = Float.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            remap(emptyList(), crop = NormalizedRect(0.5f, 0f, 0.5f, 1f))
        }
        assertThrows(IllegalArgumentException::class.java) {
            LayerEffect(UiSepiaFilter(), Float.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MaskStroke(listOf(StrokePoint(Float.POSITIVE_INFINITY, 0f)), 0.1f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MaskStroke(listOf(StrokePoint(0f, 0f)), 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LayerMask(feather = -1f)
        }
    }

    @Test
    fun cropCompensatesTextAndShapeIntrinsicSizesOnlyOnce() {
        val transform = LayerTransform(scale = 1.8f)
        val text = LayerType.Text(
            text = "Editable",
            color = 0,
            fontSizeRatio = 0.05f,
            widthRatio = 0.5f,
            outline = Outline(0, 0.01f)
        )
        val shape = ShapeSpec(ShapeKind.Rectangle, 0, widthRatio = 0.3f, heightRatio = 0.4f)
        val layers = listOf(
            MarkupLayer(type = text, transform = transform),
            MarkupLayer(type = LayerType.Shape(shape), transform = transform),
            MarkupLayer(type = LayerType.Image("image"), transform = transform)
        )
        val mapped = remap(layers, width = 1200, height = 800, crop = NormalizedRect(0.25f, 0.25f, 0.75f, 0.75f))
        val newText = mapped[0].type as LayerType.Text
        val newShape = (mapped[1].type as LayerType.Shape).spec
        assertEquals(1.8f, mapped[0].transform.scale, EPSILON)
        assertEquals(1.8f, mapped[1].transform.scale, EPSILON)
        assertEquals(3.6f, mapped[2].transform.scale, EPSILON)
        assertEquals(0.1f, newText.fontSizeRatio, EPSILON)
        assertEquals(1f, newText.widthRatio!!, EPSILON)
        assertEquals(0.02f, newText.outline!!.width, EPSILON)
        assertEquals(shape.widthRatio * 1200, newShape.widthRatio * 600, EPSILON)
        assertEquals(shape.heightRatio * 800, newShape.heightRatio * 400, EPSILON)
    }

    @Test
    fun arbitraryRotationUsesPixelAspectAndTranslatedBoundingBox() {
        val image = MarkupLayer(
            type = LayerType.Image("image"),
            transform = LayerTransform(centerX = 1f, centerY = 0f)
        )
        val mapped = remap(listOf(image), freeRotation = 45f).single()
        val rootTwo = sqrt(2.0)
        assertEquals((300.0 / rootTwo / 212.0).toFloat(), mapped.transform.centerX, EPSILON)
        assertEquals((200.0 / rootTwo / 212.0).toFloat(), mapped.transform.centerY, EPSILON)
        assertEquals(200f / 212, mapped.transform.scale, EPSILON)
        assertEquals(45f, mapped.transform.rotation, EPSILON)
    }

    @Test
    fun actualOutputSizeControlsNormalizationWithoutResizingPixels() {
        val image = MarkupLayer(
            type = LayerType.Image("image"),
            transform = LayerTransform(centerX = 0.75f, centerY = 0.25f)
        )
        val mapped = LayerBaseRemap.remap(
            listOf(image), 200, 100, 0, false, false, NormalizedRect.Full,
            outputWidth = 199, outputHeight = 99
        ).single()
        assertEquals(150f / 199, mapped.transform.centerX, EPSILON)
        assertEquals(25f / 99, mapped.transform.centerY, EPSILON)
        assertEquals(200f / 199, mapped.transform.scale, EPSILON)
    }

    @Test
    fun baseAndLayerReflectionsComposeWithTheCorrectRotationDirection() {
        for (baseH in listOf(false, true)) for (baseV in listOf(false, true)) {
            for (localH in listOf(false, true)) for (localV in listOf(false, true)) {
                val old = LayerTransform(
                    rotation = 17f, scale = 1.2f,
                    flipHorizontal = localH, flipVertical = localV
                )
                val layer = MarkupLayer(type = LayerType.Image("image"), transform = old)
                val mapped = remap(listOf(layer), freeRotation = 123f, flipH = baseH, flipV = baseV).single()
                val physicalCompensation = 1.2f / mapped.transform.scale
                for (isVertical in listOf(false, true)) {
                    val before = axis(old, isVertical)
                    val flippedX = before.first * if (baseH) -1f else 1f
                    val flippedY = before.second * if (baseV) -1f else 1f
                    val radians = Math.toRadians(123.0)
                    val expectedX = cos(radians) * flippedX - sin(radians) * flippedY
                    val expectedY = sin(radians) * flippedX + cos(radians) * flippedY
                    val after = axis(mapped.transform, isVertical)
                    assertEquals(expectedX.toFloat(), after.first * physicalCompensation, EPSILON)
                    assertEquals(expectedY.toFloat(), after.second * physicalCompensation, EPSILON)
                }
            }
        }
    }

    @Test
    fun drawOwnTransformUsesTheContentCenterNotItsTranslatedCenter() {
        val draw = MarkupLayer(
            type = LayerType.Draw(listOf(stroke(StrokePoint(0.6f, 0.5f)))),
            transform = LayerTransform(
                centerX = 0.8f, centerY = 0.2f, scale = 2f,
                rotation = 90f, flipHorizontal = true, locked = true
            )
        )
        val mapped = remap(listOf(draw)).single()
        val stroke = (mapped.type as LayerType.Draw).strokes.single()
        val point = stroke.points.single()
        assertEquals(0.8f, point.x, EPSILON)
        assertEquals(-0.2f, point.y, EPSILON)
        assertEquals(0.06f, stroke.widthRatio, EPSILON)
        assertEquals(0.5f, mapped.transform.centerX, EPSILON)
        assertEquals(1f, mapped.transform.scale, EPSILON)
        assertEquals(0f, mapped.transform.rotation, EPSILON)
        assertFalse(mapped.transform.flipHorizontal)
        assertTrue(mapped.transform.locked)
    }

    @Test
    fun quarterRotationMapsDrawCoordinatesAndStrokePixelWidths() {
        val draw = MarkupLayer(type = LayerType.Draw(listOf(stroke(StrokePoint(0.6f, 0.5f)))))
        val mapped = remap(listOf(draw), steps = 1).single()
        val result = (mapped.type as LayerType.Draw).strokes.single()
        assertEquals(0.5f, result.points.single().x, EPSILON)
        assertEquals(0.6f, result.points.single().y, EPSILON)
        assertEquals(0.06f, result.widthRatio, EPSILON)
    }

    @Test
    fun drawRectMasksBecomeFilledPolygonsNotRotatedAxisAlignedBounds() {
        val rect = NormalizedRect(0.2f, 0.2f, 0.8f, 0.8f)
        val draw = MarkupLayer(
            type = LayerType.Draw(emptyList()),
            mask = LayerMask(rect = rect, inverted = true, feather = 0.05f)
        )
        val mapped = remap(listOf(draw), freeRotation = 45f).single()
        val mask = mapped.mask!!
        assertEquals(null, mask.rect)
        assertFalse(mask.inverted)
        assertEquals(2, mask.strokes.size)
        assertFalse(mask.strokes[0].erase)
        assertTrue(mask.strokes[1].erase)
        assertEquals(0f, mask.strokes[1].widthRatio, 0f)
        assertEquals(4, mask.strokes[1].points.size)
        assertTrue(mask.strokes[1].points[0].y != mask.strokes[1].points[1].y)
        assertEquals(0.05f * 100 / 212, mask.feather, EPSILON)
    }

    @Test
    fun emptyAndOutOfFrameLayersAndTheirMetadataAreRetained() {
        val layers = listOf(
            MarkupLayer(
                type = LayerType.Draw(emptyList()),
                mask = LayerMask(inverted = true),
                imageHistory = listOf("version"),
                groupId = "group",
                transform = LayerTransform(visible = false, locked = true, alpha = 0.3f)
            ),
            MarkupLayer(type = LayerType.Image("image"), transform = LayerTransform(centerX = 2f, centerY = -1f))
        )
        val mapped = remap(layers, freeRotation = 37f)
        assertEquals(layers.map { it.id }, mapped.map { it.id })
        assertEquals(listOf("version"), mapped[0].imageHistory)
        assertEquals("group", mapped[0].groupId)
        assertTrue(mapped[0].mask!!.inverted)
        assertTrue(mapped[0].mask!!.strokes.isEmpty())
        assertFalse(mapped[0].transform.visible)
        assertTrue(mapped[0].transform.locked)
        assertEquals(0.3f, mapped[0].transform.alpha, EPSILON)
        assertTrue(mapped[1].transform.centerX > 1f || mapped[1].transform.centerY < 0f)
    }

    private fun remap(
        layers: List<MarkupLayer>,
        width: Int = 200,
        height: Int = 100,
        steps: Int = 0,
        freeRotation: Float = 0f,
        flipH: Boolean = false,
        flipV: Boolean = false,
        crop: NormalizedRect = NormalizedRect.Full,
    ) = LayerBaseRemap.remap(layers, width, height, steps, flipH, flipV, crop, freeRotation)

    private fun stroke(point: StrokePoint) = DrawStroke(listOf(point), 0, 0.03f, BrushType.Pen)

    private fun axis(transform: LayerTransform, vertical: Boolean): Pair<Float, Float> {
        val radians = Math.toRadians(transform.rotation.toDouble())
        val sign = if (if (vertical) transform.flipVertical else transform.flipHorizontal) -1 else 1
        return if (vertical) {
            (-sin(radians) * transform.scale * sign).toFloat() to
                (cos(radians) * transform.scale * sign).toFloat()
        } else {
            (cos(radians) * transform.scale * sign).toFloat() to
                (sin(radians) * transform.scale * sign).toFloat()
        }
    }

    companion object {
        private const val EPSILON = 0.0001f
    }
}
