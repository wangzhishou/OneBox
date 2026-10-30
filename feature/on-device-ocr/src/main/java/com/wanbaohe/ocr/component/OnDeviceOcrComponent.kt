package com.wanbaohe.ocr.component

import android.graphics.Bitmap
import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.data.OcrBitmapDecoder
import com.wanbaohe.ocr.data.OcrMarkdownWriter
import com.wanbaohe.ocr.data.OcrModelStore
import com.wanbaohe.ocr.data.PpOcrV6Engine
import com.wanbaohe.ocr.domain.RecognizedLine
import com.wanbaohe.ocr.domain.toMarkdown
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OnDeviceOcrUiState(
    val modelReady: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float? = null,
    val isRecognizing: Boolean = false,
    val isSavingMarkdown: Boolean = false,
    val preview: Bitmap? = null,
    val lines: List<RecognizedLine> = emptyList(),
    val selectedIndex: Int? = null,
    val errorRes: Int? = null,
)

class OnDeviceOcrComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("onGoBack") val onGoBack: () -> Unit,
    @Assisted("onOpenCloudOcr") val onOpenCloudOcr: () -> Unit,
    private val modelStore: OcrModelStore,
    private val decoder: OcrBitmapDecoder,
    private val engine: PpOcrV6Engine,
    private val markdownWriter: OcrMarkdownWriter,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(OnDeviceOcrUiState(modelReady = modelStore.isReady()))
    val uiState: StateFlow<OnDeviceOcrUiState> = _uiState.asStateFlow()

    fun downloadModel() {
        if (_uiState.value.isDownloading || modelStore.isReady()) return
        componentScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isDownloading = true, errorRes = null, downloadProgress = 0f) }
            try {
                modelStore.ensureDownloaded { progress ->
                    _uiState.update { state -> state.copy(downloadProgress = progress) }
                }
                _uiState.update { it.copy(isDownloading = false, modelReady = true, downloadProgress = 1f) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                error.makeLog("OnDeviceOcr")
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        modelReady = false,
                        errorRes = R.string.ocr_download_failed,
                    )
                }
            }
        }
    }

    fun onImagePicked(uri: Uri) {
        if (!modelStore.isReady()) {
            _uiState.update {
                it.copy(
                    modelReady = false,
                    isRecognizing = false,
                    preview = null,
                    lines = emptyList(),
                    selectedIndex = null,
                    errorRes = R.string.ocr_need_model,
                )
            }
            return
        }
        componentScope.launch(decodingDispatcher) {
            _uiState.update {
                it.copy(isRecognizing = true, errorRes = null, lines = emptyList(), selectedIndex = null)
            }
            val bitmap = try {
                decoder.decode(uri)
            } catch (error: Exception) {
                error.makeLog("OnDeviceOcr")
                _uiState.update { it.copy(isRecognizing = false, errorRes = R.string.ocr_decode_failed) }
                return@launch
            }
            if (!modelStore.isReady()) {
                _uiState.update {
                    it.copy(
                        modelReady = false,
                        preview = null,
                        isRecognizing = false,
                        errorRes = R.string.ocr_need_model,
                    )
                }
                return@launch
            }
            try {
                val lines = engine.recognize(bitmap)
                _uiState.update {
                    it.copy(preview = bitmap, lines = lines, isRecognizing = false, selectedIndex = null)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                error.makeLog("OnDeviceOcr")
                _uiState.update {
                    it.copy(
                        preview = bitmap,
                        isRecognizing = false,
                        errorRes = R.string.ocr_recognize_failed,
                    )
                }
            }
        }
    }

    fun select(index: Int?) {
        _uiState.update { state ->
            when {
                index == null -> state.copy(selectedIndex = null)
                index in state.lines.indices -> state.copy(selectedIndex = index)
                else -> state
            }
        }
    }

    fun saveMarkdown(uri: Uri, onComplete: (Boolean) -> Unit) {
        val content = _uiState.value.lines.toMarkdown()
        if (content.isBlank() || _uiState.value.isSavingMarkdown) return
        componentScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSavingMarkdown = true) }
            val saved = try {
                markdownWriter.write(uri, content)
                true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                error.makeLog("OnDeviceOcr")
                false
            }
            withContext(uiDispatcher) {
                _uiState.update { it.copy(isSavingMarkdown = false) }
                onComplete(saved)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("onGoBack") onGoBack: () -> Unit,
            @Assisted("onOpenCloudOcr") onOpenCloudOcr: () -> Unit,
        ): OnDeviceOcrComponent
    }
}
