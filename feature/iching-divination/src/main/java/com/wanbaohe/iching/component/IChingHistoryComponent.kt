package com.wanbaohe.iching.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.iching.data.IChingHistoryRecord
import com.wanbaohe.iching.data.IChingHistoryRepository
import com.wanbaohe.iching.domain.IChingTextLibrary
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IChingHistoryUiState(
    val records: List<IChingHistoryRecord> = emptyList(),
)

class IChingHistoryComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted private val onOpenRecord: (String) -> Unit,
    private val historyRepository: IChingHistoryRepository,
    private val textLibrary: IChingTextLibrary,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        IChingHistoryUiState(records = historyRepository.records.value)
    )
    val uiState = _uiState.asStateFlow()

    init {
        componentScope.launch {
            historyRepository.records.collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun openRecord(id: String) = onOpenRecord(id)

    /** 卦名/卦辞/爻辞按当前 locale 从文本库解析(历史记录只存卦号) */
    fun hexagramText(number: Int) = textLibrary.hexagram(number)

    fun deleteRecord(id: String) = historyRepository.remove(id)

    fun clearHistory() = historyRepository.clear()

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onOpenRecord: (String) -> Unit,
        ): IChingHistoryComponent
    }
}

