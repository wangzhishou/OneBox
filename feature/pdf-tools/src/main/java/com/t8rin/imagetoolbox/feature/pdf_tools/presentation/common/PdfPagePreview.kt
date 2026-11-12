/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
