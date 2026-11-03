package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.unlock.screenLogic

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
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfProcessComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

class UnlockPdfToolComponent @AssistedInject internal constructor(
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
    private val _previewUri: MutableState<Uri?> = mutableStateOf(null)
    val previewUri by _previewUri

    private val _password: MutableState<String> = mutableStateOf("")
    val password by _password

    private val _locked: MutableState<Boolean?> = mutableStateOf(null)
    val locked by _locked

    private val _passwordRejected: MutableState<Boolean> = mutableStateOf(false)
    val passwordRejected by _passwordRejected

    private val _cannotOpen: MutableState<Boolean> = mutableStateOf(false)
    val cannotOpen by _cannotOpen

    private var inspectJob: Job? by smartJob()
    private var previewJob: Job? by smartJob()

    init {
        inspect(initialUri)
    }

    override fun getKey(): Pair<String, Uri?> = "unlocked" to uri

    override fun onSecurityFailure() {
        _passwordRejected.value = true
    }

    fun setUri(uri: Uri?) {
        setSourceUri(uri)
        _password.update { "" }
        _passwordRejected.update { false }
        _cannotOpen.update { false }
        _previewUri.update { null }
        _locked.update { null }
        inspect(uri)
    }

    fun updatePassword(password: String) {
        registerChanges()
        _password.update { password }
        _passwordRejected.update { false }
        _cannotOpen.update { false }
        // Cancel the pending check, the old preview stays visible until the new one is
        // ready so the preview does not blink on every keystroke.
        previewJob = null
        schedulePreview()
    }

    private fun schedulePreview() {
        val source = _uri.value ?: return
        val typedPassword = _password.value
        if (typedPassword.isEmpty() || _locked.value != true) return

        previewJob = componentScope.launch {
            delay(PASSWORD_CHECK_DEBOUNCE)
            ensureActive()
            val matches = runCatching {
                pdfManager.passwordMatches(source.toString(), typedPassword)
            }.getOrElse {
                ensureActive()
                _cannotOpen.value = true
                return@launch
            }
            ensureActive()
            if (!matches) {
                _passwordRejected.value = true
                return@launch
            }
            runCatching {
                pdfManager.unlockPdf(source.toString(), typedPassword)
            }.onSuccess {
                _previewUri.value = it.toUri()
            }.onFailure {
                ensureActive()
                _cannotOpen.value = true
            }
        }
    }

    private fun inspect(uri: Uri?) {
        if (uri == null) return
        inspectJob = componentScope.launch {
            val open = runCatching {
                pdfManager.opensWithoutPassword(uri.toString())
            }.getOrElse {
                ensureActive()
                _cannotOpen.value = true
                return@launch
            }
            if (_uri.value != uri) return@launch
            _locked.value = !open
            if (open) {
                _previewUri.value = uri
            } else {
                schedulePreview()
            }
        }
    }

    override suspend fun processPdf(uri: String): String = pdfManager.unlockPdf(
        uri = uri,
        password = password,
    )

    private companion object {
        private const val PASSWORD_CHECK_DEBOUNCE = 400L
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            initialUri: Uri?,
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): UnlockPdfToolComponent
    }
}
