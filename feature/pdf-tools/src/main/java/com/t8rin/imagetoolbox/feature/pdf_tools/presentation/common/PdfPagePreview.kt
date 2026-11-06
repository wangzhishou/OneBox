package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.imageLoader
import coil3.size.Size
import com.t8rin.imagetoolbox.core.data.coil.PdfImageRequest
import com.t8rin.imagetoolbox.core.utils.makeLog
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.wanbaohe.core.ui.widget.image.BadImageWidget
import kotlin.math.roundToInt

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .aspectRatio(aspectRatio),
            contentAlignment = Alignment.Center,
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }.roundToInt()
            val heightPx = with(density) { maxHeight.toPx() }.roundToInt()
            if (widthPx > 0 && heightPx > 0) {
                val request = remember(uri, page, widthPx, heightPx) {
                    PdfImageRequest(
                        data = uri,
                        pdfPage = page,
                        size = Size(widthPx, heightPx),
                    )
                }
                val painter = rememberAsyncImagePainter(
                    model = request,
                    imageLoader = LocalComponentActivity.current.imageLoader,
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Success -> {
                                val image = state.result.image
                                if (image.height > 0) {
                                    aspectRatio = image.width.toFloat() / image.height.toFloat()
                                }
                            }
                            is AsyncImagePainter.State.Error -> {
                                state.result.throwable.makeLog("PdfPagePreview")
                            }
                            else -> Unit
                        }
                    },
                    contentScale = ContentScale.Fit,
                )
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                val imageState by painter.state.collectAsState()
                when (imageState) {
                    is AsyncImagePainter.State.Loading -> EnhancedLoadingIndicator()
                    is AsyncImagePainter.State.Error -> BadImageWidget()
                    else -> Unit
                }
            }
            overlay(page)
        }
    }
}
