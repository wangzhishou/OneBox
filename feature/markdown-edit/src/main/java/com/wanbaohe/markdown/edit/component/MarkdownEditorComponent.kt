package com.wanbaohe.markdown.edit.component

import android.content.Context
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.common.recent.RecentAccessRepository
import com.shifenmiao.core.R
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.data.utils.getFilename
import com.t8rin.imagetoolbox.core.data.utils.getPath
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.FilenameCreator
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.utils.appContext
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.markdown.edit.EditorDataStore
import com.shifenmiao.model.event.EditorResultEvent
import com.wanbaohe.markdown.edit.webview.MarkdownPreloadConfig
import com.wanbaohe.markdown.edit.webview.MarkdownWebViewPoolHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import com.wanbaohe.markdown.edit.R as MarkdownR

/**
 * Markdown 编辑器历史条目 (UI 轻量包装)
 */
@Immutable
data class MarkdownHistoryEntry(
    val uri: Uri,
    val displayName: String,
    val visitedAtEpochMs: Long
)

/**
 * Markdown 编辑器 UI 状态
 */
@Immutable
data class MarkdownEditorUiState(
    val content: String = "",
    val fileName: String? = null,
    val filePath: String = "",
    val fileUri: Uri? = null,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val editDraftId: Long = 0L,
    val editTitle: String? = null,
    val historyList: List<MarkdownHistoryEntry> = emptyList()
)

/**
 * 可被 Markdown 编辑器打开的扩展名白名单（历史记录过滤用）
 */
private val MARKDOWN_EXTENSIONS = setOf(
    "md", "markdown", "mkd", "mdown", "txt"
)

private fun isMarkdownExtension(name: String): Boolean {
    val ext = name.substringAfterLast('.', "").lowercase()
    return ext in MARKDOWN_EXTENSIONS
}

/**
 * 草稿状态（用于进程恢复）
 */
@Serializable
private data class DraftState(
    val content: String,
    val fileName: String?,
    val isDirty: Boolean,
    val filePath: String = "",
)

/**
 * 纯净的 Markdown 编辑器 Component
 *
 * 功能：
 * - 编辑 Markdown 内容
 * - 打开 MD/TXT 文件
 * - 保存为 MD 文件
 * - 草稿自动保存
 */
class MarkdownEditorComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUri: Uri?,
    @Assisted("editDraftId") val editDraftId: Long,
    @Assisted("editTitle") val editTitle: String?,
    @Assisted val onGoBack: () -> Unit,
    private val fileController: FileController,
    private val filenameCreator: FilenameCreator,
    private val dataDraftHelper: DataDraftHelper,
    private val recentAccessRepository: RecentAccessRepository,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private class DraftHolder(var draft: DraftState?) : InstanceKeeper.Instance

    private val draftStateKey: String = "MarkdownEditorComponent.draft"

    private val draftHolder: DraftHolder =
        componentContext.instanceKeeper.getOrCreate(draftStateKey) { DraftHolder(draft = null) }

    private val restoredDraft: DraftState? = draftHolder.draft

    private val _uiState = MutableStateFlow(
        restoredDraft?.let {
            MarkdownEditorUiState(
                content = it.content,
                fileName = it.fileName,
                isDirty = it.isDirty,
                filePath = it.filePath,
                editDraftId = editDraftId,
                editTitle = editTitle
            )
        } ?: MarkdownEditorUiState(
            editDraftId = editDraftId,
            editTitle = editTitle
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        // 预加载 HTML（使用当前主题配色）
        MarkdownWebViewPoolHelper.preloadHtml(
            MarkdownPreloadConfig.fromColorScheme(
                colorScheme = AppTheme.colorScheme,
                isDarkTheme = AppTheme.isDarkTheme,
                storageKey = "markdown_editor_screen"
            )
        )

        componentContext.lifecycle.doOnDestroy {
            MarkdownWebViewPoolHelper.clearPreloadedHtml()
            // 组件销毁时清理编辑模式草稿
            if (editDraftId != 0L) {
                val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
                scope.launch { dataDraftHelper.deleteById(editDraftId) }
            }
        }

        // 保存草稿到内存
        componentScope.launch {
            uiState.collect { state ->
                draftHolder.draft = DraftState(
                    content = state.content,
                    fileName = state.fileName,
                    isDirty = state.isDirty,
                    filePath = state.filePath
                )
            }
        }

        // 如果有初始 URI，加载文件
        // 如果没有初始 URI 且没有草稿（或草稿内容为空），加载默认 README
        when {
            restoredDraft == null && editDraftId != 0L -> {
                // 作为文本编辑器启动，从数据库加载初始内容
                componentScope.launch(ioDispatcher) {
                    val initialText = EditorDataStore.get(dataDraftHelper, editDraftId) ?: ""
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(content = initialText) }
                    }
                }
            }

            initialUri != null -> {
                loadFile(AppContext.getContext(), initialUri)
            }

            restoredDraft == null || restoredDraft.content.isEmpty() -> {
                loadDefaultReadme()
            }
        }

        // 加载历史记录
        loadHistory()
    }

    /**
     * 加载默认的 README 示例文件
     */
    private fun loadDefaultReadme() {
        componentScope.launch(ioDispatcher) {
            try {
                val context = AppContext.getContext()
                val content = context.resources.openRawResource(MarkdownR.raw.markdown_readme)
                    .bufferedReader()
                    .use { it.readText() }

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            content = content,
                            fileName = null, // 不设置文件名，表示是新文档
                            isDirty = false,
                            isLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                // 加载失败则保持空白
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    /**
     * 更新内容
     */
    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content, isDirty = true) }
    }

    /**
     * 标记为已修改
     */
    fun markAsDirty() {
        _uiState.update { it.copy(isDirty = true) }
    }

    /**
     * 加载文件
     */
    fun loadFile(context: Context, uri: Uri) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        componentScope.launch(ioDispatcher) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: ""

                // 使用文章内容摘要作为显示名称
                val summary = MarkdownSummary.derive(content, titleMaxChars = 30)
                val fallbackName = uri.lastPathSegment?.substringAfterLast('/') ?: "untitled"
                val displayName = summary.title.ifBlank { fallbackName }

                // 记录到历史 (基于 [RecentAccessRepository],与项目共用)
                recordRecent(uri, displayName)

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            content = content,
                            fileName = displayName, // 使用文章标题作为显示名称
                            fileUri = uri,
                            filePath = uri.toString().getPath(context),
                            isDirty = false,
                            isLoading = false,
                            historyList = loadMarkdownHistorySnapshot()
                        )
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppContext.getString(R.string.error_message)
                        )
                    }
                }
            }
        }
    }

    /**
     * 保存到已有文件（覆盖保存）
     */
    fun saveToExistingFile(
        context: Context,
        content: String,
        onSuccess: (SaveResult.Success) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uri = _uiState.value.fileUri ?: run {
            onFailure(AppContext.getString(R.string.save_failure))
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        componentScope.launch(ioDispatcher) {
            try {
                // 使用 "wt" 模式覆盖写入
                context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                }

                // 使用文章内容摘要作为显示名称
                val summary = MarkdownSummary.derive(content, titleMaxChars = 30)
                val displayName = summary.title.ifBlank {
                    uri.lastPathSegment?.substringAfterLast('/') ?: "untitled"
                }

                // 更新历史记录
                recordRecent(uri, displayName)

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            fileName = displayName,
                            isDirty = false,
                            isSaving = false,
                            filePath = uri.toString().getPath(context),
                            historyList = loadMarkdownHistorySnapshot()
                        )
                    }
                    draftHolder.draft = null
                    val savePath = uri.toString().getPath(context)
                    val fileName = "$displayName.md"
                    onSuccess(
                        SaveResult.Success(
                            fileName = fileName,
                            fileUri = uri.toString(),
                            savingPath = savePath,
                            message = context.getString(
                                com.t8rin.imagetoolbox.core.resources.R.string.saved_to,
                                savePath,
                                fileName
                            )
                        )
                    )
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSaving = false) }
                    onFailure(AppContext.getString(R.string.save_failure))
                }
            }
        }
    }

    /**
     * 保存到新文件（另存为）
     */
    fun saveToNewFile(
        context: Context,
        uri: Uri,
        content: String,
        onSuccess: (SaveResult.Success) -> Unit,
        onFailure: (String) -> Unit
    ) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        componentScope.launch(ioDispatcher) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                }

                // 使用文章内容摘要作为显示名称
                val summary = MarkdownSummary.derive(content, titleMaxChars = 30)
                val displayName = summary.title.ifBlank {
                    uri.lastPathSegment?.substringAfterLast('/') ?: "untitled"
                }

                // 记录到历史
                recordRecent(uri, displayName)

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            fileName = displayName, // 使用文章标题作为显示名称
                            fileUri = uri,
                            isDirty = false,
                            isSaving = false,
                            filePath = uri.toString().getPath(context),
                            historyList = loadMarkdownHistorySnapshot()
                        )
                    }
                    // 清除草稿
                    draftHolder.draft = null
                    val savePath = uri.toString().getPath(context)
                    val fileName = "$displayName.md"
                    onSuccess(
                        SaveResult.Success(
                            fileName = fileName,
                            fileUri = uri.toString(),
                            savingPath = savePath,
                            message = context.getString(
                                com.t8rin.imagetoolbox.core.resources.R.string.saved_to,
                                savePath,
                                fileName
                            )
                        )
                    )
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isSaving = false) }
                    onFailure(AppContext.getString(R.string.save_failure))
                }
            }
        }
    }

    /**
     * 基于内容生成文件名（供外部调用，传入最新内容）
     */
    fun generateFileNameFromContent(content: String): String {
        // 如果已有文件名，直接使用
        _uiState.value.fileName?.let { return it }

        // 从内容中提取标题作为文件名
        val summary = MarkdownSummary.derive(content, titleMaxChars = 30)
        val titleName = summary.title
            .replace(Regex("[\\\\/:*?\"<>|]"), "") // 移除文件名非法字符
            .trim()
            .take(10) // 限制长度

        return if (titleName.isNotBlank()) {
            "$titleName.md"
        } else {
            // 使用 filenameCreator 生成随机文件名
            filenameCreator.constructRandomFilename("md")
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 重置编辑器（新建）
     */
    fun newFile() {
        _uiState.update {
            MarkdownEditorUiState(historyList = it.historyList)
        }
        draftHolder.draft = null
    }

    /**
     * 加载历史记录
     */
    fun loadHistory() {
        componentScope.launch(ioDispatcher) {
            val history = loadMarkdownHistorySnapshot()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(historyList = history) }
            }
        }
    }

    /**
     * 清除历史记录
     */
    fun clearHistory() {
        componentScope.launch(ioDispatcher) {
            // 仅清除 markdown/text 扩展名的历史,不影响其他模块
            val entities = recentAccessRepository.getAll(RecentAccessRepository.DEFAULT_LIMIT)
            entities
                .filter { isMarkdownExtension(it.displayName) }
                .forEach { recentAccessRepository.remove(it.uri) }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(historyList = emptyList()) }
            }
        }
    }

    // ── 历史记录（基于 [RecentAccessRepository]） ─────────

    /**
     * 记录文件访问到共享历史
     */
    private fun recordRecent(uri: Uri, displayName: String) {
        componentScope.launch(ioDispatcher) {
            recentAccessRepository.recordAccess(
                uri = uri.toString(),
                displayName = displayName,
                accessType = RecentAccessRepository.TYPE_FILE,
                pathHint = uri.toString().getPath(AppContext.getContext())
            )
        }
    }

    /**
     * 一次性快照:读取 markdown 相关的历史并映射为 [MarkdownHistoryEntry]
     */
    private suspend fun loadMarkdownHistorySnapshot(): List<MarkdownHistoryEntry> {
        val entities = recentAccessRepository.getAll(RecentAccessRepository.DEFAULT_LIMIT)
        return entities
            .filter { isMarkdownExtension(it.displayName) }
            .mapNotNull { entity ->
                val uri = runCatching { Uri.parse(entity.uri) }.getOrNull() ?: return@mapNotNull null
                MarkdownHistoryEntry(
                    uri = uri,
                    displayName = entity.displayName,
                    visitedAtEpochMs = entity.accessedAt
                )
            }
    }

    /**
     * 判断当前是否作为文本编辑器使用（即通过 editDraftId 启动）
     */
    fun isEditMode(): Boolean = uiState.value.editDraftId != 0L

    /**
     * 作为纯文本编辑器保存结果
     */
    fun saveEditResult(content: String, onSuccess: () -> Unit) {
        val draftId = uiState.value.editDraftId
        if (draftId != 0L) {
            componentScope.launch(ioDispatcher) {
                EditorDataStore.update(dataDraftHelper, draftId, content)
                withContext(Dispatchers.Main) {
                    AppEventBus.emit(
                        EditorResultEvent(draftId, content)
                    )
                    // 标记为未修改，防止退出时再次弹窗
                    _uiState.update { it.copy(isDirty = false) }
                    draftHolder.draft = null
                    onSuccess()
                }
            }
        }
    }

    /**
     * 是否有已打开的文件（用于判断保存还是另存为）
     */
    fun hasExistingFile(): Boolean = _uiState.value.fileUri != null

    /**
     * 检查是否有未保存的更改
     */
    fun hasUnsavedChanges(): Boolean = _uiState.value.isDirty

    fun savePdf(
        webView: WebView?,
        context: ComponentActivity,
        fileName: String? = null,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (webView == null) {
            appContext.getString(R.string.pdf_export_failure, R.string.pdf_url_not_found)
            return
        }
        try {
            val jobName = fileName ?: "Markdown_${System.currentTimeMillis()}.pdf"
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )
            Toast.makeText(
                context,
                appContext.getString(R.string.pdf_export_prepare),
                Toast.LENGTH_SHORT
            ).show()
            // 注意：由于Android打印服务是异步的，这里无法精确知道PDF何时完成
            // 只能对打印任务发起进行回调
            onComplete(true, null)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                appContext.getString(R.string.pdf_export_failure, e.localizedMessage),
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
            onComplete(false, null)
        }

    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUri: Uri?,
            @Assisted("editDraftId") editDraftId: Long,
            @Assisted("editTitle") editTitle: String?,
            onGoBack: () -> Unit
        ): MarkdownEditorComponent
    }
}
