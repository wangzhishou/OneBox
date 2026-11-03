package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.page_numbers.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.utils.appContext
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfPageNumbersParams
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfProcessComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PageNumbersPdfToolComponent @AssistedInject internal constructor(
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
    private val _params: MutableState<PdfPageNumbersParams> = mutableStateOf(
        PdfPageNumbersParams(
            labelFormat = appContext.getString(R.string.page_number_format_default)
        )
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
    }

    override fun getKey(): Pair<String, Uri?> = "numbered" to uri

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

    fun updateParams(params: PdfPageNumbersParams) {
        registerChanges()
        _params.update { params }
    }

    override suspend fun processPdf(uri: String): String = pdfManager.addPageNumbers(
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
        ): PageNumbersPdfToolComponent
    }
}
