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

package com.wanbaohe.textcard.data.project

import com.shifenmiao.common.components.text.RichTextStyleSpan
import com.shifenmiao.model.editor.BrushType
import com.shifenmiao.model.editor.DrawStroke
import com.shifenmiao.model.editor.LayerBlendMode
import com.shifenmiao.model.editor.LayerTransform
import com.shifenmiao.model.editor.ShapeKind
import com.shifenmiao.model.editor.ShapeSpec
import com.shifenmiao.model.editor.StrokePoint
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.textcard.domain.StoredTextCardProject
import com.wanbaohe.textcard.domain.TextCardProjectSummary
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.MeshPoint
import com.wanbaohe.textcard.domain.model.PaperKind
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardAnchorAffinity
import com.wanbaohe.textcard.domain.model.TextCardAnnotation
import com.wanbaohe.textcard.domain.model.TextCardAnnotationType
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardExportFormat
import com.wanbaohe.textcard.domain.model.TextCardFontSpan
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardSourceAnchor
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

class TextCardProjectFormatException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

internal object TextCardDocumentCodec {

    const val SchemaVersion: Int = 2

    fun encode(document: TextCardDocument): String = obj(
        "schemaVersion" to json(SchemaVersion),
        "document" to encodeDocument(document),
    ).toString()

    fun decode(source: String): TextCardDocument = formatErrors {
        val root = parse(source).objectWith("schemaVersion", "document")
        checkSchema(root.int("schemaVersion"))
        decodeDocument(root.getValue("document"))
    }

    internal fun encodeDocument(document: TextCardDocument): JsonObject {
        val result = obj(
            "title" to encodeTextBlock(document.title),
            "body" to encodeTextBlock(document.body),
            "author" to encodeTextBlock(document.author),
            "source" to encodeTextBlock(document.source),
            "canvas" to encodeCanvas(document.canvas),
            "background" to encodeBackground(document.background),
            "backgroundOpacity" to json(document.backgroundOpacity),
            "templateId" to json(document.templateId),
            "showPageNumber" to json(document.showPageNumber),
            "outputMode" to json(document.outputMode.name),
            "exportFormat" to json(document.exportFormat.name),
            "exportQuality" to json(document.exportQuality),
            "annotations" to array(document.annotations, ::encodeAnnotation),
        )
        formatErrors { decodeDocument(result) }
        return result
    }

    internal fun decodeDocument(element: JsonElement): TextCardDocument {
        val value = element.objectWithOptional(
            required = arrayOf(
                "title", "body", "author", "source", "canvas", "background",
                "backgroundOpacity", "templateId", "showPageNumber", "outputMode",
                "exportFormat", "exportQuality"
            ),
            optional = setOf("shareAfterSave", "annotations")
        )
        return TextCardDocument(
            title = decodeTextBlock(value.getValue("title")),
            body = decodeTextBlock(value.getValue("body")),
            author = decodeTextBlock(value.getValue("author")),
            source = decodeTextBlock(value.getValue("source")),
            canvas = decodeCanvas(value.getValue("canvas")),
            background = decodeBackground(value.getValue("background")),
            backgroundOpacity = value.float("backgroundOpacity").unitRange("Background opacity"),
            templateId = value.string("templateId"),
            showPageNumber = value.boolean("showPageNumber"),
            outputMode = enumValue(value.string("outputMode")),
            exportFormat = enumValue(value.string("exportFormat")),
            exportQuality = value.int("exportQuality").also {
                if (it !in 0..100) invalid("Export quality must be between 0 and 100")
            },
            annotations = value["annotations"]?.array(::decodeAnnotation).orEmpty(),
        )
    }

    private fun encodeTextBlock(block: TextBlock): JsonObject = with(block) {
        obj(
            "id" to json(id),
            "content" to json(content),
            "baseSizeRatio" to json(baseSizeRatio),
            "baseTopRatio" to json(baseTopRatio),
            "font" to nullable(font, ::encodeFont),
            "sizeScale" to json(sizeScale),
            "widthRatio" to json(widthRatio),
            "widthManuallySet" to json(widthManuallySet),
            "heightRatio" to json(heightRatio),
            "letterSpacingEm" to json(letterSpacingEm),
            "lineSpacingMultiplier" to json(lineSpacingMultiplier),
            "alignment" to json(alignment.name),
            "isBold" to json(isBold),
            "isItalic" to json(isItalic),
            "color" to json(color),
            "alpha" to json(alpha),
            "isUnderline" to json(isUnderline),
            "isStrikethrough" to json(isStrikethrough),
            "backgroundColor" to json(backgroundColor),
            "outlineColor" to json(outlineColor),
            "outlineWidthRatio" to json(outlineWidthRatio),
            "styleSpans" to array(styleSpans, ::encodeStyleSpan),
            "fontSpans" to array(fontSpans, ::encodeFontSpan),
            "offsetX" to json(offsetX),
            "offsetY" to json(offsetY),
            "scale" to json(scale),
            "rotation" to json(rotation),
        )
    }

    private fun decodeTextBlock(element: JsonElement): TextBlock {
        val value = element.objectWithOptional(
            required = arrayOf(
                "id", "content", "baseSizeRatio", "baseTopRatio", "font", "sizeScale",
                "widthRatio", "widthManuallySet", "heightRatio", "letterSpacingEm",
                "lineSpacingMultiplier", "alignment", "isBold", "isItalic", "color", "alpha",
                "isUnderline", "isStrikethrough", "backgroundColor", "outlineColor",
                "outlineWidthRatio", "styleSpans", "offsetX", "offsetY", "scale", "rotation"
            ),
            optional = setOf("fontSpans")
        )
        return TextBlock(
            id = value.string("id").also { if (it.isBlank()) invalid("Blank text block ID") },
            content = value.string("content"),
            baseSizeRatio = value.float("baseSizeRatio").positive("Base text size"),
            baseTopRatio = value.float("baseTopRatio"),
            font = value.optional("font", ::decodeFont),
            sizeScale = value.float("sizeScale").positive("Text size scale"),
            widthRatio = value.float("widthRatio").positive("Text width"),
            widthManuallySet = value.boolean("widthManuallySet"),
            heightRatio = value.float("heightRatio").nonnegative("Text height"),
            letterSpacingEm = value.float("letterSpacingEm"),
            lineSpacingMultiplier = value.float("lineSpacingMultiplier").positive("Line spacing"),
            alignment = enumValue(value.string("alignment")),
            isBold = value.boolean("isBold"),
            isItalic = value.boolean("isItalic"),
            color = value.long("color"),
            alpha = value.float("alpha").unitRange("Text alpha"),
            isUnderline = value.boolean("isUnderline"),
            isStrikethrough = value.boolean("isStrikethrough"),
            backgroundColor = value.long("backgroundColor"),
            outlineColor = value.long("outlineColor"),
            outlineWidthRatio = value.float("outlineWidthRatio").nonnegative("Outline width"),
            styleSpans = value.list("styleSpans", ::decodeStyleSpan),
            fontSpans = value["fontSpans"]?.array(::decodeFontSpan).orEmpty(),
            offsetX = value.float("offsetX"),
            offsetY = value.float("offsetY"),
            scale = value.float("scale").positive("Text scale"),
            rotation = value.float("rotation"),
        ).also { block ->
            block.styleSpans.forEach { span ->
                if (span.end > block.content.length) invalid("Text style span exceeds its content")
            }
            block.fontSpans.forEach { span ->
                if (span.end > block.content.length) invalid("Font span exceeds its content")
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
        val value = element.objectWith("start", "end", "color", "bold", "italic", "sizeScale")
        return RichTextStyleSpan(
            start = value.int("start"),
            end = value.int("end"),
            color = value.optional("color") { it.longValue() },
            bold = value.optional("bold") { it.booleanValue() },
            italic = value.optional("italic") { it.booleanValue() },
            sizeScale = value.optional("sizeScale") {
                it.floatValue().positive("Text style size scale")
            },
        ).also {
            if (it.start < 0 || it.end <= it.start) invalid("Invalid text style span range")
        }
    }

    private fun encodeFontSpan(span: TextCardFontSpan): JsonObject = obj(
        "start" to json(span.start),
        "end" to json(span.end),
        "font" to nullable(span.font, ::encodeFont),
    )

    private fun decodeFontSpan(element: JsonElement): TextCardFontSpan {
        val value = element.objectWith("start", "end", "font")
        return TextCardFontSpan(
            start = value.int("start"),
            end = value.int("end"),
            font = value.optional("font", ::decodeFont)
        ).also {
            if (it.start < 0 || it.end <= it.start) invalid("Invalid font span range")
        }
    }

    private fun encodeFont(font: FontType): JsonObject = when (font) {
        is FontType.Resource -> obj(
            "type" to json("resource"),
            "resId" to json(font.resId),
        )
        is FontType.File -> obj(
            "type" to json("file"),
            "path" to json(font.path),
        )
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

    private fun encodeAnnotation(annotation: TextCardAnnotation): JsonObject = obj(
        "id" to json(annotation.id),
        "pageAnchor" to encodeAnchor(annotation.pageAnchor),
        "pageEndAnchor" to encodeAnchor(annotation.pageEndAnchor),
        "transform" to encodeTransform(annotation.transform),
        "type" to when (val type = annotation.type) {
            is TextCardAnnotationType.Text -> obj(
                "kind" to json("text"),
                "block" to encodeTextBlock(type.block),
            )
            is TextCardAnnotationType.Draw -> obj(
                "kind" to json("draw"),
                "strokes" to array(type.strokes, ::encodeStroke),
            )
            is TextCardAnnotationType.Shape -> obj(
                "kind" to json("shape"),
                "spec" to encodeShape(type.spec),
            )
        },
    )

    private fun decodeAnnotation(element: JsonElement): TextCardAnnotation {
        val value = element.objectWith("id", "pageAnchor", "pageEndAnchor", "transform", "type")
        val typeElement = value.getValue("type")
        val type = when (typeElement.asObject().string("kind")) {
            "text" -> TextCardAnnotationType.Text(
                decodeTextBlock(typeElement.asObject().also {
                    it.objectWith("kind", "block")
                }.getValue("block"))
            )
            "draw" -> TextCardAnnotationType.Draw(
                typeElement.asObject().also {
                    it.objectWith("kind", "strokes")
                }.list("strokes", ::decodeStroke).also {
                    if (it.isEmpty()) invalid("Draw annotation must contain strokes")
                }
            )
            "shape" -> TextCardAnnotationType.Shape(
                decodeShape(typeElement.asObject().also {
                    it.objectWith("kind", "spec")
                }.getValue("spec"))
            )
            else -> invalid("Unknown annotation type")
        }
        return TextCardAnnotation(
            id = value.string("id").also { if (it.isBlank()) invalid("Blank annotation ID") },
            pageAnchor = decodeAnchor(value.getValue("pageAnchor")),
            pageEndAnchor = decodeAnchor(value.getValue("pageEndAnchor")),
            transform = decodeTransform(value.getValue("transform")),
            type = type,
        )
    }

    private fun encodeAnchor(anchor: TextCardSourceAnchor): JsonObject = obj(
        "role" to json(anchor.role.name),
        "offset" to json(anchor.offset),
        "affinity" to json(anchor.affinity.name),
    )

    private fun decodeAnchor(element: JsonElement): TextCardSourceAnchor {
        val value = element.objectWith("role", "offset", "affinity")
        return TextCardSourceAnchor(
            role = enumValue<TextCardRole>(value.string("role")),
            offset = value.int("offset").also { if (it < 0) invalid("Negative anchor offset") },
            affinity = enumValue<TextCardAnchorAffinity>(value.string("affinity")),
        )
    }

    private fun encodeTransform(transform: LayerTransform): JsonObject = obj(
        "centerX" to json(transform.centerX),
        "centerY" to json(transform.centerY),
        "scale" to json(transform.scale),
        "rotation" to json(transform.rotation),
        "alpha" to json(transform.alpha),
        "visible" to json(transform.visible),
        "locked" to json(transform.locked),
        "blendMode" to json(transform.blendMode.name),
        "flipHorizontal" to json(transform.flipHorizontal),
        "flipVertical" to json(transform.flipVertical),
    )

    private fun decodeTransform(element: JsonElement): LayerTransform {
        val value = element.objectWith(
            "centerX", "centerY", "scale", "rotation", "alpha", "visible", "locked",
            "blendMode", "flipHorizontal", "flipVertical"
        )
        return LayerTransform(
            centerX = value.float("centerX"),
            centerY = value.float("centerY"),
            scale = value.float("scale").positive("Annotation scale"),
            rotation = value.float("rotation"),
            alpha = value.float("alpha").unitRange("Annotation opacity"),
            visible = value.boolean("visible"),
            locked = value.boolean("locked"),
            blendMode = enumValue<LayerBlendMode>(value.string("blendMode")),
            flipHorizontal = value.boolean("flipHorizontal"),
            flipVertical = value.boolean("flipVertical"),
        )
    }

    private fun encodeStroke(stroke: DrawStroke): JsonObject = obj(
        "points" to array(stroke.points, ::encodePoint),
        "color" to json(stroke.color.toLong()),
        "widthRatio" to json(stroke.widthRatio),
        "brush" to json(stroke.brush.name),
        "alpha" to json(stroke.alpha),
    )

    private fun decodeStroke(element: JsonElement): DrawStroke {
        val value = element.objectWith("points", "color", "widthRatio", "brush", "alpha")
        return DrawStroke(
            points = value.list("points", ::decodePoint).also {
                if (it.isEmpty()) invalid("Stroke must contain points")
            },
            color = value.long("color").toCheckedColor(),
            widthRatio = value.float("widthRatio").positive("Stroke width"),
            brush = enumValue<BrushType>(value.string("brush")),
            alpha = value.float("alpha").unitRange("Stroke opacity"),
        )
    }

    private fun encodePoint(point: StrokePoint): JsonObject = obj(
        "x" to json(point.x),
        "y" to json(point.y),
    )

    private fun decodePoint(element: JsonElement): StrokePoint {
        val value = element.objectWith("x", "y")
        return StrokePoint(x = value.float("x"), y = value.float("y"))
    }

    private fun encodeShape(spec: ShapeSpec): JsonObject = obj(
        "kind" to json(spec.kind.name),
        "color" to json(spec.color.toLong()),
        "filled" to json(spec.filled),
        "widthRatio" to json(spec.widthRatio),
        "heightRatio" to json(spec.heightRatio),
        "cornerRadiusRatio" to json(spec.cornerRadiusRatio),
        "strokeWidthRatio" to json(spec.strokeWidthRatio),
    )

    private fun decodeShape(element: JsonElement): ShapeSpec {
        val value = element.objectWith(
            "kind", "color", "filled", "widthRatio", "heightRatio",
            "cornerRadiusRatio", "strokeWidthRatio"
        )
        return ShapeSpec(
            kind = enumValue<ShapeKind>(value.string("kind")),
            color = value.long("color").toCheckedColor(),
            filled = value.boolean("filled"),
            widthRatio = value.float("widthRatio").positive("Shape width"),
            heightRatio = value.float("heightRatio").positive("Shape height"),
            cornerRadiusRatio = value.float("cornerRadiusRatio").nonnegative("Corner radius"),
            strokeWidthRatio = value.float("strokeWidthRatio").positive("Shape stroke width"),
        )
    }

    private fun Long.toCheckedColor(): Int {
        if (this !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) invalid("Invalid color")
        return toInt()
    }

    private fun encodeCanvas(canvas: CanvasSpec): JsonObject = when (canvas) {
        CanvasSpec.Portrait -> obj("type" to json("portrait"))
        CanvasSpec.Square -> obj("type" to json("square"))
        CanvasSpec.Screen -> obj("type" to json("screen"))
        is CanvasSpec.Custom -> obj(
            "type" to json("custom"),
            "width" to json(canvas.customWidth),
            "height" to json(canvas.customHeight),
        )
    }

    private fun decodeCanvas(element: JsonElement): CanvasSpec {
        val value = element.asObject()
        return when (value.string("type")) {
            "portrait" -> {
                value.objectWith("type")
                CanvasSpec.Portrait
            }
            "square" -> {
                value.objectWith("type")
                CanvasSpec.Square
            }
            "screen" -> {
                value.objectWith("type")
                CanvasSpec.Screen
            }
            "custom" -> {
                value.objectWith("type", "width", "height")
                CanvasSpec.Custom(
                    customWidth = value.int("width").also {
                        if (it <= 0) invalid("Canvas width must be positive")
                    },
                    customHeight = value.int("height").also {
                        if (it <= 0) invalid("Canvas height must be positive")
                    },
                )
            }
            else -> invalid("Unknown canvas type")
        }
    }

    private fun encodeBackground(background: BackgroundSpec): JsonObject = when (background) {
        BackgroundSpec.None -> obj("type" to json("none"))
        is BackgroundSpec.Paper -> obj(
            "type" to json("paper"),
            "kind" to json(background.kind.name),
        )
        is BackgroundSpec.Gradient -> obj(
            "type" to json("gradient"),
            "points" to array(background.points) { row -> array(row, ::encodeMeshPoint) },
        )
        is BackgroundSpec.Image -> obj(
            "type" to json("image"),
            "uri" to json(background.uri),
            "offsetX" to json(background.offsetX),
            "offsetY" to json(background.offsetY),
        )
    }

    private fun decodeBackground(element: JsonElement): BackgroundSpec {
        val value = element.asObject()
        return when (value.string("type")) {
            "none" -> {
                value.objectWith("type")
                BackgroundSpec.None
            }
            "paper" -> {
                value.objectWith("type", "kind")
                BackgroundSpec.Paper(enumValue<PaperKind>(value.string("kind")))
            }
            "gradient" -> {
                value.objectWith("type", "points")
                BackgroundSpec.Gradient(
                    points = value.list("points") { row -> row.array(::decodeMeshPoint) }
                ).also {
                    if (it.points.size != 3 || it.points.any { row -> row.size != 3 }) {
                        invalid("Gradient background must contain a 3 by 3 mesh")
                    }
                }
            }
            "image" -> {
                value.objectWith("type", "uri", "offsetX", "offsetY")
                BackgroundSpec.Image(
                    uri = value.string("uri").also { if (it.isBlank()) invalid("Blank image URI") },
                    offsetX = value.float("offsetX"),
                    offsetY = value.float("offsetY"),
                )
            }
            else -> invalid("Unknown background type")
        }
    }

    private fun encodeMeshPoint(point: MeshPoint): JsonObject = obj(
        "offsetX" to json(point.offsetX),
        "offsetY" to json(point.offsetY),
        "argb" to json(point.argb),
    )

    private fun decodeMeshPoint(element: JsonElement): MeshPoint {
        val value = element.objectWith("offsetX", "offsetY", "argb")
        return MeshPoint(
            offsetX = value.float("offsetX"),
            offsetY = value.float("offsetY"),
            argb = value.long("argb"),
        )
    }
}

internal object TextCardProjectCodec {

    fun encode(project: StoredTextCardProject): String = with(project.summary) {
        checkProjectId(id)
        obj(
            "schemaVersion" to json(TextCardDocumentCodec.SchemaVersion),
            "id" to json(id),
            "name" to json(name),
            "updatedAt" to json(updatedAt),
            "thumbnailPath" to nullable(thumbnailPath, ::json),
            "pageCount" to json(pageCount),
            "document" to TextCardDocumentCodec.encodeDocument(project.document),
        ).toString()
    }

    fun decode(source: String): StoredTextCardProject = formatErrors {
        val value = parse(source).objectWith(
            "schemaVersion", "id", "name", "updatedAt", "thumbnailPath", "pageCount", "document"
        )
        checkSchema(value.int("schemaVersion"))
        val document = TextCardDocumentCodec.decodeDocument(value.getValue("document"))
        val summary = TextCardProjectSummary(
            id = value.string("id").also(::checkProjectId),
            name = value.string("name"),
            updatedAt = value.long("updatedAt"),
            thumbnailPath = value.optional("thumbnailPath") { it.stringValue() },
            pageCount = value.int("pageCount"),
        )
        if (summary.updatedAt < 0 || summary.pageCount < 0) invalid("Invalid project summary")
        StoredTextCardProject(summary = summary, document = document)
    }
}

internal fun checkProjectId(id: String) {
    if (!id.matches(ProjectIdPattern)) invalid("Unsafe project ID")
}

private val ProjectIdPattern = Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,79}")

private fun checkSchema(version: Int) {
    if (version !in 1..TextCardDocumentCodec.SchemaVersion) {
        invalid("Unsupported project schema version: $version")
    }
}

private fun parse(source: String): JsonElement = try {
    Json.parseToJsonElement(source)
} catch (exception: SerializationException) {
    throw TextCardProjectFormatException("Malformed project JSON", exception)
}

private inline fun <T> formatErrors(block: () -> T): T = try {
    block()
} catch (exception: IllegalArgumentException) {
    throw TextCardProjectFormatException("Invalid project data: ${exception.message}", exception)
}

private fun obj(vararg fields: Pair<String, JsonElement>) = JsonObject(mapOf(*fields))
private fun json(value: String) = JsonPrimitive(value)
private fun json(value: Int) = JsonPrimitive(value)
private fun json(value: Long) = JsonPrimitive(value)
private fun json(value: Boolean) = JsonPrimitive(value)
private fun json(value: Float): JsonPrimitive {
    if (!value.isFinite()) invalid("Non-finite document value")
    return JsonPrimitive(value)
}
private fun <T> nullable(value: T?, encode: (T) -> JsonElement): JsonElement =
    value?.let(encode) ?: JsonNull
private fun <T> array(values: List<T>, encode: (T) -> JsonElement) = JsonArray(values.map(encode))
private fun JsonElement.asObject(): JsonObject = this as? JsonObject
    ?: invalid("Expected a JSON object")
private fun JsonElement.objectWith(vararg fields: String): JsonObject = asObject().also {
    if (it.keys != fields.toSet()) invalid("Missing or unknown fields: ${it.keys.toList()}")
}
private fun JsonElement.objectWithOptional(
    required: Array<String>,
    optional: Set<String>,
): JsonObject = asObject().also {
    val missing = required.filterNot(it.keys::contains)
    val unknown = it.keys - required.toSet() - optional
    if (missing.isNotEmpty() || unknown.isNotEmpty()) {
        invalid("Missing or unknown fields: required=$required, optional=$optional, actual=${it.keys.toList()}")
    }
}
private fun JsonElement.primitive(): JsonPrimitive = this as? JsonPrimitive
    ?: invalid("Expected a JSON scalar")
private fun JsonElement.stringValue(): String = primitive().let {
    if (!it.isString) invalid("Expected a string")
    it.content
}
private fun JsonElement.longValue(): Long = primitive().let {
    if (it.isString) invalid("Expected an integer")
    it.longOrNull ?: invalid("Invalid integer")
}
private fun JsonElement.floatValue(): Float = primitive().let {
    if (it.isString) invalid("Expected a number")
    it.floatOrNull?.takeIf(Float::isFinite) ?: invalid("Invalid number")
}
private fun JsonElement.booleanValue(): Boolean = primitive().let {
    if (it.isString) invalid("Expected a boolean")
    it.booleanOrNull ?: invalid("Invalid boolean")
}
private fun <T> JsonElement.array(decode: (JsonElement) -> T): List<T> =
    (this as? JsonArray ?: invalid("Expected an array")).map(decode)
private fun JsonObject.string(name: String): String =
    (this[name] ?: invalid("Missing field $name")).stringValue()
private fun JsonObject.int(name: String): Int = (this[name] ?: invalid("Missing field $name"))
    .primitive().let {
        if (it.isString) invalid("Expected an integer")
        it.intOrNull ?: invalid("Invalid integer")
    }
private fun JsonObject.long(name: String): Long =
    (this[name] ?: invalid("Missing field $name")).longValue()
private fun JsonObject.float(name: String): Float =
    (this[name] ?: invalid("Missing field $name")).floatValue()
private fun JsonObject.boolean(name: String): Boolean =
    (this[name] ?: invalid("Missing field $name")).booleanValue()
private fun <T> JsonObject.list(name: String, decode: (JsonElement) -> T): List<T> =
    (this[name] ?: invalid("Missing field $name")).array(decode)
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
private fun invalid(message: String): Nothing = throw TextCardProjectFormatException(message)
