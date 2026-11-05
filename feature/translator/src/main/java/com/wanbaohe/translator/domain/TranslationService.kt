package com.wanbaohe.translator.domain

interface TranslationService {
    val supportedModes: List<TranslationModelMode>

    fun isLocalModelInstalled(): Boolean

    fun modelName(mode: TranslationModelMode): String

    suspend fun translate(
        text: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
        modelMode: TranslationModelMode,
        onDelta: (String) -> Unit = {},
    ): TranslationOutcome
}
