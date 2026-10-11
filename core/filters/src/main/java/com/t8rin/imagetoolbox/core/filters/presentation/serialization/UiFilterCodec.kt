package com.t8rin.imagetoolbox.core.filters.presentation.serialization

import android.net.Uri
import com.t8rin.imagetoolbox.core.domain.model.ColorModel
import com.t8rin.imagetoolbox.core.domain.model.FileModel
import com.t8rin.imagetoolbox.core.domain.model.ImageModel
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.utils.Quad
import com.t8rin.imagetoolbox.core.filters.domain.model.FilterValueWrapper
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.BlurEdgeMode
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.FadeSide
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.MirrorSide
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.PaletteTransferSpace
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.PolarCoordinatesType
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.PopArtBlendingMode
import com.t8rin.imagetoolbox.core.filters.domain.model.enums.TransferFunc
import com.t8rin.imagetoolbox.core.filters.domain.model.params.ArcParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.AsciiParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.BilaterialBlurParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.ChannelMixParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.ClaheParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.CropOrPerspectiveParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.EnhancedZoomBlurParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.GlitchParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.KaleidoscopeParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.LinearGaussianParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.LinearTiltShiftParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.PinchParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.RadialTiltShiftParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.RubberStampParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.SideFadeParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.SmearParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.SparkleParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.ToneCurvesParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.VoronoiCrystallizeParams
import com.t8rin.imagetoolbox.core.filters.domain.model.params.WaterParams
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.durableFilterClasses
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

data class EncodedUiFilter(
    val id: String,
    val visible: Boolean,
    val value: FilterValue,
)

/** Format-independent, explicitly tagged values. No class name is ever loaded from disk. */
data class FilterValue(
    val type: String,
    val scalar: String? = null,
    val items: List<FilterValue> = emptyList(),
    val fields: Map<String, FilterValue> = emptyMap(),
)

class FilterCodecException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

/**
 * A strict, reusable codec for the UI filter catalogue. Reflection is limited to class literals
 * in the catalogue and the parameter allowlist, and constructor types are checked recursively.
 */
object UiFilterCodec {

    val supportedIds: Set<String> get() = durableFilterClasses.keys

    private val filterIds = durableFilterClasses.entries.associate { (id, type) -> type to id }
    private val arraySizes = mapOf(
        "UiColorBalanceFilter" to 9,
        "UiColorMatrix3x3Filter" to 9,
        "UiColorMatrix4x4Filter" to 16,
        "UiConvolution3x3Filter" to 9,
    )

    private val records = mapOf(
        "ArcParams" to ArcParams::class,
        "AsciiParams" to AsciiParams::class,
        "BilaterialBlurParams" to BilaterialBlurParams::class,
        "ChannelMixParams" to ChannelMixParams::class,
        "ClaheParams" to ClaheParams::class,
        "CropOrPerspectiveParams" to CropOrPerspectiveParams::class,
        "EnhancedZoomBlurParams" to EnhancedZoomBlurParams::class,
        "GlitchParams" to GlitchParams::class,
        "KaleidoscopeParams" to KaleidoscopeParams::class,
        "LinearGaussianParams" to LinearGaussianParams::class,
        "LinearTiltShiftParams" to LinearTiltShiftParams::class,
        "PinchParams" to PinchParams::class,
        "RadialTiltShiftParams" to RadialTiltShiftParams::class,
        "RubberStampParams" to RubberStampParams::class,
        "SideFadeParams.Relative" to SideFadeParams.Relative::class,
        "SideFadeParams.Absolute" to SideFadeParams.Absolute::class,
        "SmearParams" to SmearParams::class,
        "SparkleParams" to SparkleParams::class,
        "ToneCurvesParams" to ToneCurvesParams::class,
        "VoronoiCrystallizeParams" to VoronoiCrystallizeParams::class,
        "WaterParams" to WaterParams::class,
    )
    private val recordIds = records.entries.associate { (id, type) -> type to id }

    private val enums: Map<String, List<Enum<*>>> = mapOf(
        "BlurEdgeMode" to BlurEdgeMode.entries,
        "FadeSide" to FadeSide.entries,
        "MirrorSide" to MirrorSide.entries,
        "PaletteTransferSpace" to PaletteTransferSpace.entries,
        "PolarCoordinatesType" to PolarCoordinatesType.entries,
        "PopArtBlendingMode" to PopArtBlendingMode.entries,
        "TransferFunc" to TransferFunc.entries,
    )

    /** [mapAsset] may replace file, image and custom-font references with durable equivalents. */
    fun encode(
        filter: UiFilter<*>,
        mapAsset: (Any) -> Any = { it },
    ): EncodedUiFilter {
        val id = filterIds[filter::class] ?: invalid("Unsupported UI filter")
        val value = encodeValue(filter.value, mapAsset)
        checkArraySize(id, value)
        return EncodedUiFilter(id, filter.isVisible, value)
    }

    /** Freeze the mutable visibility flag, arrays and tone-curve lists for an undo snapshot. */
    fun snapshot(filter: UiFilter<*>): UiFilter<*> {
        val id = filterIds[filter::class] ?: invalid("Unsupported UI filter")
        val type = durableFilterClasses.getValue(id)
        val constructor = type.primaryConstructor ?: invalid("Missing constructor for $id")
        val value = when (val value = filter.value) {
            is FloatArray -> value.copyOf()
            is ToneCurvesParams -> value.copy(controlPoints = value.controlPoints.map { it.toList() })
            else -> value
        }
        val arguments = if (constructor.parameters.isEmpty()) emptyMap() else {
            mapOf(constructor.parameters.single() to value)
        }
        return constructor.callBy(arguments).also { it.isVisible = filter.isVisible }
    }

    fun decode(encoded: EncodedUiFilter): UiFilter<*> {
        val type = durableFilterClasses[encoded.id]
            ?: invalid("Unknown UI filter ID: ${encoded.id}")
        checkArraySize(encoded.id, encoded.value)
        val constructor = type.primaryConstructor
            ?: invalid("Missing constructor for ${encoded.id}")
        val parameters = constructor.parameters
        val arguments = when (parameters.size) {
            0 -> {
                decodeValue(encoded.value, Unit::class.createType())
                emptyMap()
            }
            1 -> mapOf(parameters.single() to decodeValue(encoded.value, parameters.single().type))
            else -> invalid("Unsupported constructor for ${encoded.id}")
        }
        val result = try {
            constructor.callBy(arguments)
        } catch (exception: ReflectiveOperationException) {
            throw FilterCodecException("Invalid value for ${encoded.id}", exception)
        } catch (exception: IllegalArgumentException) {
            throw FilterCodecException("Invalid value for ${encoded.id}", exception)
        }
        return result.also { it.isVisible = encoded.visible }
    }

    private fun encodeValue(value: Any?, mapAsset: (Any) -> Any): FilterValue = when (value) {
        null -> FilterValue("null")
        Unit -> FilterValue("unit")
        is Int -> scalar("int", value.toString())
        is Float -> {
            if (!value.isFinite()) invalid("Non-finite filter value")
            scalar("float", value.toString())
        }
        is Boolean -> scalar("boolean", value.toString())
        is String -> scalar("string", value)
        is FloatArray -> items("floatArray", value.map { encodeValue(it, mapAsset) })
        is List<*> -> items("list", value.map { encodeValue(it, mapAsset) })
        is Pair<*, *> -> items(
            "pair",
            listOf(encodeValue(value.first, mapAsset), encodeValue(value.second, mapAsset))
        )
        is Triple<*, *, *> -> items(
            "triple",
            listOf(
                encodeValue(value.first, mapAsset),
                encodeValue(value.second, mapAsset),
                encodeValue(value.third, mapAsset)
            )
        )
        is Quad<*, *, *, *> -> items(
            "quad",
            listOf(
                encodeValue(value.first, mapAsset),
                encodeValue(value.second, mapAsset),
                encodeValue(value.third, mapAsset),
                encodeValue(value.fourth, mapAsset)
            )
        )
        is FilterValueWrapper<*> -> items("wrapper", listOf(encodeValue(value.wrapped, mapAsset)))
        is ColorModel -> scalar("color", value.colorInt.toString())
        is FileModel -> scalar("file", assetString(mapAsset(value.uri)))
        is ImageModel -> {
            val asset = mapAsset(value.data)
            val data = when (asset) {
                is String, is Int -> asset
                is Uri -> asset.toString()
                is File -> asset.absolutePath
                else -> invalid("Unsupported filter image data; import it before serialization")
            }
            items("image", listOf(encodeValue(data, mapAsset)))
        }
        is FontType.Resource -> scalar("font.resource", value.resId.toString())
        is FontType.File -> scalar("font.file", assetString(mapAsset(value.path)).also {
            if (it.isBlank()) invalid("Blank custom font reference")
        })
        is IntegerSize -> items(
            "size",
            listOf(encodeValue(value.width, mapAsset), encodeValue(value.height, mapAsset))
        )
        is Enum<*> -> {
            val id = enums.entries.firstOrNull { (_, values) ->
                values.any { it::class == value::class }
            }?.key ?: invalid("Unsupported filter enum")
            scalar(id, value.name)
        }
        else -> encodeRecord(value, mapAsset)
    }

    private fun encodeRecord(value: Any, mapAsset: (Any) -> Any): FilterValue {
        val id = recordIds[value::class] ?: invalid("Unsupported filter parameter type")
        val constructor = value::class.primaryConstructor
            ?: invalid("Missing constructor for $id")
        val properties = value::class.memberProperties.associateBy { it.name }
        return FilterValue(
            type = id,
            fields = constructor.parameters.associate { parameter ->
                val name = parameter.name ?: invalid("Unnamed parameter in $id")
                val property = properties[name] ?: invalid("Missing property $id.$name")
                name to encodeValue(property.getter.call(value), mapAsset)
            }
        )
    }

    private fun decodeValue(value: FilterValue, expected: KType): Any? {
        if (value.type == "null") {
            empty(value)
            if (!expected.isMarkedNullable) invalid("Unexpected null filter value")
            return null
        }
        val expectedClass = expected.classifier as? KClass<*>
            ?: invalid("Unsupported filter parameter declaration")
        fun argument(index: Int): KType = expected.arguments.getOrNull(index)?.type
            ?: invalid("Missing generic filter parameter type")
        fun part(index: Int): Any? = decodeValue(value.items[index], argument(index))
        val decoded: Any = when (value.type) {
            "unit" -> {
                empty(value)
                Unit
            }
            "int" -> integer(value)
            "float" -> floating(value)
            "boolean" -> when (scalarValue(value)) {
                "true" -> true
                "false" -> false
                else -> invalid("Invalid filter boolean")
            }
            "string" -> scalarValue(value)
            "color" -> ColorModel(integer(value))
            "file" -> FileModel(scalarValue(value))
            "font.resource" -> FontType.Resource(integer(value).also {
                if (it <= 0) invalid("Invalid font resource ID")
            })
            "font.file" -> FontType.File(scalarValue(value).also {
                if (it.isBlank()) invalid("Blank custom font reference")
            })
            "floatArray" -> {
                sequence(value)
                value.items.map { floating(it.also { part ->
                    if (part.type != "float") invalid("Invalid float array element")
                }) }.toFloatArray()
            }
            "list" -> {
                sequence(value)
                if (expectedClass != List::class) invalid("Unexpected filter list")
                value.items.map { decodeValue(it, argument(0)) }
            }
            "pair" -> {
                sequence(value, 2)
                if (expectedClass != Pair::class) invalid("Unexpected filter pair")
                Pair(part(0), part(1))
            }
            "triple" -> {
                sequence(value, 3)
                if (expectedClass != Triple::class) invalid("Unexpected filter triple")
                Triple(part(0), part(1), part(2))
            }
            "quad" -> {
                sequence(value, 4)
                if (expectedClass != Quad::class) invalid("Unexpected filter quad")
                Quad(part(0), part(1), part(2), part(3))
            }
            "wrapper" -> {
                sequence(value, 1)
                if (expectedClass != FilterValueWrapper::class) invalid("Unexpected filter wrapper")
                FilterValueWrapper(part(0) ?: invalid("Null wrapped filter value"))
            }
            "size" -> {
                sequence(value, 2)
                if (value.items.any { it.type != "int" }) invalid("Invalid filter size")
                IntegerSize(integer(value.items[0]), integer(value.items[1]))
            }
            "image" -> {
                sequence(value, 1)
                val data = value.items.single()
                ImageModel(
                    when (data.type) {
                        "int" -> integer(data).also {
                            if (it <= 0) invalid("Invalid image resource ID")
                        }
                        "string" -> scalarValue(data).also {
                            if (it.isBlank()) invalid("Blank filter image reference")
                        }
                        else -> invalid("Invalid filter image reference")
                    }
                )
            }
            in enums -> {
                val name = scalarValue(value)
                enums.getValue(value.type).firstOrNull { it.name == name }
                    ?: invalid("Unknown ${value.type} value: $name")
            }
            in records -> decodeRecord(value, expectedClass)
            else -> invalid("Unknown filter value tag: ${value.type}")
        }
        if (!expectedClass.isInstance(decoded)) {
            invalid("Filter value ${value.type} does not match its constructor")
        }
        return decoded
    }

    private fun decodeRecord(value: FilterValue, expectedClass: KClass<*>): Any {
        val type = records.getValue(value.type)
        if (!expectedClass.java.isAssignableFrom(type.java)) invalid("Unexpected filter parameter")
        val constructor = type.primaryConstructor
            ?: invalid("Missing constructor for ${value.type}")
        val names = constructor.parameters.map {
            it.name ?: invalid("Unnamed filter parameter")
        }.toSet()
        if (value.scalar != null || value.items.isNotEmpty() || value.fields.keys != names) {
            invalid("Invalid fields for ${value.type}")
        }
        val arguments = constructor.parameters.associateWith { parameter ->
            decodeValue(value.fields.getValue(parameter.name!!), parameter.type)
        }
        return try {
            constructor.callBy(arguments)
        } catch (exception: ReflectiveOperationException) {
            throw FilterCodecException("Invalid ${value.type} parameters", exception)
        } catch (exception: IllegalArgumentException) {
            throw FilterCodecException("Invalid ${value.type} parameters", exception)
        }
    }

    private fun scalar(type: String, value: String) = FilterValue(type, scalar = value)
    private fun items(type: String, items: List<FilterValue>) = FilterValue(type, items = items)

    private fun scalarValue(value: FilterValue): String {
        if (value.items.isNotEmpty() || value.fields.isNotEmpty()) invalid("Invalid scalar value")
        return value.scalar ?: invalid("Missing scalar value")
    }

    private fun integer(value: FilterValue): Int =
        scalarValue(value).toIntOrNull() ?: invalid("Invalid filter integer")

    private fun floating(value: FilterValue): Float =
        scalarValue(value).toFloatOrNull()?.takeIf { it.isFinite() }
            ?: invalid("Invalid or non-finite filter float")

    private fun empty(value: FilterValue) {
        if (value.scalar != null || value.items.isNotEmpty() || value.fields.isNotEmpty()) {
            invalid("Invalid empty filter value")
        }
    }

    private fun sequence(value: FilterValue, size: Int? = null) {
        if (value.scalar != null || value.fields.isNotEmpty() ||
            (size != null && value.items.size != size)
        ) {
            invalid("Invalid filter sequence")
        }
    }

    private fun assetString(asset: Any): String = when (asset) {
        is String -> asset
        is Uri -> asset.toString()
        is File -> asset.absolutePath
        else -> invalid("Invalid filter file reference")
    }

    private fun checkArraySize(id: String, value: FilterValue) {
        arraySizes[id]?.let { size ->
            if (value.type != "floatArray" || value.items.size != size) {
                invalid("Invalid matrix size for $id")
            }
        }
    }

    private fun invalid(message: String): Nothing = throw FilterCodecException(message)
}
