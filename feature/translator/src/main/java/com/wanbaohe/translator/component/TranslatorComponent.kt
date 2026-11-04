package com.wanbaohe.translator.component

import android.net.Uri
import androidx.annotation.StringRes
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.ai.voice.VoiceRecognizer
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.interfaces.ocr.TextRecognitionService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.translator.R
import com.wanbaohe.translator.domain.SpeechOutcome
import com.wanbaohe.translator.domain.TranslationFailure
import com.wanbaohe.translator.domain.TranslationHistoryRepository
import com.wanbaohe.translator.domain.TranslationLanguage
import com.wanbaohe.translator.domain.TranslationModelMode
import com.wanbaohe.translator.domain.TranslationOutcome
import com.wanbaohe.translator.domain.TranslationRecord
import com.wanbaohe.translator.domain.TranslationService
import com.wanbaohe.translator.domain.TranslationSpeechService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class TranslatorComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialText: String,
    @Assisted("onGoBack") val onGoBack: () -> Unit,
    @Assisted("onOpenLocalModels") val onOpenLocalModels: () -> Unit,
    private val translationService: TranslationService,
    private val historyRepository: TranslationHistoryRepository,
    private val speechService: TranslationSpeechService,
    private val textRecognitionService: TextRecognitionService,
    val voiceRecognizer: VoiceRecognizer,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        TranslatorUiState(
            inputText = initialText.take(MAX_INPUT_LENGTH),
            supportedModelModes = translationService.supportedModes,
            isLocalModelInstalled = translationService.isLocalModelInstalled(),
        )
    )
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    init {
        componentScope.launch {
            historyRepository.records.collect { records ->
                _uiState.update { it.copy(history = records) }
            }
        }
        lifecycle.doOnDestroy { speechService.stop() }
    }

    fun updateInput(text: String) {
        _uiState.update {
            it.copy(
                inputText = text.take(MAX_INPUT_LENGTH),
                currentRecordId = null,
                errorRes = null,
            )
        }
    }

    fun selectTab(tab: TranslatorTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectSourceLanguage(language: TranslationLanguage) {
        _uiState.update {
            it.copy(
                sourceLanguage = language,
                targetLanguage = if (language != TranslationLanguage.AUTO && language == it.targetLanguage) {
                    if (language == TranslationLanguage.CHINESE) TranslationLanguage.ENGLISH
                    else TranslationLanguage.CHINESE
                } else it.targetLanguage,
                currentRecordId = null,
            )
        }
    }

    fun selectTargetLanguage(language: TranslationLanguage) {
        if (language == TranslationLanguage.AUTO) return
        _uiState.update {
            it.copy(
                targetLanguage = language,
                sourceLanguage = if (language == it.sourceLanguage) TranslationLanguage.AUTO else it.sourceLanguage,
                currentRecordId = null,
            )
        }
    }

    fun swapLanguages() {
        _uiState.update { state ->
            val oldSource = state.sourceLanguage
            val oldTarget = state.targetLanguage
            val newSource = oldTarget
            val newTarget = when (oldSource) {
                TranslationLanguage.AUTO -> if (oldTarget == TranslationLanguage.CHINESE) {
                    TranslationLanguage.ENGLISH
                } else {
                    TranslationLanguage.CHINESE
                }
                else -> oldSource
            }
            state.copy(
                sourceLanguage = newSource,
                targetLanguage = newTarget,
                inputText = state.outputText.ifBlank { state.inputText },
                outputText = if (state.outputText.isBlank()) "" else state.inputText,
                currentRecordId = null,
                errorRes = null,
            )
        }
    }

    fun selectModel(mode: TranslationModelMode) {
        _uiState.update {
            it.copy(
                modelMode = mode,
                isLocalModelInstalled = translationService.isLocalModelInstalled(),
                errorRes = null,
            )
        }
    }

    fun translate() {
        val snapshot = _uiState.value
        val input = snapshot.inputText.trim()
        if (snapshot.isTranslating) return
        if (input.isBlank()) {
            AppToastHost.showToast(R.string.translation_input_required)
            return
        }
        if (snapshot.modelMode == TranslationModelMode.LOCAL) {
            val errorRes = when {
                TranslationModelMode.LOCAL !in snapshot.supportedModelModes ->
                    R.string.translation_local_model_unsupported

                !translationService.isLocalModelInstalled() -> R.string.translation_local_model_missing
                else -> null
            }
            if (errorRes != null) {
                _uiState.update { it.copy(errorRes = errorRes) }
                return
            }
        }
        componentScope.launch {
            _uiState.update {
                it.copy(isTranslating = true, outputText = "", errorRes = null, currentRecordId = null)
            }
            val outcome = try {
                translationService.translate(
                    text = input,
                    sourceLanguage = snapshot.sourceLanguage,
                    targetLanguage = snapshot.targetLanguage,
                    modelMode = snapshot.modelMode,
                    onDelta = { text ->
                        _uiState.update { it.copy(outputText = text) }
                    },
                )
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                TranslationOutcome.Failed(TranslationFailure.Message(""))
            }
            when (outcome) {
                is TranslationOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            isTranslating = false,
                            outputText = outcome.result.record.translatedText,
                            currentRecordId = outcome.result.record.id,
                        )
                    }
                    AppToastHost.showToast(R.string.translation_success)
                    ReviewPromptHost.notifySuccess(ReviewPromptTrigger.TRANSLATION_SUCCESS)
                }
                is TranslationOutcome.Failed -> handleTranslationFailure(outcome.failure)
            }
        }
    }

    private fun handleTranslationFailure(failure: TranslationFailure) {
        _uiState.update {
            it.copy(
                isTranslating = false,
                errorRes = when (failure) {
                    TranslationFailure.LocalModelMissing -> R.string.translation_local_model_missing
                    TranslationFailure.LocalModelUnsupported -> R.string.translation_local_model_unsupported
                    is TranslationFailure.Message -> R.string.translation_failed
                    TranslationFailure.LoginRequired,
                    TranslationFailure.NoPoints -> null
                },
            )
        }
        when (failure) {
            TranslationFailure.LoginRequired -> ActionUtils.showLogin(source = LOGIN_SOURCE) { translate() }
            TranslationFailure.NoPoints -> AppToastHost.showToast(com.shifenmiao.core.R.string.no_points)
            is TranslationFailure.Message -> Unit
            TranslationFailure.LocalModelMissing,
            TranslationFailure.LocalModelUnsupported -> Unit
        }
    }

    fun clearInput() {
        speechService.stop()
        _uiState.update {
            it.copy(inputText = "", outputText = "", currentRecordId = null, errorRes = null)
        }
    }

    fun recognizeImage(uri: Uri) {
        if (_uiState.value.isRecognizingImage) return
        componentScope.launch {
            _uiState.update {
                it.copy(isRecognizingImage = true, ocrDownloadProgress = null, errorRes = null)
            }
            if (!textRecognitionService.isModelReady()) {
                val prepared = textRecognitionService.prepareModel { progress ->
                    _uiState.update { it.copy(ocrDownloadProgress = progress) }
                }
                if (prepared.isFailure) {
                    _uiState.update {
                        it.copy(isRecognizingImage = false, errorRes = R.string.translation_ocr_download_failed)
                    }
                    return@launch
                }
            }
            textRecognitionService.recognizeText(uri)
                .onSuccess { text -> updateInput(text) }
                .onFailure {
                    _uiState.update { state -> state.copy(errorRes = R.string.translation_ocr_failed) }
                }
            _uiState.update { it.copy(isRecognizingImage = false, ocrDownloadProgress = null) }
        }
    }

    fun speakOutput() {
        val text = _uiState.value.outputText.trim()
        if (text.isBlank() || _uiState.value.isSpeaking) return
        componentScope.launch {
            _uiState.update { it.copy(isSpeaking = true) }
            when (val outcome = speechService.speak(text)) {
                SpeechOutcome.Success -> Unit
                SpeechOutcome.LoginRequired -> ActionUtils.showLogin(source = TTS_LOGIN_SOURCE) { speakOutput() }
                SpeechOutcome.NoPoints -> AppToastHost.showToast(com.shifenmiao.core.R.string.no_points)
                is SpeechOutcome.Failed -> AppToastHost.showToast(R.string.translation_tts_failed)
            }
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    fun openHistory(record: TranslationRecord) {
        speechService.stop()
        _uiState.update {
            it.copy(
                currentTab = TranslatorTab.TRANSLATE,
                inputText = record.sourceText,
                outputText = record.translatedText,
                sourceLanguage = record.sourceLanguage,
                targetLanguage = record.targetLanguage,
                currentRecordId = record.id,
                errorRes = null,
            )
        }
    }

    fun toggleFavorite(id: String) {
        componentScope.launch(ioDispatcher) { historyRepository.toggleFavorite(id) }
    }

    fun deleteHistory(id: String) {
        componentScope.launch(ioDispatcher) { historyRepository.delete(id) }
    }

    fun clearHistory() {
        componentScope.launch(ioDispatcher) { historyRepository.clear() }
    }

    fun updateHistoryQuery(query: String) {
        _uiState.update { it.copy(historyQuery = query) }
    }

    fun selectHistoryFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(historyFilter = filter) }
    }

    fun filteredHistory(): List<TranslationRecord> {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        return state.history.filter { record ->
            val matchesQuery = state.historyQuery.isBlank() ||
                record.sourceText.contains(state.historyQuery, ignoreCase = true) ||
                record.translatedText.contains(state.historyQuery, ignoreCase = true)
            matchesQuery && matchesFilter(record.createdAt, now, state.historyFilter)
        }
    }

    private fun matchesFilter(timestamp: Long, now: Long, filter: HistoryFilter): Boolean {
        val age = now - timestamp
        return when (filter) {
            HistoryFilter.ALL -> true
            HistoryFilter.TODAY -> isSameDay(timestamp, now)
            HistoryFilter.WEEK -> age in 0..WEEK_MILLIS
            HistoryFilter.MONTH -> age in 0..MONTH_MILLIS
        }
    }

    private fun isSameDay(first: Long, second: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = first }
        val b = Calendar.getInstance().apply { timeInMillis = second }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialText: String,
            @Assisted("onGoBack") onGoBack: () -> Unit,
            @Assisted("onOpenLocalModels") onOpenLocalModels: () -> Unit,
        ): TranslatorComponent
    }

    companion object {
        const val MAX_INPUT_LENGTH = 500
        private const val LOGIN_SOURCE = "translator_cloud"
        private const val TTS_LOGIN_SOURCE = "translator_tts"
        private const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000
        private const val MONTH_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}

enum class TranslatorTab { TRANSLATE, HISTORY }
enum class HistoryFilter { ALL, TODAY, WEEK, MONTH }

data class TranslatorUiState(
    val inputText: String = "",
    val outputText: String = "",
    val sourceLanguage: TranslationLanguage = TranslationLanguage.CHINESE,
    val targetLanguage: TranslationLanguage = TranslationLanguage.ENGLISH,
    val modelMode: TranslationModelMode = TranslationModelMode.DEFAULT,
    val supportedModelModes: List<TranslationModelMode> = listOf(
        TranslationModelMode.FAST,
        TranslationModelMode.DEFAULT,
    ),
    val currentTab: TranslatorTab = TranslatorTab.TRANSLATE,
    val currentRecordId: String? = null,
    val isTranslating: Boolean = false,
    val isSpeaking: Boolean = false,
    val isRecognizingImage: Boolean = false,
    val ocrDownloadProgress: Float? = null,
    val isLocalModelInstalled: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val history: List<TranslationRecord> = emptyList(),
    val historyQuery: String = "",
    val historyFilter: HistoryFilter = HistoryFilter.ALL,
)
