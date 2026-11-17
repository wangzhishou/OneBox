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

package com.wanbaohe.code.editor.component

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.common.recent.RecentAccessRepository
import com.shifenmiao.core.R
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.event.EditorResultEvent
import com.t8rin.imagetoolbox.core.data.utils.getPath
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.domain.saving.FilenameCreator
import com.wanbaohe.code.editor.CodeEditorDataStore
import com.wanbaohe.code.editor.draft.CodeEditorDraftRepository
import com.wanbaohe.codeeditor.CodeLanguage
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
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
 * 代码编辑器 UI 状态
 */
@Immutable
data class CodeEditorUiState(
    val content: String = "",
    val fileName: String? = null,
    val fileUri: Uri? = null,
    val language: CodeLanguage = CodeLanguage.PlainText,
    val isDirty: Boolean = false,
    val isLoading: Boolean = false,
    /** file:// 加载失败(无读权限)标记,驱动统一授权引导;成功加载/新建时复位 */
    val loadFailed: Boolean = false,
    val editDraftId: Long = 0L,
    val editTitle: String? = null,
    val historyList: List<RecentAccessEntity> = emptyList()
)

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
 * 代码编辑器 Component
 *
 * Phase 6：完整业务能力
 * - 文件 I/O（loadFile / saveToExistingFile / saveToNewFile / newFile）
 * - 历史记录（基于 [RecentAccessRepository]，与项目共用）
 * - 草稿自动保存（web 端 localStorage + Kotlin 端 dirty 标记）
 * - editDraftId 模式（基于 [CodeEditorDataStore]，保存后通过 EventBus 回传）
 */
@Suppress("LongParameterList", "TooManyFunctions") // Decompose 组件：assisted + 注入依赖与文件/历史/草稿流程都收在这里
class CodeEditorComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUri: Uri?,
    @Assisted("editDraftId") val editDraftId: Long,
    @Assisted("editTitle") val editTitle: String?,
    @Assisted val onGoBack: () -> Unit,
    private val recentAccessRepository: RecentAccessRepository,
    private val filenameCreator: FilenameCreator,
    private val dataDraftHelper: DataDraftHelper,
    private val draftRepository: CodeEditorDraftRepository,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        CodeEditorUiState(
            fileName = initialUri?.lastPathSegment,
            fileUri = initialUri,
            language = CodeLanguage.fromFileName(initialUri?.lastPathSegment),
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
                                language = CodeLanguage.Html,
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
    @Suppress("TooGenericExceptionCaught") // ContentResolver 的 provider 可能抛任意 RuntimeException，统一兜底提示
    fun loadFile(context: Context, uri: Uri) {
        _uiState.update { it.copy(isLoading = true) }
        componentScope.launch(ioDispatcher) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: ""
                val displayName = uri.lastPathSegment ?: "untitled"
                val language = CodeLanguage.fromFileName(displayName)
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            content = content,
                            fileName = displayName,
                            fileUri = uri,
                            language = language,
                            isDirty = false,
                            isLoading = false,
                            loadFailed = false
                        )
                    }
                }
                // 记录到历史
                recordRecent(uri, displayName)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = uri.scheme == "file"
                        )
                    }
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
    @Suppress("TooGenericExceptionCaught") // 同上：写入失败的具体异常类型由 provider 决定
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
                    // 内容已落盘：同步 content，后续草稿校验的「原始正文」才是磁盘上的最新版本
                    _uiState.update { it.copy(isDirty = false, content = content) }
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
    @Suppress("TooGenericExceptionCaught") // 同上：写入失败的具体异常类型由 provider 决定
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
                val language = CodeLanguage.fromFileName(displayName)
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            fileName = displayName,
                            fileUri = uri,
                            language = language,
                            content = content,
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

        val extension = MimeType.All.fromLanguage(_uiState.value.language.key).toFileExtension()
        val firstLine = content.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.take(FILE_NAME_SOURCE_CHARS)
            ?.replace(Regex("[\\\\/:*?\"<>|]"), "")
            ?.trim()
            ?.take(FILE_NAME_MAX_CHARS)
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

    /**
     * 用户从工具坞切换高亮语言：只影响着色与另存为扩展名，不改文件本身。
     */
    fun updateLanguage(language: CodeLanguage) {
        _uiState.update { it.copy(language = language) }
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

    // ── 未保存草稿（进程被杀后恢复） ─────────────────────

    /**
     * 草稿分桶 key：
     * - 临时编辑（editDraftId）不落草稿——正文本来就在 Room 草稿表里；
     * - 有文件按 URI 分桶；
     * - 新建页共用一个桶，靠 original="" 校验。
     */
    fun draftKey(): String = when {
        editDraftId != 0L -> "edit_$editDraftId"
        _uiState.value.fileUri != null -> "file_${_uiState.value.fileUri}"
        else -> "code_editor_new_file"
    }

    /**
     * 读取可恢复的草稿。
     *
     * 只有草稿里的「原始正文」与当前磁盘正文完全一致才返回——文件在别处被改过时，
     * 恢复旧草稿会把磁盘内容盖回去，这里直接放弃并清掉脏草稿。
     */
    suspend fun restoreDraft(): String? = withContext(ioDispatcher) {
        if (isEditMode()) return@withContext null
        val key = draftKey()
        val draft = draftRepository.load(key) ?: return@withContext null
        if (draft.originalText != _uiState.value.content) {
            draftRepository.clear(key)
            return@withContext null
        }
        if (draft.text == draft.originalText) {
            draftRepository.clear(key)
            return@withContext null
        }
        draft.text
    }

    /** 输入停顿后落盘草稿；临时编辑模式不处理。 */
    suspend fun persistDraft(content: String) = withContext(ioDispatcher) {
        if (isEditMode()) return@withContext
        draftRepository.save(
            draftKey = draftKey(),
            originalText = _uiState.value.content,
            draftText = content,
        )
    }

    /** 保存成功时清掉草稿。 */
    suspend fun clearDraft(key: String = draftKey()) = withContext(ioDispatcher) {
        draftRepository.clear(key)
    }

    /**
     * 丢弃内容时同步清空草稿：退出页面的动作里不能依赖协程，
     * 页面一销毁协程就被取消，草稿会留在磁盘上。
     */
    fun discardDraft(key: String = draftKey()) {
        draftRepository.clear(key)
    }

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

/** 生成文件名时参考的首行字符数上限。 */
private const val FILE_NAME_SOURCE_CHARS = 30

/** 生成文件名时最终保留的字符数上限。 */
private const val FILE_NAME_MAX_CHARS = 10
