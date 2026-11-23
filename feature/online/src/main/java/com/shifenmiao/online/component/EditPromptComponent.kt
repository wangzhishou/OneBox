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

package com.shifenmiao.online.component

import androidx.compose.runtime.Immutable
import androidx.room.withTransaction
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.core.R
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemPromptLink
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.online.service.PromptRewriteService
import com.shifenmiao.online.service.PromptRewriteAction
import com.shifenmiao.online.R as OnlineR
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.ScreenCallback
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.navigation.ScreenCallbackResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

@Immutable
data class EditPromptUiState(
    val itemId: Int = 0,
    val promptId: Int = 0,
    val title: String = "",
    val description: String = "",
    val data: String = "",
    val placeholder: String = "",
    val selectedCategories: Set<Category> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val isEditing: Boolean = false
)

class EditPromptComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val draftId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onResult: ScreenCallback? = null,
    dispatchersHolder: DispatchersHolder,
    private val appDatabase: AppDatabase,
    private val dataDraftHelper: DataDraftHelper,
    private val rewriteService: PromptRewriteService,
    private val activityLog: ActivityLogRecorder,
    aiEngineManager: AIEngineManager,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val itemDao = appDatabase.itemEntityDao()
    private val categoryDao = appDatabase.categoryDao()
    private val chatPromptDao = appDatabase.chatPromptDao()

    private val _uiState = MutableStateFlow(EditPromptUiState())
    val uiState = _uiState.asStateFlow()

    private val _rewrite = MutableStateFlow<PromptRewriteState?>(null)
    val rewrite = _rewrite.asStateFlow()
    val rewriteEngine = aiEngineManager.currentAIEngine
    private var rewriteJob: Job? = null

    fun effectiveRewriteEngine() = rewriteService.effectiveEngine()

    fun openRewrite(action: PromptRewriteAction, original: String) {
        closeRewrite()
        _rewrite.value = PromptRewriteState(action, original)
    }

    fun updateRequirement(text: String) {
        _rewrite.update { it?.copy(requirement = text, candidate = "", error = null) }
    }

    fun generateRewrite() {
        val state = _rewrite.value ?: return
        val input = state.input
        if (state.running || input.isBlank()) return
        rewriteService.withAccess {
            if (_rewrite.value !== state || rewriteJob?.isActive == true) return@withAccess
            _rewrite.value = state.copy(running = true, candidate = "", error = null)
            rewriteJob = componentScope.launch {
                val outcome = rewriteService.rewrite(state.action, input, AppContext.getString(state.action.label))
                ensureActive()
                val candidate = outcome.content.trim()
                val error = when {
                    !outcome.isSuccess || candidate.isBlank() -> OnlineR.string.prompt_ai_failed
                    state.action != PromptRewriteAction.Create &&
                        !rewriteService.preservesProtectedText(state.original, candidate) -> OnlineR.string.prompt_ai_protected
                    else -> null
                }
                _rewrite.value = state.copy(candidate = candidate.takeIf { error == null }.orEmpty(), error = error)
            }
        }
    }

    fun stopRewrite() {
        rewriteJob?.cancel()
        rewriteJob = null
        _rewrite.update { it?.copy(running = false) }
    }

    fun closeRewrite() {
        stopRewrite()
        _rewrite.value = null
    }

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val reorderableType: ReorderableType = ReorderableType.CATEGORY

    init {
        componentScope.launch(ioDispatcher) {
            coroutineScope {
                launch {
                    try {
                        loadDraft()
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                launch { observeCategories() }
            }
            onResult?.invoke(
                ScreenCallbackResult.pendingUserAction(
                    idLong = draftId.takeIf { it > 0L },
                    message = "Prompt 编辑页已打开，等待用户完成编辑"
                )
            )
        }
    }

    private suspend fun loadDraft() {
        val draft = dataDraftHelper.getById(draftId) ?: return
        val draftCategoryIds = DataDraftHelper.decodeCategoryIds(draft.selectedCategoryIds)
        val draftCategories = if (draftCategoryIds.isEmpty()) emptySet() else {
            categoryDao.getAllCategories().firstOrNull().orEmpty()
                .filter { it.id in draftCategoryIds }.toSet()
        }
        val draftPromptId = draft.relatedEntityId?.takeIf { it > 0 }

        val draftItemId = draft.itemId
        if (draftItemId != null) {
            val itemWithRelation = itemDao.getItemById(draftItemId).firstOrNull()
            if (itemWithRelation != null) {
                val item = itemWithRelation.item
                val promptId = chatPromptDao.getPromptLinkByItemId(item.id)
                val promptEntity = promptId?.let { chatPromptDao.getPromptById(it) }
                val promptContent = promptEntity?.prompt ?: ""
                val placeholder = promptEntity?.placeholder ?: ""
                val selectedCategories = if (draftCategoryIds.isNotEmpty()) {
                    draftCategories
                } else {
                    itemWithRelation.categories.toSet()
                }

                _uiState.update {
                    it.copy(
                        itemId = item.id,
                        promptId = promptId ?: 0,
                        title = draft.title.ifEmpty { item.title },
                        description = draft.description.ifEmpty { item.description },
                        data = draft.data.ifEmpty { promptContent },
                        placeholder = placeholder,
                        selectedCategories = selectedCategories,
                        isEditing = true
                    )
                }
                return
            }
        }

        if (draftPromptId != null) {
            val promptEntity = chatPromptDao.getPromptById(draftPromptId)
            // draftPromptId 是 prompt 表主键，需要反查 link 拿到 item_id
            val linkedItemId = chatPromptDao.getItemIdByPromptId(draftPromptId)
            val itemWithRelation = linkedItemId?.let {
                itemDao.getItemById(it).firstOrNull()
            }
            if (itemWithRelation != null || promptEntity != null) {
                val selectedCategories = if (draftCategoryIds.isNotEmpty()) {
                    draftCategories
                } else {
                    itemWithRelation?.categories?.toSet().orEmpty()
                }

                _uiState.update {
                    it.copy(
                        itemId = itemWithRelation?.item?.id ?: 0,
                        promptId = draftPromptId,
                        title = draft.title.ifEmpty {
                            itemWithRelation?.item?.title ?: promptEntity?.title.orEmpty()
                        },
                        description = draft.description.ifEmpty {
                            itemWithRelation?.item?.description ?: promptEntity?.description.orEmpty()
                        },
                        data = draft.data.ifEmpty { promptEntity?.prompt.orEmpty() },
                        placeholder = promptEntity?.placeholder.orEmpty(),
                        selectedCategories = selectedCategories,
                        isEditing = true
                    )
                }
                return
            }
        }

        _uiState.update {
            it.copy(
                promptId = draftPromptId ?: 0,
                title = draft.title,
                description = draft.description,
                data = draft.data,
                selectedCategories = draftCategories,
            )
        }
    }

    private suspend fun observeCategories() {
        categoryDao.getAllCategories().collect { _categories.value = it }
    }

    fun markAsDirty() {
        _uiState.update { it.copy(isDirty = true) }
    }

    fun savePromptData(
        markdown: String,
        title: String,
        description: String,
        selectedCategoryIds: Set<Int>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
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
            val (itemId, promptId) = try {
                appDatabase.withTransaction { persistPrompt(currentState, selectedCategoryIds) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(AppContext.getString(R.string.prompt_save_failed))
                }
                return@launch
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
            activityLog.recordPrompt(
                itemId, currentState.title, AppContext.getString(R.string.prompt_create),
                Screen.EditPromptItem().id.toString(),
            )
            _uiState.update { it.copy(isDirty = false) }
            withContext(Dispatchers.Main) {
                onResult?.invoke(
                    ScreenCallbackResult.saved(
                        id = itemId,
                        message = "Prompt 已保存",
                        extra = mapOf("promptId" to promptId)
                    )
                )
                onSuccess()
            }
        }
    }

    private suspend fun persistPrompt(
        currentState: EditPromptUiState,
        selectedCategoryIds: Set<Int>,
    ): Pair<Int, Int> {
        val now = System.currentTimeMillis()
        return run {
            val draft = dataDraftHelper.getById(draftId)
            val targetItemId = draft?.itemId
                ?: currentState.itemId.takeIf { it != 0 }
                ?: 0

            val item = ItemEntity(
                id = targetItemId,
                remoteId = null,
                source = Source.LOCAL,
                listType = ListItemType.PROMPT.id,
                title = currentState.title,
                description = currentState.description,
                url = "",
                iconName = currentState.title.firstOrNull()?.toString(),
                createdAt = now,
                updatedAt = now,
                publishedAt = now,
            )
            val itemId = itemDao.upsertItem(item)

            itemDao.upsertUserState(
                ItemUserState(
                    itemId = itemId,
                    isPinned = true,
                    pinnedAt = now,
                    canEdit = true,
                    updatedAt = now,
                )
            )

            // 资源行 + 关联
            val existingPromptId = chatPromptDao.getPromptLinkByItemId(itemId)
            val existingPrompt = existingPromptId?.let { chatPromptDao.getPromptById(it) }
            val promptEntity = PromptEntity(
                id = existingPromptId ?: 0,
                title = currentState.title,
                description = currentState.description,
                prompt = currentState.data,
                placeholder = currentState.placeholder,
                templates = existingPrompt?.templates,
                source = Source.LOCAL,
            )
            val promptId = chatPromptDao.upsertLocalPrompt(promptEntity)
            chatPromptDao.insertPromptLink(ItemPromptLink(itemId = itemId, promptId = promptId))

            categoryDao.deleteCategoriesByItemId(itemId)
            val existingCategories = categoryDao.getAllCategories().firstOrNull().orEmpty()
                .filter { it.id in selectedCategoryIds }
            val selectedCategories = existingCategories.ifEmpty {
                listOf(ensureCategoryByName(AppContext.getString(R.string.create_ai_chat_prompt_default_category)))
            }
            selectedCategories.forEach { category ->
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(itemId = itemId, categoryId = category.id)
                )
            }

            dataDraftHelper.updateDraft(
                draftId = draftId,
                title = currentState.title,
                description = currentState.description,
                data = currentState.data,
                selectedCategoryIds = selectedCategories.map { it.id }.toSet(),
                status = DataDraftEntity.STATUS_SUCCESS,
            )
            itemId to promptId
        }
    }

    fun addCategory(name: String, onAdded: (Int) -> Unit) {
        if (name.isBlank()) return
        componentScope.launch(ioDispatcher) {
            val category = ensureCategoryByName(name)
            withContext(Dispatchers.Main) { onAdded(category.id) }
        }
    }

    private suspend fun ensureCategoryByName(name: String): Category {
        val normalizedName = name.trim()
        categoryDao.getCategoryByName(normalizedName)?.let { return it }

        val category = Category(
            id = 0,
            name = normalizedName,
            canEdit = true,
            source = Source.LOCAL
        )
        categoryDao.insertOrUpdateCategory(category)
        return categoryDao.getCategoryByName(normalizedName) ?: category
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
    val error: Int? = null,
) {
    val input: String get() = if (action == PromptRewriteAction.Create) requirement.trim() else original
}
