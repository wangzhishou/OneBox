package com.shifenmiao.ai.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.model.ModelProvider.AppJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
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

    /** 展示用的美化实例,在全局 AppJson 配置基础上开 prettyPrint */
    private val prettyJson = Json(AppJson) { prettyPrint = true }

    /**
     * 美化格式化当前 JSON
     */
    fun formatJson() {
        val content = _uiState.value.content
        if (content.isBlank()) return
        try {
            val element = AppJson.parseToJsonElement(content)
            val formatted = prettyJson.encodeToString(JsonElement.serializer(), element)
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
            val root = AppJson.parseToJsonElement(content)
            updateElementAtPath(root, path, 0, newValue)?.let { updated ->
                _uiState.update { it.copy(content = updated.toString(), isDirty = true) }
            }
        } catch (_: Exception) {
            // 解析失败时忽略
        }
    }

    /**
     * 递归更新 JsonElement 指定路径的值(kotlinx 树不可变,返回替换后的新树;失败返回 null)
     */
    private fun updateElementAtPath(element: JsonElement, path: List<String>, index: Int, newValue: String): JsonElement? {
        if (index >= path.size) return null
        val key = path[index]
        val isLast = index == path.size - 1

        return when (element) {
            is JsonObject -> {
                if (isLast) {
                    JsonObject(element + (key to JsonPrimitive(newValue)))
                } else {
                    val child = element[key] ?: return null
                    val updatedChild = updateElementAtPath(child, path, index + 1, newValue) ?: return null
                    JsonObject(element + (key to updatedChild))
                }
            }
            is JsonArray -> {
                val arrIndex = key.toIntOrNull() ?: return null
                if (arrIndex < 0 || arrIndex >= element.size) return null
                if (isLast) {
                    JsonArray(element.toMutableList().apply { set(arrIndex, JsonPrimitive(newValue)) })
                } else {
                    val updatedChild = updateElementAtPath(element[arrIndex], path, index + 1, newValue) ?: return null
                    JsonArray(element.toMutableList().apply { set(arrIndex, updatedChild) })
                }
            }
            else -> null
        }
    }

    /**
     * 验证 JSON 字符串是否合法，返回错误信息或 null
     */
    private fun validateJson(json: String): String? {
        return try {
            AppJson.parseToJsonElement(json)
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
