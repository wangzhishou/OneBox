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
        language: String,
    )

    suspend fun getCachedBitmap(code: String, language: String): Bitmap? = null

    fun getCachedSvg(code: String, language: String): String? = null

    fun getCachedSvgFile(code: String, language: String): File? = null
}

val LocalMermaidRenderer = compositionLocalOf<MermaidRenderer?> { null }
