package com.wanbaohe.teleprompter.ai.tool

import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.teleprompter.R
import com.wanbaohe.teleprompter.service.TeleprompterService
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

/**
 * AI Agent 工具：提词器文稿管理
 *
 * 支持文稿的列表查询、详情读取、创建、更新与删除，
 * 结果附带 deeplink 可直达提词器编辑页或播放页。
 */
class ManageTeleprompterTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val teleprompterService: TeleprompterService,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "manage_teleprompter"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_manage_teleprompter)

    override val title: String =
        textProvider.string(R.string.agent_tool_manage_teleprompter_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_manage_teleprompter_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_manage_teleprompter_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_manage_teleprompter_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_teleprompter_param_action),
                enum = listOf("list", "get", "create", "update", "delete")
            ),
            "script_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_teleprompter_param_script_id)
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_teleprompter_param_title)
            ),
            "content" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_teleprompter_param_content)
            )
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = try {
            gson.fromJson(arguments, ManageTeleprompterParams::class.java)
        } catch (e: Exception) {
            return errorResult(
                textProvider.string(
                    R.string.agent_tool_manage_teleprompter_failed,
                    e.message.orEmpty()
                )
            )
        }

        return try {
            when (params.action?.lowercase()) {
                "list" -> handleList()
                "get" -> handleGet(params)
                "create" -> handleCreate(params)
                "update" -> handleUpdate(params)
                "delete" -> handleDelete(params)
                else -> errorResult(
                    textProvider.string(R.string.agent_tool_manage_teleprompter_unknown_action)
                )
            }
        } catch (e: Exception) {
            errorResult(
                textProvider.string(
                    R.string.agent_tool_manage_teleprompter_failed,
                    e.message.orEmpty()
                )
            )
        }
    }

    // ── action handlers ─────────────────────────────────────────────────

    private suspend fun handleList(): AgentToolResult {
        val scripts = teleprompterService.observeScripts().firstOrNull().orEmpty()
        val data = scripts.map { script ->
            mapOf(
                "script_id" to script.id,
                "title" to script.title,
                "word_count" to script.wordCount,
                "updated_at" to script.updatedAt
            )
        }
        val result = mapOf(
            "action" to "list",
            "success" to true,
            "count" to scripts.size,
            "scripts" to data,
            "deepLinks" to listOf(listDeepLink())
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleGet(params: ManageTeleprompterParams): AgentToolResult {
        val scriptId = params.script_id?.takeIf { it.isNotBlank() }
            ?: return errorResult(textProvider.string(R.string.agent_tool_manage_teleprompter_missing_script_id))

        return teleprompterService.getScript(scriptId).fold(
            onSuccess = { script ->
                val result = mapOf(
                    "action" to "get",
                    "success" to true,
                    "script_id" to script.id,
                    "title" to script.title,
                    "content" to script.content,
                    "word_count" to script.wordCount,
                    "created_at" to script.createdAt,
                    "updated_at" to script.updatedAt,
                    "deepLinks" to listOf(editDeepLink(script.id, primary = true), playDeepLink(script.id, primary = false))
                )
                AgentToolResult(content = gson.toJson(result))
            },
            onFailure = {
                errorResult(textProvider.string(R.string.agent_tool_manage_teleprompter_not_found, scriptId))
            }
        )
    }

    private suspend fun handleCreate(params: ManageTeleprompterParams): AgentToolResult {
        val title = params.title?.takeIf { it.isNotBlank() }
            ?: return errorResult(textProvider.string(R.string.agent_tool_manage_teleprompter_missing_title))
        val content = params.content.orEmpty()

        return teleprompterService.saveScript(
            scriptId = null,
            title = title,
            content = content,
            source = LOG_SOURCE,
        ).fold(
            onSuccess = { saved ->
                val result = mapOf(
                    "action" to "create",
                    "success" to true,
                    "script_id" to saved.id,
                    "title" to saved.title,
                    "word_count" to saved.wordCount,
                    "message" to textProvider.string(
                        R.string.agent_tool_manage_teleprompter_created,
                        saved.title
                    ),
                    "deepLinks" to listOf(playDeepLink(saved.id, primary = true), listDeepLink())
                )
                AgentToolResult(content = gson.toJson(result))
            },
            onFailure = { e ->
                errorResult(
                    textProvider.string(
                        R.string.agent_tool_manage_teleprompter_failed,
                        e.message.orEmpty()
                    )
                )
            }
        )
    }

    private suspend fun handleUpdate(params: ManageTeleprompterParams): AgentToolResult {
        val scriptId = params.script_id?.takeIf { it.isNotBlank() }
            ?: return errorResult(textProvider.string(R.string.agent_tool_manage_teleprompter_missing_script_id))

        val existing = teleprompterService.getScript(scriptId).getOrNull()
            ?: return errorResult(textProvider.string(R.string.agent_tool_manage_teleprompter_not_found, scriptId))

        val newTitle = params.title?.takeIf { it.isNotBlank() } ?: existing.title
        val newContent = params.content ?: existing.content

        return teleprompterService.saveScript(
            scriptId = scriptId,
            title = newTitle,
            content = newContent,
            source = LOG_SOURCE,
        ).fold(
            onSuccess = { saved ->
                val result = mapOf(
                    "action" to "update",
                    "success" to true,
                    "script_id" to saved.id,
                    "title" to saved.title,
                    "word_count" to saved.wordCount,
                    "message" to textProvider.string(
                        R.string.agent_tool_manage_teleprompter_updated,
                        saved.title
                    ),
                    "deepLinks" to listOf(editDeepLink(saved.id, primary = true), playDeepLink(saved.id, primary = false))
                )
                AgentToolResult(content = gson.toJson(result))
            },
            onFailure = { e ->
                errorResult(
                    textProvider.string(
                        R.string.agent_tool_manage_teleprompter_failed,
                        e.message.orEmpty()
                    )
                )
            }
        )
    }

    private suspend fun handleDelete(params: ManageTeleprompterParams): AgentToolResult {
        val scriptId = params.script_id?.takeIf { it.isNotBlank() }
            ?: return errorResult(textProvider.string(R.string.agent_tool_manage_teleprompter_missing_script_id))

        teleprompterService.getScript(scriptId).getOrNull()
            ?: return errorResult(textProvider.string(R.string.agent_tool_manage_teleprompter_not_found, scriptId))

        return teleprompterService.deleteScript(scriptId, source = LOG_SOURCE).fold(
            onSuccess = {
                val result = mapOf(
                    "action" to "delete",
                    "success" to true,
                    "script_id" to scriptId,
                    "message" to textProvider.string(R.string.agent_tool_manage_teleprompter_deleted),
                    "deepLinks" to listOf(listDeepLink())
                )
                AgentToolResult(content = gson.toJson(result))
            },
            onFailure = { e ->
                errorResult(
                    textProvider.string(
                        R.string.agent_tool_manage_teleprompter_failed,
                        e.message.orEmpty()
                    )
                )
            }
        )
    }

    // ── deeplink helpers ────────────────────────────────────────────────

    private fun teleprompterDeepLink(
        label: String,
        guidance: String,
        primary: Boolean,
        vararg extraParams: Pair<String, String>,
    ): Map<String, Any> = mapOf(
        "uri" to AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = ROUTE_KEY,
            params = extraParams.toMap(),
        ),
        "label" to label,
        "guidance" to guidance,
        "primary" to primary,
    )

    private fun listDeepLink(): Map<String, Any> = teleprompterDeepLink(
        label = textProvider.string(R.string.agent_tool_manage_teleprompter_deeplink_label_list),
        guidance = textProvider.string(R.string.agent_tool_manage_teleprompter_deeplink_guidance_list),
        primary = false,
    )

    private fun editDeepLink(scriptId: String, primary: Boolean): Map<String, Any> = teleprompterDeepLink(
        label = textProvider.string(R.string.agent_tool_manage_teleprompter_deeplink_label_edit),
        guidance = textProvider.string(R.string.agent_tool_manage_teleprompter_deeplink_guidance_edit),
        primary = primary,
        "type" to "edit",
        "script_id" to scriptId,
    )

    private fun playDeepLink(scriptId: String, primary: Boolean): Map<String, Any> = teleprompterDeepLink(
        label = textProvider.string(R.string.agent_tool_manage_teleprompter_deeplink_label_play),
        guidance = textProvider.string(R.string.agent_tool_manage_teleprompter_deeplink_guidance_play),
        primary = primary,
        "type" to "play",
        "script_id" to scriptId,
    )

    private fun errorResult(message: String): AgentToolResult =
        AgentToolResult(content = message, isError = true)

    private data class ManageTeleprompterParams(
        val action: String? = null,
        val script_id: String? = null,
        val title: String? = null,
        val content: String? = null,
    )

    private companion object {
        const val ROUTE_KEY = "teleprompter"
        const val LOG_SOURCE = "AgentTool:manage_teleprompter"
    }
}
