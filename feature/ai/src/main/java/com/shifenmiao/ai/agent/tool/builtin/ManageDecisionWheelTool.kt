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

package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.R
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.decisionwheel.WheelDto
import com.shifenmiao.model.decisionwheel.WheelServiceInterface
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ManageDecisionWheelTool @Inject constructor(
    private val wheelService: WheelServiceInterface,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "manage_decision_wheel"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_manage_decision_wheel)

    override val title: String =
        textProvider.string(R.string.agent_tool_manage_decision_wheel_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_manage_decision_wheel_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_manage_decision_wheel_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_manage_decision_wheel_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_decision_wheel_param_action),
                enum = listOf("list", "create", "update_options", "delete")
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_decision_wheel_param_title)
            ),
            "options" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_manage_decision_wheel_param_options)
            ),
            "wheel_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_decision_wheel_param_wheel_id)
            ),
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = AppJson.decodeFromString<ManageDecisionWheelParams>(arguments)
            when (params.action) {
                "list" -> handleList()
                "create" -> handleCreate(params)
                "update_options" -> handleUpdateOptions(params)
                "delete" -> handleDelete(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_manage_decision_wheel_unknown_action,
                        params.action
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_decision_wheel_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    // ── action handlers ──────────────────────────────

    private suspend fun handleList(): AgentToolResult {
        val wheels = wheelService.listWheels()
        val result = mapOf(
            "action" to "list",
            "success" to true,
            "count" to wheels.size,
            "wheels" to wheels.map { it.toMap() },
            "deepLinks" to listOf(wheelDeepLink())
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleCreate(params: ManageDecisionWheelParams): AgentToolResult {
        val title = params.title
        if (title.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_decision_wheel_missing_title)
        }
        val options = params.options
        if (options.isNullOrEmpty()) {
            return errorResult(R.string.agent_tool_manage_decision_wheel_missing_options)
        }
        val wheel = wheelService.createWheel(title, options).getOrElse {
            return errorResult(
                R.string.agent_tool_manage_decision_wheel_failed,
                it.message ?: textProvider.string(R.string.agent_tool_unknown_error)
            )
        }
        val result = mapOf(
            "action" to "create",
            "success" to true,
            "wheel" to wheel.toMap(),
            "message" to textProvider.string(
                R.string.agent_tool_manage_decision_wheel_created,
                wheel.title
            ),
            "deepLinks" to listOf(wheelDeepLink(wheel.id))
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleUpdateOptions(params: ManageDecisionWheelParams): AgentToolResult {
        val wheelId = params.wheel_id
        if (wheelId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_decision_wheel_missing_wheel_id)
        }
        val options = params.options
        if (options.isNullOrEmpty()) {
            return errorResult(R.string.agent_tool_manage_decision_wheel_missing_options)
        }
        val existing = wheelService.getWheel(wheelId)
            ?: return errorResult(R.string.agent_tool_manage_decision_wheel_not_found, wheelId)
        val wheel = wheelService.updateWheelOptions(wheelId, options).getOrElse {
            return errorResult(
                R.string.agent_tool_manage_decision_wheel_failed,
                it.message ?: textProvider.string(R.string.agent_tool_unknown_error)
            )
        }
        val result = mapOf(
            "action" to "update_options",
            "success" to true,
            "wheel" to wheel.toMap(),
            "message" to textProvider.string(
                R.string.agent_tool_manage_decision_wheel_updated,
                wheel.title
            ),
            "deepLinks" to listOf(wheelDeepLink(wheel.id))
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleDelete(params: ManageDecisionWheelParams): AgentToolResult {
        val wheelId = params.wheel_id
        if (wheelId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_decision_wheel_missing_wheel_id)
        }
        val existing = wheelService.getWheel(wheelId)
            ?: return errorResult(R.string.agent_tool_manage_decision_wheel_not_found, wheelId)
        wheelService.deleteWheel(wheelId).getOrElse {
            return errorResult(
                R.string.agent_tool_manage_decision_wheel_failed,
                it.message ?: textProvider.string(R.string.agent_tool_unknown_error)
            )
        }
        val result = mapOf(
            "action" to "delete",
            "success" to true,
            "wheel_id" to wheelId,
            "title" to existing.title,
            "message" to textProvider.string(
                R.string.agent_tool_manage_decision_wheel_deleted,
                existing.title
            ),
            "deepLinks" to listOf(wheelDeepLink())
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    // ── helpers ───────────────────────────────────

    private fun errorResult(resId: Int, vararg args: Any): AgentToolResult =
        AgentToolResult(content = textProvider.string(resId, *args), isError = true)

    /**
     * 转盘页 deeplink。带 [wheelId] 时直达该转盘的转动页,
     * 不带时进模块默认流程(当前/最近转盘)。
     */
    private fun wheelDeepLink(wheelId: String? = null): Map<String, Any?> = mapOf(
        "uri" to AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = DECISION_WHEEL_ROUTE_KEY,
            params = wheelId?.let { mapOf("wheel_id" to it) }.orEmpty(),
        ),
        "label" to textProvider.string(R.string.agent_tool_manage_decision_wheel_deeplink_label),
        "guidance" to textProvider.string(R.string.agent_tool_manage_decision_wheel_deeplink_guidance),
        "primary" to true,
    )

    private fun WheelDto.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "options" to options.map { option ->
            mapOf("id" to option.id, "name" to option.name)
        },
        "deeplink" to AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = DECISION_WHEEL_ROUTE_KEY,
            params = mapOf("wheel_id" to id),
        )
    )

    @Serializable
    private data class ManageDecisionWheelParams(
        val action: String = "",
        val title: String? = null,
        val options: List<String>? = null,
        val wheel_id: String? = null,
    )

    companion object {
        // AppNavigationRegistry 已注册 decision_wheel 带参条目(wheel_id → 转动页),
        // 旧 routeKey decision_wheel_screen 经 aliases 兼容
        private const val DECISION_WHEEL_ROUTE_KEY = "decision_wheel"
    }
}
