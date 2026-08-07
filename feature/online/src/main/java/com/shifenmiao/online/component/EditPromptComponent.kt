package com.shifenmiao.online.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.dao.DataDraftDao
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
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallback
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult
import com.wanbaohe.markdown.edit.webview.MarkdownPreloadConfig
import com.wanbaohe.markdown.edit.webview.MarkdownWebViewPoolHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
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
    val allCategories: List<Category> = emptyList(),
    val selectedCategories: Set<Category> = emptySet(),
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
    appDatabase: AppDatabase,
    private val dataDraftHelper: DataDraftHelper,
    private val dataDraftDao: DataDraftDao,
    private val activityLogRecorder: ActivityLogRecorder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val itemDao = appDatabase.itemEntityDao()
    private val categoryDao = appDatabase.categoryDao()
    private val chatPromptDao = appDatabase.chatPromptDao()

    private val _uiState = MutableStateFlow(EditPromptUiState())
    val uiState = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val reorderableType: ReorderableType = ReorderableType.CATEGORY

    init {
        MarkdownWebViewPoolHelper.preloadHtml(
            MarkdownPreloadConfig.fromColorScheme(
                colorScheme = AppTheme.colorScheme,
                isDarkTheme = AppTheme.isDarkTheme,
                storageKey = "edit_prompt_editor"
            )
        )
        componentContext.lifecycle.doOnDestroy {
            MarkdownWebViewPoolHelper.clearPreloadedHtml()
        }

        componentScope.launch(ioDispatcher) {
            coroutineScope {
                launch { loadDraft() }
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
                    itemWithRelation.categories.filter { it.id in draftCategoryIds }.toSet()
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
                    itemWithRelation?.categories?.filter { it.id in draftCategoryIds }?.toSet().orEmpty()
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
            )
        }
    }

    private suspend fun observeCategories() {
        categoryDao.getAllCategories()
            .onEach { categories ->
                _categories.value = categories
                _uiState.update { state ->
                    val draft = dataDraftHelper.getById(draftId)
                    val draftCategoryIds = draft?.let {
                        DataDraftHelper.decodeCategoryIds(it.selectedCategoryIds)
                    } ?: emptySet()
                    val existingById = categories.associateBy { it.id }

                    val selected: Set<Category> = when {
                        state.selectedCategories.isNotEmpty() ->
                            state.selectedCategories.mapNotNull { existingById[it.id] }.toSet()
                        draftCategoryIds.isNotEmpty() ->
                            draftCategoryIds.mapNotNull { existingById[it] }.toSet()
                        else -> emptySet()
                    }
                    state.copy(
                        allCategories = categories,
                        selectedCategories = selected
                    )
                }
            }
            .collect { }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, isDirty = true) }
    }

    fun markAsDirty() {
        _uiState.update { it.copy(isDirty = true) }
    }

    fun savePromptData(
        markdown: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        _uiState.update { it.copy(data = markdown, isDirty = true) }
        saveItem(onSuccess, onFailure)
    }

    private fun saveItem(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            onFailure(AppContext.getString(R.string.error_title_empty))
            return
        }
        if (currentState.data.isBlank()) {
            onFailure(AppContext.getString(R.string.error_data_empty))
            return
        }
        val now = System.currentTimeMillis()
        _uiState.update { it.copy(isSaving = true) }

        componentScope.launch(ioDispatcher) {
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
            val promptEntity = PromptEntity(
                id = existingPromptId ?: 0,
                title = currentState.title,
                description = currentState.description,
                prompt = currentState.data,
                placeholder = currentState.placeholder,
                source = Source.LOCAL,
            )
            val promptId = chatPromptDao.upsertLocalPrompt(promptEntity)
            chatPromptDao.insertPromptLink(ItemPromptLink(itemId = itemId, promptId = promptId))

            categoryDao.deleteCategoriesByItemId(itemId)
            val selectedCategories = if (currentState.selectedCategories.isEmpty()) {
                listOf(ensureCategoryByName(AppContext.getString(R.string.create_ai_chat_prompt_default_category)))
            } else {
                currentState.selectedCategories.toList()
            }
            selectedCategories.forEach { category ->
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(itemId = itemId, categoryId = category.id)
                )
            }

            _uiState.update { it.copy(isSaving = false, isDirty = false) }

            dataDraftHelper.updateDraft(
                draftId = draftId,
                title = currentState.title,
                description = currentState.description,
                data = currentState.data,
                status = DataDraftEntity.STATUS_SUCCESS,
            )

            onResult?.invoke(
                ScreenCallbackResult.saved(
                    id = itemId,
                    message = "Prompt 已保存",
                    extra = mapOf("promptId" to itemId)
                )
            )

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun saveCategories(it: List<Category>) {
        componentScope.launch(ioDispatcher) {
            _uiState.update { state ->
                state.copy(selectedCategories = it.toMutableSet(), isDirty = true)
            }
        }
    }

    fun insertCategoryByName(name: String) {
        if (name.isBlank()) return
        componentScope.launch(ioDispatcher) {
            val category = ensureCategoryByName(name)
            _uiState.update { state ->
                state.copy(
                    selectedCategories = state.selectedCategories + category,
                    isDirty = true
                )
            }
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
