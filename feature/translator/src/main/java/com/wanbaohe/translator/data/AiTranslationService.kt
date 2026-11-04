package com.wanbaohe.translator.data

import com.shifenmiao.ai.request.LocalLlmGenerateEvent
import com.shifenmiao.ai.request.LocalLlmGenerateRequest
import com.shifenmiao.ai.request.LocalLlmModelRegistry
import com.shifenmiao.ai.request.LocalLlmPrepareResult
import com.shifenmiao.ai.request.LocalLlmRuntime
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.unified.LlmMessage
import com.shifenmiao.model.remote.LocalTranslationModel
import com.shifenmiao.storage.TokenStorage
import com.wanbaohe.translator.R
import com.wanbaohe.translator.domain.TranslationFailure
import com.wanbaohe.translator.domain.TranslationHistoryRepository
import com.wanbaohe.translator.domain.TranslationLanguage
import com.wanbaohe.translator.domain.TranslationModelMode
import com.wanbaohe.translator.domain.TranslationOutcome
import com.wanbaohe.translator.domain.TranslationResult
import com.wanbaohe.translator.domain.TranslationService
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AiTranslationService @Inject constructor(
    private val aiExecutor: AIPromptExecutor,
    private val localRuntime: LocalLlmRuntime,
    private val localModelRegistry: LocalLlmModelRegistry,
    private val historyRepository: TranslationHistoryRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) : TranslationService {
    override fun isLocalModelAvailable(): Boolean =
        localModelRegistry.resolve(LocalTranslationModel.MODEL_NAME) != null

    override suspend fun translate(
        text: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
        modelMode: TranslationModelMode,
        onDelta: (String) -> Unit,
    ): TranslationOutcome {
        val generated = when (modelMode) {
            TranslationModelMode.LOCAL -> translateLocally(text, sourceLanguage, targetLanguage, onDelta)
            TranslationModelMode.FAST -> translateInCloud(
                text, sourceLanguage, targetLanguage, AIPromptExecutor.EngineMode.FAST, onDelta,
            )
            TranslationModelMode.DEFAULT -> translateInCloud(
                text, sourceLanguage, targetLanguage, AIPromptExecutor.EngineMode.DEFAULT, onDelta,
            )
        }
        when (generated) {
            is GeneratedTranslation.Failure -> return generated.outcome
            is GeneratedTranslation.Success -> Unit
        }
        val record = historyRepository.save(
            sourceText = text,
            translatedText = generated.text,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            modelName = generated.modelName,
        )
        runCatching {
            activityLogRecorder.recordTranslation(
                recordId = record.id,
                sourceText = record.sourceText,
                translatedText = record.translatedText,
                sourceLanguage = record.sourceLanguage.code,
                targetLanguage = record.targetLanguage.code,
                modelName = record.modelName,
                appTitle = AppContext.getString(R.string.translation_title),
            )
        }
        return TranslationOutcome.Success(TranslationResult(record))
    }

    private suspend fun translateInCloud(
        text: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
        mode: AIPromptExecutor.EngineMode,
        onDelta: (String) -> Unit,
    ): GeneratedTranslation {
        val result = aiExecutor.executeStreaming(
            input = text,
            systemPrompt = cloudSystemPrompt(sourceLanguage, targetLanguage),
            engineMode = mode,
            onDelta = onDelta,
            billingDesc = AppContext.getString(R.string.translation_title),
        )
        if (!result.isSuccess) {
            val failure = when {
                result.isBlocked && !TokenStorage.isLogin() -> TranslationFailure.LoginRequired
                result.isBlocked -> TranslationFailure.NoPoints
                else -> TranslationFailure.Message(result.errorMessage.orEmpty())
            }
            return GeneratedTranslation.Failure(TranslationOutcome.Failed(failure))
        }
        val translated = result.content.trim()
        if (translated.isBlank()) {
            return GeneratedTranslation.Failure(
                TranslationOutcome.Failed(
                    TranslationFailure.Message(AppContext.getString(com.shifenmiao.core.R.string.ai_empty_response_error))
                )
            )
        }
        return GeneratedTranslation.Success(
            text = translated,
            modelName = result.modelName.ifBlank { result.engineName },
        )
    }

    private suspend fun translateLocally(
        text: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
        onDelta: (String) -> Unit,
    ): GeneratedTranslation {
        val spec = localModelRegistry.resolve(LocalTranslationModel.MODEL_NAME)
            ?: return GeneratedTranslation.Failure(
                TranslationOutcome.Failed(TranslationFailure.LocalModelMissing)
            )
        when (val prepared = localRuntime.prepare(spec)) {
            is LocalLlmPrepareResult.Failure -> return GeneratedTranslation.Failure(
                TranslationOutcome.Failed(TranslationFailure.Message(prepared.error.toString()))
            )
            is LocalLlmPrepareResult.Success -> Unit
        }
        val sessionId = "translation-${UUID.randomUUID()}"
        val content = StringBuilder()
        var failure: String? = null
        val request = LocalLlmGenerateRequest(
            sessionId = sessionId,
            model = spec,
            messages = listOf(
                LlmMessage.createTextMessage(
                    role = RoleType.USER.value,
                    text = localPrompt(text, sourceLanguage, targetLanguage),
                )
            ),
            temperature = 0.0,
            topP = 1.0,
            maxTokens = spec.maxOutputTokens,
        )
        localRuntime.generate(request).collect { event ->
            when (event) {
                is LocalLlmGenerateEvent.Token -> {
                    content.append(event.text)
                    onDelta(content.toString())
                }
                is LocalLlmGenerateEvent.Failed -> failure = event.message
                is LocalLlmGenerateEvent.Completed -> Unit
            }
        }
        val translated = content.toString().trim()
        return when {
            failure != null -> GeneratedTranslation.Failure(
                TranslationOutcome.Failed(TranslationFailure.Message(failure.orEmpty()))
            )
            translated.isBlank() -> GeneratedTranslation.Failure(
                TranslationOutcome.Failed(
                    TranslationFailure.Message(AppContext.getString(com.shifenmiao.core.R.string.ai_empty_response_error))
                )
            )
            else -> GeneratedTranslation.Success(translated, LocalTranslationModel.DISPLAY_NAME)
        }
    }

    private fun cloudSystemPrompt(
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
    ): String = buildString {
        append("You are a professional translation engine. Translate the user's text ")
        if (sourceLanguage == TranslationLanguage.AUTO) {
            append("after detecting its language ")
        } else {
            append("from ").append(sourceLanguage.promptName).append(' ')
        }
        append("into ").append(targetLanguage.promptName)
        append(". Preserve meaning, tone, line breaks, numbers and placeholders. ")
        append("Output only the translated text, without quotes or explanations.")
    }

    private fun localPrompt(
        text: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
    ): String = buildString {
        if (sourceLanguage != TranslationLanguage.AUTO) {
            append("The source language is ").append(sourceLanguage.promptName).append(". ")
        }
        append("Translate the following text into ").append(targetLanguage.promptName)
        append(". Only output the translated result without any additional explanation:\n")
        append(text)
    }

    private sealed interface GeneratedTranslation {
        data class Success(val text: String, val modelName: String) : GeneratedTranslation
        data class Failure(val outcome: TranslationOutcome.Failed) : GeneratedTranslation
    }
}
