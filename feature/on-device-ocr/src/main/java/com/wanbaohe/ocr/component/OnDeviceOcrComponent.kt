package com.wanbaohe.ocr.component

import android.graphics.Bitmap
import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.note.NoteResult
import com.shifenmiao.model.note.NoteSaveParams
import com.shifenmiao.model.note.NoteService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.data.OcrBitmapDecoder
import com.wanbaohe.ocr.data.OcrMarkdownWriter
import com.wanbaohe.ocr.data.OcrModelStore
import com.wanbaohe.ocr.data.PpOcrV6Engine
import com.wanbaohe.ocr.domain.OcrHistoryEntry
import com.wanbaohe.ocr.domain.OcrHistoryRepository
import com.wanbaohe.ocr.domain.RecognizedLine
import com.wanbaohe.ocr.domain.toMarkdown
import com.wanbaohe.ocr.domain.toPlainText
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

enum class OcrTab {
    RECOGNIZE,
    HISTORY,
}

data class OnDeviceOcrUiState(
    val modelReady: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float? = null,
    val isRecognizing: Boolean = false,
    val isSavingMarkdown: Boolean = false,
    val isSavingNote: Boolean = false,
    val preview: Bitmap? = null,
    val lines: List<RecognizedLine> = emptyList(),
    val selectedIndex: Int? = null,
    val currentTab: OcrTab = OcrTab.RECOGNIZE,
    val history: List<OcrHistoryEntry> = emptyList(),
    val activeHistoryId: String? = null,
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
    private val historyRepository: OcrHistoryRepository,
    private val noteService: NoteService,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(OnDeviceOcrUiState(modelReady = modelStore.isReady()))
    val uiState: StateFlow<OnDeviceOcrUiState> = _uiState.asStateFlow()

    init {
        componentScope.launch {
            historyRepository.records.collect { records ->
                _uiState.update { it.copy(history = records) }
            }
        }
    }

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
                    activeHistoryId = null,
                    errorRes = R.string.ocr_need_model,
                )
            }
            return
        }
        if (_uiState.value.isRecognizing) return
        componentScope.launch(decodingDispatcher) {
            _uiState.update {
                it.copy(
                    isRecognizing = true,
                    errorRes = null,
                    lines = emptyList(),
                    selectedIndex = null,
                    activeHistoryId = null,
                )
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
            recognize(bitmap)
        }
    }

    fun recognizeAgain() {
        val state = _uiState.value
        val bitmap = state.preview ?: return
        if (state.isRecognizing || !modelStore.isReady()) return
        componentScope.launch(decodingDispatcher) {
            _uiState.update {
                it.copy(isRecognizing = true, errorRes = null, selectedIndex = null)
            }
            recognize(bitmap)
        }
    }

    private suspend fun recognize(bitmap: Bitmap) {
        try {
            val lines = engine.recognize(bitmap)
            val historyEntry = try {
                historyRepository.save(
                    bitmap = bitmap,
                    lines = lines,
                    existingId = _uiState.value.activeHistoryId,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                error.makeLog("OnDeviceOcr")
                null
            }
            _uiState.update {
                it.copy(
                    preview = bitmap,
                    lines = lines,
                    isRecognizing = false,
                    selectedIndex = null,
                    activeHistoryId = historyEntry?.id ?: it.activeHistoryId,
                    errorRes = if (historyEntry == null) R.string.ocr_history_save_failed else null,
                )
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

    fun selectTab(tab: OcrTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun openHistory(record: OcrHistoryEntry, onFailure: () -> Unit) {
        if (_uiState.value.isRecognizing) return
        componentScope.launch(decodingDispatcher) {
            try {
                val bitmap = historyRepository.loadImage(record)
                _uiState.update {
                    it.copy(
                        currentTab = OcrTab.RECOGNIZE,
                        preview = bitmap,
                        lines = record.lines,
                        activeHistoryId = record.id,
                        selectedIndex = null,
                        errorRes = null,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                error.makeLog("OnDeviceOcr")
                withContext(uiDispatcher) { onFailure() }
            }
        }
    }

    fun deleteHistory(id: String) {
        componentScope.launch(ioDispatcher) {
            historyRepository.delete(id)
            _uiState.update { state ->
                if (state.activeHistoryId == id) state.copy(activeHistoryId = null) else state
            }
        }
    }

    fun clearHistory() {
        componentScope.launch(ioDispatcher) {
            historyRepository.clear()
            _uiState.update { it.copy(activeHistoryId = null) }
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

    fun saveAsNote(onComplete: (Boolean) -> Unit) {
        val state = _uiState.value
        val content = state.lines.toMarkdown()
        if (content.isBlank() || state.isSavingNote) return
        componentScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSavingNote = true) }
            val saved = try {
                val plainText = state.lines.toPlainText()
                noteService.saveNote(
                    NoteSaveParams(
                        title = state.lines.noteTitle(),
                        description = plainText.take(NOTE_DESCRIPTION_LENGTH),
                        data = content,
                        categoryIds = emptyList(),
                    )
                ) is NoteResult.Success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                error.makeLog("OnDeviceOcr")
                false
            }
            withContext(uiDispatcher) {
                _uiState.update { it.copy(isSavingNote = false) }
                onComplete(saved)
            }
        }
    }

    private fun List<RecognizedLine>.noteTitle(): String {
        return firstOrNull { it.text.isNotBlank() }
            ?.text
            ?.trim()
            ?.take(NOTE_TITLE_LENGTH)
            ?: "OCR"
    }

    private companion object {
        const val NOTE_TITLE_LENGTH = 48
        const val NOTE_DESCRIPTION_LENGTH = 100
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
