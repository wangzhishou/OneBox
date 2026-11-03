package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.imagetoolbox.feature.pdf_tools.data.utils.defaultPdfFontFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun rememberPdfTextStyle(): TextStyle {
    val style by produceState(
        initialValue = TextStyle(fontWeight = FontWeight.Bold)
    ) {
        val fontFile = withContext(Dispatchers.IO) {
            runCatching { appContext.defaultPdfFontFile() }.getOrNull()
        }
        value = TextStyle(
            fontFamily = fontFile?.let {
                FontFamily(Font(file = it, weight = FontWeight.Bold))
            } ?: FontFamily.Default,
            fontWeight = FontWeight.Bold,
        )
    }
    return style
}
