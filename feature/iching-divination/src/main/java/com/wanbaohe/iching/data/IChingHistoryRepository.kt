package com.wanbaohe.iching.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IChingHistoryRepository @Inject constructor() {
    private val _records = MutableStateFlow(IChingHistoryStorage.loadHistory())
    val records = _records.asStateFlow()

    fun find(id: String): IChingHistoryRecord? = _records.value.firstOrNull { it.id == id }

    fun append(record: IChingHistoryRecord) {
        _records.value = IChingHistoryStorage.append(record)
    }

    fun updateAIContent(id: String, content: String) {
        _records.value = IChingHistoryStorage.updateAIContent(id, content)
    }

    fun remove(id: String) {
        _records.value = IChingHistoryStorage.remove(id)
    }

    fun clear() {
        IChingHistoryStorage.clear()
        _records.value = emptyList()
    }
}

