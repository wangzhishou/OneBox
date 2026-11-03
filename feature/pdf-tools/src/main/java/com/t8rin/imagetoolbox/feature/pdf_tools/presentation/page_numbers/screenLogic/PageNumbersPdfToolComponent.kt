package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.page_numbers.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
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
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PageNumbersPdfToolComponent @AssistedInject internal constructor(
    @Assisted val initialUri: Uri?,
    @Assisted componentContext: ComponentContext,
    @Assisted onGoBack: () -> Unit,
    @Assisted onNavigate: (Screen) -> Unit,
    private val pdfManager: PdfManager,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val fileController: FileController,
    dispatchersHolder: DispatchersHolder,
) : BasePdfToolComponent(
    onGoBack = onGoBack,
    onNavigate = onNavigate,
    dispatchersHolder = dispatchersHolder,
    componentContext = componentContext,
    pdfManager = pdfManager,
) {
    override val _haveChanges: MutableState<Boolean> = mutableStateOf(initialUri != null)
    override val haveChanges: Boolean by _haveChanges

    private val _uri: MutableState<Uri?> = mutableStateOf(initialUri)
    val uri by _uri

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
        if (uri == null) registerChangesCleared() else registerChanges()
        _uri.update { uri }
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

    override fun saveTo(uri: Uri) {
        doSaving {
            val processed = pdfManager.addPageNumbers(
                uri = _uri.value.toString(),
                params = params,
            )
            fileController.transferBytes(
                fromUri = processed,
                toUri = uri.toString(),
            ).onSuccess(::registerSave)
        }
    }

    override fun performSharing(
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        prepareForSharing(
            onSuccess = {
                shareProvider.shareUris(it.map(Uri::toString))
                registerSave()
                onSuccess()
            },
            onFailure = onFailure,
        )
    }

    override fun prepareForSharing(
        onSuccess: suspend (List<Uri>) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        doSharing(
            action = {
                onSuccess(
                    listOf(
                        pdfManager.addPageNumbers(
                            uri = _uri.value.toString(),
                            params = params,
                        ).toUri()
                    )
                )
                registerSave()
            },
            onFailure = onFailure,
        )
    }

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
