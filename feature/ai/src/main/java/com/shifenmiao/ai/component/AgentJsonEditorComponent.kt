package com.shifenmiao.ai.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.markdown.edit.EditorDataStore
import com.shifenmiao.model.event.EditorResultEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * 代码编辑器 UI 状态
 */
@Immutable
data class CodeEditorUiState(
    val content: String = "",
    val isDirty: Boolean = false,
    val editDraftId: Long = 0L,
    val editTitle: String? = null,
    val isTreeMode: Boolean = false,
    val parseError: String? = null,
)

/**
 * 轻量代码编辑器 Component，用于编辑 Agent JSON 等代码内容。
 *
 * 功能：
 * - 从 EditorDataStore 加载初始内容
 * - 纯文本编辑（Monospace）
 * - 保存时通过 EventBus 回传结果
 */
class AgentJsonEditorComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("editDraftId") val editDraftId: Long,
    @Assisted("editTitle") val editTitle: String?,
    @Assisted val onGoBack: () -> Unit,
    private val dataDraftHelper: DataDraftHelper,
    private val gson: Gson,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        CodeEditorUiState(
            editDraftId = editDraftId,
            editTitle = editTitle
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        // 加载初始内容
        if (editDraftId != 0L) {
            componentScope.launch(ioDispatcher) {
                val initialText = EditorDataStore.get(dataDraftHelper, editDraftId) ?: ""
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(content = initialText) }
                }
            }
        }

        componentContext.lifecycle.doOnDestroy {
            if (editDraftId != 0L) {
                val scope = kotlinx.coroutines.CoroutineScope(ioDispatcher)
                scope.launch { EditorDataStore.clear(dataDraftHelper, editDraftId) }
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
     * 保存编辑结果并通过 EventBus 回传
     */
    fun saveEditResult(onSuccess: () -> Unit) {
        val draftId = _uiState.value.editDraftId
        val content = _uiState.value.content
        if (draftId != 0L) {
            componentScope.launch(ioDispatcher) {
                EditorDataStore.update(dataDraftHelper, draftId, content)
                withContext(Dispatchers.Main) {
                    AppEventBus.emit(
                        EditorResultEvent(draftId, content)
                    )
                    _uiState.update { it.copy(isDirty = false) }
                    onSuccess()
                }
            }
        } else {
            onSuccess()
        }
    }

    /**
     * 检查是否有未保存的更改
     */
    fun hasUnsavedChanges(): Boolean = _uiState.value.isDirty

    /**
     * 切换文本/树形模式
     */
    fun toggleTreeMode() {
        val newMode = !_uiState.value.isTreeMode
        _uiState.update {
            it.copy(
                isTreeMode = newMode,
                parseError = if (newMode) validateJson(it.content) else null
            )
        }
    }

    /**
     * 用 Gson 美化格式化当前 JSON
     */
    fun formatJson() {
        val content = _uiState.value.content
        if (content.isBlank()) return
        try {
            val element = JsonParser.parseString(content)
            val prettyGson = GsonBuilder().setPrettyPrinting().create()
            val formatted = prettyGson.toJson(element)
            _uiState.update { it.copy(content = formatted, isDirty = true) }
        } catch (_: Exception) {
            // 格式错误时静默忽略，不破坏用户输入
        }
    }

    /**
     * 通过路径定位并修改 JSON 节点值，然后序列化更新 content
     *
     * @param path 路径列表，例如 ["body", "children", 0, "props", "text"]
     * @param newValue 新的字符串值
     */
    fun updateJsonValue(path: List<String>, newValue: String) {
        val content = _uiState.value.content
        if (content.isBlank() || path.isEmpty()) return
        try {
            val root = JsonParser.parseString(content)
            if (updateElementAtPath(root, path, 0, newValue)) {
                val updated = gson.toJson(root)
                _uiState.update { it.copy(content = updated, isDirty = true) }
            }
        } catch (_: Exception) {
            // 解析失败时忽略
        }
    }

    /**
     * 递归更新 JsonElement 指定路径的值
     */
    private fun updateElementAtPath(element: JsonElement, path: List<String>, index: Int, newValue: String): Boolean {
        if (index >= path.size) return false
        val key = path[index]
        val isLast = index == path.size - 1

        return when {
            element.isJsonObject -> {
                val obj = element.asJsonObject
                if (isLast) {
                    obj.addProperty(key, newValue)
                    true
                } else {
                    val child = obj.get(key) ?: return false
                    updateElementAtPath(child, path, index + 1, newValue)
                }
            }
            element.isJsonArray -> {
                val arr = element.asJsonArray
                val arrIndex = key.toIntOrNull() ?: return false
                if (arrIndex < 0 || arrIndex >= arr.size()) return false
                if (isLast) {
                    arr.set(arrIndex, com.google.gson.JsonPrimitive(newValue))
                    true
                } else {
                    updateElementAtPath(arr.get(arrIndex), path, index + 1, newValue)
                }
            }
            else -> false
        }
    }

    /**
     * 验证 JSON 字符串是否合法，返回错误信息或 null
     */
    private fun validateJson(json: String): String? {
        return try {
            JsonParser.parseString(json)
            null
        } catch (e: Exception) {
            e.message
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("editDraftId") editDraftId: Long,
            @Assisted("editTitle") editTitle: String?,
            onGoBack: () -> Unit
        ): AgentJsonEditorComponent
    }
}
