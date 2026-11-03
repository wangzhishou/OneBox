package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.protect.screenLogic

import android.content.ClipData
import android.content.ClipboardManager
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
import com.t8rin.imagetoolbox.core.domain.saving.RandomStringGenerator
import com.t8rin.imagetoolbox.core.utils.appContext
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ProtectPdfToolComponent @AssistedInject internal constructor(
    @Assisted val initialUri: Uri?,
    @Assisted componentContext: ComponentContext,
    @Assisted onGoBack: () -> Unit,
    @Assisted onNavigate: (Screen) -> Unit,
    private val pdfManager: PdfManager,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val fileController: FileController,
    private val randomStringGenerator: RandomStringGenerator,
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

    private val _password: MutableState<String> = mutableStateOf("")
    val password by _password

    private val _confirmPassword: MutableState<String> = mutableStateOf("")
    val confirmPassword by _confirmPassword

    private val _passwordCopied: MutableState<Boolean> = mutableStateOf(false)
    val passwordCopied by _passwordCopied

    init {
        checkPdf(
            uri = initialUri,
            onDecrypted = { _uri.value = it },
        )
    }

    override fun getKey(): Pair<String, Uri?> = "protected" to uri

    fun setUri(uri: Uri?) {
        if (uri == null) registerChangesCleared() else registerChanges()
        _uri.update { uri }
        checkPdf(
            uri = uri,
            onDecrypted = { _uri.value = it },
        )
    }

    fun updatePassword(password: String) {
        registerChanges()
        _passwordCopied.update { false }
        _password.update { password }
    }

    fun updateConfirmPassword(password: String) {
        registerChanges()
        _confirmPassword.update { password }
    }

    fun fillGeneratedPassword() {
        val generated = randomStringGenerator.generate(18)
        registerChanges()
        _password.update { generated }
        _confirmPassword.update { generated }
        copyPassword()
    }

    private fun copyPassword() {
        val clipboard = appContext.getSystemService(ClipboardManager::class.java) ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("pdf-password", password))
        _passwordCopied.value = true
    }

    override fun saveTo(uri: Uri) {
        copyPassword()
        doSaving {
            val processed = pdfManager.protectPdf(
                uri = _uri.value.toString(),
                password = password,
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
                        pdfManager.protectPdf(
                            uri = _uri.value.toString(),
                            password = password,
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
        ): ProtectPdfToolComponent
    }
}
