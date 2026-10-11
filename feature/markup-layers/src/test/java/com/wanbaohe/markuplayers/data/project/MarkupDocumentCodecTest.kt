package com.wanbaohe.markuplayers.data.project

import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.model.Outline
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiBrightnessFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiSepiaFilter
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import com.wanbaohe.markuplayers.domain.model.BrushType
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.MaskStroke
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.model.ShapeKind
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import com.wanbaohe.markuplayers.domain.model.StrokePoint
import com.wanbaohe.markuplayers.domain.project.MarkupDocument
import com.wanbaohe.markuplayers.domain.project.MarkupProjectSummary
import com.wanbaohe.markuplayers.presentation.editor.CanvasBackground
import com.wanbaohe.markuplayers.presentation.export.ExportResolution
import com.wanbaohe.markuplayers.presentation.export.ExportSettings
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkupDocumentCodecTest {

    @Test
    fun faithfullyRoundTripsEveryLayerKindAndDocumentField() {
        val document = detailedDocument()
        val encoded = MarkupDocumentCodec.encode(document)
        val restored = MarkupDocumentCodec.decode(encoded)
        assertEquals(encoded, MarkupDocumentCodec.encode(restored))
        assertEquals(document.baseAdjustments, restored.baseAdjustments)
        assertEquals(document.baseMask, restored.baseMask)
        assertEquals(document.exportSettings, restored.exportSettings)
        assertEquals(document.layers.first().type, restored.layers.first().type)
        assertEquals(document.layers.first().transform, restored.layers.first().transform)
        assertEquals(document.originalUri, restored.originalUri)
        assertEquals(document.workspaceBackground, restored.workspaceBackground)
        assertTrue(restored.saveExif)
        assertFalse(restored.layers.first().filter!!.isVisible)
    }

    @Test
    fun roundTripsAllRecognizedImageFormatsWithoutDefaulting() {
        ImageFormat.entries.forEach { format ->
            val document = baseDocument().copy(exportSettings = ExportSettings(format = format))
            val restored = MarkupDocumentCodec.decode(MarkupDocumentCodec.encode(document))
            assertEquals(format, restored.exportSettings.format)
        }
    }

    @Test
    fun rejectsMalformedMissingUnknownAndNewerSchemaData() {
        assertCorrupt("{not json")
        val root = Json.parseToJsonElement(MarkupDocumentCodec.encode(baseDocument())).jsonObject
        assertCorrupt(JsonObject(root + ("schemaVersion" to JsonPrimitive(2))).toString())
        assertCorrupt(JsonObject(root - "schemaVersion").toString())
        assertCorrupt(JsonObject(root + ("unexpected" to JsonPrimitive(true))).toString())
        val document = root.getValue("document").jsonObject
        assertCorrupt(withDocument(root, JsonObject(document - "baseEffects")))
        assertCorrupt(withDocument(root, JsonObject(document + ("width" to JsonPrimitive("10")))))
        assertCorrupt(withDocument(root, JsonObject(document + ("height" to JsonPrimitive(0)))))
        assertCorrupt(withDocument(root, JsonObject(document + ("baseUri" to JsonPrimitive("")))))
    }

    @Test
    fun rejectsUnknownFilterRatherThanDroppingTheEffect() {
        val source = MarkupDocumentCodec.encode(
            baseDocument().copy(baseEffects = listOf(LayerEffect(UiSepiaFilter())))
        )
        assertCorrupt(source.replace("UiSepiaFilter", "java.lang.Runtime"))
    }

    @Test
    fun rejectsUnknownEnumInvalidGeometryAndDuplicateLayerIds() {
        val source = MarkupDocumentCodec.encode(detailedDocument())
        assertCorrupt(source.replace("\"Multiply\"", "\"FutureBlendMode\""))
        assertCorrupt(source.replace("\"fontSizeRatio\":0.08", "\"fontSizeRatio\":-0.08"))
        assertCorrupt(source.replace("\"alpha\":0.6", "\"alpha\":1.6"))
        val duplicate = baseDocument().copy(layers = listOf(
            MarkupLayer(id = "same", type = LayerType.Image("/a.png")),
            MarkupLayer(id = "same", type = LayerType.Image("/b.png"))
        ))
        assertThrows(MarkupProjectFormatException::class.java) {
            MarkupDocumentCodec.encode(duplicate)
        }
    }

    @Test
    fun projectEnvelopePreservesSummaryAndRejectsMismatchedCountsAndUnsafeIds() {
        val document = detailedDocument()
        val project = StoredMarkupProject(
            MarkupProjectSummary("project-1", "A \"project\"\n测试", 1234, "/private/thumb.png", 8),
            document
        )
        val source = MarkupProjectCodec.encode(project)
        val restored = MarkupProjectCodec.decode(source)
        assertEquals(project.summary, restored.summary)
        assertEquals(source, MarkupProjectCodec.encode(restored))
        assertProjectCorrupt(source.replace("\"layerCount\":8", "\"layerCount\":99"))
        assertProjectCorrupt(source.replace("\"project-1\"", "\"../escape\""))
    }

    @Test
    fun rejectsUnsupportedImageObjectsAndUnsafeBundledPaths() {
        assertThrows(MarkupProjectFormatException::class.java) {
            MarkupDocumentCodec.encode(baseDocument().copy(layers = listOf(
                MarkupLayer(type = LayerType.Image(Any()))
            )))
        }
        assertThrows(MarkupProjectFormatException::class.java) {
            MarkupDocumentCodec.encode(baseDocument().copy(layers = listOf(
                MarkupLayer(type = LayerType.Sticker(StickerSource.Asset("../outside.svg")))
            )))
        }
    }

    private fun detailedDocument(): MarkupDocument {
        val mask = LayerMask(
            NormalizedRect(0.1f, 0.2f, 0.9f, 0.8f),
            listOf(MaskStroke(listOf(StrokePoint(-0.1f, 0.3f), StrokePoint(1.2f, 0.5f)), 0.025f, true)),
            inverted = true, feather = 0.05f
        )
        val text = LayerType.Text(
            text = "Text \"quoted\"\n中文:🖼", color = 0xffabcdef.toInt(), fontSizeRatio = 0.08f,
            font = FontType.File("/private/a:font.ttf"), backgroundColor = 0x33123456,
            decorations = LayerType.Text.Decoration.entries.toSet(),
            outline = Outline(-1, 1.5f), alignment = LayerType.Text.TextAlignment.Justify,
            lineHeight = 1.8f, letterSpacingEm = -0.02f, widthRatio = 0.7f
        )
        val types = listOf(
            text,
            text.copy(font = FontType.Resource(12345), outline = null, widthRatio = null),
            LayerType.Draw(listOf(DrawStroke(
                listOf(StrokePoint(0.1f, 0.2f), StrokePoint(0.8f, 0.9f)),
                0xff123456.toInt(), 0.03f, BrushType.Marker, 0.6f
            ))),
            LayerType.Shape(ShapeSpec(ShapeKind.Arrow, -1, false, 0.6f, 0.2f, 0.04f, 0.02f)),
            LayerType.Sticker(StickerSource.Asset("stickers/decor/flower.svg")),
            LayerType.Sticker(StickerSource.Emoji(42)),
            LayerType.Sticker(StickerSource.Generated("/private/ai-sticker.png")),
            LayerType.Image("/private/image.png")
        )
        return MarkupDocument(
            baseUri = "/private/base.jpg", width = 2048, height = 1536,
            layers = types.mapIndexed { index, type ->
                MarkupLayer(
                    id = "layer-$index", name = "Layer $index", type = type,
                    transform = LayerTransform(
                        0.3f, 0.8f, 1.4f, -37f, 0.6f, false, true,
                        LayerBlendMode.Multiply, true, true
                    ),
                    adjustments = BaseAdjustments(1, -2, 3, -4, 5, -6),
                    filter = UiBrightnessFilter(0.35f).also { it.isVisible = false },
                    effects = listOf(LayerEffect(UiSepiaFilter(), 0.2f)),
                    mask = mask, imageHistory = listOf("/private/previous.png"),
                    groupId = "group-1",
                )
            },
            baseAdjustments = BaseAdjustments(10, 20, -30, 40, -50, 60),
            baseEffects = listOf(LayerEffect(UiBrightnessFilter(-0.3f), 0.7f)),
            baseMask = mask,
            exportSettings = ExportSettings(
                ImageFormat.Webp.Lossy, 85, ExportResolution.Custom, 1200, 900, true,
                saveCopy = false, jpegBackgroundColor = 0xff123456.toInt(), maxFileSizeBytes = 524_288
            ),
            originalUri = "/private/original.raw", saveExif = true,
            workspaceBackground = CanvasBackground.Solid(0xff222222.toInt())
        )
    }

    private fun baseDocument() = MarkupDocument("/private/base.png", 100, 200, emptyList())
    private fun withDocument(root: JsonObject, document: JsonObject) =
        JsonObject(root + ("document" to document)).toString()
    private fun assertCorrupt(source: String) {
        assertThrows(MarkupProjectFormatException::class.java) {
            MarkupDocumentCodec.decode(source)
        }
    }
    private fun assertProjectCorrupt(source: String) {
        assertThrows(MarkupProjectFormatException::class.java) {
            MarkupProjectCodec.decode(source)
        }
    }
}
