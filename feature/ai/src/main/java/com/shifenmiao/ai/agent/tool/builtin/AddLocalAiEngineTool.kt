package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.base.utils.LoginUtils
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Agent 工具：添加本地 AI 引擎。
 *
 * 允许 Agent 通过工具调用的方式在本地创建自定义 AI 引擎配置。
 * 需要管理员权限（vipLevel >= 10）。
 */
class AddLocalAiEngineTool @Inject constructor(
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val textProvider: AgentToolTextProvider,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "add_local_ai_engine"

    override val description: String = textProvider.string(R.string.agent_tool_add_local_ai_engine_description)

    override val title: String = textProvider.string(R.string.agent_tool_add_local_ai_engine_title)

    override val summary: String = textProvider.string(R.string.agent_tool_add_local_ai_engine_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val parallelizable: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_name)
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_title)
            ),
            "request_url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_request_url)
            ),
            "request_path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_request_path)
            ),
            "request_protocol" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_request_protocol),
                enum = listOf("openai_compatible", "responses_compatible", "anthropic_compatible", "own_proxy")
            ),
            "auth_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_auth_type),
                enum = listOf("bearer", "api_key", "none")
            ),
            "authorization_code" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_authorization_code)
            ),
            "description" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_local_ai_engine_param_description)
            ),
        ),
        required = listOf("name", "title", "request_url", "request_path")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        if (!LoginUtils.isAdmin()) {
            return AgentToolResult(
                content = gson.toJson(
                    mapOf(
                        "toolName" to name,
                        "executed" to false,
                        "reason_code" to "admin_required",
                        "message" to textProvider.string(R.string.agent_tool_add_local_ai_engine_admin_required)
                    )
                ),
                isError = true
            )
        }

        return try {
            val params = gson.fromJson(arguments, AddLocalAiEngineParams::class.java)
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_add_local_ai_engine_invalid_params),
                    isError = true
                )

            val validationError = validateParams(params)
            if (validationError != null) {
                return AgentToolResult(content = validationError, isError = true)
            }

            val protocol = AiRequestProtocol.fromValue(params.request_protocol)
            val authType = params.auth_type?.let { AuthType.fromValue(it) }
                ?: AuthType.defaultFor(protocol)

            val draft = aiEngineCatalogManager.createLocalEngineDraft().copy(
                name = params.name!!.trim(),
                title = params.title!!.trim(),
                description = params.description?.trim().orEmpty(),
                requestUrl = params.request_url!!.trim(),
                requestPath = params.request_path!!.trim(),
                requestProtocol = protocol,
                authType = authType,
                authorizationCode = params.authorization_code?.trim().orEmpty(),
                apiCanSet = true
            )

            val success = suspendCancellableCoroutine<Boolean> { continuation ->
                aiEngineCatalogManager.saveEngineConfigOnly(draft) { result ->
                    if (continuation.isActive) {
                        continuation.resume(result) {}
                    }
                }
            }

            if (success) {
                AgentToolResult(
                    content = gson.toJson(
                        mapOf(
                            "toolName" to name,
                            "executed" to true,
                            "success" to true,
                            "engine_name" to draft.name,
                            "engine_title" to draft.title,
                            "request_protocol" to draft.requestProtocol.name,
                            "auth_type" to draft.authType.name,
                            "message" to textProvider.string(
                                R.string.agent_tool_add_local_ai_engine_success,
                                draft.title
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
                            "message" to textProvider.string(R.string.agent_tool_add_local_ai_engine_save_failed)
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
                            R.string.agent_tool_add_local_ai_engine_failed,
                            e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                        )
                    )
                ),
                isError = true
            )
        }
    }

    private fun validateParams(params: AddLocalAiEngineParams): String? {
        return when {
            params.name.isNullOrBlank() ->
                textProvider.string(R.string.agent_tool_add_local_ai_engine_missing_name)

            params.title.isNullOrBlank() ->
                textProvider.string(R.string.agent_tool_add_local_ai_engine_missing_title)

            params.request_url.isNullOrBlank() ->
                textProvider.string(R.string.agent_tool_add_local_ai_engine_missing_request_url)

            params.request_path.isNullOrBlank() ->
                textProvider.string(R.string.agent_tool_add_local_ai_engine_missing_request_path)

            else -> null
        }
    }

    private data class AddLocalAiEngineParams(
        val name: String? = null,
        val title: String? = null,
        val request_url: String? = null,
        val request_path: String? = null,
        val request_protocol: String? = null,
        val auth_type: String? = null,
        val authorization_code: String? = null,
        val description: String? = null,
    )
}
