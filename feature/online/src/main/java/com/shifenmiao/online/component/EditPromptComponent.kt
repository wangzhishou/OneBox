/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.online.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.core.R
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.chat_prompt.LocalPromptRepository
import com.shifenmiao.database.chat_prompt.PromptSaveInput
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.tool.PromptToolCatalog
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.online.R as OnlineR
import com.shifenmiao.online.service.PromptRewriteAction
import com.shifenmiao.online.service.PromptRewriteService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.navigation.ScreenCallback
import com.wanbaohe.core.ui.utils.navigation.ScreenCallbackResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class EditPromptUiState(
    val itemId: Int = 0,
    val promptId: Int = 0,
    val title: String = "",
    val description: String = "",
    val data: String = "",
    val placeholder: String = "",
    val templates: String? = null,
    val selectedCategoryIds: Set<Int> = emptySet(),
    val selectedToolNames: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val isEditing: Boolean = false,
)

@Immutable
data class PromptRewriteError(
    val message: String,
    val isBlocked: Boolean = false,
)

class EditPromptComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val draftId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onResult: ScreenCallback? = null,
    dispatchersHolder: DispatchersHolder,
    private val promptRepository: LocalPromptRepository,
    private val rewriteService: PromptRewriteService,
    private val activityLog: ActivityLogRecorder,
    private val toolCatalog: PromptToolCatalog,
    aiEngineManager: AIEngineManager,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(EditPromptUiState())
    val uiState = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _availableTools = MutableStateFlow<List<ToolCatalogItem>>(emptyList())
    val availableTools: StateFlow<List<ToolCatalogItem>> = _availableTools.asStateFlow()

    private val _rewrite = MutableStateFlow<PromptRewriteState?>(null)
    val rewrite = _rewrite.asStateFlow()
    val rewriteEngine = aiEngineManager.currentAIEngine
    private var rewriteJob: Job? = null

    val reorderableType: ReorderableType = ReorderableType.CATEGORY

    init {
        onResult?.invoke(
            ScreenCallbackResult.pendingUserAction(
                idLong = draftId.takeIf { it > 0L },
                message = "Prompt 编辑页已打开，等待用户完成编辑",
            )
        )
        componentScope.launch(ioDispatcher) {
            try {
                loadDraft()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
        componentScope.launch(ioDispatcher) {
            promptRepository.observeCategories().collect { _categories.value = it }
        }
        componentScope.launch(ioDispatcher) {
            _availableTools.value = toolCatalog.getVisibleTools()
                .sortedWith(compareBy<ToolCatalogItem> { it.sortOrder }.thenBy { it.title })
        }
    }

    fun effectiveRewriteEngine() = rewriteService.effectiveEngine()

    suspend fun recentRewriteResults(action: PromptRewriteAction) = rewriteService.recent(action)

    private suspend fun loadDraft() {
        val snapshot = promptRepository.loadEditorSnapshot(draftId)
        _uiState.update {
            it.copy(
                itemId = snapshot.itemId,
                promptId = snapshot.promptId,
                title = snapshot.title,
                description = snapshot.description,
                data = snapshot.data,
                placeholder = snapshot.placeholder,
                templates = snapshot.templates,
                selectedCategoryIds = snapshot.selectedCategoryIds,
                selectedToolNames = snapshot.selectedToolNames,
                isEditing = snapshot.isEditing,
            )
        }
    }

    fun markAsDirty() {
        _uiState.update { it.copy(isDirty = true) }
    }

    fun updateTitle(title: String) {
        _uiState.update { if (it.title == title) it else it.copy(title = title, isDirty = true) }
    }

    fun updateDescription(description: String) {
        _uiState.update {
            if (it.description == description) it else it.copy(description = description, isDirty = true)
        }
    }

    fun updatePlaceholder(placeholder: String) {
        _uiState.update {
            if (it.placeholder == placeholder) it else it.copy(placeholder = placeholder, isDirty = true)
        }
    }

    fun updateTemplates(templates: String) {
        _uiState.update {
            val value = templates.ifBlank { null }
            if (it.templates == value) it else it.copy(templates = value, isDirty = true)
        }
    }

    fun updateSelectedCategoryIds(selectedCategoryIds: Set<Int>) {
        _uiState.update {
            if (it.selectedCategoryIds == selectedCategoryIds) it
            else it.copy(selectedCategoryIds = selectedCategoryIds, isDirty = true)
        }
    }

    fun updateAdvancedConfiguration(selectedCategoryIds: Set<Int>, selectedToolNames: Set<String>) {
        _uiState.update {
            if (it.selectedCategoryIds == selectedCategoryIds && it.selectedToolNames == selectedToolNames) {
                it
            } else {
                it.copy(
                    selectedCategoryIds = selectedCategoryIds,
                    selectedToolNames = selectedToolNames,
                    isDirty = true,
                )
            }
        }
    }

    fun openRewrite(action: PromptRewriteAction, original: String) {
        closeRewrite()
        _rewrite.value = PromptRewriteState(action, original)
    }

    fun updateRequirement(text: String) {
        _rewrite.update { current ->
            if (current == null || current.running) current
            else current.copy(requirement = text, candidate = "", error = null)
        }
    }

    fun selectRewriteResult(result: String) {
        val state = _rewrite.value ?: return
        if (state.running) return
        _rewrite.value = state.withCandidate(result)
    }

    private fun PromptRewriteState.withCandidate(result: String): PromptRewriteState {
        val candidate = result.trim()
        val error = when {
            candidate.isBlank() -> PromptRewriteError(AppContext.getString(OnlineR.string.prompt_ai_failed))
            action != PromptRewriteAction.Create &&
                !rewriteService.preservesProtectedText(original, candidate) -> {
                PromptRewriteError(AppContext.getString(OnlineR.string.prompt_ai_protected))
            }
            else -> null
        }
        return copy(
            candidate = candidate.takeIf { error == null }.orEmpty(),
            error = error,
            running = false,
        )
    }

    fun generateRewrite() {
        val state = _rewrite.value ?: return
        if (state.running || !state.canGenerate) return
        rewriteService.withAccess {
            if (_rewrite.value !== state || rewriteJob?.isActive == true) return@withAccess
            val runningState = state.copy(running = true, candidate = "", error = null)
            _rewrite.value = runningState
            rewriteJob = componentScope.launch {
                val outcome = rewriteService.rewrite(
                    action = state.action,
                    original = state.original,
                    requirement = state.requirement,
                    billingLabel = AppContext.getString(state.action.label),
                    onDelta = { fullText ->
                        _rewrite.update { current ->
                            if (current === runningState) current.copy(candidate = fullText) else current
                        }
                    },
                )
                ensureActive()
                _rewrite.value = if (outcome.isSuccess) {
                    runningState.withCandidate(outcome.content)
                } else {
                    runningState.copy(
                        candidate = "",
                        running = false,
                        error = PromptRewriteError(
                            message = outcome.errorMessage
                                ?: AppContext.getString(OnlineR.string.prompt_ai_failed),
                            isBlocked = outcome.isBlocked,
                        ),
                    )
                }
            }
        }
    }

    fun stopRewrite() {
        rewriteJob?.cancel()
        rewriteJob = null
        _rewrite.update { it?.copy(running = false, candidate = "", error = null) }
    }

    fun closeRewrite() {
        stopRewrite()
        _rewrite.value = null
    }

    fun savePromptData(
        markdown: String,
        title: String,
        description: String,
        selectedCategoryIds: Set<Int>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val state = _uiState.value
        if (state.isLoading || state.isSaving) return
        val currentState = state.copy(data = markdown, title = title, description = description)
        if (currentState.title.isBlank()) {
            onFailure(AppContext.getString(R.string.error_title_empty))
            return
        }
        if (currentState.data.isBlank()) {
            onFailure(AppContext.getString(R.string.error_data_empty))
            return
        }
        _uiState.update { it.copy(isSaving = true) }

        componentScope.launch(ioDispatcher) {
            try {
                val saved = promptRepository.save(
                    PromptSaveInput(
                        draftId = draftId,
                        itemId = currentState.itemId,
                        promptId = currentState.promptId,
                        title = currentState.title,
                        description = currentState.description,
                        prompt = currentState.data,
                        placeholder = currentState.placeholder,
                        templates = currentState.templates,
                        selectedCategoryIds = selectedCategoryIds,
                        selectedToolNames = currentState.selectedToolNames,
                        defaultCategoryName = AppContext.getString(
                            R.string.create_ai_chat_prompt_default_category
                        ),
                    )
                )
                activityLog.recordPrompt(
                    saved.itemId,
                    currentState.title,
                    AppContext.getString(R.string.prompt_create),
                    Screen.EditPromptItem().id.toString(),
                )
                _uiState.update {
                    it.copy(
                        itemId = saved.itemId,
                        promptId = saved.prompt.id,
                        data = currentState.data,
                        title = currentState.title,
                        description = currentState.description,
                        selectedCategoryIds = saved.selectedCategories.mapTo(linkedSetOf()) { it.id },
                        selectedToolNames = saved.selectedToolNames,
                        isDirty = false,
                        isEditing = true,
                    )
                }
                withContext(Dispatchers.Main) {
                    onResult?.invoke(
                        ScreenCallbackResult.saved(
                            id = saved.itemId,
                            message = "Prompt 已保存",
                            extra = mapOf(
                                "promptId" to saved.prompt.id,
                                "promptEntity" to saved.prompt,
                            ),
                        )
                    )
                    onSuccess()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(AppContext.getString(R.string.prompt_save_failed))
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun addCategory(name: String, onAdded: (Int) -> Unit) {
        if (name.isBlank()) return
        componentScope.launch(ioDispatcher) {
            val category = promptRepository.ensureCategoryByName(name)
            withContext(Dispatchers.Main) { onAdded(category.id) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            draftId: Long,
            onGoBack: () -> Unit,
            onResult: ScreenCallback?,
        ): EditPromptComponent
    }
}

data class PromptRewriteState(
    val action: PromptRewriteAction,
    val original: String,
    val requirement: String = "",
    val candidate: String = "",
    val running: Boolean = false,
    val error: PromptRewriteError? = null,
) {
    val canGenerate: Boolean
        get() = if (action == PromptRewriteAction.Create) {
            requirement.isNotBlank()
        } else {
            original.isNotBlank()
        }
}
