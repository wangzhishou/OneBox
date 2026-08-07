package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.pdf_to_images.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.ImageTransformer
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.model.ExtraDataType
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.KeepAliveService
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.saving.model.onSuccess
import com.t8rin.imagetoolbox.core.domain.saving.updateProgress
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.ExtractPagesAction
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfExtractPagesParams
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion

class ExtractPagesPdfToolComponent @AssistedInject internal constructor(
    @Assisted val initialUri: Uri?,
    @Assisted componentContext: ComponentContext,
    @Assisted onGoBack: () -> Unit,
    @Assisted onNavigate: (Screen) -> Unit,
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageTransformer: ImageTransformer<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val pdfManager: PdfManager,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val fileController: FileController,
    dispatchersHolder: DispatchersHolder
) : BasePdfToolComponent(
    onGoBack = onGoBack,
    onNavigate = onNavigate,
    dispatchersHolder = dispatchersHolder,
    componentContext = componentContext,
    pdfManager = pdfManager
) {
    override val _haveChanges: MutableState<Boolean> = mutableStateOf(false)
    override val haveChanges: Boolean by _haveChanges

    override val extraDataType: ExtraDataType? = null

    private val _uri: MutableState<Uri?> = mutableStateOf(null)
    val uri by _uri

    private val _params: MutableState<PdfExtractPagesParams> =
        mutableStateOf(PdfExtractPagesParams())
    val params by _params

    private val _imageInfo = mutableStateOf(ImageInfo())
    val imageInfo by _imageInfo

    init {
        if (initialUri != null) {
            checkPdf(
                uri = initialUri,
                onDecrypted = { _uri.value = it },
                onSuccess = { _uri.value = it }
            )
        }
        componentScope.launch {
            snapshotFlow { uri }
                .distinctUntilChanged()
                .collect {
                    _params.update { it.copy(pages = null) }
                }
        }
    }

    fun setUri(uri: Uri?) {
        if (uri == null) {
            registerChangesCleared()
        } else {
            registerChanges()
        }
        _uri.update { uri }
        checkPdf(
            uri = uri,
            onDecrypted = { _uri.value = it }
        )
    }

    fun updatePages(pages: List<Int>) {
        registerChanges()
        _params.update {
            it.copy(
                pages = pages
            )
        }
    }

    fun selectPreset(preset: Preset.Percentage) {
        preset.value()?.takeIf { it <= 100f }?.let { quality ->
            _imageInfo.update {
                it.copy(
                    quality = when (val q = it.quality) {
                        is Quality.Base -> q.copy(qualityValue = quality)
                        else -> q
                    }
                )
            }
        }
        _params.update {
            it.copy(
                preset = preset
            )
        }
        registerChanges()
    }

    fun updateImageFormat(imageFormat: ImageFormat) {
        _imageInfo.update {
            it.copy(imageFormat = imageFormat)
        }
        registerChanges()
    }

    fun setQuality(quality: Quality) {
        _imageInfo.update {
            it.copy(quality = quality)
        }
        registerChanges()
    }

    fun save(
        oneTimeSaveLocationUri: String?
    ) {
        doSharing(
            action = {
                extractPages(
                    onPage = { bitmap, imageInfo ->
                        fileController.save(
                            saveTarget = ImageSaveTarget(
                                imageInfo = imageInfo,
                                metadata = null,
                                originalUri = uri?.toString().orEmpty(),
                                sequenceNumber = null,
                                data = imageCompressor.compressAndTransform(
                                    image = bitmap,
                                    imageInfo = imageInfo
                                )
                            ),
                            keepOriginalMetadata = false,
                            oneTimeSaveLocationUri = oneTimeSaveLocationUri
                        )
                    },
                    onSuccess = {
                        parseSaveResults(it.onSuccess(::registerSave))
                    },
                    onFailure = {
                        parseSaveResults(listOf(SaveResult.Error.Exception(it)))
                    }
                )
            },
            onFailure = {
                parseSaveResults(listOf(SaveResult.Error.Exception(it)))
            }
        )
    }

    override fun saveTo(
        uri: Uri
    ) = Unit

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
                extractPages(
                    onPage = { bitmap, imageInfo ->
                        shareProvider.cacheImage(
                            imageInfo = imageInfo,
                            image = bitmap
                        )?.toUri()
                    },
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onFailure = onFailure
        )
    }

    private suspend fun <T : Any> KeepAliveService.extractPages(
        onPage: suspend (Bitmap, ImageInfo) -> T?,
        onSuccess: suspend (List<T>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        var done = 0
        var left = 1

        val results = mutableListOf<T>()

        pdfManager.extractPages(
            uri = uri.toString(),
            params = params
        ).onCompletion {
            onSuccess(results)
            registerSave()
        }.catch {
            onFailure(it)
        }.collect { action ->
            when (action) {
                is ExtractPagesAction.PagesCount -> left = action.count

                is ExtractPagesAction.Progress -> {
                    val bitmap = imageGetter.getImage(action.image) ?: return@collect
                    val imageInfo = imageTransformer.applyPresetBy(
                        image = bitmap,
                        preset = params.preset,
                        currentInfo = imageInfo
                    ).copy(
                        originalUri = uri?.toString()
                    )

                    onPage(bitmap, imageInfo)?.let(results::add)

                    done += 1
                    updateProgress(
                        done = done,
                        total = left
                    )
                }
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            initialUri: Uri?,
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ExtractPagesPdfToolComponent
    }
}
