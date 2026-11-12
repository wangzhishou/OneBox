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

package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.watermark.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfWatermarkParams
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfProcessComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.distinctUntilChanged

class WatermarkPdfToolComponent @AssistedInject internal constructor(
    @Assisted val initialUri: Uri?,
    @Assisted componentContext: ComponentContext,
    @Assisted onGoBack: () -> Unit,
    @Assisted onNavigate: (Screen) -> Unit,
    pdfManager: PdfManager,
    shareProvider: ImageShareProvider<Bitmap>,
    fileController: FileController,
    dispatchersHolder: DispatchersHolder,
) : BasePdfProcessComponent(
    initialUri = initialUri,
    onGoBack = onGoBack,
    onNavigate = onNavigate,
    dispatchersHolder = dispatchersHolder,
    componentContext = componentContext,
    pdfManager = pdfManager,
    shareProvider = shareProvider,
    fileController = fileController,
) {
    private val _params: MutableState<PdfWatermarkParams> = mutableStateOf(
        PdfWatermarkParams(color = Color.Gray.toArgb())
    )
    val params by _params

    init {
        refreshPageSizes(initialUri)
        checkPdf(
            uri = initialUri,
            onDecrypted = {
                _uri.value = it
                refreshPageSizes(it)
            },
        )
        componentScope.launch {
            snapshotFlow { uri }
                .distinctUntilChanged()
                .collect {
                    _params.update { current -> current.copy(pages = emptyList()) }
                }
        }
    }

    override fun getKey(): Pair<String, Uri?> = "watermarked" to uri

    fun setUri(uri: Uri?) {
        setSourceUri(uri)
        refreshPageSizes(uri)
        checkPdf(
            uri = uri,
            onDecrypted = {
                _uri.value = it
                refreshPageSizes(it)
            },
        )
    }

    fun updateParams(params: PdfWatermarkParams) {
        registerChanges()
        _params.update { params }
    }

    fun ensurePages(pages: List<Int>) {
        if (_params.value.pages.isNotEmpty() || pages.isEmpty()) return
        _params.update { it.copy(pages = pages) }
    }

    override suspend fun processPdf(uri: String): String = pdfManager.addWatermark(
        uri = uri,
        params = params,
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            initialUri: Uri?,
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): WatermarkPdfToolComponent
    }
}
