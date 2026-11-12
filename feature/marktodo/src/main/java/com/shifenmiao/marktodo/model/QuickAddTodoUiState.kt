/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

@Immutable
data class QuickAddTodoUiState(
    val isVisible: Boolean = false,
    val title: String = "",
    val cleanedTitle: String = "",
    val note: String = "",
    val categories: List<TodoCategory> = emptyList(),
    val tags: List<TodoTag> = emptyList(),
    val initialCategoryId: String? = null,
    val selectedCategory: TodoCategory? = null,
    val selectedTagNames: Set<String> = emptySet(),
    val priority: Int = TodoTask.PRIORITY_MEDIUM,
    val dueDateMillis: Long? = null,
    val hasDueTime: Boolean = false,
    val recognizedTokens: List<QuickAddToken> = emptyList(),
    val manuallyEditedMetadata: Set<QuickAddMetadataType> = emptySet(),
    val preservedContextMetadata: Set<QuickAddMetadataType> = emptySet(),
    val suppressedTokenKeys: Set<String> = emptySet(),
    val showDetails: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showValidationErrors: Boolean = false,
    val isSaving: Boolean = false,
    val titleFocusRequestId: Int = 0,
) {
    val isTitleValid: Boolean get() = cleanedTitle.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
}

@Immutable
data class QuickAddParseResult(
    val cleanedTitle: String = "",
    val dueDateMillis: Long? = null,
    val dueTimeMinutes: Int? = null,
    val priority: Int? = null,
    val tagNames: List<String> = emptyList(),
    val categoryId: String? = null,
    val tokens: List<QuickAddToken> = emptyList(),
)

@Immutable
data class QuickAddToken(
    val type: QuickAddTokenType,
    val raw: String,
    val value: String,
    val start: Int,
    val end: Int,
) {
    val key: String get() = "${type.name}:$start:$end:$raw"
}

enum class QuickAddTokenType {
    DATE,
    TIME,
    PRIORITY,
    TAG,
    CATEGORY,
}

enum class QuickAddMetadataType {
    DATE,
    TIME,
    PRIORITY,
    TAG,
    CATEGORY,
}

sealed interface QuickAddTodoUiEvent {
    data class UpdateTitle(val title: String) : QuickAddTodoUiEvent
    data class UpdateNote(val note: String) : QuickAddTodoUiEvent
    data class SelectCategory(val category: TodoCategory?) : QuickAddTodoUiEvent
    data class ToggleTag(val tagName: String) : QuickAddTodoUiEvent
    data class SelectPriority(val priority: Int) : QuickAddTodoUiEvent
    data class UpdateDueDate(val dateMillis: Long?) : QuickAddTodoUiEvent
    data class UpdateDueTime(val hour: Int, val minute: Int) : QuickAddTodoUiEvent
    data object ClearDueTime : QuickAddTodoUiEvent
    data class DismissRecognizedToken(val token: QuickAddToken) : QuickAddTodoUiEvent
    data object ToggleDetails : QuickAddTodoUiEvent
    data class ToggleDatePicker(val show: Boolean) : QuickAddTodoUiEvent
    data class ToggleTimePicker(val show: Boolean) : QuickAddTodoUiEvent
    data object ShowAiCreate : QuickAddTodoUiEvent
    data object Submit : QuickAddTodoUiEvent
    data object SubmitAndContinue : QuickAddTodoUiEvent
}
