package com.shifenmiao.online.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.dao.DataDraftDao
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemDataKind
import com.shifenmiao.database.item.entity.ItemDataLink
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.event.BookmarkChangedEvent
import com.shifenmiao.model.event.EditorResultEvent
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.network.downloader.HtmlDownloadConfig
import com.shifenmiao.network.downloader.HtmlDownloadResult
import com.shifenmiao.network.downloader.HtmlDownloader
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallback
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult
import com.t8rin.imagetoolbox.core.utils.appContext
import com.wanbaohe.code.editor.CodeEditorDataStore
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

@Immutable
data class CreateHtmlUiState(
    val itemId: Int = 0,
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val data: String = "",
    val allCategories: List<Category> = emptyList(),
    val selectedCategories: Set<Category> = emptySet(),
    /** 图标注册表 key（IconRegistry）；为空时卡片回退为标题首字（LetterIcon） */
    val iconName: String = "",
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val isDownloading: Boolean = false,
    val isEditing: Boolean = false
)

class CreateHtmlComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val draftId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onResult: ScreenCallback? = null,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val dataDraftHelper: DataDraftHelper,
    private val dataDraftDao: DataDraftDao,
    private val htmlDownloader: HtmlDownloader,
    private val activityLogRecorder: ActivityLogRecorder
) : BaseComponent(dispatchersHolder, componentContext) {

    private var editorDraftId: Long? = null

    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()
    private val categoryDao = appDatabase.categoryDao()

    private val _uiState = MutableStateFlow(CreateHtmlUiState())
    val uiState = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val reorderableType: ReorderableType = ReorderableType.CATEGORY

    init {
        componentContext.lifecycle.doOnDestroy {
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
                    message = "HTML 编辑页已打开，等待用户完成编辑"
                )
            )
        }

        componentScope.launch {
            AppEventBus.editorResultEvents.collect { event ->
                onCodeEditorResult(event)
            }
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
                        url = draft.url.ifEmpty { itemWithRelation.item.url },
                        data = draft.data.ifEmpty { itemData },
                        selectedCategories = itemWithRelation.categories.toSet(),
                        iconName = itemWithRelation.item.iconName.orEmpty(),
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
                url = draft.url,
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

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, isDirty = true) }
    }

    /** 标题行 leadIcon：存图标注册表 key，空串表示回退标题首字。 */
    fun onIconChange(iconName: String) {
        _uiState.update { it.copy(iconName = iconName, isDirty = true) }
    }

    fun onUrlChange(url: String) {
        _uiState.update { it.copy(url = url, isDirty = true) }
    }

    fun onDataChange(data: String) {
        _uiState.update { it.copy(data = data, isDirty = true) }
    }

    fun saveItem(
        onSuccess: (savedItemId: Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentState = _uiState.value
        if (resolveSaveUrl(currentState) == null) {
            onFailure(AppContext.getString(R.string.error_url_empty))
            return
        }
        val resolvedTitle = currentState.title.ifBlank {
            if (currentState.data.isNotBlank()) {
                extractTitleFromHtml(currentState.data, currentState.url)
            } else {
                currentState.url.ifBlank {
                    AppContext.getString(R.string.new_html)
                }
            }
        }

        val now = System.currentTimeMillis()
        _uiState.update { it.copy(isSaving = true) }

        componentScope.launch(ioDispatcher) {
            val draft = dataDraftHelper.getById(draftId)
            val isEditing = draft?.itemId != null
            val normalizedUrl = resolveSaveUrl(currentState)
            val item = ItemEntity(
                id = draft?.itemId ?: 0,
                remoteId = null,
                source = Source.LOCAL,
                listType = ListItemType.HTML.id,
                title = resolvedTitle,
                description = currentState.description,
                url = normalizedUrl ?: currentState.url,
                iconName = currentState.iconName.ifBlank {
                    resolvedTitle.firstOrNull()?.toString()
                },
                createdAt = now,
                updatedAt = now,
                publishedAt = now,
            )
            val itemId = itemDao.upsertItem(item)

            // 本地可编辑 + 置顶
            itemDao.upsertUserState(
                ItemUserState(
                    itemId = itemId,
                    isPinned = true,
                    pinnedAt = now,
                    canEdit = true,
                    updatedAt = now,
                )
            )

            // data 资源 + link：编辑时重用现有 data_id，避免 id=0 重复插入
            val existingDataId = if (draft?.itemId != null) {
                itemDataDao.getDataLinkByItemId(itemId)
            } else null
            val dataId = itemDataDao.upsert(
                ItemDataEntity(
                    id = existingDataId ?: 0,
                    title = resolvedTitle,
                    kind = ItemDataKind.HTML,
                    data = currentState.data,
                    url = currentState.url,
                    source = Source.LOCAL,
                )
            )
            itemDataDao.insertLink(ItemDataLink(itemId = itemId, dataId = dataId))

            applyCategories(
                itemId = itemId,
                selected = currentState.selectedCategories,
                isEditing = isEditing,
            )
            _uiState.update { it.copy(isSaving = false, isDirty = false) }

            dataDraftHelper.deleteById(draftId)

            // 常驻返回栈里的收藏查看器（浏览器）据此重载快照，不显示编辑前的旧内容
            AppEventBus.emit(
                BookmarkChangedEvent(
                    itemId = itemId,
                    url = currentState.url,
                )
            )

            onResult?.invoke(
                ScreenCallbackResult.saved(
                    id = itemId,
                    url = currentState.url,
                    message = "HTML 条目已保存"
                )
            )

            sendOperationEvent(item.copy(id = itemId), onSuccess)
        }
    }

    /**
     * 保存时落库的 url（已 trim）。url 是收藏在浏览器里打开的唯一入口，
     * 仍然必填——「分类可选」不等于「内容可空」，两者都空只会在列表里生成点不开的僵尸卡片。
     */
    private fun resolveSaveUrl(state: CreateHtmlUiState): String? =
        state.url.trim().takeIf { it.isNotEmpty() }

    /**
     * 分类现在可选：选了就按选择落库；编辑时清空选择保留原有分类；
     * 新建且一个都没选时回落到「未分类」，避免条目在 INNER JOIN item_category 的列表里消失。
     */
    private suspend fun applyCategories(
        itemId: Int,
        selected: Set<Category>,
        isEditing: Boolean,
    ) {
        if (selected.isEmpty()) {
            if (!isEditing) {
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(
                        itemId = itemId,
                        categoryId = ensureDefaultCategoryId().toInt(),
                    )
                )
            }
            return
        }
        categoryDao.deleteCategoriesByItemId(itemId)
        selected.forEach { category ->
            itemDao.insertItemCategoryCrossRef(
                ItemCategoryCrossRef(itemId = itemId, categoryId = category.id)
            )
        }
    }

    private suspend fun ensureDefaultCategoryId(): Long {
        val name = AppContext.getString(R.string.note_default_category)
        categoryDao.getCategoryByName(name)?.let { return it.id.toLong() }
        return categoryDao.insert(
            Category(name = name, canEdit = true, source = Source.LOCAL)
        )
    }

    private fun sendOperationEvent(item: ItemEntity, onSuccess: (Int) -> Unit = { }) {
        componentScope.launch(ioDispatcher) {
            val description = AppContext.getString(
                R.string.operation_history_description_html_saved,
                item.title
            )
            val screen = Screen.CreateHtml()
            activityLogRecorder.recordHtml(
                itemId = item.id,
                title = item.title,
                appTitle = appContext.getString(screen.title),
                description = description,
                screenRoute = screen.id.toString()
            )
            withContext(Dispatchers.Main) {
                onSuccess(item.id)
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

    fun addCategory(name: String) {
        if (name.isBlank()) return
        componentScope.launch(ioDispatcher) {
            val newCategory = Category(
                name = name,
                canEdit = true,
                source = com.shifenmiao.model.Source.LOCAL
            )
            val insertedId = categoryDao.insertOrUpdateCategory(newCategory)
            val inserted = categoryDao.getCategoryById(insertedId.toInt()) ?: return@launch
            _uiState.update { state ->
                state.copy(
                    selectedCategories = state.selectedCategories + inserted,
                    isDirty = true
                )
            }
        }
    }

    private fun extractTitleFromHtml(html: String, url: String): String {
        val titleMatch = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE).find(html)
        if (titleMatch != null) return titleMatch.groupValues[1].trim()
        return try {
            java.net.URI(url).host?.removePrefix("www.") ?: url
        } catch (_: Exception) {
            url
        }
    }

    fun loadHtmlDataFromUrl(
        config: HtmlDownloadConfig = HtmlDownloadConfig.DEFAULT,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        val url = _uiState.value.url

        if (url.isBlank()) {
            onFailure?.invoke(AppContext.getString(R.string.error_url_empty))
            return
        }

        _uiState.update { it.copy(isDownloading = true) }

        componentScope.launch(ioDispatcher) {
            try {
                when (val result = htmlDownloader.downloadHtml(url, config)) {
                    is HtmlDownloadResult.Success -> {
                        val autoTitle = extractTitleFromHtml(result.htmlContent, result.url)
                        _uiState.update {
                            it.copy(
                                data = result.htmlContent,
                                title = if (it.title.isBlank()) autoTitle else it.title,
                                isDirty = true,
                                isDownloading = false,
                                url = result.url
                            )
                        }
                        withContext(Dispatchers.Main) {
                            onSuccess?.invoke(result.htmlContent)
                        }
                    }

                    is HtmlDownloadResult.Failure -> {
                        _uiState.update { it.copy(isDownloading = false) }
                        withContext(Dispatchers.Main) {
                            onFailure?.invoke(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDownloading = false) }
                withContext(Dispatchers.Main) {
                    onFailure?.invoke("Unexpected error: ${e.message}")
                }
            }
        }
    }

    suspend fun prepareCodeEditorDraft(): Long {
        val codeDraftId = CodeEditorDataStore.put(dataDraftHelper, _uiState.value.data)
        editorDraftId = codeDraftId
        return codeDraftId
    }

    private fun onCodeEditorResult(event: EditorResultEvent) {
        val expected = editorDraftId ?: return
        if (event.editDraftId != expected) return
        componentScope.launch(ioDispatcher) {
            _uiState.update {
                it.copy(data = event.text, isDirty = true)
            }
            CodeEditorDataStore.clear(dataDraftHelper, event.editDraftId)
            editorDraftId = null
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            draftId: Long,
            onGoBack: () -> Unit,
            onResult: ScreenCallback?,
        ): CreateHtmlComponent
    }
}
