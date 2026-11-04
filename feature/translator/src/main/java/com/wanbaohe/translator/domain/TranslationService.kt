package com.wanbaohe.translator.domain

interface TranslationService {
    fun isLocalModelAvailable(): Boolean

    suspend fun translate(
        text: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
        modelMode: TranslationModelMode,
        onDelta: (String) -> Unit = {},
    ): TranslationOutcome
}
