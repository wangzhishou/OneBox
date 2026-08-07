package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.callback.ToolCallback
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.AppNavigationCatalogRepository
import com.shifenmiao.ai.agent.tool.InteractiveAgentTool
import com.shifenmiao.ai.agent.tool.ScreenNavigationToolSupport
import com.shifenmiao.common.handle.ItemScreenAction
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult
import javax.inject.Inject

class NavigateAppScreenTool @Inject constructor(
    private val appNavigationCatalogRepository: AppNavigationCatalogRepository,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : InteractiveAgentTool {

    override val name: String = "navigate_app_screen"
    override val description: String = textProvider.string(R.string.agent_tool_navigate_app_screen_description)
    override val title: String = textProvider.string(R.string.agent_tool_navigate_app_screen_title)
    override val summary: String = textProvider.string(R.string.agent_tool_navigate_app_screen_summary)
    override val category: ToolCategory = ToolCategory.BUSINESS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE
    override val sortOrder: Int = -81
    override val dependencies: List<String> = listOf("discover_apps")

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "deeplink" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_navigate_deeplink_param)
            ),
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_navigate_action_param),
                enum = listOf("open", "edit")
            ),
            "wait_for_result" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_navigate_wait_for_result_param)
            )
        ),
        required = listOf("deeplink")
    )

    override fun shouldRequireConfirmation(arguments: String): Boolean {
        val params = parseParams(arguments) ?: return false
        return resolveAction(params.action) == ItemScreenAction.EDIT
    }

    override suspend fun execute(arguments: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(R.string.agent_tool_callback_required),
            isError = true
        )
    }

    override suspend fun execute(arguments: String, callback: ToolCallback): AgentToolResult {
        return runCatching {
            val params = parseParams(arguments) ?: NavigateAppScreenParams()
            val deeplink = params.deeplink?.trim().orEmpty()
            if (deeplink.isBlank()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_navigate_deeplink_required),
                    isError = true
                )
            }
            val action = resolveAction(params.action)
                ?: return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_navigate_invalid_action,
                        params.action?.trim().orEmpty()
                    ),
                    isError = true
                )
            val target = appNavigationCatalogRepository.resolveTarget(
                action = action,
                deeplink = deeplink
            ) ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_navigate_target_not_found),
                isError = true
            )

            val defaultAwaitResult = true
            val shouldAwait = (params.waitForResult ?: defaultAwaitResult) && target.supportsResultCallback

            val execution = target.callbackScreenBuilder?.takeIf { shouldAwait }?.let { builder ->
                val result = callback.navigateToScreen<ScreenCallbackResult> { onResult ->
                    builder(onResult)
                }
                com.shifenmiao.ai.agent.tool.ScreenNavigationExecution(
                    mode = "await_result",
                    callbackResult = result
                )
            } ?: ScreenNavigationToolSupport.navigate(
                callback = callback,
                screen = target.screen,
                awaitResult = shouldAwait
            )

            AgentToolResult(
                content = gson.toJson(
                    NavigateAppScreenResponse(
                        deeplink = target.deeplink.takeIf { it.isNotBlank() },
                        deeplinkMarkdownLink = target.deeplink
                            .takeIf { it.isNotBlank() }
                            ?.let { link -> buildMarkdownLink(buildReopenLinkLabel(target.title, action), link) },
                        deeplinkHtmlLink = target.deeplink
                            .takeIf { it.isNotBlank() }
                            ?.let { link -> buildHtmlLink(buildReopenLinkLabel(target.title, action), link) },
                        itemId = target.itemId,
                        itemTitle = target.title,
                        targetType = target.targetType.name,
                        routeKey = target.routeKey,
                        canonicalName = target.canonicalName,
                        action = action.name.lowercase(),
                        screen = target.screen.simpleName,
                        navigationMode = execution.mode,
                        callbackResult = execution.callbackResult
                    )
                )
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_navigate_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private fun parseParams(arguments: String): NavigateAppScreenParams? {
        return runCatching {
            if (arguments.isBlank()) NavigateAppScreenParams()
            else gson.fromJson(arguments, NavigateAppScreenParams::class.java)
        }.getOrNull()
    }

    private fun resolveAction(rawAction: String?): ItemScreenAction? {
        return when (rawAction?.trim()?.lowercase().orEmpty()) {
            "", "open" -> ItemScreenAction.OPEN
            "edit" -> ItemScreenAction.EDIT
            else -> null
        }
    }

    private fun buildReopenLinkLabel(title: String, action: ItemScreenAction): String {
        val normalizedTitle = title.trim().ifBlank {
            if (action == ItemScreenAction.EDIT) {
                textProvider.string(R.string.agent_tool_navigate_default_edit_link_label)
            } else {
                textProvider.string(R.string.agent_tool_navigate_default_open_link_label)
            }
        }
        val templateRes = if (action == ItemScreenAction.EDIT) {
            R.string.agent_tool_navigate_edit_link_label
        } else {
            R.string.agent_tool_navigate_open_link_label
        }
        return textProvider.string(templateRes, sanitizeMarkdownLabel(normalizedTitle))
    }

    private fun buildMarkdownLink(label: String, deeplink: String): String {
        return "[$label]($deeplink)"
    }

    private fun buildHtmlLink(label: String, deeplink: String): String {
        return "<a href=\"${escapeHtml(deeplink)}\">${escapeHtml(label)}</a>"
    }

    private fun sanitizeMarkdownLabel(value: String): String {
        return value
            .replace("[", "［")
            .replace("]", "］")
    }

    private fun escapeHtml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private data class NavigateAppScreenParams(
        @SerializedName("deeplink")
        val deeplink: String? = null,
        val action: String? = null,
        val wait_for_result: Boolean? = null
    ) {
        val waitForResult: Boolean? get() = wait_for_result
    }

    private data class NavigateAppScreenResponse(
        val deeplink: String?,
        val deeplinkMarkdownLink: String?,
        val deeplinkHtmlLink: String?,
        val itemId: Int?,
        val itemTitle: String,
        val targetType: String,
        val routeKey: String?,
        val canonicalName: String?,
        val action: String,
        val screen: String,
        val navigationMode: String,
        val callbackResult: Any?
    )
}

