package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Agent 工具：添加本地 AI 模型到已有引擎。
 *
 * 允许 Agent 通过工具调用的方式在本地为已有引擎添加自定义模型配置。
 * 需要管理员权限（vipLevel >= 10）。
 */
class AddLocalAiModelTool @Inject constructor(
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val textProvider: AgentToolTextProvider,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "add_local_ai_model"

    override val description: String = textProvider.string(R.string.agent_tool_add_local_ai_model_description)

    override val title: String = textProvider.string(R.string.agent_tool_add_local_ai_model_title)

    override val summary: String = textProvider.string(R.string.agent_tool_add_local_ai_model_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val parallelizable: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "engine_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_model_param_engine_name)
            ),
            "model_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_model_param_model_name)
            ),
            "model_title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_model_param_model_title)
            ),
            "description" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_model_param_description)
            ),
            "temperature" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_add_local_ai_model_param_temperature)
            ),
            "top_p" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_add_local_ai_model_param_top_p)
            ),
            "max_tokens" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_add_local_ai_model_param_max_tokens)
            ),
        ),
        required = listOf("engine_name", "model_name", "model_title")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, AddLocalAiModelParams::class.java)
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_add_local_ai_model_invalid_params),
                    isError = true
                )

            val validationError = validateParams(params)
            if (validationError != null) {
                return AgentToolResult(content = validationError, isError = true)
            }

            val engineName = params.engine_name!!.trim()
            val modelName = params.model_name!!.trim()
            val modelTitle = params.model_title!!.trim()

            val draft = aiEngineCatalogManager.createLocalModelDraft(engineName).copy(
                name = modelName,
                title = modelTitle,
                description = params.description?.trim().orEmpty(),
                temperature = params.temperature?.coerceIn(0.0, 2.0) ?: 0.95,
                topP = params.top_p?.coerceIn(0.0, 1.0) ?: 0.8,
                maxTokens = params.max_tokens?.coerceIn(256, 8192) ?: 2048,
                engineName = engineName,
                canEdit = true
            )

            val (success, savedModel) = suspendCancellableCoroutine<Pair<Boolean, AiModel?>> { continuation ->
                aiEngineCatalogManager.upsertLocalModel(draft) { result, model ->
                    if (continuation.isActive) {
                        continuation.resume(result to model) {}
                    }
                }
            }

            if (success && savedModel != null) {
                AgentToolResult(
                    content = gson.toJson(
                        mapOf(
                            "toolName" to name,
                            "executed" to true,
                            "success" to true,
                            "engine_name" to engineName,
                            "model_id" to savedModel.id,
                            "model_name" to savedModel.name,
                            "model_title" to savedModel.title,
                            "message" to textProvider.string(
                                R.string.agent_tool_add_local_ai_model_success,
                                savedModel.title
                            )
                        )
                    )
                )
            } else {
                AgentToolResult(
                    content = gson.toJson(
                        mapOf(
                            "toolName" to name,
                            "executed" to true,
                            "success" to false,
                            "reason_code" to "save_failed",
                            "message" to textProvider.string(R.string.agent_tool_add_local_ai_model_save_failed)
                        )
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = gson.toJson(
                    mapOf(
                        "toolName" to name,
                        "executed" to false,
                        "reason_code" to "exception",
                        "message" to textProvider.string(
                            R.string.agent_tool_add_local_ai_model_failed,
                            e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                        )
                    )
                ),
                isError = true
            )
        }
    }

    private fun validateParams(params: AddLocalAiModelParams): String? {
        return when {
            params.engine_name.isNullOrBlank() ->
                textProvider.string(R.string.agent_tool_add_local_ai_model_missing_engine_name)

            params.model_name.isNullOrBlank() ->
                textProvider.string(R.string.agent_tool_add_local_ai_model_missing_model_name)

            params.model_title.isNullOrBlank() ->
                textProvider.string(R.string.agent_tool_add_local_ai_model_missing_model_title)

            else -> null
        }
    }

    private data class AddLocalAiModelParams(
        val engine_name: String? = null,
        val model_name: String? = null,
        val model_title: String? = null,
        val description: String? = null,
        val temperature: Double? = null,
        val top_p: Double? = null,
        val max_tokens: Int? = null,
    )
}
