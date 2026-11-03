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
