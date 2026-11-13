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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.color_tools.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ToolDeepLink
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.model.ai.tool.ToolCategory
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.color_tools.R

/**
 * 调色板这一组 AI 工具的共同壳：分类、关键词、示例、跳转都一致。
 *
 * 具体工具只需要声明 name / description / title / schema / execute，
 * 保证四个工具的目录元数据不会各写一套。
 */
abstract class BasePaletteTool(
    protected val textProvider: AgentToolTextProvider
) : AgentTool {

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String>
        get() = textProvider.array(R.array.agent_tool_palette_keywords)

    override val examples: List<String>
        get() = textProvider.array(R.array.agent_tool_palette_examples)

    override val deepLinks: List<ToolDeepLink>
        get() = listOf(
            ToolDeepLink(
                uri = AppNavigationRegistry.buildStructuredDeeplink(
                    targetType = AppNavigationTargetType.SCREEN,
                    routeKey = Screen.ColorTools().routeKey,
                    params = mapOf("tab" to "palette")
                ),
                label = textProvider.string(R.string.agent_tool_palette_deeplink_label),
                guidance = textProvider.string(R.string.agent_tool_palette_deeplink_guidance),
                primary = true
            )
        )

    protected fun successResult(
        action: String,
        message: String,
        extra: Map<String, Any?> = emptyMap()
    ): AgentToolResult = AgentToolResult(
        content = jsonStringOf(
            linkedMapOf<String, Any?>(
                "status" to "ok",
                "action" to action,
                "message" to message
            ).apply { putAll(extra) }
        )
    )

    protected fun errorResult(
        action: String,
        reasonCode: String,
        message: String,
        validOptions: Map<String, Any?>? = null
    ): AgentToolResult = AgentToolResult(
        content = jsonStringOf(
            linkedMapOf<String, Any?>(
                "status" to "error",
                "action" to action,
                "reasonCode" to reasonCode,
                "message" to message
            ).apply {
                if (validOptions != null) put("validOptions", validOptions)
            }
        ),
        isError = true
    )

    protected fun failureResult(
        action: String,
        error: Throwable
    ): AgentToolResult = errorResult(
        action = action,
        reasonCode = "exception",
        message = textProvider.string(
            R.string.agent_tool_palette_failed,
            error.message ?: error::class.java.simpleName
        )
    )
}
