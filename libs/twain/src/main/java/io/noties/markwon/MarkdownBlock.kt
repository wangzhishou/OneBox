package io.noties.markwon

import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.noties.jlatexmath.JLatexMathDrawable

private val latexDrawableCache = object : LruCache<String, JLatexMathDrawable>(64) {}

/**
 * Inline LaTeX math renderer.
 *
 * Uses [LocalTextStyle] by default so the math size/color matches surrounding text.
 */
@Composable
fun MarkdownJLatexInlineMath(
    latex: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    background: Int = Color.Transparent.toArgb(),
) {
    val resolvedSize: TextUnit = if (textStyle.fontSize.isSpecified) textStyle.fontSize else 16.sp
    val resolvedColor = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onSurface }

    val density = LocalDensity.current
    val textSizePx = remember(resolvedSize, density) {
        with(density) { resolvedSize.toPx() }
    }

    MarkdownJLatexMathInternal(
        latex = latex,
        modifier = modifier,
        textSizePx = textSizePx.toInt().coerceAtLeast(10),
        textColor = resolvedColor.toArgb(),
        background = background
    )
}

/**
 * Block LaTeX math renderer.
 */
@Composable
fun MarkdownJLatexBlockMath(
    latex: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    background: Int = Color.Transparent.toArgb(),
) {
    val resolvedSize: TextUnit = if (textStyle.fontSize.isSpecified) textStyle.fontSize else 20.sp
    val resolvedColor = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onSurface }

    val density = LocalDensity.current
    val textSizePx = remember(resolvedSize, density) {
        with(density) { resolvedSize.toPx() }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        MarkdownJLatexMathInternal(
            latex = latex,
            modifier = Modifier,
            textSizePx = textSizePx.toInt().coerceAtLeast(12),
            textColor = resolvedColor.toArgb(),
            background = background
        )
    }
}

/**
 * Backward compatible API: defaults to block-like rendering.
 */
@Deprecated(
    message = "Use MarkdownJLatexInlineMath or MarkdownJLatexBlockMath instead",
    replaceWith = ReplaceWith("MarkdownJLatexBlockMath(latex, modifier)")
)
@Composable
fun MarkdownJLatexNodeMath(
    latex: String,
    modifier: Modifier = Modifier,
    textSize: Int = 40,
    textColor: Int = MaterialTheme.colorScheme.onSurface.toArgb(),
    background: Int = Color.Transparent.toArgb()
) {
    MarkdownJLatexMathInternal(
        latex = latex,
        modifier = modifier,
        textSizePx = textSize,
        textColor = textColor,
        background = background
    )
}

@Composable
@Suppress("DEPRECATION")
private fun MarkdownJLatexMathInternal(
    latex: String,
    modifier: Modifier,
    textSizePx: Int,
    textColor: Int,
    background: Int,
) {
    var drawable by remember { mutableStateOf<JLatexMathDrawable?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(latex, textSizePx, textColor, background) {
        val cacheKey = "$latex|$textSizePx|$textColor|$background"
        val cached = latexDrawableCache.get(cacheKey)
        if (cached != null) {
            drawable = cached
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        val builtDrawable = withContext(Dispatchers.IO) {
            try {
                JLatexMathDrawable.builder(latex)
                    .textSize(textSizePx.toFloat())
                    .color(textColor)
                    .background(background)
                    .fitCanvas(false)
                    .build()
            } catch (e: Exception) {
                Log.e("MarkdownJLatexMath", "Error rendering LaTeX: ${e.message}", e)
                null
            }
        }
        if (builtDrawable != null) {
            latexDrawableCache.put(cacheKey, builtDrawable)
        }
        drawable = builtDrawable
        isLoading = false
    }

    when {
        isLoading -> {
            CircularProgressIndicator(
                modifier = modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }

        drawable != null -> {
            val painter = rememberDrawablePainter(drawable!!)
            Image(
                painter = painter,
                contentDescription = "LaTeX数学公式",
                modifier = modifier
            )
        }

        else -> {
            Text(
                text = "无法渲染LaTeX: $latex",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                modifier = modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun rememberDrawablePainter(drawable: Drawable): Painter {
    val density = LocalDensity.current
    return remember(drawable) {
        object : Painter() {
            override val intrinsicSize: Size
                get() = Size(
                    drawable.intrinsicWidth.toFloat(),
                    drawable.intrinsicHeight.toFloat()
                )

            override fun DrawScope.onDraw() {
                with(density) {
                    drawable.setBounds(
                        0, 0,
                        size.width.toInt(),
                        size.height.toInt()
                    )
                    drawIntoCanvas { canvas ->
                        drawable.draw(canvas.nativeCanvas)
                    }
                }
            }
        }
    }
}
