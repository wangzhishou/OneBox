package com.halilibo.richtext.ui.mermaid

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import java.io.File

interface MermaidRenderer {

    @Composable
    fun RenderDiagram(
        code: String,
        modifier: Modifier,
    )

    suspend fun getCachedBitmap(code: String): Bitmap? = null

    fun getCachedSvg(code: String): String? = null

    fun getCachedSvgFile(code: String): File? = null
}

val LocalMermaidRenderer = compositionLocalOf<MermaidRenderer?> { null }
