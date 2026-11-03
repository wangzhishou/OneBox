package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.data.coil.PdfImageRequest
import com.wanbaohe.core.ui.widget.image.Picture

@Composable
internal fun PdfPagePreview(
    uri: Uri?,
    pageCount: Int,
    activePages: List<Int>? = null,
    overlay: @Composable BoxScope.(page: Int) -> Unit = { _ -> },
) {
    if (uri == null || pageCount <= 0) return

    PageSwitcher(
        pageCount = pageCount,
        activePages = activePages,
    ) { page ->
        var aspectRatio by rememberSaveable { mutableFloatStateOf(0.72f) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .aspectRatio(aspectRatio.coerceIn(0.2f, 5f))
        ) {
            Picture(
                model = remember(uri, page) {
                    PdfImageRequest(data = uri, pdfPage = page)
                },
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize(),
                showTransparencyChecker = false,
                onSuccess = { state ->
                    val image = state.result.image
                    if (image.height > 0) {
                        aspectRatio = image.width.toFloat() / image.height.toFloat()
                    }
                },
            )
            overlay(page)
        }
    }
}
