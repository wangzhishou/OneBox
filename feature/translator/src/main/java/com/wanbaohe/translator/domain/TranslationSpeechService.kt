package com.wanbaohe.translator.domain

interface TranslationSpeechService {
    suspend fun speak(text: String): SpeechOutcome
    fun stop()
}

sealed interface SpeechOutcome {
    data object Success : SpeechOutcome
    data object LoginRequired : SpeechOutcome
    data object NoPoints : SpeechOutcome
    data class Failed(val reason: String) : SpeechOutcome
}
