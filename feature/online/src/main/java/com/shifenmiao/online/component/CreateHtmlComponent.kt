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
import com.shifenmiao.model.event.EditorResultEvent
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.network.downloader.HtmlDownloadConfig
import com.shifenmiao.network.downloader.HtmlDownloadResult
import com.shifenmiao.network.downloader.HtmlDownloader
import com.shifenmiao.webview.WebViewComponent
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
    webViewComponentFactory: WebViewComponent.Factory,
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

    private val _webViewParams = MutableStateFlow<WebViewParams?>(WebViewParams())

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val reorderableType: ReorderableType = ReorderableType.CATEGORY

    val webViewComponent = webViewComponentFactory(
        componentContext = componentContext,
        onGoBack = {},
        webViewParams = null
    )

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
                        isEditing = true
                    )
                }
                updateWebViewParams()
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
        updateWebViewParams()
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

    fun updateWebViewParams() {
        val currentState = _uiState.value
        _webViewParams.update {
            it?.copy(
                title = currentState.title,
                url = currentState.url,
                htmlData = currentState.data,
                isHtml = true
            )
        }
    }

    fun loadWebViewData() {
        webViewComponent.setWebViewParams(_webViewParams.value)
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, isDirty = true) }
        updateWebViewParams()
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, isDirty = true) }
    }

    fun onUrlChange(url: String) {
        _uiState.update { it.copy(url = url, isDirty = true) }
        updateWebViewParams()
    }

    fun onDataChange(data: String) {
        _uiState.update { it.copy(data = data, isDirty = true) }
        updateWebViewParams()
    }

    fun saveItem(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentState = _uiState.value
        if (currentState.selectedCategories.isEmpty()) {
            onFailure(AppContext.getString(R.string.error_category_empty))
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

            val item = ItemEntity(
                id = draft?.itemId ?: 0,
                remoteId = null,
                source = Source.LOCAL,
                listType = ListItemType.HTML.id,
                title = resolvedTitle,
                description = currentState.description,
                url = currentState.url,
                iconName = currentState.title.firstOrNull()?.toString(),
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

            categoryDao.deleteCategoriesByItemId(itemId)
            currentState.selectedCategories.forEach { category ->
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(itemId = itemId, categoryId = category.id)
                )
            }
            _uiState.update { it.copy(isSaving = false, isDirty = false) }

            dataDraftHelper.deleteById(draftId)

            onResult?.invoke(
                ScreenCallbackResult.saved(
                    id = itemId,
                    url = currentState.url,
                    message = "HTML 条目已保存"
                )
            )

            sendOperationEvent(item, onSuccess)
        }
    }

    private fun sendOperationEvent(item: ItemEntity, onSuccess: () -> Unit = { }) {
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
                        updateWebViewParams()
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
            updateWebViewParams()
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
