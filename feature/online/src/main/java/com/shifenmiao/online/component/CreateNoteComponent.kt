package com.shifenmiao.online.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.dao.DataDraftDao
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.item.ItemDataUiState
import com.shifenmiao.model.note.NoteResult
import com.shifenmiao.model.note.NoteSaveParams
import com.shifenmiao.model.note.NoteService
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallback
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult
import com.wanbaohe.com.string.MarkdownSummary
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.shifenmiao.model.Source

/** 笔记编辑器工具栏自定义按钮 action：分类 */
internal const val NOTE_TOOLBAR_ACTION_CATEGORY = "noteCategory"

/** 笔记编辑器工具栏自定义按钮 action：AI 创作 */
internal const val NOTE_TOOLBAR_ACTION_AI = "noteAi"

/**
 * 笔记编辑页追加在编辑器工具栏末尾的按钮 HTML：分类、AI 创作。
 * 点击经 MarkdownEditorBridge.onCustomToolbarAction 回调给 Compose 层。
 */
internal const val NOTE_EDITOR_TOOLBAR_EXTRAS = """
    <button class="toolbar-btn" data-action="noteCategory" title="分类" onclick="window.Android && window.Android.onCustomToolbarAction && window.Android.onCustomToolbarAction('noteCategory')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
    </button>
    <button class="toolbar-btn" data-action="noteAi" title="AI 创作" onclick="window.Android && window.Android.onCustomToolbarAction && window.Android.onCustomToolbarAction('noteAi')">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M7.5 5.6 10 7 8.6 4.5 10 2 7.5 3.4 5 2l1.4 2.5L5 7l2.5-1.4z"/><path d="M19.5 15.4 17 14l1.4 2.5L17 19l2.5-1.4 2.5 1.4-1.4-2.5L22 14l-2.5 1.4z"/><path d="M22 2l-2.5 1.4L17 2l1.4 2.5L17 7l2.5-1.4L22 7l-1.4-2.5L22 2z"/><path d="M14.37 7.29c-.39-.39-1.02-.39-1.41 0L1.29 18.96c-.39.39-.39 1.02 0 1.41l2.34 2.34c.39.39 1.02.39 1.41 0L16.7 11.05c.39-.39.39-1.02 0-1.41l-2.33-2.35zm-1.03 5.49-2.12-2.12 2.44-2.44 2.12 2.12-2.44 2.44z"/></svg>
    </button>
    <div class="toolbar-divider"></div>
"""


class CreateNoteComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val draftId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onResult: ScreenCallback? = null,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val noteService: NoteService,
    private val dataDraftHelper: DataDraftHelper,
    private val dataDraftDao: DataDraftDao
) : BaseComponent(dispatchersHolder, componentContext) {

    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()
    private val categoryDao = appDatabase.categoryDao()

    private val _uiState = MutableStateFlow(ItemDataUiState())
    val uiState = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val reorderableType: ReorderableType = ReorderableType.CATEGORY

    init {
        MarkdownWebViewPoolHelper.preloadHtml(
            MarkdownPreloadConfig.fromColorScheme(
                colorScheme = AppTheme.colorScheme,
                isDarkTheme = AppTheme.isDarkTheme,
                storageKey = "create_note_item",
                toolbarExtras = NOTE_EDITOR_TOOLBAR_EXTRAS
            )
        )
        componentContext.lifecycle.doOnDestroy {
            MarkdownWebViewPoolHelper.clearPreloadedHtml()
            if (draftId != 0L) {
                val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
                scope.launch { dataDraftHelper.deleteById(draftId) }
            }
        }

        componentScope.launch(ioDispatcher) {
            coroutineScope {
                launch { loadDraft() }
                launch { observeCategories() }
            }
            onResult?.invoke(
                ScreenCallbackResult.pendingUserAction(
                    idLong = draftId.takeIf { it > 0L },
                    message = "笔记编辑页已打开，等待用户完成编辑"
                )
            )
        }
    }

    private suspend fun loadDraft() {
        val draft = dataDraftHelper.getById(draftId) ?: return

        val draftItemId = draft.itemId
        if (draftItemId != null) {
            val itemWithRelation = itemDao.getItemById(draftItemId).firstOrNull()
            if (itemWithRelation != null) {
                val itemData = itemDataDao.getByItemId(itemWithRelation.item.id)?.data ?: ""
                _uiState.update {
                    it.copy(
                        itemId = itemWithRelation.item.id,
                        title = draft.title.ifEmpty { itemWithRelation.item.title },
                        description = draft.description.ifEmpty { itemWithRelation.item.description },
                        data = draft.data.ifEmpty { itemData },
                        selectedCategories = itemWithRelation.categories.map(Category::toModel).toSet(),
                        isEditing = true
                    )
                }
                return
            }
        }

        _uiState.update {
            it.copy(
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

                    val selected = when {
                        state.selectedCategories.isNotEmpty() ->
                            state.selectedCategories.mapNotNull { existingById[it.id]?.toModel() }.toSet()
                        draftCategoryIds.isNotEmpty() ->
                            draftCategoryIds.mapNotNull { existingById[it]?.toModel() }.toSet()
                        else -> emptySet()
                    }
                    state.copy(
                        allCategories = categories.map(Category::toModel),
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

    fun saveMarkDownData(
        markdown: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val derived = MarkdownSummary.derive(markdown = markdown)
        val currentTitle = _uiState.value.title
        _uiState.update {
            it.copy(
                data = markdown,
                title = if (currentTitle.isNotBlank()) currentTitle else derived.title,
                description = derived.description,
                isDirty = true
            )
        }
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

        _uiState.update { it.copy(isSaving = true) }

        componentScope.launch(ioDispatcher) {
            val draft = dataDraftHelper.getById(draftId)

            val result = noteService.saveNote(
                NoteSaveParams(
                    existingItemId = draft?.itemId,
                    title = currentState.title,
                    description = currentState.description,
                    data = currentState.data,
                    categoryIds = currentState.selectedCategories.map { it.id.toLong() }
                )
            )

            when (result) {
                is NoteResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isDirty = false) }
                    dataDraftHelper.deleteById(draftId)
                    onResult?.invoke(
                        ScreenCallbackResult.saved(
                            id = result.itemId,
                            message = "笔记已保存"
                        )
                    )
                    withContext(Dispatchers.Main) { onSuccess() }
                }
                is NoteResult.Error -> {
                    _uiState.update { it.copy(isSaving = false) }
                    withContext(Dispatchers.Main) { onFailure(result.message) }
                }
            }
        }
    }

    fun saveCategories(it: List<com.shifenmiao.model.Category>) {
        componentScope.launch(ioDispatcher) {
            _uiState.update { state ->
                state.copy(selectedCategories = it.toSet(), isDirty = true)
            }
        }
    }

    /**
     * 新增一个本地分类，并自动将其加入已选分类。
     */
    fun addCategory(name: String) {
        if (name.isBlank()) return
        componentScope.launch(ioDispatcher) {
            val newCategory = com.shifenmiao.database.item.entity.Category(
                name = name,
                canEdit = true,
                source = Source.LOCAL,
            )
            val insertedId = categoryDao.insertOrUpdateCategory(newCategory)
            val inserted = categoryDao.getCategoryById(insertedId.toInt())?.toModel() ?: return@launch
            _uiState.update { state ->
                state.copy(
                    selectedCategories = state.selectedCategories + inserted,
                    isDirty = true
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            draftId: Long,
            onGoBack: () -> Unit,
            onResult: ScreenCallback? = null,
        ): CreateNoteComponent
    }
}
