package com.wanbaohe.markuplayers.data.project

import com.t8rin.imagetoolbox.core.domain.model.ColorModel
import com.t8rin.imagetoolbox.core.domain.model.FileModel
import com.t8rin.imagetoolbox.core.domain.model.ImageModel
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.BlurEdgeMode
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.PaletteTransferSpace
import com.t8rin.imagetoolbox.core.filters.domain.model.params.AsciiParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.ToneCurvesParams
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiAsciiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiBrightnessFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiColorMatrix3x3Filter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiCubeLutFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiGaussianBlurFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiLUT512x512Filter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiPaletteTransferFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiPaletteTransferVariantFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiToneCurvesFilter
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.FilterCodecException
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.FilterValue
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.UiFilterCodec
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import kotlin.reflect.full.primaryConstructor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test

class UiFilterCodecTest {

    @Test
    fun roundTripsTheCompleteCurrentCatalogueIncludingVisibilityAndAllValueTypes() {
        val filters = UiFilter::class.sealedSubclasses.map { type ->
            when (type) {
                // Its Android-backed default uses PointF; explicit control points keep this JVM-only.
                UiToneCurvesFilter::class -> UiToneCurvesFilter(
                    ToneCurvesParams(List(4) { listOf(0f, 0.25f, 0.5f, 0.75f, 1f) })
                )
                // Library R IDs are placeholders until APK linking, so use explicit image references.
                UiLUT512x512Filter::class -> UiLUT512x512Filter(0.7f to ImageModel("/lut.png"))
                UiPaletteTransferFilter::class -> UiPaletteTransferFilter(0.8f to ImageModel("/palette.png"))
                UiPaletteTransferVariantFilter::class -> UiPaletteTransferVariantFilter(
                    Triple(0.6f, PaletteTransferSpace.entries.first(), ImageModel("/palette.png"))
                )
                else -> type.primaryConstructor!!.callBy(emptyMap())
            }
        }
        val seen = mutableSetOf<String>()
        filters.forEachIndexed { index, filter ->
            filter.isVisible = index % 2 == 0
            val encoded = UiFilterCodec.encode(filter)
            seen += encoded.id
            val restored = UiFilterCodec.decode(encoded)
            assertEquals("Filter ${encoded.id}", encoded, UiFilterCodec.encode(restored))
            assertEquals(filter::class, restored::class)
            assertNotSame(filter, restored)
        }
        assertEquals(UiFilterCodec.supportedIds, seen)
    }

    @Test
    fun roundTripsCustomFontsAndDelimiterRichStringsWithoutLosingContent() {
        listOf<FontType?>(null, FontType.Resource(1234), FontType.File("/fonts/a:b.ttf")).forEach {
            val filter = UiAsciiFilter(AsciiParams(
                gradient = "@%:#*+=-.\n中文\\,:\u2603", fontSize = 12.5f,
                backgroundColor = ColorModel(0x33123456), isGrayscale = false, font = it
            ))
            val restored = UiFilterCodec.decode(UiFilterCodec.encode(filter)) as UiAsciiFilter
            assertEquals(filter.value, restored.value)
        }
    }

    @Test
    fun mapsExternalImageFileAndFontAssetsWithoutChangingOtherValues() {
        val filters = listOf(
            UiLUT512x512Filter(0.45f to ImageModel("content://provider/lut.png")),
            UiCubeLutFilter(0.9f to FileModel("content://provider/lookup.cube")),
            UiAsciiFilter(AsciiParams(
                " .#", 14f, ColorModel(-1), true, FontType.File("/cache/custom.ttf")
            ))
        )
        filters.forEach { filter ->
            val encoded = UiFilterCodec.encode(filter) { source ->
                if (source is String) "/private/managed.asset" else source
            }
            val restored = UiFilterCodec.decode(encoded)
            when (restored) {
                is UiLUT512x512Filter -> {
                    assertEquals(0.45f, restored.value.first, 0f)
                    assertEquals("/private/managed.asset", restored.value.second.data)
                }
                is UiCubeLutFilter -> assertEquals("/private/managed.asset", restored.value.second.uri)
                is UiAsciiFilter -> assertEquals(FontType.File("/private/managed.asset"), restored.value.font)
                else -> fail("Unexpected filter")
            }
        }
    }

    @Test
    fun rejectsUnknownClassesInvalidScalarsWrongGenericTypesAndUnknownEnums() {
        val brightness = UiFilterCodec.encode(UiBrightnessFilter(0.3f))
        assertInvalid { UiFilterCodec.decode(brightness.copy(id = "java.lang.Runtime")) }
        assertInvalid { UiFilterCodec.decode(brightness.copy(value = FilterValue("int", "1"))) }
        assertInvalid { UiFilterCodec.decode(brightness.copy(value = FilterValue("float", "NaN"))) }
        assertInvalid {
            UiFilterCodec.decode(brightness.copy(
                value = FilterValue("float", "0.3", items = listOf(FilterValue("unit")))
            ))
        }
        val blur = UiFilterCodec.encode(UiGaussianBlurFilter(Triple(9f, 2f, BlurEdgeMode.Clamp)))
        val parts = blur.value.items.toMutableList()
        parts[2] = FilterValue("BlurEdgeMode", "FutureMode")
        assertInvalid { UiFilterCodec.decode(blur.copy(value = blur.value.copy(items = parts))) }
        parts[2] = FilterValue("boolean", "true")
        assertInvalid { UiFilterCodec.decode(blur.copy(value = blur.value.copy(items = parts))) }
        assertInvalid { UiFilterCodec.decode(blur.copy(value = blur.value.copy(items = parts.take(2)))) }
    }

    @Test
    fun rejectsTruncatedMatricesInsteadOfResettingToDefaults() {
        assertInvalid { UiFilterCodec.encode(UiColorMatrix3x3Filter(floatArrayOf(1f))) }
        val encoded = UiFilterCodec.encode(UiColorMatrix3x3Filter())
        assertInvalid {
            UiFilterCodec.decode(encoded.copy(value = encoded.value.copy(items = encoded.value.items.drop(1))))
        }
    }

    @Test
    fun rejectsMissingCustomFontsAndMissingImageReferences() {
        assertInvalid {
            UiFilterCodec.encode(UiAsciiFilter(AsciiParams(
                " .#", 14f, ColorModel(-1), true, FontType.File("")
            )))
        }
        val image = UiFilterCodec.encode(UiLUT512x512Filter(1f to ImageModel("/lut.png")))
        val blank = image.value.items[1].copy(items = listOf(FilterValue("string", "")))
        assertInvalid {
            UiFilterCodec.decode(image.copy(value = image.value.copy(
                items = listOf(image.value.items[0], blank)
            )))
        }
    }

    @Test
    fun snapshotPreservesVisibilityAndCopiesMutableArrays() {
        val filter = UiColorMatrix3x3Filter().also { it.isVisible = false }
        val snapshot = UiFilterCodec.snapshot(filter) as UiColorMatrix3x3Filter
        filter.value[0] = 9f
        filter.isVisible = true
        assertFalse(snapshot.isVisible)
        assertEquals(1f, snapshot.value[0], 0f)
        assertNotSame(filter.value, snapshot.value)
    }

    private fun assertInvalid(block: () -> Unit) {
        assertThrows(FilterCodecException::class.java) { block() }
    }
}
