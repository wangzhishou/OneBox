package com.wanbaohe.code.editor.component

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.common.recent.RecentAccessRepository
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.event.EditorResultEvent
import com.t8rin.imagetoolbox.core.data.utils.getPath
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.domain.saving.FilenameCreator
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.shifenmiao.core.R
import com.wanbaohe.code.editor.CodeEditorDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * WebView 通用代码编辑器 UI 状态
 */
@Immutable
data class CodeEditorUiState(
    val content: String = "",
    val fileName: String? = null,
    val fileUri: Uri? = null,
    val language: String = "plaintext",
    val isDirty: Boolean = false,
    val isLoading: Boolean = false,
    val editDraftId: Long = 0L,
    val editTitle: String? = null,
    val historyList: List<RecentAccessEntity> = emptyList()
)

/**
 * 简易语言检测器（Phase 5 已实现，Phase 6 不变）
 */
internal object LanguageDetector {
    private val EXT_MAP = mapOf(
        "kt" to "kotlin", "kts" to "kotlin",
        "java" to "java",
        "py" to "python",
        "js" to "javascript", "jsx" to "javascript", "mjs" to "javascript",
        "ts" to "typescript", "tsx" to "typescript",
        "json" to "json",
        "html" to "html", "htm" to "html", "xhtml" to "html",
        "css" to "css", "scss" to "css",
        "sql" to "sql",
        "md" to "markdown", "markdown" to "markdown", "mdown" to "markdown",
        "yml" to "yaml", "yaml" to "yaml",
        "sh" to "shell", "bash" to "shell", "zsh" to "shell",
        "xml" to "html",           // XML 用 html lang (cm6 没有专门的 xml lang)
        "dart" to "plaintext",     // CM6 无官方 dart lang
        "rs" to "plaintext",       // CM6 无官方 rust lang
        "toml" to "plaintext",
        "gradle" to "kotlin",      // groovy/kts: 与 kotlin 语法较接近
        "groovy" to "kotlin",
        "kts" to "kotlin",
    )

    fun fromExtension(uri: Uri?): String {
        if (uri == null) return "plaintext"
        val ext = uri.path?.substringAfterLast('.', "")?.lowercase().orEmpty()
        return EXT_MAP[ext] ?: "plaintext"
    }
}

/**
 * 可被代码编辑器打开的文件扩展名白名单（用于在历史记录中过滤）
 */
private val TEXT_EXTENSIONS = setOf(
    "txt", "log", "json", "xml", "yaml", "yml",
    "css", "scss", "js", "jsx", "mjs", "ts", "tsx",
    "kt", "kts", "java", "py", "rb", "go", "rs",
    "c", "cpp", "h", "hpp", "sh", "bash", "zsh",
    "conf", "ini", "cfg", "properties", "gradle", "toml",
    "csv", "config", "gitignore", "editorconfig",
    "sql", "dart", "md", "markdown", "mdown", "html", "htm", "xhtml"
)

/**
 * WebView 通用代码编辑器 Component
 *
 * Phase 6：完整业务能力
 * - 文件 I/O（loadFile / saveToExistingFile / saveToNewFile / newFile）
 * - 历史记录（基于 [RecentAccessRepository]，与项目共用）
 * - 草稿自动保存（web 端 localStorage + Kotlin 端 dirty 标记）
 * - editDraftId 模式（基于 [CodeEditorDataStore]，保存后通过 EventBus 回传）
 */
class CodeEditorComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUri: Uri?,
    @Assisted("editDraftId") val editDraftId: Long,
    @Assisted("editTitle") val editTitle: String?,
    @Assisted val onGoBack: () -> Unit,
    private val recentAccessRepository: RecentAccessRepository,
    private val filenameCreator: FilenameCreator,
    private val dataDraftHelper: DataDraftHelper,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        CodeEditorUiState(
            fileName = initialUri?.lastPathSegment,
            fileUri = initialUri,
            language = LanguageDetector.fromExtension(initialUri),
            editDraftId = editDraftId,
            editTitle = editTitle
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        componentContext.lifecycle.doOnDestroy {
            if (editDraftId != 0L) {
                componentScope.launch(ioDispatcher) {
                    CodeEditorDataStore.clear(dataDraftHelper, editDraftId)
                }
            }
        }

        when {
            initialUri != null -> {
                loadFile(AppContext.getContext(), initialUri)
            }
            editDraftId != 0L -> {
                componentScope.launch(ioDispatcher) {
                    val initialText = CodeEditorDataStore.get(dataDraftHelper, editDraftId) ?: ""
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                content = initialText,
                                language = "html",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
        loadHistory()
    }

    // ── 文件加载 ──────────────────────────────────────────

    /**
     * 加载文件
     */
    fun loadFile(context: Context, uri: Uri) {
        _uiState.update { it.copy(isLoading = true) }
        componentScope.launch(ioDispatcher) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: ""
                val displayName = uri.lastPathSegment ?: "untitled"
                val language = LanguageDetector.fromExtension(uri)
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            content = content,
                            fileName = displayName,
                            fileUri = uri,
                            language = language,
                            isDirty = false,
                            isLoading = false
                        )
                    }
                }
                // 记录到历史
                recordRecent(uri, displayName)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                    // 失败时给用户 Toast 提示,否则静默失败毫无线索
                    AppToastHost.showToast(
                        e.message ?: AppContext.getString(R.string.error_message)
                    )
                }
            }
        }
    }

    // ── 文件保存 ──────────────────────────────────────────

    /**
     * 覆盖保存到已有文件
     */
    fun saveToExistingFile(
        context: Context,
        content: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uri = _uiState.value.fileUri ?: run {
            onFailure("no file uri")
            return
        }
        componentScope.launch(ioDispatcher) {
            try {
                // componentScope 已在 ioDispatcher 上,不需要 withContext(Dispatchers.IO) 嵌套
                context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                }
                val displayName = uri.lastPathSegment ?: "untitled"
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isDirty = false) }
                }
                recordRecent(uri, displayName)
                onSuccess()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "save failed")
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
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        componentScope.launch(ioDispatcher) {
            try {
                // "wt" = 写入并截断,与 saveToExistingFile 行为一致
                context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                }
                val displayName = uri.lastPathSegment ?: "untitled"
                val language = LanguageDetector.fromExtension(uri)
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            fileName = displayName,
                            fileUri = uri,
                            language = language,
                            isDirty = false
                        )
                    }
                }
                recordRecent(uri, displayName)
                onSuccess()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e.message ?: "save failed")
                }
            }
        }
    }

    /**
     * 基于内容与当前语言生成文件名（扩展名跟语言走，避免一律 .txt）
     */
    fun generateFileNameFromContent(content: String): String {
        _uiState.value.fileName?.let { return it }

        val extension = MimeType.All.fromLanguage(_uiState.value.language).toFileExtension()
        val firstLine = content.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.take(30)
            ?.replace(Regex("[\\\\/:*?\"<>|]"), "")
            ?.trim()
            ?.take(10)
            .orEmpty()

        return if (firstLine.isNotBlank()) {
            "$firstLine.$extension"
        } else {
            filenameCreator.constructRandomFilename(extension)
        }
    }

    // ── 新建 / 状态 ──────────────────────────────────────

    /**
     * 重置编辑器（新建）
     */
    fun newFile() {
        _uiState.update {
            CodeEditorUiState(historyList = it.historyList)
        }
    }

    fun markAsDirty() {
        _uiState.update { it.copy(isDirty = true) }
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
            // 重新拉取,只显示代码编辑器能打开的扩展名
            loadHistoryInternal()
        }
    }

    /**
     * 加载历史
     */
    fun loadHistory() {
        componentScope.launch(ioDispatcher) {
            loadHistoryInternal()
        }
    }

    private suspend fun loadHistoryInternal() {
        val entities = recentAccessRepository.getAll(RecentAccessRepository.DEFAULT_LIMIT)
        val filtered = entities.filter { entity ->
            val ext = entity.displayName.substringAfterLast('.', "").lowercase()
            ext in TEXT_EXTENSIONS
        }
        withContext(Dispatchers.Main) {
            _uiState.update { it.copy(historyList = filtered) }
        }
    }

    /**
     * 清除历史
     */
    fun clearHistory() {
        componentScope.launch(ioDispatcher) {
            // 仅清除文本/代码文件的历史,不影响其他模块
            val entities = recentAccessRepository.getAll(RecentAccessRepository.DEFAULT_LIMIT)
            entities
                .filter { it.displayName.substringAfterLast('.', "").lowercase() in TEXT_EXTENSIONS }
                .forEach { recentAccessRepository.remove(it.uri) }
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(historyList = emptyList()) }
            }
        }
    }

    // ── Edit 模式（基于 [CodeEditorDataStore]） ──────────

    /**
     * 作为纯文本编辑器保存结果（edit mode）
     *
     * - 写入 [CodeEditorDataStore]（draftId 不变）
     * - emit [EditorResultEvent]，调用方通过 [AppEventBus.editorResultEvents] 接收
     * - 标记 isDirty=false,防止退出确认弹窗再次弹出
     */
    fun saveEditResult(content: String, onSuccess: () -> Unit) {
        if (editDraftId == 0L) {
            onSuccess()
            return
        }
        componentScope.launch(ioDispatcher) {
            CodeEditorDataStore.update(dataDraftHelper, editDraftId, content)
            withContext(Dispatchers.Main) {
                AppEventBus.emit(EditorResultEvent(editDraftId, content))
                _uiState.update { it.copy(isDirty = false) }
                onSuccess()
            }
        }
    }

    fun isEditMode(): Boolean = editDraftId != 0L

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUri: Uri?,
            @Assisted("editDraftId") editDraftId: Long,
            @Assisted("editTitle") editTitle: String?,
            onGoBack: () -> Unit
        ): CodeEditorComponent
    }
}
