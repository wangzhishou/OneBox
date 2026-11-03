package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.signature.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfSignatureParams
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class SignaturePdfToolComponent @AssistedInject internal constructor(
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

    private val _params: MutableState<PdfSignatureParams> = mutableStateOf(
        PdfSignatureParams(opacity = 1f)
    )
    val params by _params

    private val _hasSignature: MutableState<Boolean> = mutableStateOf(false)
    val hasSignature by _hasSignature

    val savedSignatures: StateFlow<List<String>> = pdfManager.savedSignatures

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

    override fun getKey(): Pair<String, Uri?> = "signed" to uri

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

    fun updateParams(params: PdfSignatureParams) {
        registerChanges()
        _params.update { params }
    }

    fun ensurePages(pages: List<Int>) {
        if (_params.value.pages.isNotEmpty() || pages.isEmpty()) return
        _params.update { it.copy(pages = pages) }
    }

    fun updateSignature(data: Any, save: Boolean = false) {
        registerChanges()
        _hasSignature.update { true }
        _params.update { it.copy(signatureImage = data, opacity = if (save) 1f else it.opacity) }
        if (save) {
            componentScope.launch {
                pdfManager.saveSignature(data)
            }
        }
    }

    override fun saveTo(uri: Uri) {
        doSaving {
            val processed = pdfManager.addSignature(
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
                        pdfManager.addSignature(
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
        ): SignaturePdfToolComponent
    }
}
