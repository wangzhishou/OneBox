package com.wanbaohe.dynamicui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wanbaohe.dynamicui.ir.ListConfig
import com.wanbaohe.dynamicui.ir.UiNode
import com.wanbaohe.dynamicui.ir.propInt
import com.wanbaohe.dynamicui.ir.propString
import com.wanbaohe.dynamicui.modifier.ModifierRegistry.Companion.parseDp
import com.wanbaohe.dynamicui.renderer.ComponentRegistry
import com.wanbaohe.dynamicui.renderer.RenderContext
import com.wanbaohe.dynamicui.renderer.UiNodeRenderer
import com.wanbaohe.dynamicui.state.UiStateScope
import com.wanbaohe.dynamicui.state.ValueExprResolver
import com.wanbaohe.dynamicui.state.rememberResolvedBool
import com.wanbaohe.dynamicui.state.rememberResolvedString

/**
 * Layout component renderers: Column, Row, FlowRow, Box, LazyColumn, LazyRow,
 * LazyVerticalGrid, Spacer, Divider, ForEach.
 *
 * All renderers are registered in [registerLayoutComponents].
 */

// ─── Registration ────────────────────────────────────────────────────────────

fun ComponentRegistry.registerLayoutComponents() {
    register("Column") { node, scope, ctx, itemCtx -> ColumnRenderer(node, scope, ctx, itemCtx) }
    register("Row") { node, scope, ctx, itemCtx -> RowRenderer(node, scope, ctx, itemCtx) }
    register("FlowRow") { node, scope, ctx, itemCtx -> FlowRowRenderer(node, scope, ctx, itemCtx) }
    register("Box") { node, scope, ctx, itemCtx -> BoxRenderer(node, scope, ctx, itemCtx) }
    register("LazyColumn") { node, scope, ctx, itemCtx -> LazyColumnRenderer(node, scope, ctx, itemCtx) }
    register("LazyRow") { node, scope, ctx, itemCtx -> LazyRowRenderer(node, scope, ctx, itemCtx) }
    register("LazyVerticalGrid") { node, scope, ctx, itemCtx -> LazyGridRenderer(node, scope, ctx, itemCtx) }
    register("Spacer") { node, scope, ctx, itemCtx -> SpacerRenderer(node, scope, ctx, itemCtx) }
    register("Divider") { node, scope, ctx, itemCtx -> DividerRenderer(node, scope, ctx, itemCtx) }
    register("HorizontalDivider") { node, scope, ctx, itemCtx -> DividerRenderer(node, scope, ctx, itemCtx) }
    register("ForEach") { node, scope, ctx, itemCtx -> ForEachRenderer(node, scope, ctx, itemCtx) }
    register("AnimatedVisibility") { node, scope, ctx, itemCtx -> AnimatedVisibilityRenderer(node, scope, ctx, itemCtx) }
    register("AnimatedContent") { node, scope, ctx, itemCtx -> AnimatedContentRenderer(node, scope, ctx, itemCtx) }
    register("Crossfade") { node, scope, ctx, itemCtx -> CrossfadeRenderer(node, scope, ctx, itemCtx) }
}

// ─── Column ──────────────────────────────────────────────────────────────────

@Composable
private fun ColumnRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    Column(
        modifier = ctx.modifierPipeline.build(node.modifier),
        verticalArrangement = parseVerticalArrangement(node.propString("verticalArrangement")),
        horizontalAlignment = parseHorizontalAlignment(node.propString("horizontalAlignment")),
    ) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

// ─── Row ─────────────────────────────────────────────────────────────────────

@Composable
private fun RowRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    Row(
        modifier = ctx.modifierPipeline.build(node.modifier),
        horizontalArrangement = parseHorizontalArrangement(node.propString("horizontalArrangement")),
        verticalAlignment = parseVerticalAlignmentHoriz(node.propString("verticalAlignment")),
    ) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

// ─── FlowRow ─────────────────────────────────────────────────────────────────

@Composable
private fun FlowRowRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val maxItems = node.propInt("maxItemsInEachRow", 0).takeIf { it > 0 }
    FlowRow(
        modifier = ctx.modifierPipeline.build(node.modifier),
        horizontalArrangement = parseHorizontalArrangement(node.propString("horizontalArrangement")),
        verticalArrangement = parseVerticalArrangement(node.propString("verticalArrangement")),
        maxItemsInEachRow = maxItems ?: Int.MAX_VALUE,
    ) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

// ─── Box ─────────────────────────────────────────────────────────────────────

@Composable
private fun BoxRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    Box(
        modifier = ctx.modifierPipeline.build(node.modifier),
        contentAlignment = parseBoxAlignment(node.propString("contentAlignment")),
    ) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

// ─── LazyColumn ──────────────────────────────────────────────────────────────

@Composable
private fun LazyColumnRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val cfg = node.listConfig
    // Not appending padding from LocalScaffoldPadding because Scaffold's Box now applies .padding() globally.
    // Edge-to-edge support can be revisited if requested, but for now we keep it simple to prevent topBar overlap.
    if (cfg != null) {
        val items by remember(cfg.dataExpr, itemContext) {
            derivedStateOf { resolveList(cfg.dataExpr, scope, itemContext) }
        }
        LazyColumn(
            modifier = ctx.modifierPipeline.build(node.modifier),
        ) {
            items(
                items = items,
                key = cfg.itemKey?.let { keyField -> { item -> extractKey(item, keyField) } },
                contentType = { cfg.itemTemplate.type },
            ) { item ->
                val itemCtx = remember(item) { itemToContext(item) }
                UiNodeRenderer.Render(cfg.itemTemplate, scope, ctx, itemCtx)
            }
        }
    } else {
        LazyColumn(
            modifier = ctx.modifierPipeline.build(node.modifier),
        ) {
            items(node.children) { child ->
                UiNodeRenderer.Render(child, scope, ctx, itemContext)
            }
        }
    }
}

// ─── LazyRow ─────────────────────────────────────────────────────────────────

@Composable
private fun LazyRowRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val cfg = node.listConfig
    if (cfg != null) {
        val items by remember(cfg.dataExpr, itemContext) {
            derivedStateOf { resolveList(cfg.dataExpr, scope, itemContext) }
        }
        LazyRow(
            modifier = ctx.modifierPipeline.build(node.modifier),
        ) {
            items(
                items = items,
                key = cfg.itemKey?.let { keyField -> { item -> extractKey(item, keyField) } },
                contentType = { cfg.itemTemplate.type },
            ) { item ->
                val itemCtx = remember(item) { itemToContext(item) }
                UiNodeRenderer.Render(cfg.itemTemplate, scope, ctx, itemCtx)
            }
        }
    } else {
        LazyRow(
            modifier = ctx.modifierPipeline.build(node.modifier),
        ) {
            items(node.children) { child ->
                UiNodeRenderer.Render(child, scope, ctx, itemContext)
            }
        }
    }
}

// ─── LazyVerticalGrid ────────────────────────────────────────────────────────

@Composable
private fun LazyGridRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val cfg = node.listConfig ?: return
    val columns = if (cfg.columns > 1) cfg.columns else
        node.propInt("columns", 2)
    val dataItems by remember(cfg.dataExpr, itemContext) {
        derivedStateOf { resolveList(cfg.dataExpr, scope, itemContext) }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = ctx.modifierPipeline.build(node.modifier),
    ) {
        items(
            items = dataItems,
            key = cfg.itemKey?.let { keyField -> { item -> extractKey(item, keyField) } },
            contentType = { cfg.itemTemplate.type },
        ) { item ->
            val itemCtx = remember(item) { itemToContext(item) }
            UiNodeRenderer.Render(cfg.itemTemplate, scope, ctx, itemCtx)
        }
    }
}

// ─── Spacer ──────────────────────────────────────────────────────────────────

@Composable
private fun SpacerRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val baseModifier = ctx.modifierPipeline.build(node.modifier)
    val wMod = parseDp(node.propString("width"))?.let { baseModifier.width(it) } ?: baseModifier
    val finalMod = parseDp(node.propString("height"))?.let { wMod.height(it) } ?: wMod
    Spacer(modifier = finalMod)
}

// ─── Divider ─────────────────────────────────────────────────────────────────

@Composable
private fun DividerRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    HorizontalDivider(modifier = ctx.modifierPipeline.build(node.modifier))
}

// ─── ForEach ─────────────────────────────────────────────────────────────────

@Composable
private fun ForEachRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val cfg = node.listConfig ?: return
    val dataItems by remember(cfg.dataExpr, itemContext) {
        derivedStateOf { resolveList(cfg.dataExpr, scope, itemContext) }
    }
    dataItems.forEach { item ->
        val key = cfg.itemKey?.let { extractKey(item, it) }
        androidx.compose.runtime.key(key ?: item) {
            val itemCtx = remember(item) { itemToContext(item) }
            UiNodeRenderer.Render(cfg.itemTemplate, scope, ctx, itemCtx)
        }
    }
}

// ─── Animation ───────────────────────────────────────────────────────────────

@Composable
private fun AnimatedVisibilityRenderer(
    node: UiNode,
    scope: UiStateScope,
    ctx: RenderContext,
    itemContext: Map<String, Any?> = emptyMap(),
) {
    val visible = rememberResolvedBool(node.props["visible"] ?: true, scope, itemContext)
    val duration = node.propInt("durationMillis", 220).coerceAtLeast(1)
    val enter = parseEnterTransition(node.propString("enter"), duration)
    val exit = parseExitTransition(node.propString("exit"), duration)

    AnimatedVisibility(
        visible = visible,
        modifier = ctx.modifierPipeline.build(node.modifier),
        enter = enter,
        exit = exit,
    ) {
        node.children.forEach { child ->
            UiNodeRenderer.Render(child, scope, ctx, itemContext)
        }
    }
}

@Composable
private fun AnimatedContentRenderer(
    node: UiNode,
    scope: UiStateScope,
    ctx: RenderContext,
    itemContext: Map<String, Any?> = emptyMap(),
) {
    val targetState = rememberResolvedString(node.props["targetState"] ?: node.props["state"], scope, itemContext)
    val duration = node.propInt("durationMillis", 220).coerceAtLeast(1)

    AnimatedContent(
        targetState = targetState,
        modifier = ctx.modifierPipeline.build(node.modifier),
        transitionSpec = {
            fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
        },
        label = "DynamicUiAnimatedContent",
    ) { state ->
        val contentNode = resolveStateContentNode(node = node, state = state)
            ?: node.children.firstOrNull()
        contentNode?.let { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
    }
}

@Composable
private fun CrossfadeRenderer(
    node: UiNode,
    scope: UiStateScope,
    ctx: RenderContext,
    itemContext: Map<String, Any?> = emptyMap(),
) {
    val targetState = rememberResolvedString(node.props["targetState"] ?: node.props["state"], scope, itemContext)
    val duration = node.propInt("durationMillis", 220).coerceAtLeast(1)

    Crossfade(
        targetState = targetState,
        modifier = ctx.modifierPipeline.build(node.modifier),
        animationSpec = tween(duration),
        label = "DynamicUiCrossfade",
    ) { state ->
        val contentNode = resolveStateContentNode(node = node, state = state)
            ?: node.children.firstOrNull()
        contentNode?.let { UiNodeRenderer.Render(it, scope, ctx, itemContext) }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Suppress("UNCHECKED_CAST")
private fun resolveList(expr: String, scope: UiStateScope, itemContext: Map<String, Any?>): List<Any> {
    val raw = ValueExprResolver.resolve(expr, scope, itemContext)
    return when (raw) {
        is List<*> -> raw.filterNotNull()
        else -> emptyList()
    }
}

@Suppress("UNCHECKED_CAST")
private fun itemToContext(item: Any): Map<String, Any?> = when (item) {
    is Map<*, *> -> item as Map<String, Any?>
    else -> mapOf("value" to item)
}

private fun extractKey(item: Any, keyField: String): Any = when (item) {
    is Map<*, *> -> item[keyField] ?: item.hashCode()
    else -> item.hashCode()
}

@Suppress("UNCHECKED_CAST")
private fun resolveStateContentNode(node: UiNode, state: String): UiNode? {
    val contentMap = node.props["contentMap"] as? Map<String, Any?> ?: return null
    return (contentMap[state] ?: contentMap["default"]) as? UiNode
}

private fun parseEnterTransition(raw: String?, durationMillis: Int): EnterTransition = when (raw) {
    "none" -> EnterTransition.None
    "scale" -> scaleIn(animationSpec = tween(durationMillis))
    "slideVertical" -> slideInVertically(animationSpec = tween(durationMillis))
    "slideHorizontal" -> slideInHorizontally(animationSpec = tween(durationMillis))
    null, "", "fade" -> fadeIn(animationSpec = tween(durationMillis))
    else -> fadeIn(animationSpec = tween(durationMillis))
}

private fun parseExitTransition(raw: String?, durationMillis: Int): ExitTransition = when (raw) {
    "none" -> ExitTransition.None
    "scale" -> scaleOut(animationSpec = tween(durationMillis))
    "slideVertical" -> slideOutVertically(animationSpec = tween(durationMillis))
    "slideHorizontal" -> slideOutHorizontally(animationSpec = tween(durationMillis))
    null, "", "fade" -> fadeOut(animationSpec = tween(durationMillis))
    else -> fadeOut(animationSpec = tween(durationMillis))
}

// ─── Arrangement / Alignment parsers ─────────────────────────────────────────

private fun parseVerticalArrangement(raw: String?): Arrangement.Vertical = when (raw) {
    "Center" -> Arrangement.Center
    "Bottom" -> Arrangement.Bottom
    "Top", null -> Arrangement.Top
    else -> if (raw.startsWith("spacedBy")) {
        val dp = parseDp(raw.substringAfter("spacedBy(").substringBefore(")")) ?: 8.dp
        Arrangement.spacedBy(dp)
    } else Arrangement.Top
}

private fun parseHorizontalArrangement(raw: String?): Arrangement.Horizontal = when (raw) {
    "Center" -> Arrangement.Center
    "End" -> Arrangement.End
    "SpaceBetween" -> Arrangement.SpaceBetween
    "SpaceAround" -> Arrangement.SpaceAround
    "SpaceEvenly" -> Arrangement.SpaceEvenly
    "Start", null -> Arrangement.Start
    else -> if (raw.startsWith("spacedBy")) {
        val dp = parseDp(raw.substringAfter("spacedBy(").substringBefore(")")) ?: 8.dp
        Arrangement.spacedBy(dp)
    } else Arrangement.Start
}

private fun parseHorizontalAlignment(raw: String?): Alignment.Horizontal = when (raw) {
    "Center", "CenterHorizontally" -> Alignment.CenterHorizontally
    "End" -> Alignment.End
    "Start", null -> Alignment.Start
    else -> Alignment.Start
}

private fun parseVerticalAlignmentHoriz(raw: String?): Alignment.Vertical = when (raw) {
    "Center", "CenterVertically" -> Alignment.CenterVertically
    "Bottom" -> Alignment.Bottom
    "Top", null -> Alignment.Top
    else -> Alignment.Top
}

private fun parseBoxAlignment(raw: String?): Alignment = when (raw) {
    "Center" -> Alignment.Center
    "TopStart" -> Alignment.TopStart
    "TopCenter" -> Alignment.TopCenter
    "TopEnd" -> Alignment.TopEnd
    "CenterStart" -> Alignment.CenterStart
    "CenterEnd" -> Alignment.CenterEnd
    "BottomStart" -> Alignment.BottomStart
    "BottomCenter" -> Alignment.BottomCenter
    "BottomEnd" -> Alignment.BottomEnd
    null -> Alignment.TopStart
    else -> Alignment.TopStart
}

private val Int.dp get() = androidx.compose.ui.unit.Dp(this.toFloat())
