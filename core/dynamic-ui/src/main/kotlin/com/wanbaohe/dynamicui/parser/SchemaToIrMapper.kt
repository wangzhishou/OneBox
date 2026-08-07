package com.wanbaohe.dynamicui.parser

import com.wanbaohe.dynamicui.ir.ActionSpec
import com.wanbaohe.dynamicui.ir.ListConfig
import com.wanbaohe.dynamicui.ir.UiNode
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Converts raw JSON payloads into the immutable IR consumed by the renderer.
 *
 * The published JSON contract treats `props` as the only container of component
 * parameters. Framework metadata stays outside `props`.
 */
internal object SchemaToIrMapper {

    private val reservedNodeKeys = setOf(
        "type",
        "id",
        "prompt",
        "meta",
        "if",
        "children",
        "props",
        "listConfig",
        "actions",
    )

    /** Returns (IR root node, initial-state map). */
    fun map(doc: JsonObject): Pair<UiNode, Map<String, Any?>> {
        val root = when {
            doc["root"] is JsonObject -> doc["root"]!!.jsonObject
            doc["body"] is JsonObject -> doc["body"]!!.jsonObject
            doc["type"] is JsonPrimitive -> doc
            else -> error("UI JSON is missing required field 'root'")
        }
        val initialState = doc["dataContext"]?.jsonObject?.mapValues { (_, value) -> jsonElementToAny(value) }.orEmpty()
        return Pair(mapNode(root), initialState)
    }

    fun mapNode(node: JsonObject): UiNode {
        val type = node["type"]?.jsonPrimitive?.content
            ?: error("UI JSON node is missing required field 'type'")
        val invalidKeys = node.keys - reservedNodeKeys
        check(invalidKeys.isEmpty()) {
            "UI JSON node(type=$type) has unsupported top-level fields: ${invalidKeys.joinToString()}. Put component parameters inside 'props'."
        }

        val propsObject = node["props"]?.jsonObject

        val propsMap = buildMap<String, Any?> {
            propsObject?.forEach { (key, value) ->
                when {
                    key == "modifier" -> Unit
                    key == "data" || key == "options" -> {
                        put(key, jsonElementToAny(value))
                    }
                    key == "textStyle" && value is JsonObject -> {
                        value.forEach { (styleKey, styleValue) ->
                            put(styleKey, jsonElementToAny(styleValue))
                        }
                    }
                    else -> put(key, jsonElementToPropValue(value))
                }
            }
        }

        val actionsMap = buildMap<String, ActionSpec> {
            node["actions"]?.jsonObject?.forEach { (event, value) -> put(event, mapAction(value.jsonObject)) }
        }

        val children = node["children"]?.jsonArray
            ?.map { mapNode(it.jsonObject) }
            .orEmpty()

        return UiNode(
            type = type,
            id = primitiveContentOrNull(node["id"]),
            prompt = primitiveContentOrNull(node["prompt"]),
            modifier = resolveModifiers(propsObject),
            visibleExpr = primitiveContentOrNull(node["if"]),
            children = children,
            props = propsMap,
            listConfig = node["listConfig"]?.jsonObject?.let(::mapListConfig),
            actions = actionsMap,
        )
    }

    private fun mapListConfig(config: JsonObject): ListConfig = ListConfig(
        dataExpr = config["dataSource"]?.jsonPrimitive?.content
            ?: error("listConfig is missing required field 'dataSource'"),
        itemKey = primitiveContentOrNull(config["itemKey"]),
        itemTemplate = config["itemTemplate"]?.jsonObject?.let(::mapNode)
            ?: error("listConfig is missing required field 'itemTemplate'"),
        columns = config["columns"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
    )

    internal fun mapAction(action: JsonObject): ActionSpec {
        val paramsObject = action["params"]?.jsonObject
        val bodyObject = action["body"]?.jsonObject
        val resolvedType = action["type"]?.jsonPrimitive?.content
            ?: when {
                action["setState"] != null -> "setState"
                action["screen"] != null -> "navigate"
                action["url"] != null -> "http"
                action["message"] != null -> "toast"
                action["text"] != null -> "copy"
                else -> "unknown"
            }

        val resolvedParams = buildMap<String, String> {
            paramsObject?.forEach { (key, value) -> put(key, jsonElementToString(value)) }
            primitiveContentOrNull(action["setState"])?.let { put("key", it) }
            action["value"]?.let { put("value", jsonElementToString(it)) }
            primitiveContentOrNull(action["screen"])?.let { put("screen", it) }
            primitiveContentOrNull(action["url"])?.let { put("url", it) }
            primitiveContentOrNull(action["method"])?.let { put("method", it) }
            bodyObject?.forEach { (key, value) -> put("body.$key", jsonElementToString(value)) }
            action["message"]?.let { put("message", jsonElementToString(it)) }
            action["title"]?.let { put("title", jsonElementToString(it)) }
            action["confirm"]?.let { put("confirm", jsonElementToString(it)) }
            action["dismiss"]?.let { put("dismiss", jsonElementToString(it)) }
            action["text"]?.let { put("text", jsonElementToString(it)) }
        }

        return ActionSpec(
            type = resolvedType,
            params = resolvedParams,
            onSuccess = action["onSuccess"]?.jsonObject?.let(::mapAction),
            onError = action["onError"]?.jsonObject?.let(::mapAction),
            onConfirm = action["onConfirm"]?.jsonObject?.let(::mapAction),
        )
    }

    private fun jsonElementToString(el: JsonElement): String = when (el) {
        is JsonPrimitive -> el.content
        is JsonNull -> ""
        else -> el.toString()
    }

    private fun primitiveContentOrNull(element: JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return if (primitive is JsonNull) null else primitive.content
    }

    private fun resolveModifiers(propsObject: JsonObject?): List<String> {
        val rawModifiers = propsObject?.get("modifier")
        return when (rawModifiers) {
            is JsonArray -> rawModifiers.mapNotNull(::mapStructuredModifier)
            else -> emptyList()
        }
    }

    private fun jsonElementToPropValue(el: JsonElement): Any? = when (el) {
        is JsonNull -> null
        is JsonPrimitive -> when {
            el.isString -> el.content
            el.booleanOrNull != null -> el.boolean
            else -> el.content.toDoubleOrNull() ?: el.content
        }
        is JsonArray -> el.map { jsonElementToPropValue(it) }
        is JsonObject -> {
            if (el["type"] is JsonPrimitive) {
                mapNode(el)
            } else {
                el.mapValues { (_, value) -> jsonElementToPropValue(value) }
            }
        }
    }

    private fun jsonElementToAny(el: JsonElement): Any? = when (el) {
        is JsonNull -> null
        is JsonPrimitive -> when {
            el.isString -> el.content
            el.booleanOrNull != null -> el.boolean
            else -> el.content.toDoubleOrNull() ?: el.content
        }
        is JsonArray -> el.map { jsonElementToAny(it) }
        is JsonObject -> el.mapValues { (_, value) -> jsonElementToAny(value) }
    }

    private fun mapStructuredModifier(element: JsonElement): String? {
        val entry = element as? JsonObject ?: return null
        if (entry.size != 1) return null
        val (name, value) = entry.entries.first()
        if (value is JsonPrimitive && value.booleanOrNull == false) return null
        return when (name) {
            "fillMaxSize",
            "fillMaxWidth",
            "fillMaxHeight",
            "wrapContentSize",
            "wrapContentWidth",
            "wrapContentHeight",
            "clickable" -> name
            "padding" -> buildPaddingModifier(value)
            "background" -> buildBackgroundModifier(value)
            "clip" -> buildClipModifier(value)
            "border" -> buildBorderModifier(value)
            "shadow" -> buildShadowModifier(value)
            "size" -> buildSizeModifier(value)
            "width",
            "height" -> "$name(${formatDpLike(value)})"
            "alpha",
            "zIndex",
            "aspectRatio" -> "$name(${primitiveOrJsonValue(value)})"
            "offset" -> buildOffsetModifier(value)
            else -> buildGenericModifier(name, value)
        }
    }

    private fun buildPaddingModifier(value: JsonElement): String = when (value) {
        is JsonPrimitive -> "padding(${primitiveOrJsonValue(value)}dp)"
        is JsonObject -> {
            value["all"]?.let { return "padding(${formatDpLike(it)})" }
            val named = listOf("start", "top", "end", "bottom", "horizontal", "vertical", "all")
                .mapNotNull { key ->
                    if (key == "all") return@mapNotNull null
                    value[key]?.let { "$key=${formatDpLike(it)}" }
                }
            if (named.isEmpty()) "padding(0dp)" else "padding(${named.joinToString(", ")})"
        }
        else -> "padding(0dp)"
    }

    private fun buildBackgroundModifier(value: JsonElement): String {
        if (value !is JsonObject) return "background(${primitiveOrJsonValue(value)})"
        val color = value["color"]?.let(::primitiveOrJsonValue) ?: return "background(transparent)"
        val shape = value["shape"]?.let(::mapShape)
        return if (shape != null) "background($color, $shape)" else "background($color)"
    }

    private fun buildClipModifier(value: JsonElement): String {
        val shape = if (value is JsonObject) value["shape"]?.let(::mapShape) else mapShape(value)
        return "clip(${shape ?: "RoundedCornerShape(0dp)"})"
    }

    private fun buildBorderModifier(value: JsonElement): String {
        if (value !is JsonObject) return "border(1dp, #000000)"
        val width = value["width"]?.let(::formatDpLike) ?: "1dp"
        val color = value["color"]?.let(::primitiveOrJsonValue) ?: "#000000"
        val shape = value["shape"]?.let(::mapShape)
        return if (shape != null) "border($width, $color, $shape)" else "border($width, $color)"
    }

    private fun buildShadowModifier(value: JsonElement): String = when (value) {
        is JsonPrimitive -> "shadow(${formatDpLike(value)})"
        is JsonObject -> {
            val elevation = value["elevation"]?.let(::formatDpLike) ?: "0dp"
            val shape = value["shape"]?.let(::mapShape)
            if (shape != null) "shadow($elevation, $shape)" else "shadow($elevation)"
        }
        else -> "shadow(0dp)"
    }

    private fun buildSizeModifier(value: JsonElement): String = when (value) {
        is JsonPrimitive -> "size(${formatDpLike(value)})"
        is JsonObject -> {
            val width = value["width"]?.let(::formatDpLike)
            val height = value["height"]?.let(::formatDpLike)
            when {
                width != null && height != null -> "size($width, $height)"
                width != null -> "size($width)"
                else -> "size(0dp)"
            }
        }
        else -> "size(0dp)"
    }

    private fun buildOffsetModifier(value: JsonElement): String {
        if (value !is JsonObject) return "offset(x=0dp, y=0dp)"
        val x = value["x"]?.let(::formatDpLike) ?: "0dp"
        val y = value["y"]?.let(::formatDpLike) ?: "0dp"
        return "offset(x=$x, y=$y)"
    }

    private fun buildGenericModifier(name: String, value: JsonElement): String = when (value) {
        is JsonPrimitive -> {
            if (value.booleanOrNull == true) name else "$name(${primitiveOrJsonValue(value)})"
        }
        is JsonArray -> "$name(${value.joinToString(", ") { primitiveOrJsonValue(it) }})"
        is JsonObject -> "$name(${value.entries.joinToString(", ") { (key, item) -> "$key=${primitiveOrJsonValue(item)}" }})"
    }

    private fun mapShape(value: JsonElement?): String? {
        when (value) {
            null, JsonNull -> return null
            is JsonPrimitive -> return primitiveOrJsonValue(value)
            is JsonObject -> {
                val type = primitiveContentOrNull(value["type"]) ?: return null
                return when (type) {
                    "rounded", "RoundedCornerShape" -> {
                        val radius = value["radius"]?.let(::formatDpLike)
                            ?: value["topStart"]?.let(::formatDpLike)
                            ?: "0dp"
                        "RoundedCornerShape($radius)"
                    }
                    "circle", "CircleShape" -> "CircleShape"
                    else -> primitiveOrJsonValue(value)
                }
            }
            else -> return null
        }
    }

    private fun primitiveOrJsonValue(value: JsonElement): String = when (value) {
        is JsonPrimitive -> value.content
        is JsonNull -> ""
        else -> value.toString()
    }

    private fun formatDpLike(value: JsonElement): String {
        val raw = primitiveOrJsonValue(value)
        return if (raw.endsWith("dp") || raw.endsWith("sp")) raw else "${raw}dp"
    }
}
