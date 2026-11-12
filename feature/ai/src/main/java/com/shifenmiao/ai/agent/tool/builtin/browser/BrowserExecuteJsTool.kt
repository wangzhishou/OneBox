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

package com.shifenmiao.ai.agent.tool.builtin.browser

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.interfaces.browser.BrowserAutomationService
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class BrowserExecuteJsTool @Inject constructor(
    private val automationService: BrowserAutomationService,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "browser_execute_js"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_browser_execute_js)

    override val title: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_browser_execute_js_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_browser_execute_js_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val requiresConfirmation: Boolean = true

    override val parallelizable: Boolean = false

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_confirm_title)

    override val confirmationToolPresentation: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_confirm_presentation)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "script" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browser_execute_js_param_script)
            )
        ),
        required = listOf("script")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        if (!automationService.isAvailable.value) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browser_not_available),
                isError = true
            )
        }
        return try {
            val params = if (arguments.isBlank()) BrowserExecuteJsParams() else {
                AppJson.decodeFromString<BrowserExecuteJsParams>(arguments)
            }
            val script = params.script?.trim().orEmpty()
            if (script.isBlank()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_browser_execute_js_missing_script),
                    isError = true
                )
            }
            val result = automationService.executeJavaScript(script)
            AgentToolResult(
                content = jsonStringOf(
                    mapOf(
                        "tool" to name,
                        "executed" to true,
                        "result" to result
                    )
                )
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_browser_execute_js_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    @Serializable
    private data class BrowserExecuteJsParams(
        val script: String? = null
    )
}
