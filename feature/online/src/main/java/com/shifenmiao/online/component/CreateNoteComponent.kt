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

import com.shifenmiao.database.ai.EditorConversationRepository
import com.shifenmiao.model.ai.AIConversationEntryType

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.longtask.LongTaskCenter
import com.shifenmiao.base.longtask.LongTaskKind
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.EditorAiPromptKey
import com.shifenmiao.common.ai.EditorAiPrompts
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.online.R as OnlineR
import com.shifenmiao.model.item.ItemDataUiState
import com.shifenmiao.model.note.NoteResult
import com.shifenmiao.model.note.NoteSaveParams
import com.shifenmiao.model.note.NoteService
import com.shifenmiao.model.reorderable.ReorderableType
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.ScreenCallback
import com.wanbaohe.core.ui.utils.navigation.ScreenCallbackResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CreateNoteComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val draftId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onResult: ScreenCallback? = null,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val noteService: NoteService,
    private val editorHistory: EditorConversationRepository,
    private val dataDraftHelper: DataDraftHelper,
    private val aiPromptExecutor: AIPromptExecutor,
    private val editorAiPrompts: EditorAiPrompts,
    private val aiEngineManager: AIEngineManager,
    private val longTaskCenter: LongTaskCenter,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()
    private val categoryDao = appDatabase.categoryDao()

    private val _uiState = MutableStateFlow(ItemDataUiState())
    val uiState = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _aiState = MutableStateFlow(NoteAiUiState())
    val aiState: StateFlow<NoteAiUiState> = _aiState.asStateFlow()

    /** 当前全局 AI 引擎；面板展示与生成请求共用它，切换走 AppComponent 的公共模型选择面板。 */
    val currentAiEngine: StateFlow<AiEngine> = aiEngineManager.currentAIEngine

    private var aiGenerationJob: Job? = null

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
                    message = "笔记编辑页已打开，等待用户完成编辑"
                )
            )
        }
    }

    private suspend fun loadDraft() {
        val draft = dataDraftHelper.getById(draftId)
        val relatedItem = draft?.itemId?.let { itemId -> itemDao.getItemById(itemId).firstOrNull() }

        if (draft != null && relatedItem != null) {
            val itemData = itemDataDao.getByItemId(relatedItem.item.id)?.data ?: ""
            _uiState.update {
                it.copy(
                    itemId = relatedItem.item.id,
                    title = draft.title.ifEmpty { relatedItem.item.title },
                    description = draft.description.ifEmpty { relatedItem.item.description },
                    data = draft.data.ifEmpty { itemData },
                    selectedCategories = relatedItem.categories.map(Category::toModel).toSet(),
                    isEditing = true,
                    isLoading = false,
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                title = draft?.title.orEmpty(),
                description = draft?.description.orEmpty(),
                data = draft?.data.orEmpty(),
                isLoading = false,
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

    /** 打开笔记 AI 写作面板；[markdown] 是点开面板那一刻的正文快照。 */
    fun openAiAction(action: NoteAiAction, markdown: String) {
        aiGenerationJob?.cancel()
        aiGenerationJob = null
        _aiState.update {
            it.copy(
                visible = true,
                action = action,
                prompt = "",
                originalMarkdown = markdown,
                result = "",
                isGenerating = false,
                error = null,
                tokens = 0,
            )
        }
        loadAiActionPrompt(action)
    }

    fun changeAiAction(action: NoteAiAction) {
        if (_aiState.value.action == action) return
        aiGenerationJob?.cancel()
        aiGenerationJob = null
        _aiState.update {
            it.copy(
                action = action,
                prompt = "",
                result = "",
                isGenerating = false,
                error = null,
                tokens = 0,
            )
        }
        loadAiActionPrompt(action)
    }

    private fun loadAiActionPrompt(action: NoteAiAction) {
        componentScope.launch(ioDispatcher) {
            val prompt = editorAiPrompts.get(action.promptKey)
            _aiState.update { current ->
                if (current.visible && current.action == action && current.prompt.isBlank()) {
                    current.copy(prompt = prompt)
                } else {
                    current
                }
            }
        }
    }

    fun updateAiPrompt(prompt: String) {
        _aiState.update { it.copy(prompt = prompt) }
    }

    fun cancelAiGeneration() {
        aiGenerationJob?.cancel()
        aiGenerationJob = null
        _aiState.update { it.copy(isGenerating = false, error = null) }
    }

    fun dismissAi() {
        aiGenerationJob?.cancel()
        aiGenerationJob = null
        _aiState.update { it.copy(visible = false, isGenerating = false) }
    }

    /**
     * 执行一次 AI 写作。请求包在 [LongTaskCenter] 里跑，退后台时进程仍有前台服务兜底，
     * 流式结果会持续写进 [aiState]，回到前台能直接看到。
     */
    suspend fun recentAiResults(action: NoteAiAction) =
        editorHistory.recent(AIConversationEntryType.NOTE_EDIT, action.name)

    fun selectAiResult(result: String) {
        _aiState.update { if (it.isGenerating) it else it.copy(result = cleanAiMarkdown(result), error = null, tokens = 0) }
    }

    fun runAiGeneration() {
        val state = _aiState.value
        if (state.isGenerating) return
        if (state.originalMarkdown.isBlank()) {
            _aiState.update {
                it.copy(error = AppContext.getString(OnlineR.string.note_ai_too_short))
            }
            return
        }
        // 统一准入：直连引擎直接执行；代理引擎先拉起登录；积分闸门由 AIPromptExecutor 的 MANAGED 计费负责。
        aiPromptExecutor.runWithAccessGate(source = NOTE_AI_WRITER_SOURCE) {
            startAiGeneration(state)
        }
    }

    /** 供保存弹窗的「AI 生成标题/摘要」复用同一套登录准入。 */
    fun runWithAiAccess(source: String, onAllowed: () -> Unit) {
        aiPromptExecutor.runWithAccessGate(source = source, onAllowed = onAllowed)
    }

    private fun startAiGeneration(state: NoteAiUiState) {
        aiGenerationJob?.cancel()
        aiGenerationJob = componentScope.launch(ioDispatcher) {
            _aiState.update { it.copy(isGenerating = true, result = "", error = null, tokens = 0) }
            val taskId = "note-ai-rewrite-$draftId"
            longTaskCenter.begin(
                id = taskId,
                kind = LongTaskKind.AI_GENERATION,
                title = AppContext.getString(OnlineR.string.note_ai_keepalive_title),
            )
            try {
                val input = AppContext.getContext().getString(
                    OnlineR.string.note_ai_input_template,
                    state.prompt.trim(),
                    state.originalMarkdown,
                )
                val result = aiPromptExecutor.executeStreaming(
                    input = input,
                    systemPrompt = editorAiPrompts.get(EditorAiPromptKey.NOTE_SYSTEM),
                    billingDesc = AppContext.getString(OnlineR.string.note_ai_billing_desc),
                    editorCall = AIPromptExecutor.EditorCall(
                        AIConversationEntryType.NOTE_EDIT, state.action.name, AppContext.getString(state.action.labelRes),
                    ),
                    onDelta = { full ->
                        _aiState.update { it.copy(result = cleanAiMarkdown(full)) }
                    },
                )
                if (!isActive) return@launch
                if (result.isSuccess) {
                    val cleaned = cleanAiMarkdown(result.content)
                    _aiState.update {
                        it.copy(
                            isGenerating = false,
                            result = cleaned,
                            error = if (cleaned.isBlank()) {
                                AppContext.getString(OnlineR.string.note_ai_empty_result)
                            } else {
                                null
                            },
                            tokens = result.totalTokens,
                        )
                    }
                } else {
                    _aiState.update {
                        it.copy(
                            isGenerating = false,
                            error = result.errorMessage
                                ?: AppContext.getString(OnlineR.string.note_ai_empty_result),
                        )
                    }
                }
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                _aiState.update {
                    it.copy(
                        isGenerating = false,
                        error = t.message ?: AppContext.getString(OnlineR.string.note_ai_empty_result),
                    )
                }
            } finally {
                longTaskCenter.end(taskId)
            }
        }
    }

    /**
     * 清理模型常见的外层代码块围栏；只在整个输出都被 ``` 包裹时剥壳，
     * 正文内部真正的代码块不受影响。
     */
    private fun cleanAiMarkdown(raw: String): String {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("```")) return trimmed
        val firstNewline = trimmed.indexOf('\n')
        if (firstNewline < 0) return trimmed
        val closingFence = trimmed.lastIndexOf("```")
        if (closingFence <= firstNewline) return trimmed
        if (trimmed.substring(closingFence).trim() != "```") return trimmed
        return trimmed.substring(firstNewline + 1, closingFence).trim()
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, isDirty = true) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, isDirty = true) }
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
        val currentDescription = _uiState.value.description
        _uiState.update {
            it.copy(
                data = markdown,
                title = if (currentTitle.isNotBlank()) currentTitle else derived.title,
                description = if (currentDescription.isNotBlank()) {
                    currentDescription
                } else {
                    derived.description
                },
                isDirty = true
            )
        }
        saveItem(onSuccess, onFailure)
    }

    /**
     * 保存确认弹窗确认后使用：标题/简介/分类已经在弹窗里编辑完成，
     * 这里只负责把它们和正文一起提交，不再二次覆盖用户输入。
     */
    fun saveNoteData(
        markdown: String,
        title: String,
        description: String,
        categories: List<com.shifenmiao.model.Category>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        _uiState.update {
            it.copy(
                data = markdown,
                title = title.trim(),
                description = description.trim(),
                selectedCategories = categories.toSet(),
                isDirty = true,
            )
        }
        saveItem(onSuccess, onFailure)
    }

    /**
     * 用当前 AI 引擎为笔记生成标题、简介和分类建议。
     *
     * 失败（未登录/积分不足/网络/模型返回不可解析）时回退到本地 [MarkdownSummary]，
     * 并把错误原因带回 UI：保存流程永远不会因为 AI 不可用而卡住。
     */
    suspend fun generateMetadata(markdown: String): NoteMetadata = withContext(ioDispatcher) {
        val local = MarkdownSummary.derive(markdown)
        if (markdown.isBlank()) {
            return@withContext NoteMetadata(
                title = local.title,
                description = local.description,
                categoryId = null,
                usedFallback = true,
                errorMessage = null,
            )
        }

        val categories = _categories.value
        val categoryNames = categories
            .joinToString(separator = "、") { it.name }
            .ifBlank { AppContext.getString(OnlineR.string.note_ai_no_categories) }
        val systemPrompt = editorAiPrompts.get(EditorAiPromptKey.NOTE_METADATA)
        val input = AppContext.getContext().getString(
            OnlineR.string.note_ai_metadata_input_template,
            categoryNames,
            markdown.take(6000),
        )

        val result = aiPromptExecutor.execute(
            input = input,
            systemPrompt = systemPrompt,
            billingDesc = AppContext.getString(com.shifenmiao.common.R.string.note_ai_generate),
            editorCall = AIPromptExecutor.EditorCall(
                AIConversationEntryType.NOTE_EDIT, "Metadata",
                AppContext.getString(com.shifenmiao.common.R.string.note_ai_generate),
            ),
        )
        if (!result.isSuccess) {
            return@withContext NoteMetadata(
                title = local.title,
                description = local.description,
                categoryId = null,
                usedFallback = true,
                errorMessage = result.errorMessage,
            )
        }

        val parsed = runCatching { parseMetadataJson(result.content) }.getOrNull()
        val suggestedCategory = parsed?.category.orEmpty().trim()
        val matchedCategory = categories.firstOrNull {
            it.name.equals(suggestedCategory, ignoreCase = true)
        } ?: categories.firstOrNull {
            suggestedCategory.isNotBlank() &&
                (it.name.contains(suggestedCategory) || suggestedCategory.contains(it.name))
        }

        NoteMetadata(
            title = parsed?.title?.takeIf { it.isNotBlank() } ?: local.title,
            description = parsed?.description?.takeIf { it.isNotBlank() } ?: local.description,
            categoryId = matchedCategory?.id,
            usedFallback = false,
            errorMessage = null,
        )
    }

    private fun parseMetadataJson(content: String): AiNoteMetadataJson? {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val obj = runCatching {
            Json.parseToJsonElement(content.substring(start, end + 1)).jsonObject
        }.getOrNull() ?: return null
        return AiNoteMetadataJson(
            title = obj["title"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
            description = obj["summary"]?.jsonPrimitive?.contentOrNull
                ?: obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
            category = obj["category"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
        )
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
    fun addCategory(
        name: String,
        onAdded: (com.shifenmiao.model.Category) -> Unit = {},
    ) {
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
            withContext(Dispatchers.Main) { onAdded(inserted) }
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

data class NoteMetadata(
    val title: String,
    val description: String,
    val categoryId: Int?,
    val usedFallback: Boolean,
    val errorMessage: String?,
)

private data class AiNoteMetadataJson(
    val title: String,
    val description: String,
    val category: String,
)

private const val NOTE_AI_WRITER_SOURCE = "note_ai_writer"
