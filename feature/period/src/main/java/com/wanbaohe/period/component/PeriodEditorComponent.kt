package com.wanbaohe.period.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.period.R
import com.wanbaohe.period.model.PeriodDayStatus
import com.wanbaohe.period.model.PeriodEditorState
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.model.PeriodSymptom
import com.wanbaohe.period.service.PeriodService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
/** 新增 / 编辑经期记录表单 Component。 */
class PeriodEditorComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted("editingRecordId") private val editingRecordId: String?,
    dispatchersHolder: DispatchersHolder,
    private val service: PeriodService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _editorState = MutableStateFlow(
        PeriodEditorState(isEditing = editingRecordId != null, recordId = editingRecordId)
    )
    val editorState: StateFlow<PeriodEditorState> = _editorState

    init {
        if (editingRecordId != null) loadRecord(editingRecordId)
    }

    fun onDateChange(date: LocalDate) {
        _editorState.update { it.copy(recordDate = date, showDatePicker = false) }
    }

    fun toggleDatePicker() {
        _editorState.update { it.copy(showDatePicker = !it.showDatePicker) }
    }

    fun onStatusSelect(status: PeriodDayStatus) {
        _editorState.update {
            it.copy(
                isPeriodStart = status == PeriodDayStatus.START,
                isPeriodEnd = status == PeriodDayStatus.END,
            )
        }
    }

    fun onFlowChange(flow: PeriodFlow) {
        _editorState.update { it.copy(flowIntensity = flow) }
    }

    fun onSymptomToggle(symptom: PeriodSymptom) {
        _editorState.update { state ->
            val next = state.symptoms.toMutableSet()
            if (!next.add(symptom)) next.remove(symptom)
            state.copy(symptoms = next)
        }
    }

    fun onMoodChange(mood: PeriodMood) {
        _editorState.update { it.copy(mood = mood) }
    }

    fun onNoteChange(value: String) {
        if (value.length <= NOTE_MAX_LENGTH) {
            _editorState.update { it.copy(note = value) }
        }
    }

    fun toggleDeleteConfirm() {
        _editorState.update { it.copy(showDeleteConfirm = !it.showDeleteConfirm) }
    }

    fun save() {
        val state = _editorState.value
        if (state.isSaving) return
        _editorState.update { it.copy(isSaving = true) }
        componentScope.launch {
            val input = PeriodService.RecordInput(
                recordDate = state.recordDate,
                isPeriodStart = state.isPeriodStart,
                isPeriodEnd = state.isPeriodEnd,
                flowIntensity = state.flowIntensity,
                symptoms = state.symptoms.toList(),
                mood = state.mood,
                note = state.note.trim().takeIf { it.isNotEmpty() },
            )
            val result = if (state.isEditing && state.recordId != null) {
                service.updateRecord(
                    recordId = state.recordId,
                    input = input,
                    actor = PeriodService.ACTOR_USER,
                    source = PeriodService.SOURCE_UI,
                )
            } else {
                service.addRecord(
                    input = input,
                    actor = PeriodService.ACTOR_USER,
                    source = PeriodService.SOURCE_UI,
                ).map { }
            }
            _editorState.update { it.copy(isSaving = false) }
            result
                .onSuccess {
                    AppToastHost.showToast(
                        AppContext.getString(
                            if (state.isEditing) R.string.period_saved else R.string.period_record_added
                        )
                    )
                    AppToastHost.showConfetti()
                    onGoBack()
                }
                .onFailure {
                    AppToastHost.showToast(AppContext.getString(R.string.period_save_failed))
                }
        }
    }

    fun delete() {
        val id = _editorState.value.recordId ?: return
        componentScope.launch {
            service.deleteRecord(id, PeriodService.ACTOR_USER, PeriodService.SOURCE_UI)
                .onSuccess {
                    AppToastHost.showToast(AppContext.getString(R.string.period_deleted))
                    onGoBack()
                }
                .onFailure {
                    AppToastHost.showToast(AppContext.getString(R.string.period_save_failed))
                }
        }
    }

    private fun loadRecord(recordId: String) {
        componentScope.launch {
            val view = service.getRecordUi(recordId) ?: return@launch
            _editorState.update {
                it.copy(
                    recordDate = view.recordDate,
                    isPeriodStart = view.isPeriodStart,
                    isPeriodEnd = view.isPeriodEnd,
                    flowIntensity = view.flowIntensity,
                    symptoms = view.symptoms.toSet(),
                    mood = view.mood,
                    note = view.note.orEmpty(),
                )
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            @Assisted("editingRecordId") editingRecordId: String?,
        ): PeriodEditorComponent
    }

    companion object {
        const val NOTE_MAX_LENGTH = 200
    }
}
