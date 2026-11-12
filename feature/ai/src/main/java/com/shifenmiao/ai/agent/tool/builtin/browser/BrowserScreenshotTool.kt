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
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject

class BrowserScreenshotTool @Inject constructor(
    private val automationService: BrowserAutomationService,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "browser_screenshot"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_browser_screenshot)

    override val title: String =
        textProvider.string(R.string.agent_tool_browser_screenshot_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_browser_screenshot_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_browser_screenshot_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_browser_screenshot_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        if (!automationService.isAvailable.value) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browser_not_available),
                isError = true
            )
        }
        return try {
            val pageInfo = automationService.getPageInfo()
            val base64 = automationService.captureScreenshot()
            if (base64 == null) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_browser_screenshot_failed),
                    isError = true
                )
            }
            AgentToolResult(
                content = jsonStringOf(
                    mapOf(
                        "tool" to name,
                        "success" to true,
                        "format" to "png",
                        "encoding" to "base64",
                        "url" to (pageInfo?.url ?: ""),
                        "title" to (pageInfo?.title ?: ""),
                        "image_base64" to base64,
                        "size_bytes" to (base64.length * 3 / 4)
                    )
                )
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_browser_screenshot_failed,
                ),
                isError = true
            )
        }
    }
}
