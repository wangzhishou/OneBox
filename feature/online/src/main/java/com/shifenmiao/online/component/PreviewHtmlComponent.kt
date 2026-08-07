package com.shifenmiao.online.component

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemDataKind
import com.shifenmiao.database.item.entity.ItemDataLink
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.event.EditorResultEvent
import com.shifenmiao.model.item.ItemEntityParams
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.webview.WebViewComponent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallback
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult
import com.wanbaohe.code.editor.CodeEditorDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class PreviewHtmlUiState(
    val itemId: Int = 0,
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val data: String = "",
    val localUri: Uri? = null,
    val localName: String? = null,
    val isLoadingLocal: Boolean = false,
)

class PreviewHtmlComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val itemEntityParams: ItemEntityParams?,
    @Assisted val localUri: Uri?,
    @Assisted val onResult: ScreenCallback? = null,
    webViewComponentFactory: WebViewComponent.Factory,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val dataDraftHelper: DataDraftHelper,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()

    private val _uiState = MutableStateFlow(PreviewHtmlUiState())
    val uiState = _uiState.asStateFlow()

    private val _webViewParams = MutableStateFlow<WebViewParams?>(WebViewParams())

    val webViewComponent = webViewComponentFactory(
        componentContext = componentContext,
        onGoBack = {},
        webViewParams = null
    )

    private var editorDraftId: Long? = null

    init {
        if (localUri != null) {
            loadLocalFile(AppContext.getContext(), localUri)
        } else {
            componentScope.launch(ioDispatcher) {
                itemEntityParams?.let { params ->
                    if (params.id != null) {
                        val itemWithRelation = itemDao.getItemById(params.id!!).firstOrNull()
                        if (itemWithRelation != null) {
                            val itemData = itemDataDao.getByItemId(itemWithRelation.item.id)
                            _uiState.update {
                                it.copy(
                                    itemId = itemWithRelation.item.id,
                                    title = itemWithRelation.item.title,
                                    description = itemWithRelation.item.description,
                                    url = itemData?.url ?: itemWithRelation.item.url,
                                    data = itemData?.data ?: "",
                                )
                            }
                            updateWebViewParams()
                            onResult?.invoke(
                                ScreenCallbackResult.opened(
                                    id = itemWithRelation.item.id,
                                    url = itemData?.url ?: itemWithRelation.item.url,
                                    message = "HTML 预览页已打开"
                                )
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                itemId = 0,
                                title = params.title,
                                description = params.description,
                                url = params.url.orEmpty(),
                                data = params.data.orEmpty(),
                            )
                        }
                        updateWebViewParams()
                        onResult?.invoke(
                            ScreenCallbackResult.opened(
                                id = params.id,
                                url = params.url,
                                message = "HTML 预览页已打开"
                            )
                        )
                    }
                }
            }
        }

        componentScope.launch {
            AppEventBus.editorResultEvents.collect { event ->
                onEditorResult(event)
            }
        }
    }

    private fun updateWebViewParams() {
        val currentState = _uiState.value
        val titleForWebView = currentState.localName?.takeIf { it.isNotBlank() }
            ?: currentState.title
        _webViewParams.update {
            it?.copy(
                title = titleForWebView,
                url = currentState.url,
                htmlData = currentState.data,
                isHtml = true
            )
        }
    }

    fun loadWebViewData() {
        webViewComponent.setWebViewParams(_webViewParams.value)
    }

    fun refreshData() {
        componentScope.launch(ioDispatcher) {
            itemEntityParams?.id?.let { itemId ->
                val itemWithRelation = itemDao.getItemById(itemId).firstOrNull()
                if (itemWithRelation != null) {
                    val itemData = itemDataDao.getByItemId(itemWithRelation.item.id)
                    _uiState.update {
                        it.copy(
                            itemId = itemWithRelation.item.id,
                            title = itemWithRelation.item.title,
                            description = itemWithRelation.item.description,
                            url = itemData?.url ?: itemWithRelation.item.url,
                            data = itemData?.data ?: "",
                        )
                    }
                    updateWebViewParams()
                    loadWebViewData()
                }
            }
        }
    }

    fun loadLocalFile(context: Context, uri: Uri) {
        val name = uri.lastPathSegment ?: "local.html"
        _uiState.update {
            it.copy(
                isLoadingLocal = true,
                localUri = uri,
                localName = name,
                url = uri.toString(),
            )
        }
        updateWebViewParams()
        componentScope.launch(ioDispatcher) {
            val size = queryFileSize(context, uri)
            if (size != null && size > MAX_LOCAL_FILE_SIZE_BYTES) {
                withContext(Dispatchers.Main) {
                    AppToastHost.showToast("文件过大，无法渲染预览")
                    _uiState.update {
                        it.copy(isLoadingLocal = false)
                    }
                }
                return@launch
            }
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                }
            }.getOrNull()
            if (text == null) {
                withContext(Dispatchers.Main) {
                    AppToastHost.showToast("文件读取失败，尝试直接加载")
                    _uiState.update {
                        it.copy(isLoadingLocal = false)
                    }
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        data = text,
                        isLoadingLocal = false,
                    )
                }
                updateWebViewParams()
                loadWebViewData()
            }
        }
    }

    private fun queryFileSize(context: Context, uri: Uri): Long? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
                } else null
            }
        }.getOrNull()
    }

    suspend fun prepareEditorDraft(content: String): Long {
        val draftId = CodeEditorDataStore.put(dataDraftHelper, content)
        editorDraftId = draftId
        return draftId
    }

    private fun onEditorResult(event: EditorResultEvent) {
        val expected = editorDraftId ?: return
        if (event.editDraftId != expected) return
        val itemId = _uiState.value.itemId
        if (itemId == 0) return
        componentScope.launch(ioDispatcher) {
            val now = System.currentTimeMillis()
            val existingDataId = itemDataDao.getDataLinkByItemId(itemId)
            val dataId = itemDataDao.upsert(
                ItemDataEntity(
                    id = existingDataId ?: 0,
                    title = _uiState.value.title,
                    kind = ItemDataKind.HTML,
                    data = event.text,
                    url = _uiState.value.url,
                    source = Source.LOCAL,
                    updatedAt = now,
                )
            )
            itemDataDao.insertLink(ItemDataLink(itemId = itemId, dataId = dataId))
            CodeEditorDataStore.clear(dataDraftHelper, event.editDraftId)
            editorDraftId = null
            refreshData()
            onResult?.invoke(
                ScreenCallbackResult.saved(
                    id = itemId,
                    data = event.text,
                    message = "HTML 已更新"
                )
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            itemEntityParams: ItemEntityParams?,
            localUri: Uri?,
            onResult: ScreenCallback?
        ): PreviewHtmlComponent
    }

    companion object {
        private const val MAX_LOCAL_FILE_SIZE_BYTES = 1024L * 1024L
    }
}
