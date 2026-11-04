package com.wanbaohe.translator.domain

import kotlinx.coroutines.flow.StateFlow

interface TranslationHistoryRepository {
    val records: StateFlow<List<TranslationRecord>>

    suspend fun save(
        sourceText: String,
        translatedText: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
        modelName: String,
    ): TranslationRecord

    suspend fun toggleFavorite(id: String)
    suspend fun delete(id: String)
    suspend fun clear()
}
