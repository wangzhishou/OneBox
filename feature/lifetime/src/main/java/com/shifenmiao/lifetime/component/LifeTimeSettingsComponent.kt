package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.LifeTimeRepository
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate

@Immutable
data class LifeTimeSettingsUiState(
    val startDate: LocalDate? = null,
    val targetAge: Int = 100,
    val showDatePicker: Boolean = false
)

class LifeTimeSettingsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: LifeTimeRepository
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(LifeTimeSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        repository.birthDateFlow
            .onEach { _uiState.emit(_uiState.value.copy(startDate = it)) }
            .launchIn(componentScope)

        repository.targetAgeFlow
            .onEach { _uiState.emit(_uiState.value.copy(targetAge = it)) }
            .launchIn(componentScope)
    }

    fun saveStartDate(date: LocalDate) {
        componentScope.launch {
            val today = LocalDate.now()
            val validDate = if (date.isAfter(today)) today else date
            repository.saveBirthDate(validDate)
            _uiState.emit(_uiState.value.copy(startDate = validDate, showDatePicker = false))
        }
    }

    fun saveAndGoBack() {
        componentScope.launch {
            uiState.value.startDate?.let { date ->
                val today = LocalDate.now()
                val validDate = if (date.isAfter(today)) today else date
                repository.saveBirthDate(validDate)
            }
            onGoBack()
        }
    }

    fun saveTargetAge(age: Int) {
        componentScope.launch {
            repository.saveTargetAge(age)
        }
    }

    fun showDatePicker() {
        componentScope.launch {
            _uiState.emit(_uiState.value.copy(showDatePicker = true))
        }
    }

    fun hideDatePicker() {
        componentScope.launch {
            _uiState.emit(_uiState.value.copy(showDatePicker = false))
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): LifeTimeSettingsComponent
    }
}
