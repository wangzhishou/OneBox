package com.wanbaohe.translator.domain

import kotlinx.serialization.Serializable

enum class TranslationLanguage(val code: String, val promptName: String) {
    AUTO("auto", "the detected source language"),
    CHINESE("zh", "Simplified Chinese"),
    CHINESE_TRADITIONAL("zh-Hant", "Traditional Chinese"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    SPANISH("es", "Spanish"),
    PORTUGUESE("pt", "Portuguese"),
    ITALIAN("it", "Italian"),
    DUTCH("nl", "Dutch"),
    RUSSIAN("ru", "Russian"),
    UKRAINIAN("uk", "Ukrainian"),
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    INDONESIAN("id", "Indonesian"),
    VIETNAMESE("vi", "Vietnamese"),
    THAI("th", "Thai"),
    TURKISH("tr", "Turkish"),
    FILIPINO("fil", "Filipino"),
}

enum class TranslationModelMode {
    FAST,
    DEFAULT,
    LOCAL,
}

@Serializable
data class TranslationRecord(
    val id: String,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: TranslationLanguage,
    val targetLanguage: TranslationLanguage,
    val modelName: String,
    val createdAt: Long,
    val isFavorite: Boolean = false,
)

data class TranslationResult(
    val record: TranslationRecord,
)

sealed interface TranslationFailure {
    data class Message(val reason: String) : TranslationFailure
    data object LocalModelMissing : TranslationFailure
    data object LoginRequired : TranslationFailure
    data object NoPoints : TranslationFailure
}

sealed interface TranslationOutcome {
    data class Success(val result: TranslationResult) : TranslationOutcome
    data class Failed(val failure: TranslationFailure) : TranslationOutcome
}
