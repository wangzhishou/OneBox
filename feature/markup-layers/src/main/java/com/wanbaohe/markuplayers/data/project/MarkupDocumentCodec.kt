package com.wanbaohe.markuplayers.data.project

import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.model.Outline
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.EncodedUiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.FilterValue
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.UiFilterCodec
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.shifenmiao.common.components.text.RichTextStyleSpan
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import com.wanbaohe.markuplayers.domain.model.BrushType
import com.wanbaohe.markuplayers.domain.model.BaseTransformSpec
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
import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

class MarkupProjectFormatException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

/** Strict schema: missing/unknown fields, invalid values and unknown filters are errors. */
object MarkupDocumentCodec {

    const val SchemaVersion: Int = 1

    fun encode(document: MarkupDocument): String = obj(
        "schemaVersion" to json(SchemaVersion),
        "document" to encodeDocument(document)
    ).toString()

    fun decode(source: String): MarkupDocument = formatErrors {
        val root = parse(source).objectWith("schemaVersion", "document")
        checkSchema(root.int("schemaVersion"))
        decodeDocument(root.getValue("document"))
    }

    internal fun encodeDocument(document: MarkupDocument): JsonObject {
        val result = obj(
            "baseUri" to json(document.baseUri),
            "width" to json(document.width),
            "height" to json(document.height),
            "layers" to array(document.layers, ::encodeLayer),
            "baseAdjustments" to encodeAdjustments(document.baseAdjustments),
            "baseEffects" to array(document.baseEffects, ::encodeEffect),
            "baseMask" to nullable(document.baseMask, ::encodeMask),
            "baseTransforms" to array(document.baseTransforms, ::encodeBaseTransform),
            "baseImageHistory" to array(document.baseImageHistory, ::json),
            "exportSettings" to encodeExport(document.exportSettings),
            "originalUri" to nullable(document.originalUri, ::json),
            "sourceUri" to nullable(document.sourceUri, ::json),
            "saveExif" to json(document.saveExif),
            "workspaceBackground" to when (val background = document.workspaceBackground) {
                CanvasBackground.Default -> obj("type" to json("default"))
                is CanvasBackground.Solid -> obj(
                    "type" to json("solid"),
                    "color" to json(background.color)
                )
            },
            "baseVisible" to json(document.baseVisible),
            "baseOrder" to json(document.baseOrder),
        )
        // Validate the same contract for writes and reads, without clamping or substituting defaults.
        formatErrors { decodeDocument(result) }
        return result
    }

    internal fun decodeDocument(element: JsonElement): MarkupDocument {
        // baseVisible/baseOrder 为后加字段:旧项目文件没有,按默认值兼容解码;
        // 其余字段保持严格 schema(缺失/未知均报错)
        val value = element.objectWithOptional(
            arrayOf(
                "baseUri", "width", "height", "layers", "baseAdjustments", "baseEffects", "baseMask",
                "baseTransforms", "baseImageHistory", "exportSettings", "originalUri", "sourceUri",
                "saveExif", "workspaceBackground"
            ),
            optional = setOf("baseVisible", "baseOrder")
        )
        val document = MarkupDocument(
            baseUri = value.string("baseUri"),
            width = value.int("width"),
            height = value.int("height"),
            layers = value.list("layers", ::decodeLayer),
            baseAdjustments = decodeAdjustments(value.getValue("baseAdjustments")),
            baseEffects = value.list("baseEffects", ::decodeEffect),
            baseMask = value.optional("baseMask", ::decodeMask),
            baseTransforms = value.list("baseTransforms", ::decodeBaseTransform),
            baseImageHistory = value.list("baseImageHistory") {
                it.stringValue().also { path -> if (path.isBlank()) invalid("Blank base image history") }
            },
            exportSettings = decodeExport(value.getValue("exportSettings")),
            originalUri = value.optional("originalUri") { it.stringValue() },
            sourceUri = value.optional("sourceUri") { it.stringValue() },
            saveExif = value.boolean("saveExif"),
            workspaceBackground = decodeBackground(value.getValue("workspaceBackground")),
            baseVisible = value["baseVisible"]?.let {
                it.primitive().let { p -> if (p.isString) invalid("Invalid base visibility"); p.booleanOrNull ?: invalid("Invalid base visibility") }
            } ?: true,
            baseOrder = (value["baseOrder"]?.let {
                it.primitive().let { p -> if (p.isString) invalid("Invalid base order"); p.intOrNull ?: invalid("Invalid base order") }
            } ?: 0).also { if (it < 0) invalid("Invalid base order") },
        )
        if (document.baseUri.isBlank() || document.width <= 0 || document.height <= 0) {
            invalid("The document must have a base asset and positive dimensions")
        }
        if (document.layers.map { it.id }.toSet().size != document.layers.size) {
            invalid("Duplicate layer IDs")
        }
        document.originalUri?.let { if (it.isBlank()) invalid("Blank original asset reference") }
        document.sourceUri?.let { if (it.isBlank()) invalid("Blank source reference") }
        return document
    }

    private fun encodeLayer(layer: MarkupLayer) = obj(
        "id" to json(layer.id),
        "name" to json(layer.name),
        "type" to encodeType(layer.type),
        "transform" to encodeTransform(layer.transform),
        "adjustments" to encodeAdjustments(layer.adjustments),
        "filter" to nullable(layer.filter, ::encodeFilter),
        "effects" to array(layer.effects, ::encodeEffect),
        "mask" to nullable(layer.mask, ::encodeMask),
        "imageHistory" to array(layer.imageHistory, ::json),
        "groupId" to nullable(layer.groupId, ::json),
    )

    private fun decodeLayer(element: JsonElement): MarkupLayer {
        val value = element.objectWith(
            "id", "name", "type", "transform", "adjustments", "filter", "effects", "mask",
            "imageHistory", "groupId"
        )
        return MarkupLayer(
            id = value.string("id").also { if (it.isBlank()) invalid("Blank layer ID") },
            name = value.string("name"),
            type = decodeType(value.getValue("type")),
            transform = decodeTransform(value.getValue("transform")),
            adjustments = decodeAdjustments(value.getValue("adjustments")),
            filter = value.optional("filter", ::decodeFilter),
            effects = value.list("effects", ::decodeEffect),
            mask = value.optional("mask", ::decodeMask),
            imageHistory = value.list("imageHistory") {
                it.stringValue().also { path -> if (path.isBlank()) invalid("Blank image history") }
            },
            groupId = value.optional("groupId") { it.stringValue() },
        )
    }

private fun encodeType(type: LayerType): JsonObject = when (type) {
        is LayerType.Text -> obj(
            "kind" to json("text"),
            "text" to json(type.text),
            "color" to json(type.color),
            "fontSizeRatio" to json(type.fontSizeRatio),
            "font" to nullable(type.font, ::encodeFont),
            "backgroundColor" to json(type.backgroundColor),
            "decorations" to array(type.decorations.toList().sortedBy { it.name }) { json(it.name) },
            "outline" to nullable(type.outline) {
                obj("color" to json(it.color), "width" to json(it.width))
            },
            "alignment" to json(type.alignment.name),
            "lineHeight" to json(type.lineHeight),
            "letterSpacingEm" to json(type.letterSpacingEm),
            "styleSpans" to array(type.styleSpans, ::encodeStyleSpan),
            "widthRatio" to nullable(type.widthRatio, ::json),
            "widthManuallySet" to json(type.widthManuallySet),
        )
        is LayerType.Draw -> obj(
            "kind" to json("draw"),
            "strokes" to array(type.strokes) {
                obj(
                    "points" to array(it.points, ::encodePoint),
                    "color" to json(it.color),
                    "widthRatio" to json(it.widthRatio),
                    "brush" to json(it.brush.name),
                    "alpha" to json(it.alpha)
                )
            }
        )
        is LayerType.Shape -> obj(
            "kind" to json("shape"),
            "spec" to with(type.spec) {
                obj(
                    "kind" to json(kind.name),
                    "color" to json(color),
                    "filled" to json(filled),
                    "widthRatio" to json(widthRatio),
                    "heightRatio" to json(heightRatio),
                    "cornerRadiusRatio" to json(cornerRadiusRatio),
                    "strokeWidthRatio" to json(strokeWidthRatio),
                )
            }
        )
        is LayerType.Sticker -> obj(
            "kind" to json("sticker"),
            "source" to when (val source = type.source) {
                is StickerSource.Asset -> obj("type" to json("asset"), "path" to json(source.path))
                is StickerSource.Emoji -> obj(
                    "type" to json("emoji"), "emojiIndex" to json(source.emojiIndex)
                )
                is StickerSource.Generated -> obj(
                    "type" to json("generated"), "path" to json(source.path)
                )
            }
        )
        is LayerType.Image -> obj(
            "kind" to json("image"),
            "path" to json(type.imageData as? String
                ?: invalid("Image layers must be imported before serialization"))
        )
    }

    private fun decodeType(element: JsonElement): LayerType {
        val value = element.asObject()
        return when (value.string("kind")) {
            "text" -> {
                // widthManuallySet 为后加字段:旧文件没有,缺省回退 false = 自适应收紧
                // (选框始终贴合文字;旧文件里的 widthRatio 降级为折行上限,不再固定框宽——
                // 固定宽框放大后手柄飞出画布、文字不跟随,正是本次要修的问题;
                // 代价是旧项目重开时文字围绕锚点重新居中一次,折行不变)
                value.objectWithOptional(
                    required = arrayOf(
                        "kind", "text", "color", "fontSizeRatio", "font", "backgroundColor",
                        "decorations", "outline", "alignment", "lineHeight", "letterSpacingEm",
                        "widthRatio"
                    ),
                    optional = setOf("widthManuallySet", "styleSpans")
                )
                val decorations = value.list("decorations") {
                    enumValue<LayerType.Text.Decoration>(it.stringValue())
                }
                if (decorations.toSet().size != decorations.size) invalid("Duplicate decorations")
                val widthRatio = value.optional("widthRatio") {
                    it.floatValue().positive("Text width")
                }
                LayerType.Text(
                    text = value.string("text"),
                    color = value.int("color"),
                    fontSizeRatio = value.float("fontSizeRatio").positive("Font size"),
                    font = value.optional("font", ::decodeFont),
                    backgroundColor = value.int("backgroundColor"),
                    decorations = decorations.toSet(),
                    outline = value.optional("outline") {
                        val outline = it.objectWith("color", "width")
                        Outline(outline.int("color"), outline.float("width").nonnegative("Outline"))
                    },
                    alignment = enumValue(value.string("alignment")),
                    lineHeight = value.float("lineHeight").positive("Line height"),
                    letterSpacingEm = value.float("letterSpacingEm"),
                    styleSpans = when (val spans = value["styleSpans"]) {
                        null, JsonNull -> emptyList()
                        is JsonArray -> spans.map(::decodeStyleSpan)
                        else -> invalid("Invalid text style spans")
                    },
                    widthRatio = widthRatio,
                    widthManuallySet = (value["widthManuallySet"] as? JsonPrimitive)
                        ?.booleanOrNull ?: false,
                )
            }
            "draw" -> {
                value.objectWith("kind", "strokes")
                LayerType.Draw(value.list("strokes") {
                    val stroke = it.objectWith("points", "color", "widthRatio", "brush", "alpha")
                    DrawStroke(
                        points = stroke.list("points", ::decodePoint),
                        color = stroke.int("color"),
                        widthRatio = stroke.float("widthRatio").positive("Stroke width"),
                        brush = enumValue<BrushType>(stroke.string("brush")),
                        alpha = stroke.float("alpha").unitRange("Stroke alpha"),
                    )
                })
            }
            "shape" -> {
                value.objectWith("kind", "spec")
                val spec = value.getValue("spec").objectWith(
                    "kind", "color", "filled", "widthRatio", "heightRatio",
                    "cornerRadiusRatio", "strokeWidthRatio"
                )
                LayerType.Shape(ShapeSpec(
                    kind = enumValue<ShapeKind>(spec.string("kind")),
                    color = spec.int("color"),
                    filled = spec.boolean("filled"),
                    widthRatio = spec.float("widthRatio").positive("Shape width"),
                    heightRatio = spec.float("heightRatio").positive("Shape height"),
                    cornerRadiusRatio = spec.float("cornerRadiusRatio").nonnegative("Corner radius"),
                    strokeWidthRatio = spec.float("strokeWidthRatio").nonnegative("Shape stroke"),
                ))
            }
            "sticker" -> {
                value.objectWith("kind", "source")
                val source = value.getValue("source").asObject()
                LayerType.Sticker(when (source.string("type")) {
                    "asset" -> {
                        source.objectWith("type", "path")
                        StickerSource.Asset(source.string("path").also(::checkAssetPath))
                    }
                    "emoji" -> {
                        source.objectWith("type", "emojiIndex")
                        StickerSource.Emoji(source.int("emojiIndex").also {
                            if (it < 0) invalid("Invalid emoji index")
                        })
                    }
                    "generated" -> {
                        source.objectWith("type", "path")
                        StickerSource.Generated(source.string("path"))
                    }
                    else -> invalid("Unknown sticker source")
                })
            }
            "image" -> {
                value.objectWith("kind", "path")
                LayerType.Image(value.string("path").also {
                    if (it.isBlank()) invalid("Blank image path")
                })
            }
            else -> invalid("Unknown layer kind")
        }
    }

    private fun encodeFont(font: FontType) = when (font) {
        is FontType.Resource -> obj("type" to json("resource"), "resId" to json(font.resId))
        is FontType.File -> obj("type" to json("file"), "path" to json(font.path))
    }

    private fun decodeFont(element: JsonElement): FontType {
        val value = element.asObject()
        return when (value.string("type")) {
            "resource" -> {
                value.objectWith("type", "resId")
                FontType.Resource(value.int("resId").also {
                    if (it <= 0) invalid("Invalid font resource ID")
                })
            }
            "file" -> {
                value.objectWith("type", "path")
                FontType.File(value.string("path").also {
                    if (it.isBlank()) invalid("Blank font path")
                })
            }
            else -> invalid("Unknown font type")
        }
    }

    private fun encodeTransform(transform: LayerTransform) = with(transform) {
        obj(
            "centerX" to json(centerX), "centerY" to json(centerY),
            "scale" to json(scale), "rotation" to json(rotation), "alpha" to json(alpha),
            "visible" to json(visible), "locked" to json(locked),
            "blendMode" to json(blendMode.name),
            "flipHorizontal" to json(flipHorizontal), "flipVertical" to json(flipVertical)
        )
    }

    private fun decodeTransform(element: JsonElement): LayerTransform {
        val value = element.objectWith(
            "centerX", "centerY", "scale", "rotation", "alpha", "visible", "locked",
            "blendMode", "flipHorizontal", "flipVertical"
        )
        return LayerTransform(
            centerX = value.float("centerX"), centerY = value.float("centerY"),
            scale = value.float("scale").positive("Layer scale"),
            rotation = value.float("rotation"), alpha = value.float("alpha").unitRange("Layer alpha"),
            visible = value.boolean("visible"), locked = value.boolean("locked"),
            blendMode = enumValue<LayerBlendMode>(value.string("blendMode")),
            flipHorizontal = value.boolean("flipHorizontal"),
            flipVertical = value.boolean("flipVertical"),
        )
    }

    private fun encodeAdjustments(adjustments: BaseAdjustments) = with(adjustments) {
        obj(
            "brightness" to json(brightness), "contrast" to json(contrast),
            "saturation" to json(saturation), "exposure" to json(exposure),
            "temperature" to json(temperature), "tint" to json(tint)
        )
    }

    private fun decodeAdjustments(element: JsonElement): BaseAdjustments {
        val value = element.objectWith(
            "brightness", "contrast", "saturation", "exposure", "temperature", "tint"
        )
        fun channel(name: String) = value.int(name).also {
            if (it !in -100..100) invalid("Invalid $name adjustment")
        }
        return BaseAdjustments(
            brightness = channel("brightness"), contrast = channel("contrast"),
            saturation = channel("saturation"), exposure = channel("exposure"),
            temperature = channel("temperature"), tint = channel("tint")
        )
    }

    private fun encodeEffect(effect: LayerEffect) = obj(
        "filter" to encodeFilter(effect.filter), "strength" to json(effect.strength)
    )

    private fun decodeEffect(element: JsonElement): LayerEffect {
        val value = element.objectWith("filter", "strength")
        return LayerEffect(
            filter = decodeFilter(value.getValue("filter")),
            strength = value.float("strength").unitRange("Effect strength"),
        )
    }

    private fun encodeMask(mask: LayerMask) = obj(
        "rect" to nullable(mask.rect) {
            obj(
                "left" to json(it.left), "top" to json(it.top),
                "right" to json(it.right), "bottom" to json(it.bottom)
            )
        },
        "strokes" to array(mask.strokes) {
            obj(
                "points" to array(it.points, ::encodePoint),
                "widthRatio" to json(it.widthRatio), "erase" to json(it.erase)
            )
        },
        "inverted" to json(mask.inverted), "feather" to json(mask.feather)
    )

    private fun decodeMask(element: JsonElement): LayerMask {
        val value = element.objectWith("rect", "strokes", "inverted", "feather")
        return LayerMask(
            rect = value.optional("rect") {
                val rect = it.objectWith("left", "top", "right", "bottom")
                NormalizedRect(
                    rect.float("left"), rect.float("top"), rect.float("right"), rect.float("bottom")
                ).also { result ->
                    if (result.width <= 0f || result.height <= 0f) invalid("Invalid mask rect")
                }
            },
            strokes = value.list("strokes") {
                val stroke = it.objectWith("points", "widthRatio", "erase")
                MaskStroke(
                    points = stroke.list("points", ::decodePoint),
                    widthRatio = stroke.float("widthRatio").positive("Mask stroke width"),
                    erase = stroke.boolean("erase"),
                )
            },
            inverted = value.boolean("inverted"),
            feather = value.float("feather").nonnegative("Mask feather"),
        )
    }

    private fun encodePoint(point: StrokePoint) = obj("x" to json(point.x), "y" to json(point.y))

    private fun decodePoint(element: JsonElement): StrokePoint {
        val value = element.objectWith("x", "y")
        return StrokePoint(value.float("x"), value.float("y"))
    }

    private fun encodeBaseTransform(spec: BaseTransformSpec) = obj(
        "rotationSteps" to json(spec.rotationSteps),
        "freeRotation" to json(spec.freeRotation),
        "flipHorizontal" to json(spec.flipHorizontal),
        "flipVertical" to json(spec.flipVertical),
        "cropRect" to encodeNormalizedRect(spec.cropRect)
    )

    private fun decodeBaseTransform(element: JsonElement): BaseTransformSpec {
        val value = element.objectWith(
            "rotationSteps", "freeRotation", "flipHorizontal", "flipVertical", "cropRect"
        )
        return BaseTransformSpec(
            rotationSteps = value.int("rotationSteps").also {
                if (it !in 0..3) invalid("Invalid rotation steps")
            },
            freeRotation = value.float("freeRotation"),
            flipHorizontal = value.boolean("flipHorizontal"),
            flipVertical = value.boolean("flipVertical"),
            cropRect = decodeNormalizedRect(value.getValue("cropRect")),
        )
    }

    private fun encodeNormalizedRect(rect: NormalizedRect) = obj(
        "left" to json(rect.left), "top" to json(rect.top),
        "right" to json(rect.right), "bottom" to json(rect.bottom)
    )

    private fun decodeNormalizedRect(element: JsonElement): NormalizedRect {
        val value = element.objectWith("left", "top", "right", "bottom")
        return NormalizedRect(
            value.float("left"), value.float("top"),
            value.float("right"), value.float("bottom")
        ).also {
            if (it.width <= 0f || it.height <= 0f) invalid("Invalid normalized rect")
        }
    }

    private fun encodeFilter(filter: UiFilter<*>): JsonObject {
        val value = UiFilterCodec.encode(filter)
        return obj(
            "id" to json(value.id), "visible" to json(value.visible),
            "value" to encodeFilterValue(value.value)
        )
    }

    private fun decodeFilter(element: JsonElement): UiFilter<*> {
        val value = element.objectWith("id", "visible", "value")
        return UiFilterCodec.decode(EncodedUiFilter(
            id = value.string("id"), visible = value.boolean("visible"),
            value = decodeFilterValue(value.getValue("value"))
        ))
    }

    private fun encodeFilterValue(value: FilterValue): JsonObject = obj(
        "type" to json(value.type), "scalar" to nullable(value.scalar, ::json),
        "items" to array(value.items, ::encodeFilterValue),
        "fields" to JsonObject(value.fields.mapValues { encodeFilterValue(it.value) })
    )

    private fun decodeFilterValue(element: JsonElement, depth: Int = 0): FilterValue {
        if (depth > 64) invalid("Filter value nesting is too deep")
        val value = element.objectWith("type", "scalar", "items", "fields")
        return FilterValue(
            type = value.string("type"),
            scalar = value.optional("scalar") { it.stringValue() },
            items = value.list("items") { decodeFilterValue(it, depth + 1) },
            fields = value.getValue("fields").asObject().mapValues {
                decodeFilterValue(it.value, depth + 1)
            },
        )
    }

    private val formats: Map<String, ImageFormat> = mapOf(
        "jpg" to ImageFormat.Jpg, "jpeg" to ImageFormat.Jpeg,
        "moz-jpeg" to ImageFormat.MozJpeg, "jpegli" to ImageFormat.Jpegli,
        "png-lossless" to ImageFormat.Png.Lossless, "png-lossy" to ImageFormat.Png.Lossy,
        "webp-lossless" to ImageFormat.Webp.Lossless, "webp-lossy" to ImageFormat.Webp.Lossy,
        "bmp" to ImageFormat.Bmp, "jp2" to ImageFormat.Jpeg2000.Jp2,
        "j2k" to ImageFormat.Jpeg2000.J2k, "tiff" to ImageFormat.Tiff,
        "tif" to ImageFormat.Tif, "qoi" to ImageFormat.Qoi,
        "ico" to ImageFormat.Ico, "gif" to ImageFormat.Gif
    )

    private fun encodeExport(settings: ExportSettings) = with(settings) {
        obj(
            "format" to json(formats.entries.firstOrNull { it.value == format }?.key
                ?: invalid("Unsupported export format")),
            "quality" to json(quality), "resolution" to json(resolution.name),
            "customWidth" to json(customWidth), "customHeight" to json(customHeight),
            "shareAfterSave" to json(shareAfterSave), "saveCopy" to json(saveCopy),
            "jpegBackgroundColor" to json(jpegBackgroundColor),
            "maxFileSizeBytes" to nullable(maxFileSizeBytes, ::json)
        )
    }

    private fun decodeExport(element: JsonElement): ExportSettings {
        val value = element.objectWith(
            "format", "quality", "resolution", "customWidth", "customHeight", "shareAfterSave",
            "saveCopy", "jpegBackgroundColor", "maxFileSizeBytes"
        )
        val settings = ExportSettings(
            format = formats[value.string("format")] ?: invalid("Unknown export format"),
            quality = value.int("quality"),
            resolution = enumValue<ExportResolution>(value.string("resolution")),
            customWidth = value.int("customWidth"), customHeight = value.int("customHeight"),
            shareAfterSave = value.boolean("shareAfterSave"),
            saveCopy = value.boolean("saveCopy"),
            jpegBackgroundColor = value.int("jpegBackgroundColor"),
            maxFileSizeBytes = value.optional("maxFileSizeBytes") {
                it.primitive().let { number ->
                    if (number.isString) invalid("Expected a file size integer")
                    number.longOrNull ?: invalid("Invalid file size")
                }
            },
        )
        if (settings.quality !in ExportSettings.MIN_QUALITY..100 ||
            settings.customWidth < 0 || settings.customHeight < 0 ||
            (settings.resolution == ExportResolution.Custom &&
                (settings.customWidth == 0 || settings.customHeight == 0)) ||
            (settings.maxFileSizeBytes != null &&
                settings.maxFileSizeBytes < ExportSettings.MIN_FILE_SIZE_BYTES) ||
            (settings.format == ImageFormat.Jpg && settings.jpegBackgroundColor ushr 24 != 255)
        ) {
            invalid("Invalid export settings")
        }
        return settings
    }

    private fun decodeBackground(element: JsonElement): CanvasBackground {
        val value = element.asObject()
        return when (value.string("type")) {
            "default" -> {
                value.objectWith("type")
                CanvasBackground.Default
            }
            "solid" -> {
                value.objectWith("type", "color")
                CanvasBackground.Solid(value.int("color"))
            }
            else -> invalid("Unknown workspace background")
        }
    }
}

private fun encodeStyleSpan(span: RichTextStyleSpan): JsonObject = obj(
    "start" to json(span.start),
    "end" to json(span.end),
    "color" to nullable(span.color, ::json),
    "bold" to nullable(span.bold, ::json),
    "italic" to nullable(span.italic, ::json),
    "sizeScale" to nullable(span.sizeScale, ::json),
)

private fun decodeStyleSpan(element: JsonElement): RichTextStyleSpan {
    val obj = element.asObject()
    val allowed = setOf("start", "end", "color", "bold", "italic", "sizeScale")
    if (obj.keys.any { it !in allowed } || "start" !in obj || "end" !in obj) {
        invalid("Invalid text style span")
    }
    val start = obj.int("start")
    val end = obj.int("end")
    if (start < 0 || end <= start) invalid("Invalid text style span range")
    val color = obj["color"]?.let {
        if (it == JsonNull) null else it.primitive().let { p ->
            if (p.isString) invalid("Invalid text style color")
            p.longOrNull ?: invalid("Invalid text style color")
        }
    }
    val bold = obj["bold"]?.let {
        if (it == JsonNull) null else it.primitive().let { p ->
            if (p.isString) invalid("Invalid text style bold")
            p.booleanOrNull ?: invalid("Invalid text style bold")
        }
    }
    val italic = obj["italic"]?.let {
        if (it == JsonNull) null else it.primitive().let { p ->
            if (p.isString) invalid("Invalid text style italic")
            p.booleanOrNull ?: invalid("Invalid text style italic")
        }
    }
    val sizeScale = obj["sizeScale"]?.let {
        if (it == JsonNull) null else it.floatValue().also { scale ->
            if (scale <= 0f) invalid("Invalid text style size")
        }
    }
    return RichTextStyleSpan(
        start = start,
        end = end,
        color = color,
        bold = bold,
        italic = italic,
        sizeScale = sizeScale
    )
}

internal data class StoredMarkupProject(
    val summary: MarkupProjectSummary,
    val document: MarkupDocument,
)

internal object MarkupProjectCodec {

    fun encode(project: StoredMarkupProject): String = with(project.summary) {
        checkProjectId(id)
        obj(
            "schemaVersion" to json(MarkupDocumentCodec.SchemaVersion),
            "id" to json(id), "name" to json(name), "updatedAt" to json(updatedAt),
            "thumbnailPath" to nullable(thumbnailPath, ::json), "layerCount" to json(layerCount),
            "document" to MarkupDocumentCodec.encodeDocument(project.document)
        ).toString()
    }

    fun decode(source: String): StoredMarkupProject = formatErrors {
        val value = parse(source).objectWith(
            "schemaVersion", "id", "name", "updatedAt", "thumbnailPath", "layerCount", "document"
        )
        checkSchema(value.int("schemaVersion"))
        val document = MarkupDocumentCodec.decodeDocument(value.getValue("document"))
        val summary = MarkupProjectSummary(
            id = value.string("id").also(::checkProjectId),
            name = value.string("name"), updatedAt = value.long("updatedAt"),
            thumbnailPath = value.optional("thumbnailPath") { it.stringValue() },
            layerCount = value.int("layerCount"),
        )
        if (summary.updatedAt < 0 || summary.layerCount != document.layers.size) {
            invalid("Invalid project summary")
        }
        StoredMarkupProject(summary, document)
    }
}

internal fun checkProjectId(id: String) {
    if (!id.matches(Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,79}"))) invalid("Unsafe project ID")
}

internal fun checkAssetPath(path: String) {
    if (path.isBlank() || path.startsWith('/') || '\\' in path ||
        path.split('/').any { it.isEmpty() || it == "." || it == ".." } || '\u0000' in path
    ) {
        invalid("Unsafe bundled asset path")
    }
}

private fun checkSchema(version: Int) {
    if (version != MarkupDocumentCodec.SchemaVersion) {
        invalid("Unsupported project schema version: $version")
    }
}

private fun parse(source: String): JsonElement = try {
    Json.parseToJsonElement(source)
} catch (exception: SerializationException) {
    throw MarkupProjectFormatException("Malformed project JSON", exception)
}

private inline fun <T> formatErrors(block: () -> T): T = try {
    block()
} catch (exception: IllegalArgumentException) {
    throw MarkupProjectFormatException("Invalid project data: ${exception.message}", exception)
}

private fun obj(vararg fields: Pair<String, JsonElement>) = JsonObject(mapOf(*fields))
private fun json(value: String) = JsonPrimitive(value)
private fun json(value: Int) = JsonPrimitive(value)
private fun json(value: Long) = JsonPrimitive(value)
private fun json(value: Boolean) = JsonPrimitive(value)
private fun json(value: Float): JsonPrimitive {
    if (!value.isFinite()) invalid("Non-finite document coordinate")
    return JsonPrimitive(value)
}
private fun <T> nullable(value: T?, encode: (T) -> JsonElement): JsonElement =
    value?.let(encode) ?: JsonNull
private fun <T> array(values: List<T>, encode: (T) -> JsonElement) = JsonArray(values.map(encode))
private fun JsonElement.asObject(): JsonObject = this as? JsonObject
    ?: invalid("Expected a JSON object")
private fun JsonElement.objectWith(vararg fields: String): JsonObject = asObject().also {
    if (it.keys != fields.toSet()) invalid("Missing or unknown fields: ${fields.toList()}")
}
private fun JsonElement.objectWithOptional(required: Array<String>, optional: Set<String>): JsonObject =
    asObject().also {
        if (!required.all { f -> f in it.keys } || !(required.toSet() + optional).containsAll(it.keys)) {
            invalid("Missing or unknown fields: ${it.keys.toList()}")
        }
    }
private fun JsonElement.primitive(): JsonPrimitive = this as? JsonPrimitive
    ?: invalid("Expected a JSON scalar")
private fun JsonElement.stringValue(): String = primitive().let {
    if (!it.isString) invalid("Expected a string")
    it.content
}
private fun JsonElement.floatValue(): Float = primitive().let {
    if (it.isString) invalid("Expected a number")
    it.floatOrNull?.takeIf(Float::isFinite) ?: invalid("Invalid number")
}
private fun JsonObject.string(name: String): String =
    (this[name] ?: invalid("Missing field $name")).stringValue()
private fun JsonObject.int(name: String): Int = (this[name] ?: invalid("Missing field $name"))
    .primitive().let {
        if (it.isString) invalid("Expected an integer")
        it.intOrNull ?: invalid("Invalid integer")
    }
private fun JsonObject.long(name: String): Long = (this[name] ?: invalid("Missing field $name"))
    .primitive().let {
        if (it.isString) invalid("Expected an integer")
        it.longOrNull ?: invalid("Invalid integer")
    }
private fun JsonObject.float(name: String): Float =
    (this[name] ?: invalid("Missing field $name")).floatValue()
private fun JsonObject.boolean(name: String): Boolean =
    (this[name] ?: invalid("Missing field $name")).primitive().let {
        if (it.isString) invalid("Expected a boolean")
        it.booleanOrNull ?: invalid("Invalid boolean")
    }
private fun <T> JsonObject.list(name: String, decode: (JsonElement) -> T): List<T> =
    ((this[name] ?: invalid("Missing field $name")) as? JsonArray
        ?: invalid("Expected an array")).map(decode)
private fun <T> JsonObject.optional(name: String, decode: (JsonElement) -> T): T? =
    (this[name] ?: invalid("Missing field $name")).let { if (it == JsonNull) null else decode(it) }
private inline fun <reified T : Enum<T>> enumValue(name: String): T =
    enumValues<T>().firstOrNull { it.name == name } ?: invalid("Unknown enum value: $name")
private fun Float.positive(name: String): Float = also {
    if (it <= 0f) invalid("$name must be positive")
}
private fun Float.nonnegative(name: String): Float = also {
    if (it < 0f) invalid("$name must be nonnegative")
}
private fun Float.unitRange(name: String): Float = also {
    if (it !in 0f..1f) invalid("$name must be between 0 and 1")
}
private fun invalid(message: String): Nothing = throw MarkupProjectFormatException(message)
