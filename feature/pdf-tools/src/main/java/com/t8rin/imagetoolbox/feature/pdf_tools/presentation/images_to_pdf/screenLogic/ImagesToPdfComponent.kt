package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.images_to_pdf.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.domain.model.ExtraDataType
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.utils.timestamp
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfCreationParams
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.random.Random

class ImagesToPdfComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialImageUris: List<Uri>?,
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
    pdfManager = pdfManager
) {

    override val _haveChanges: MutableState<Boolean> = mutableStateOf(!initialImageUris.isNullOrEmpty())
    override val haveChanges: Boolean by _haveChanges

    override val extraDataType: ExtraDataType? = null

    private val _imagesToPdfState: MutableState<List<Uri>?> = mutableStateOf(initialImageUris)
    val imagesToPdfState: List<Uri>? by _imagesToPdfState

    private val _presetSelected: MutableState<Preset.Percentage> =
        mutableStateOf(Preset.Percentage(100))
    val presetSelected: Preset.Percentage by _presetSelected

    private val _scaleSmallImagesToLarge: MutableState<Boolean> = mutableStateOf(false)
    val scaleSmallImagesToLarge: Boolean by _scaleSmallImagesToLarge

    private val _quality: MutableState<Int> = mutableIntStateOf(85)
    val quality: Int by _quality

    val canShare: Boolean
        get() = !_imagesToPdfState.value.isNullOrEmpty() && !isSaving

    init {
        if (!initialImageUris.isNullOrEmpty()) registerChanges()
    }

    fun setImages(uris: List<Uri>?) {
        _imagesToPdfState.update { uris }
        if (uris.isNullOrEmpty()) registerChangesCleared() else registerChanges()
    }

    fun addImages(uris: List<Uri>) {
        _imagesToPdfState.update {
            it?.plus(uris)?.distinct() ?: uris
        }
        registerChanges()
    }

    fun removeAt(index: Int) {
        runCatching {
            _imagesToPdfState.update {
                it?.toMutableList()?.apply { removeAt(index) }
            }
            registerChanges()
        }
    }

    fun reorder(uris: List<Uri>?) {
        _imagesToPdfState.update { uris }
        registerChanges()
    }

    fun toggleScaleSmallImagesToLarge() {
        _scaleSmallImagesToLarge.update { !it }
        registerChanges()
    }

    fun setQuality(quality: Int) {
        _quality.update { quality }
        registerChanges()
    }

    fun selectPreset(preset: Preset.Percentage) {
        _presetSelected.update { preset }
        registerChanges()
    }

    fun save(targetUri: Uri) {
        val uris = _imagesToPdfState.value ?: return
        doSaving {
            fileController.transferBytes(
                fromUri = createPdf(uris),
                toUri = targetUri.toString()
            ).onSuccess(::registerSave)
        }
    }

    fun share(
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val uris = _imagesToPdfState.value ?: return
        doSharing(
            action = {
                shareProvider.shareUri(
                    uri = createPdf(uris),
                    onComplete = onSuccess
                )
            },
            onFailure = onFailure
        )
    }

    override fun saveTo(uri: Uri) = save(uri)

    override fun performSharing(
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        prepareForSharing(
            onSuccess = {
                shareProvider.shareUris(it.map(Uri::toString))
                registerSave()
                onSuccess()
            },
            onFailure = onFailure
        )
    }

    override fun prepareForSharing(
        onSuccess: suspend (List<Uri>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        doSharing(
            action = {
                val uris = _imagesToPdfState.value ?: emptyList()
                onSuccess(listOf(createPdf(uris).toUri()))
                registerSave()
            },
            onFailure = onFailure
        )
    }

    fun clear() {
        _imagesToPdfState.update { null }
        _scaleSmallImagesToLarge.update { false }
        _presetSelected.update { Preset.Percentage(100) }
        registerChangesCleared()
    }

    fun generatePdfFilename(): String {
        val stamp = "${timestamp()}_${Random(Random.nextInt()).hashCode().toString().take(4)}"
        return "PDF_$stamp.pdf"
    }

    private val pdfCreationParams: PdfCreationParams
        get() = PdfCreationParams(
            scaleSmallImagesToLarge = _scaleSmallImagesToLarge.value,
            preset = _presetSelected.value,
            quality = _quality.value
        )

    private suspend fun createPdf(uris: List<Uri>): String {
        return pdfManager.createPdf(
            imageUris = uris.map { it.toString() },
            params = pdfCreationParams
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialImageUris: List<Uri>?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ImagesToPdfComponent
    }

    companion object {
        private const val SCREEN_ROUTE = "images_to_pdf"
    }
}
