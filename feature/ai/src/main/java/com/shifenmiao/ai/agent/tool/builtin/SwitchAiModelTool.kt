package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Agent 工具：切换当前对话使用的 AI 引擎/模型。
 *
 * 对应手动操作：聊天页 → 模型选择。支持主对话（default）与快速任务（fast）两个工作槽位，
 * 引擎/模型未命中时返回可用清单，便于 LLM 自行纠正重试。
 */
class SwitchAiModelTool @Inject constructor(
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val aiEngineManager: AIEngineManager,
    private val textProvider: AgentToolTextProvider,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "switch_ai_model"

    override val description: String = textProvider.string(R.string.agent_tool_switch_ai_model_description)

    override val title: String = textProvider.string(R.string.agent_tool_switch_ai_model_title)

    override val summary: String = textProvider.string(R.string.agent_tool_switch_ai_model_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "engine_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_switch_ai_model_param_engine_name)
            ),
            "model_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_switch_ai_model_param_model_name)
            ),
            "slot" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_switch_ai_model_param_slot),
                enum = listOf("default", "fast")
            ),
        ),
        required = listOf("engine_name")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, SwitchAiModelParams::class.java)
            val engineName = params?.engine_name?.trim().orEmpty()
            if (engineName.isEmpty()) {
                return errorResult(
                    reasonCode = "invalid_params",
                    message = textProvider.string(R.string.agent_tool_switch_ai_model_invalid_params)
                )
            }

            val engine = aiEngineCatalogManager.getEngineByName(engineName)
            if (engine == null) {
                val availableEngines = aiEngineCatalogManager.observeAvailableEngines().first()
                    .joinToString(", ") { it.name }
                return errorResult(
                    reasonCode = "engine_not_found",
                    message = textProvider.string(
                        R.string.agent_tool_switch_ai_model_engine_not_found,
                        engineName,
                        availableEngines
                    )
                )
            }

            val models = aiEngineCatalogManager.observeModelsByProvider().first()[engine.name.lowercase()].orEmpty()
            val modelName = params.model_name?.trim().orEmpty()
            val model = if (modelName.isEmpty()) {
                engine.model
            } else {
                models.firstOrNull {
                    it.name.equals(modelName, ignoreCase = true) ||
                        it.title.equals(modelName, ignoreCase = true)
                }
            }
            if (model == null) {
                val availableModels = models.joinToString(", ") { it.name }
                return errorResult(
                    reasonCode = "model_not_found",
                    message = textProvider.string(
                        R.string.agent_tool_switch_ai_model_model_not_found,
                        engine.name,
                        modelName,
                        availableModels
                    )
                )
            }

            val slot = params.slot?.trim()?.lowercase().orEmpty()
            when (slot) {
                "fast" -> aiEngineManager.switchFastModel(engine, model)
                else -> aiEngineManager.switchModel(engine, model)
            }

            AgentToolResult(
                content = gson.toJson(
                    mapOf(
                        "toolName" to name,
                        "executed" to true,
                        "success" to true,
                        "engine_name" to engine.name,
                        "model_name" to model.name,
                        "slot" to slot.ifEmpty { "default" },
                        "message" to textProvider.string(
                            R.string.agent_tool_switch_ai_model_success,
                            engine.title.ifBlank { engine.name },
                            model.title.ifBlank { model.name }
                        )
                    )
                )
            )
        } catch (e: Exception) {
            errorResult(
                reasonCode = "exception",
                message = textProvider.string(
                    R.string.agent_tool_switch_ai_model_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                )
            )
        }
    }

    private fun errorResult(reasonCode: String, message: String): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "toolName" to name,
                    "executed" to false,
                    "success" to false,
                    "reason_code" to reasonCode,
                    "message" to message
                )
            ),
            isError = true
        )
    }

    private data class SwitchAiModelParams(
        val engine_name: String? = null,
        val model_name: String? = null,
        val slot: String? = null,
    )
}
