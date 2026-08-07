package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.callback.ToolCallback
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolExecutionContext
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ContextAwareCallbackAgentTool
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.ai.service.CreationMetaService
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.note.NoteDetail
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.item.ItemEntityParams
import com.shifenmiao.model.note.NoteResult
import com.shifenmiao.model.note.NoteSaveParams
import com.shifenmiao.model.note.NoteService
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import org.json.JSONObject
import javax.inject.Inject

/**
 * 笔记保存工具 — 创建或更新笔记
 *
 * 创建笔记：提供 title、content、category 即可。
 * 更新笔记：额外传入 existing_note_id，会覆盖该笔记的全部内容；category 可省略，未传时保留原分类。
 *
 * 成功后自动记录活动日志，无需调用方处理。
 */
class CreateNoteTool @Inject constructor(
    private val noteService: NoteService,
    private val textProvider: AgentToolTextProvider,
    private val interactiveToolRuntime: InteractiveToolRuntime,
    private val creationMetaService: CreationMetaService,
    private val gson: Gson
) : AgentTool, ContextAwareCallbackAgentTool {
    override val name = "create_note"
    override val description = textProvider.string(R.string.agent_tool_create_note_description)
    override val title: String = textProvider.string(R.string.agent_tool_create_note_title)
    override val summary: String = textProvider.string(R.string.agent_tool_create_note_summary)
    override val category: ToolCategory = ToolCategory.BUSINESS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE
    override val dependencies: List<String> = listOf("discover_apps")
    override val sortOrder: Int = -70
    override val parametersSchema = ToolParameters(
        properties = mapOf(
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_create_note_param_title)
            ),
            "content" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_create_note_param_content)
            ),
            "category" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_create_note_param_category)
            ),
            "existing_note_id" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_create_note_param_existing_note_id)
            )
        ),
        required = listOf("title", "content")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return saveNoteAndMaybeOpen(
            arguments = arguments,
            context = AgentToolExecutionContext(),
            callback = null
        )
    }

    override suspend fun execute(arguments: String, callback: ToolCallback): AgentToolResult {
        return saveNoteAndMaybeOpen(
            arguments = arguments,
            context = AgentToolExecutionContext(
                toolCallId = "create_note_${System.currentTimeMillis()}"
            ),
            callback = callback
        )
    }

    override suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext,
        callback: ToolCallback
    ): AgentToolResult {
        return saveNoteAndMaybeOpen(
            arguments = arguments,
            context = context,
            callback = callback
        )
    }

    private suspend fun saveNoteAndMaybeOpen(
        arguments: String,
        context: AgentToolExecutionContext,
        callback: ToolCallback?
    ): AgentToolResult {
        return try {
            val json = JSONObject(arguments)
            val title = json.getString("title")
            val content = json.getString("content")
            val categoryName = json.optString("category").trim()
            val existingNoteId = json.optInt("existing_note_id", 0).takeIf { it > 0 }

            val existingNote = existingNoteId?.let { noteId ->
                noteService.getNoteById(noteId) ?: return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_create_note_note_not_found,
                        noteId
                    ),
                    isError = true
                )
            }

            val categorySelection = resolveCategorySelection(
                categoryName = categoryName,
                existingNote = existingNote
            ) ?: return buildMissingCategoryResult(existingNoteId != null)

            val result = noteService.saveNote(
                NoteSaveParams(
                    existingItemId = existingNoteId,
                    title = title,
                    description = content.take(100),
                    data = content,
                    categoryIds = categorySelection.categoryIds
                )
            )

            when (result) {
                is NoteResult.Success -> {
                    val openDecision = callback != null && askWhetherToOpenNote(
                        context = context,
                        result = result
                    )
                    val opened = if (openDecision) {
                        openNote(
                            callback = callback,
                            itemId = result.itemId,
                            title = result.title,
                            description = content.take(100)
                        )
                        true
                    } else {
                        false
                    }

                    AgentToolResult(
                        content = gson.toJson(
                            CreateNoteResponse(
                                tool = name,
                                mode = if (existingNoteId == null) OperationMode.CREATE else OperationMode.UPDATE,
                                success = true,
                                noteId = result.itemId,
                                title = result.title,
                                categoryIds = categorySelection.categoryIds.map(Long::toInt),
                                categoryNames = categorySelection.categoryNames,
                                categoryCreated = categorySelection.categoryCreated,
                                categoryPreserved = categorySelection.categoryPreserved,
                                askedToOpen = callback != null,
                                openDecision = if (callback == null) null else openDecision,
                                opened = opened,
                                message = when {
                                    opened -> textProvider.string(R.string.agent_tool_create_note_opened)
                                    callback == null -> textProvider.string(
                                        R.string.agent_tool_create_note_success,
                                        result.title,
                                        result.itemId
                                    )
                                    else -> textProvider.string(R.string.agent_tool_create_note_not_opened)
                                }
                            )
                        )
                    )
                }
                is NoteResult.Error -> {
                    AgentToolResult(content = result.message, isError = true)
                }
            }
        } catch (e: IllegalArgumentException) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_create_note_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_create_note_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun resolveCategorySelection(
        categoryName: String,
        existingNote: NoteDetail?
    ): NoteCategorySelection? {
        if (categoryName.isNotBlank()) {
            val resolution = creationMetaService.resolveCategoryByName(categoryName)
            return NoteCategorySelection(
                categoryIds = listOf(resolution.category.id.toLong()),
                categoryNames = listOf(resolution.category.name.trim()).filter(String::isNotEmpty),
                categoryCreated = resolution.created,
                categoryPreserved = false
            )
        }

        val existingCategories = existingNote?.categories
            ?.mapNotNull { category ->
                val name = category.name?.trim().orEmpty()
                if (name.isBlank()) null else category.id.toLong() to name
            }
            ?.distinctBy { it.first }
            .orEmpty()

        if (existingCategories.isEmpty()) {
            return null
        }

        return NoteCategorySelection(
            categoryIds = existingCategories.map { it.first },
            categoryNames = existingCategories.map { it.second },
            categoryCreated = false,
            categoryPreserved = true
        )
    }

    private suspend fun buildMissingCategoryResult(isUpdateMode: Boolean): AgentToolResult {
        val message = if (isUpdateMode) {
            textProvider.string(R.string.agent_tool_create_note_category_missing_for_update)
        } else {
            val availableCategories = creationMetaService.getAllCategoryNames()
            if (availableCategories.isEmpty()) {
                textProvider.string(R.string.agent_tool_create_note_category_required_for_create_no_existing)
            } else {
                textProvider.string(
                    R.string.agent_tool_create_note_category_required_for_create,
                    availableCategories.joinToString(separator = "、")
                )
            }
        }
        return AgentToolResult(content = message, isError = true)
    }

    private suspend fun askWhetherToOpenNote(
        context: AgentToolExecutionContext,
        result: NoteResult.Success
    ): Boolean {
        val toolCallId = context.toolCallId ?: "create_note_${result.itemId}_${System.currentTimeMillis()}"
        val decision = interactiveToolRuntime.requestConfirmation(
            toolCallId = "${toolCallId}_open_note",
            toolName = name,
            interactionOwnerId = context.interactionOwnerId,
            dialogTitle = textProvider.string(R.string.agent_tool_create_note_open_confirm_title),
            dialogMessage = textProvider.string(
                R.string.agent_tool_create_note_open_confirm_message,
                result.title
            ),
            confirmPayload = """{"decision":"open"}""",
            dismissPayload = """{"decision":"not_open"}"""
        )
        return runCatching {
            decision != null && JSONObject(decision).optString("decision") == "open"
        }.getOrDefault(false)
    }

    private fun openNote(
        callback: ToolCallback,
        itemId: Int,
        title: String,
        description: String
    ) {
        val screen = Screen.NoteItem(
            itemEntityParams = ItemEntityParams(
                id = itemId,
                title = title,
                description = description,
                listType = ListItemType.NOTE.id
            )
        )
        callback.openScreen(screen)
    }

    private data class CreateNoteResponse(
        val tool: String,
        val mode: OperationMode,
        val success: Boolean,
        val noteId: Int,
        val title: String,
        val categoryIds: List<Int>,
        val categoryNames: List<String>,
        val categoryCreated: Boolean,
        val categoryPreserved: Boolean,
        val askedToOpen: Boolean,
        val openDecision: Boolean?,
        val opened: Boolean,
        val message: String
    )

    private data class NoteCategorySelection(
        val categoryIds: List<Long>,
        val categoryNames: List<String>,
        val categoryCreated: Boolean,
        val categoryPreserved: Boolean
    )

    private enum class OperationMode {
        CREATE,
        UPDATE
    }
}
