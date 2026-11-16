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

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.Source
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
    private val dataDraftHelper: DataDraftHelper,
    private val aiPromptExecutor: AIPromptExecutor,
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
            .ifBlank { "（暂无）" }
        val systemPrompt = """
            你是笔记整理助手。请阅读用户给出的 Markdown 笔记正文，输出：
            1. title：简洁标题，不超过 30 个字；
            2. summary：一句话简介，不超过 80 个字；
            3. category：从给定分类列表中选择最贴切的一个；没有合适分类时输出空字符串。
            使用与正文相同的语言。只输出一个 JSON 对象，不要 Markdown 代码块或解释：
            {"title":"...","summary":"...","category":"..."}
        """.trimIndent()
        val input = "可选分类：$categoryNames\n\n笔记正文：\n${markdown.take(6000)}"

        val result = aiPromptExecutor.execute(
            input = input,
            systemPrompt = systemPrompt,
            billingDesc = AppContext.getString(com.shifenmiao.common.R.string.note_ai_generate),
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
