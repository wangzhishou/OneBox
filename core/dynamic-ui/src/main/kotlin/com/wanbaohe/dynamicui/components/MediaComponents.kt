package com.wanbaohe.dynamicui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.wanbaohe.dynamicui.ir.UiNode
import com.wanbaohe.dynamicui.ir.propBool
import com.wanbaohe.dynamicui.ir.propFloat
import com.wanbaohe.dynamicui.ir.propString
import com.wanbaohe.dynamicui.renderer.ComponentRegistry
import com.wanbaohe.dynamicui.renderer.RenderContext
import com.wanbaohe.dynamicui.state.UiStateScope
import com.wanbaohe.dynamicui.state.rememberResolvedBool
import com.wanbaohe.dynamicui.state.rememberResolvedString

/**
 * Media component renderers: AsyncImage (Coil3), LottieAnimation.
 * Registered via [registerMediaComponents].
 */

// ─── Registration ────────────────────────────────────────────────────────────

fun ComponentRegistry.registerMediaComponents() {
    register("AsyncImage") { node, scope, ctx, itemCtx -> AsyncImageRenderer(node, scope, ctx, itemCtx) }
    register("Image") { node, scope, ctx, itemCtx -> AsyncImageRenderer(node, scope, ctx, itemCtx) }
    register("NetworkImage") { node, scope, ctx, itemCtx -> AsyncImageRenderer(node, scope, ctx, itemCtx) }
    register("LottieAnimation") { node, scope, ctx, itemCtx -> LottieRenderer(node, scope, ctx, itemCtx) }
    register("Lottie") { node, scope, ctx, itemCtx -> LottieRenderer(node, scope, ctx, itemCtx) }
}

// ─── AsyncImage (Coil 3) ──────────────────────────────────────────────────────

@Composable
private fun AsyncImageRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val model = rememberResolvedString(
        node.props["url"] ?: node.props["model"] ?: node.props["src"],
        scope,
        itemContext,
    )
    val contentDescription = node.propString("contentDescription")
    val contentScale = parseContentScale(node.propString("contentScale").ifEmpty { node.propString("scale") })

    AsyncImage(
        model = model.ifEmpty { null },
        contentDescription = contentDescription,
        modifier = ctx.modifierPipeline.build(node.modifier),
        contentScale = contentScale,
    )
}

// ─── Lottie ──────────────────────────────────────────────────────────────────

@Composable
private fun LottieRenderer(node: UiNode, scope: UiStateScope, ctx: RenderContext, itemContext: Map<String, Any?> = emptyMap()) {
    val url = rememberResolvedString(
        node.props["url"] ?: node.props["src"] ?: node.props["asset"],
        scope,
        itemContext,
    )
    val isLooping = node.propString("loop") != "false"
    val isPlaying = rememberResolvedBool(node.props["isPlaying"] ?: true, scope, itemContext)
    val speed = node.propFloat("speed", 1f)

    val spec = if (url.startsWith("http")) {
        LottieCompositionSpec.Url(url)
    } else {
        LottieCompositionSpec.Asset(url)
    }

    val composition by rememberLottieComposition(spec)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = if (isLooping) LottieConstants.IterateForever else 1,
        speed = speed,
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = ctx.modifierPipeline.build(node.modifier).let {
            if (node.modifier.isEmpty()) it.fillMaxSize() else it
        },
    )
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun parseContentScale(raw: String?): ContentScale = when (raw) {
    "Crop" -> ContentScale.Crop
    "FillBounds" -> ContentScale.FillBounds
    "FillHeight" -> ContentScale.FillHeight
    "FillWidth" -> ContentScale.FillWidth
    "Fit" -> ContentScale.Fit
    "Inside" -> ContentScale.Inside
    "None" -> ContentScale.None
    else -> ContentScale.Crop
}

