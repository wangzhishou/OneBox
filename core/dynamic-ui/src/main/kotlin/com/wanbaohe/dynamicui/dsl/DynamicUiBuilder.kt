package com.wanbaohe.dynamicui.dsl

import com.wanbaohe.dynamicui.ir.ActionSpec
import com.wanbaohe.dynamicui.ir.ListConfig
import com.wanbaohe.dynamicui.ir.UiNode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@DslMarker
annotation class UiDsl

@UiDsl
class UiNodeBuilder(private val type: String) {
    var id: String? = null
    var prompt: String? = null
    var visibleExpr: String? = null
    private val modifierExprs = mutableListOf<String>()
    private val props = mutableMapOf<String, Any?>()
    private val actions = mutableMapOf<String, ActionSpec>()
    private val children = mutableListOf<UiNode>()
    private var listConfig: ListConfig? = null

    fun modifiers(vararg exprs: String) {
        modifierExprs.addAll(exprs)
    }

    fun prop(key: String, value: Any?) {
        props[key] = value
    }

    fun action(event: String, spec: ActionSpec) {
        actions[event] = spec
    }

    fun visibleIf(expr: String) {
        visibleExpr = expr
    }

    fun prompt(template: String) {
        prompt = template
    }

    operator fun UiNode.unaryPlus() {
        this@UiNodeBuilder.children.add(this)
    }

    fun column(
        modifiers: List<String> = emptyList(),
        block: UiNodeBuilder.() -> Unit,
    ): UiNode = UiNodeBuilder("Column")
        .apply { this.modifiers(*modifiers.toTypedArray()); block() }
        .build()
        .also(children::add)

    fun row(
        modifiers: List<String> = emptyList(),
        block: UiNodeBuilder.() -> Unit,
    ): UiNode = UiNodeBuilder("Row")
        .apply { this.modifiers(*modifiers.toTypedArray()); block() }
        .build()
        .also(children::add)

    fun box(
        modifiers: List<String> = emptyList(),
        block: UiNodeBuilder.() -> Unit,
    ): UiNode = UiNodeBuilder("Box")
        .apply { this.modifiers(*modifiers.toTypedArray()); block() }
        .build()
        .also(children::add)

    fun text(
        text: String,
        textStyle: String? = null,
        color: String? = null,
        modifiers: List<String> = emptyList(),
    ): UiNode = UiNode(
        type = "Text",
        modifier = modifiers,
        props = buildMap {
            put("text", text)
            textStyle?.let { put("textStyle", it) }
            color?.let { put("color", it) }
        },
    ).also(children::add)

    fun button(
        text: String? = null,
        onClick: ActionSpec? = null,
        modifiers: List<String> = emptyList(),
        block: (UiNodeBuilder.() -> Unit)? = null,
    ): UiNode = UiNodeBuilder("Button").apply {
        this.modifiers(*modifiers.toTypedArray())
        onClick?.let { action("onClick", it) }
        if (text != null) {
            +UiNode(type = "Text", props = mapOf("text" to text))
        }
        block?.invoke(this)
    }.build().also(children::add)

    fun image(
        url: String,
        contentScale: String? = null,
        modifiers: List<String> = emptyList(),
    ): UiNode = UiNode(
        type = "AsyncImage",
        modifier = modifiers,
        props = buildMap {
            put("url", url)
            contentScale?.let { put("contentScale", it) }
        },
    ).also(children::add)

    fun spacer(width: String? = null, height: String? = null): UiNode = UiNode(
        type = "Spacer",
        props = buildMap {
            width?.let { put("width", it) }
            height?.let { put("height", it) }
        },
    ).also(children::add)

    fun lazyColumn(
        items: String,
        itemKey: String? = null,
        columns: Int = 1,
        modifiers: List<String> = emptyList(),
        itemTemplate: UiNodeBuilder.() -> Unit,
    ): UiNode {
        val templateBuilder = UiNodeBuilder("_template").apply(itemTemplate)
        val templateNode = templateBuilder.build().let {
            templateBuilder.getChildren().firstOrNull() ?: UiNode("Spacer")
        }
        return UiNode(
            type = if (columns > 1) "LazyVerticalGrid" else "LazyColumn",
            modifier = modifiers,
            listConfig = ListConfig(
                dataExpr = items,
                itemKey = itemKey,
                itemTemplate = templateNode,
                columns = columns,
            ),
        ).also(children::add)
    }

    fun scaffold(
        title: String? = null,
        modifiers: List<String> = emptyList(),
        block: ScaffoldBuilder.() -> Unit,
    ): UiNode = ScaffoldBuilder().apply(block).build(title, modifiers).also(children::add)

    internal fun getChildren(): List<UiNode> = children.toList()

    fun build(): UiNode = UiNode(
        type = type,
        id = id,
        prompt = prompt,
        modifier = modifierExprs.toList(),
        visibleExpr = visibleExpr,
        children = children.toList(),
        props = props.toMap(),
        actions = actions.toMap(),
        listConfig = listConfig,
    )
}

@UiDsl
class ScaffoldBuilder {
    private var topBar: UiNode? = null
    private var bottomBar: UiNode? = null
    private var fab: UiNode? = null
    private var body: UiNode? = null

    fun topBar(block: UiNodeBuilder.() -> Unit) {
        topBar = UiNodeBuilder("TopAppBar").apply(block).build()
    }

    fun body(block: UiNodeBuilder.() -> Unit) {
        body = UiNodeBuilder("Column").apply(block).build()
    }

    fun floatingActionButton(block: UiNodeBuilder.() -> Unit) {
        fab = UiNodeBuilder("Button").apply(block).build()
    }

    fun bottomBar(block: UiNodeBuilder.() -> Unit) {
        bottomBar = UiNodeBuilder("Row").apply(block).build()
    }

    internal fun build(title: String?, parentModifiers: List<String>): UiNode = UiNode(
        type = "Scaffold",
        modifier = parentModifiers,
        props = buildMap {
            title?.let { put("title", it) }
            topBar?.let { put("topBar", it) }
            bottomBar?.let { put("bottomBar", it) }
            fab?.let { put("floatingActionButton", it) }
            body?.let { put("body", it) }
        },
    )
}

class UiDocumentBuilder {
    private var root: UiNode = UiNode("Spacer")
    private val initialState = mutableMapOf<String, Any?>()

    fun state(vararg pairs: Pair<String, Any?>) {
        initialState.putAll(pairs)
    }

    fun root(type: String = "Column", block: UiNodeBuilder.() -> Unit) {
        root = UiNodeBuilder(type).apply(block).build()
    }

    fun column(modifiers: List<String> = emptyList(), block: UiNodeBuilder.() -> Unit) {
        root = UiNodeBuilder("Column").apply { this.modifiers(*modifiers.toTypedArray()); block() }.build()
    }

    fun build(): UiNode = root
    fun buildState(): Map<String, Any?> = initialState.toMap()
}

fun dynamicUi(block: UiDocumentBuilder.() -> Unit): DynamicUiDocument {
    val builder = UiDocumentBuilder().apply(block)
    return DynamicUiDocument(root = builder.build(), initialState = builder.buildState())
}

data class DynamicUiDocument(
    val root: UiNode,
    val initialState: Map<String, Any?> = emptyMap(),
) {
    private val jsonSerializer = Json { prettyPrint = true; encodeDefaults = false }

    fun toJson(): String {
        val doc = buildJsonObject {
            if (initialState.isNotEmpty()) {
                put("dataContext", JsonObject(initialState.mapValues { anyToJsonElement(it.value) }))
            }
            put("root", nodeToJsonObject(root))
        }
        return jsonSerializer.encodeToString(doc)
    }
}

fun navigate(screen: String, vararg params: Pair<String, String>): ActionSpec =
    ActionSpec(type = "navigate", params = mapOf("screen" to screen) + params.toMap())

fun setState(key: String, value: String): ActionSpec =
    ActionSpec(type = "setState", params = mapOf("key" to key, "value" to value))

fun httpGet(url: String, responseKey: String = "response"): ActionSpec =
    ActionSpec(type = "http", params = mapOf("url" to url, "method" to "GET", "responseKey" to responseKey))

fun httpPost(url: String, vararg bodyFields: Pair<String, String>): ActionSpec =
    ActionSpec(
        type = "http",
        params = mapOf("url" to url, "method" to "POST") + bodyFields.associate { "body.${it.first}" to it.second },
    )

fun toast(message: String): ActionSpec =
    ActionSpec(type = "toast", params = mapOf("message" to message))

fun back(): ActionSpec = ActionSpec(type = "back")

fun ActionSpec.then(next: ActionSpec): ActionSpec = copy(onSuccess = next)
fun ActionSpec.onError(handler: ActionSpec): ActionSpec = copy(onError = handler)

private fun nodeToJsonObject(node: UiNode): JsonObject = buildJsonObject {
    put("type", node.type)
    node.id?.let { put("id", it) }
    node.prompt?.let { put("prompt", it) }
    node.visibleExpr?.let { put("if", it) }
    val propsJson = buildJsonObject {
        if (node.modifier.isNotEmpty()) {
            put("modifier", JsonArray(node.modifier.map(::legacyModifierExprToJson)))
        }
        node.props.forEach { (key, value) -> put(key, anyToJsonElement(value)) }
    }
    if (propsJson.isNotEmpty()) {
        put("props", propsJson)
    }
    if (node.children.isNotEmpty()) {
        put("children", JsonArray(node.children.map(::nodeToJsonObject)))
    }
    node.listConfig?.let { cfg ->
        put("listConfig", buildJsonObject {
            put("dataSource", cfg.dataExpr)
            cfg.itemKey?.let { put("itemKey", it) }
            if (cfg.columns > 1) put("columns", cfg.columns)
            put("itemTemplate", nodeToJsonObject(cfg.itemTemplate))
        })
    }
    if (node.actions.isNotEmpty()) {
        put("actions", buildJsonObject {
            node.actions.forEach { (event, action) -> put(event, actionToJson(action)) }
        })
    }
}

private fun actionToJson(action: ActionSpec): JsonObject = buildJsonObject {
    put("type", action.type)
    if (action.params.isNotEmpty()) {
        put("params", JsonObject(action.params.mapValues { JsonPrimitive(it.value) }))
    }
    action.onSuccess?.let { put("onSuccess", actionToJson(it)) }
    action.onError?.let { put("onError", actionToJson(it)) }
    action.onConfirm?.let { put("onConfirm", actionToJson(it)) }
}

private fun anyToJsonElement(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is JsonElement -> value
    is UiNode -> nodeToJsonObject(value)
    is String -> JsonPrimitive(value)
    is Number -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is Map<*, *> -> JsonObject(
        value.entries.associate { (key, item) ->
            key.toString() to anyToJsonElement(item)
        },
    )
    is List<*> -> JsonArray(value.map(::anyToJsonElement))
    else -> JsonPrimitive(value.toString())
}

private fun legacyModifierExprToJson(expr: String): JsonObject {
    val trimmed = expr.trim()
    val parenIndex = trimmed.indexOf('(')
    if (parenIndex == -1 || !trimmed.endsWith(")")) {
        return buildJsonObject { put(trimmed, true) }
    }
    val name = trimmed.substring(0, parenIndex).trim()
    val argBody = trimmed.substring(parenIndex + 1, trimmed.lastIndexOf(')')).trim()
    return buildJsonObject {
        if (argBody.isEmpty()) {
            put(name, true)
        } else {
            put(name, parseModifierArgs(argBody))
        }
    }
}

private fun parseModifierArgs(raw: String): JsonElement {
    val parts = splitModifierArgs(raw)
    if (parts.size == 1 && '=' !in parts.first()) {
        return inferPrimitive(parts.first())
    }
    val named = parts.filter { '=' in it }
    if (named.isNotEmpty()) {
        return JsonObject(
            named.associate { part ->
                val key = part.substringBefore('=').trim()
                val value = part.substringAfter('=').trim()
                key to inferPrimitive(value)
            },
        )
    }
    return JsonArray(parts.map(::inferPrimitive))
}

private fun splitModifierArgs(raw: String): List<String> {
    val result = mutableListOf<String>()
    var depth = 0
    var start = 0
    raw.forEachIndexed { index, c ->
        when (c) {
            '(' -> depth++
            ')' -> depth--
            ',' -> if (depth == 0) {
                result += raw.substring(start, index).trim()
                start = index + 1
            }
        }
    }
    result += raw.substring(start).trim()
    return result.filter { it.isNotEmpty() }
}

private fun inferPrimitive(raw: String): JsonElement {
    val value = raw.trim()
    return when {
        value.equals("true", ignoreCase = true) -> JsonPrimitive(true)
        value.equals("false", ignoreCase = true) -> JsonPrimitive(false)
        value.toLongOrNull() != null -> JsonPrimitive(value.toLong())
        value.toDoubleOrNull() != null -> JsonPrimitive(value.toDouble())
        else -> JsonPrimitive(value)
    }
}
