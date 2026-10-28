package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.domain.QuickAddTextParser
import com.shifenmiao.marktodo.model.AddTodoUiState
import com.shifenmiao.marktodo.model.QuickAddMetadataType
import com.shifenmiao.marktodo.model.QuickAddParseResult
import com.shifenmiao.marktodo.model.QuickAddTodoUiEvent
import com.shifenmiao.marktodo.model.QuickAddTodoUiState
import com.shifenmiao.marktodo.model.QuickAddToken
import com.shifenmiao.marktodo.model.QuickAddTokenType
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.model.todo.TaskInput
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class QuickAddTodoComponent @AssistedInject internal constructor(
    @Assisted private val componentScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
) {
    private val _uiState = MutableStateFlow(QuickAddTodoUiState())
    val uiState: StateFlow<QuickAddTodoUiState> = _uiState.asStateFlow()

    init {
        componentScope.launch {
            repository.observeDashboard().collect { dashboard ->
                val categories = dashboard.map { relation ->
                    relation.category.toModel(
                        icon = iconFromKey(relation.category.iconKey.ifBlank { "inbox" }),
                        tasks = emptyList(),
                    )
                }
                val current = _uiState.value
                val selectedCategory = current.selectedCategory?.let { selected ->
                    categories.firstOrNull { it.id == selected.id }
                } ?: categories.firstOrNull { it.id == current.initialCategoryId }
                _uiState.value = current.copy(
                    categories = categories,
                    selectedCategory = selectedCategory,
                )
                reparseTitle()
            }
        }
        componentScope.launch {
            repository.observeTags().collect { tags ->
                _uiState.value = _uiState.value.copy(tags = tags.map { it.toModel() })
                reparseTitle()
            }
        }
    }

    fun open(initialCategoryId: String? = null) {
        val current = _uiState.value
        _uiState.value = QuickAddTodoUiState(
            isVisible = true,
            categories = current.categories,
            tags = current.tags,
            initialCategoryId = initialCategoryId,
            selectedCategory = current.categories.firstOrNull { it.id == initialCategoryId },
            titleFocusRequestId = current.titleFocusRequestId + 1,
        )
    }

    fun close() {
        if (_uiState.value.isSaving) return
        _uiState.value = _uiState.value.copy(
            isVisible = false,
            showDatePicker = false,
            showTimePicker = false,
        )
    }

    fun handleEvent(event: QuickAddTodoUiEvent): Boolean = when (event) {
        is QuickAddTodoUiEvent.UpdateTitle -> {
            updateTitle(event.title)
            true
        }

        is QuickAddTodoUiEvent.UpdateNote -> {
            _uiState.value = _uiState.value.copy(
                note = event.note.take(AddTodoUiState.NOTE_MAX_LENGTH),
            )
            true
        }

        is QuickAddTodoUiEvent.SelectCategory -> {
            _uiState.value = _uiState.value.copy(
                selectedCategory = event.category,
                manuallyEditedMetadata = _uiState.value.manuallyEditedMetadata + QuickAddMetadataType.CATEGORY,
            )
            true
        }

        is QuickAddTodoUiEvent.ToggleTag -> {
            val selected = _uiState.value.selectedTagNames
            _uiState.value = _uiState.value.copy(
                selectedTagNames = if (event.tagName in selected) selected - event.tagName else selected + event.tagName,
                manuallyEditedMetadata = _uiState.value.manuallyEditedMetadata + QuickAddMetadataType.TAG,
            )
            true
        }

        is QuickAddTodoUiEvent.SelectPriority -> {
            _uiState.value = _uiState.value.copy(
                priority = event.priority,
                manuallyEditedMetadata = _uiState.value.manuallyEditedMetadata + QuickAddMetadataType.PRIORITY,
            )
            true
        }

        is QuickAddTodoUiEvent.UpdateDueDate -> {
            val current = _uiState.value
            val updatedDate = event.dateMillis?.let { date ->
                if (current.hasDueTime && current.dueDateMillis != null) {
                    withTimeOfDay(date, current.dueDateMillis)
                } else {
                    startOfDay(date)
                }
            }
            _uiState.value = current.copy(
                dueDateMillis = updatedDate,
                hasDueTime = updatedDate != null && current.hasDueTime,
                showDatePicker = false,
                manuallyEditedMetadata = current.manuallyEditedMetadata + QuickAddMetadataType.DATE,
            )
            true
        }

        is QuickAddTodoUiEvent.UpdateDueTime -> {
            val current = _uiState.value
            val date = current.dueDateMillis ?: startOfDay(System.currentTimeMillis())
            _uiState.value = current.copy(
                dueDateMillis = withTimeOfDay(date, event.hour, event.minute),
                hasDueTime = true,
                showTimePicker = false,
                manuallyEditedMetadata = current.manuallyEditedMetadata + QuickAddMetadataType.TIME,
            )
            true
        }

        QuickAddTodoUiEvent.ClearDueTime -> {
            val current = _uiState.value
            _uiState.value = current.copy(
                dueDateMillis = current.dueDateMillis?.let(::startOfDay),
                hasDueTime = false,
                manuallyEditedMetadata = current.manuallyEditedMetadata + QuickAddMetadataType.TIME,
            )
            true
        }

        is QuickAddTodoUiEvent.DismissRecognizedToken -> {
            val metadataType = when (event.token.type) {
                QuickAddTokenType.DATE -> QuickAddMetadataType.DATE
                QuickAddTokenType.TIME -> QuickAddMetadataType.TIME
                QuickAddTokenType.PRIORITY -> QuickAddMetadataType.PRIORITY
                QuickAddTokenType.TAG -> QuickAddMetadataType.TAG
                QuickAddTokenType.CATEGORY -> QuickAddMetadataType.CATEGORY
            }
            val current = _uiState.value
            _uiState.value = current.copy(
                preservedContextMetadata = current.preservedContextMetadata - metadataType,
            )
            updateTitle(current.title, additionalSuppressedToken = event.token)
            true
        }

        QuickAddTodoUiEvent.ToggleDetails -> {
            _uiState.value = _uiState.value.copy(showDetails = !_uiState.value.showDetails)
            true
        }

        is QuickAddTodoUiEvent.ToggleDatePicker -> {
            _uiState.value = _uiState.value.copy(showDatePicker = event.show)
            true
        }

        is QuickAddTodoUiEvent.ToggleTimePicker -> {
            _uiState.value = _uiState.value.copy(showTimePicker = event.show)
            true
        }

        QuickAddTodoUiEvent.Submit -> submit(closeAfterSave = true)
        QuickAddTodoUiEvent.SubmitAndContinue -> submit(closeAfterSave = false)
    }

    private fun submit(closeAfterSave: Boolean): Boolean {
        val current = _uiState.value
        val title = current.cleanedTitle.trim()
        if (current.isSaving) return false
        if (title.isBlank()) {
            _uiState.value = current.copy(showValidationErrors = true)
            return false
        }

        val input = TaskInput(
            categoryId = current.selectedCategory?.id,
            title = title,
            note = current.note.trim().takeIf { it.isNotBlank() },
            dueDateMillis = current.dueDateMillis,
            tags = current.selectedTagNames.toList(),
            priority = current.priority,
            hasDueTime = current.hasDueTime && current.dueDateMillis != null,
        )
        _uiState.value = current.copy(isSaving = true, showValidationErrors = false)
        componentScope.launch {
            service.createTask(input = input, source = "UI:QuickAddTodoSheet")
                .onSuccess {
                    AppToastHost.showToast(context.getString(R.string.snackbar_task_added))
                    val latest = _uiState.value
                    _uiState.value = if (closeAfterSave) {
                        latest.copy(
                            isVisible = false,
                            isSaving = false,
                            showDatePicker = false,
                            showTimePicker = false,
                        )
                    } else {
                        latest.copy(
                            title = "",
                            cleanedTitle = "",
                            recognizedTokens = emptyList(),
                            preservedContextMetadata = QuickAddMetadataType.entries.toSet(),
                            suppressedTokenKeys = emptySet(),
                            showValidationErrors = false,
                            isSaving = false,
                            titleFocusRequestId = latest.titleFocusRequestId + 1,
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    AppToastHost.showToast(context.getString(R.string.error_save_failed))
                }
        }
        return true
    }

    private fun reparseTitle() {
        val current = _uiState.value
        if (current.title.isNotEmpty()) updateTitle(current.title)
    }

    private fun updateTitle(title: String, additionalSuppressedToken: QuickAddToken? = null) {
        val current = _uiState.value
        val result = QuickAddTextParser.parse(title, current.categories, current.tags)
        val suppressedKeys = current.suppressedTokenKeys + listOfNotNull(additionalSuppressedToken?.key)
        val activeTokens = result.tokens.filterNot { it.key in suppressedKeys }
        val updated = current.copy(
            title = title,
            cleanedTitle = result.cleanedTitle,
            recognizedTokens = activeTokens,
            suppressedTokenKeys = suppressedKeys.intersect(result.tokens.map { it.key }.toSet()),
            showValidationErrors = current.showValidationErrors && result.cleanedTitle.isBlank(),
        )
        _uiState.value = applyParsedMetadata(updated, result, activeTokens)
    }

    private fun applyParsedMetadata(
        current: QuickAddTodoUiState,
        result: QuickAddParseResult,
        activeTokens: List<QuickAddToken>,
    ): QuickAddTodoUiState {
        val manual = current.manuallyEditedMetadata
        val preserved = current.preservedContextMetadata
        val activeTypes = activeTokens.map { it.type }.toSet()
        var selectedCategory = current.selectedCategory
        var selectedTags = current.selectedTagNames
        var priority = current.priority
        var dueDate = current.dueDateMillis
        var hasDueTime = current.hasDueTime

        val categoryToken = activeTokens.firstOrNull { it.type == QuickAddTokenType.CATEGORY }
        if (QuickAddMetadataType.CATEGORY !in manual &&
            (categoryToken != null || QuickAddMetadataType.CATEGORY !in preserved)
        ) {
            selectedCategory = current.categories.firstOrNull {
                it.id == (categoryToken?.value ?: current.initialCategoryId)
            }
        }
        if (QuickAddMetadataType.TAG !in manual &&
            (QuickAddTokenType.TAG in activeTypes || QuickAddMetadataType.TAG !in preserved)
        ) {
            selectedTags = activeTokens.filter { it.type == QuickAddTokenType.TAG }
                .map { it.value }
                .distinctBy { it.lowercase() }
                .toSet()
        }
        if (QuickAddMetadataType.PRIORITY !in manual &&
            (QuickAddTokenType.PRIORITY in activeTypes || QuickAddMetadataType.PRIORITY !in preserved)
        ) {
            priority = activeTokens.firstOrNull { it.type == QuickAddTokenType.PRIORITY }
                ?.value?.toIntOrNull() ?: TodoTask.PRIORITY_MEDIUM
        }

        val hasDateToken = QuickAddTokenType.DATE in activeTypes
        val hasTimeToken = QuickAddTokenType.TIME in activeTypes
        if (QuickAddMetadataType.DATE !in manual &&
            (hasDateToken || QuickAddMetadataType.DATE !in preserved)
        ) {
            dueDate = if (hasDateToken || hasTimeToken) result.dueDateMillis?.let(::startOfDay) else null
            val keepExistingTime = current.hasDueTime && (
                QuickAddMetadataType.TIME in manual ||
                    (QuickAddMetadataType.TIME in preserved && !hasTimeToken)
                )
            if (dueDate != null && keepExistingTime) {
                current.dueDateMillis?.let { dueDate = withTimeOfDay(dueDate!!, it) }
                hasDueTime = true
            } else if (QuickAddMetadataType.TIME !in manual) {
                hasDueTime = false
            }
            hasDueTime = hasDueTime && dueDate != null
        }
        if (QuickAddMetadataType.TIME !in manual &&
            (hasTimeToken || QuickAddMetadataType.TIME !in preserved)
        ) {
            if (hasTimeToken && result.dueTimeMinutes != null) {
                val timeDate = dueDate ?: result.dueDateMillis
                if (timeDate != null) {
                    dueDate = withTimeOfDay(
                        timeDate,
                        result.dueTimeMinutes / 60,
                        result.dueTimeMinutes % 60,
                    )
                    hasDueTime = true
                }
            } else if (!hasTimeToken) {
                dueDate = dueDate?.let(::startOfDay)
                hasDueTime = false
            }
        }

        return current.copy(
            selectedCategory = selectedCategory,
            selectedTagNames = selectedTags,
            priority = priority,
            dueDateMillis = dueDate,
            hasDueTime = hasDueTime && dueDate != null,
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(componentScope: CoroutineScope): QuickAddTodoComponent
    }
}

private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun withTimeOfDay(dateMillis: Long, timeMillis: Long): Long {
    val time = Calendar.getInstance().apply { timeInMillis = timeMillis }
    return withTimeOfDay(dateMillis, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
}

private fun withTimeOfDay(dateMillis: Long, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
    timeInMillis = dateMillis
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
