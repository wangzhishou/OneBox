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

package com.wanbaohe.blog.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.RetryPolicy
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.network.api.ApiService
import com.wanbaohe.blog.R
import com.wanbaohe.blog.repository.FeedbackRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * AI Agent 工具:`submit_feedback` — 向开发团队提交用户反馈(建议/问题/功能请求)。
 *
 * 登录态说明:反馈提交接口(POST api/blogs/feedback)走 AnyAuth 鉴权,
 * 游客 token 即可调用,服务端对未登录请求有默认用户兜底,
 * 因此本工具不声明 requiresLogin;也正因如此,下面声明的
 * [RetryPolicy.API_DEFAULT] 重试策略会真实生效(框架只对
 * requiresLogin / 需确认 / 交互式 / 带权限的工具强制跳过重试)。
 *
 * 数据层(FeedbackRequest)没有独立的联系方式与分类字段:
 * contact 由工具拼接进正文末尾;分类(标签)不暴露给 LLM,按反馈页默认行为提交。
 */
class SubmitFeedbackTool @Inject constructor(
    apiService: ApiService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    private val repository = FeedbackRepository(apiService)

    override val name: String = "submit_feedback"

    override val description: String =
        textProvider.string(R.string.agent_tool_submit_feedback_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_submit_feedback_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_submit_feedback_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    // 网络写操作,串行执行避免同轮重复提交
    override val parallelizable: Boolean = false

    override val retryPolicy: RetryPolicy = RetryPolicy.API_DEFAULT

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_submit_feedback_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_submit_feedback_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "content" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_submit_feedback_param_content),
            ),
            "contact" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_submit_feedback_param_contact),
            ),
        ),
        required = listOf("content"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val content = json.optString("content").trim()
            require(content.isNotEmpty()) {
                textProvider.string(R.string.agent_tool_submit_feedback_error_empty_content)
            }
            require(content.length <= MAX_CONTENT_LENGTH) {
                textProvider.string(
                    R.string.agent_tool_submit_feedback_error_too_long,
                    MAX_CONTENT_LENGTH,
                )
            }
            val contact = json.optString("contact").trim().takeIf { it.isNotEmpty() }
            submitFeedback(content, contact)
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_submit_feedback_failed,
                    error.message
                        ?: textProvider.string(R.string.agent_tool_submit_feedback_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private suspend fun submitFeedback(content: String, contact: String?): AgentToolResult {
        // 数据层无独立联系方式字段,有 contact 时拼接进正文末尾
        val fullContent = contact?.let {
            content + "\n\n" +
                textProvider.string(R.string.agent_tool_submit_feedback_contact_label) + ": " + it
        } ?: content

        if (!repository.submitFeedback(fullContent)) {
            return AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_submit_feedback_failed,
                    textProvider.string(R.string.agent_tool_submit_feedback_unknown_error),
                ),
                isError = true,
            )
        }
        return successResult()
    }

    /** 成功结果:markdown(供 LLM)+ deepLinks(供 UI 渲染跳转卡片,跳反馈列表页) */
    private fun successResult(): AgentToolResult {
        val deeplink = AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = FEEDBACK_ROUTE_KEY,
        )
        val linkLabel = textProvider.string(R.string.agent_tool_submit_feedback_open_link)
        val markdown = buildString {
            appendLine("# $title")
            appendLine()
            appendLine(textProvider.string(R.string.agent_tool_submit_feedback_success))
            appendLine()
            appendLine("- [$linkLabel]($deeplink)")
        }.trimEnd()

        val json = JSONObject().apply {
            put("success", true)
            put("markdown", markdown)
            put(
                "deepLinks",
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put("uri", deeplink)
                            put("label", linkLabel)
                            put("primary", true)
                        }
                    )
                }
            )
        }
        return AgentToolResult(content = json.toString())
    }

    private companion object {
        /** 反馈内容长度上限 */
        const val MAX_CONTENT_LENGTH = 2000

        /** 反馈列表页路由 key,与 AppNavigationRegistry 中 Screen.Feedback 注册的别名一致 */
        const val FEEDBACK_ROUTE_KEY = "feedback"
    }
}
