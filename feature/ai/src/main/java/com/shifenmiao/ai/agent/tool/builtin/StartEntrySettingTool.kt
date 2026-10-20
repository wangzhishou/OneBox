package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ToolDeepLink
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.base.utils.Navigation
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.storage.AppSharedStorage
import com.wanbaohe.core.ui.utils.navigation.Screen
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * 启动入口设置工具:查看/设置 App 冷启动后直达的页面。
 *
 * 按 Screen.id 持久化(AppSharedStorage.startEntryScreenId),
 * RootComponent.startEntryStack 只在建栈时读取,因此仅下次冷启动生效。
 * AppSharedStorage 没有清除方法,恢复默认即写入默认首页 Screen.NewApp().id
 * (与 Navigation.resolveStartEntry 的未设置回退一致)。
 */
class StartEntrySettingTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "start_entry_setting"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_start_entry_setting)

    override val title: String = textProvider.string(R.string.agent_tool_start_entry_setting_title)

    override val summary: String = textProvider.string(R.string.agent_tool_start_entry_setting_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_start_entry_setting_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_start_entry_setting_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val maxResultLength: Int = 8192

    override val sortOrder: Int = -75

    override val deepLinks: List<ToolDeepLink> = listOf(
        ToolDeepLink(
            uri = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "display_settings",
            ),
            label = textProvider.string(R.string.agent_tool_start_entry_setting_deeplink_label),
            guidance = textProvider.string(R.string.agent_tool_start_entry_setting_deeplink_guidance),
            primary = true,
        )
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_start_entry_setting_param_action),
                enum = listOf("list", "get", "set"),
            ),
            "screen_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_start_entry_setting_param_screen_id),
            ),
        ),
        required = listOf("action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            when (params.action?.lowercase()) {
                "list" -> handleList()
                "get" -> handleGet()
                "set", "update" -> handleSet(params)
                null, "" -> errorResult(
                    action = "unknown",
                    reasonCode = "missing_action",
                    message = textProvider.string(R.string.agent_tool_start_entry_setting_missing_action),
                )
                else -> errorResult(
                    action = params.action,
                    reasonCode = "unknown_action",
                    message = textProvider.string(
                        R.string.agent_tool_start_entry_setting_unknown_action,
                        params.action,
                    ),
                )
            }
        }.getOrElse { error ->
            errorResult(
                action = "unknown",
                reasonCode = "exception",
                message = textProvider.string(
                    R.string.agent_tool_start_entry_setting_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
            )
        }
    }

    private fun handleList(): AgentToolResult {
        val currentId = AppSharedStorage.loadStartEntryScreenId()
        return successResult(
            action = "list",
            message = textProvider.string(R.string.agent_tool_start_entry_setting_list_message),
            extra = mapOf(
                "currentScreenId" to (currentId ?: defaultHomeScreen().id),
                "isDefault" to (currentId == null),
                "candidates" to Navigation.startEntryCandidates().map(::screenSummary),
            ),
        )
    }

    private fun handleGet(): AgentToolResult {
        val currentId = AppSharedStorage.loadStartEntryScreenId()
        val current = Navigation.startEntryCandidates().firstOrNull { it.id == currentId }
        return successResult(
            action = "get",
            message = textProvider.string(R.string.agent_tool_start_entry_setting_get_message),
            extra = mapOf(
                "current" to screenSummary(current ?: defaultHomeScreen()),
                "isDefault" to (currentId == null),
            ),
        )
    }

    private fun handleSet(params: StartEntrySettingParams): AgentToolResult {
        val raw = params.screen_id?.trim()
        if (raw.isNullOrEmpty()) {
            return errorResult(
                action = "set",
                reasonCode = "missing_screen_id",
                message = textProvider.string(R.string.agent_tool_start_entry_setting_missing_screen_id),
                validOptions = mapOf("keywords" to DEFAULT_KEYWORDS),
            )
        }
        val target = if (raw.lowercase() in DEFAULT_KEYWORDS) {
            defaultHomeScreen()
        } else {
            val screenId = raw.toIntOrNull() ?: return errorResult(
                action = "set",
                reasonCode = "invalid_screen_id",
                message = textProvider.string(R.string.agent_tool_start_entry_setting_invalid_screen_id, raw),
                validOptions = mapOf("keywords" to DEFAULT_KEYWORDS),
            )
            Navigation.startEntryCandidates().firstOrNull { it.id == screenId } ?: return errorResult(
                action = "set",
                reasonCode = "unknown_screen_id",
                message = textProvider.string(R.string.agent_tool_start_entry_setting_unknown_screen_id, raw),
            )
        }
        AppSharedStorage.saveStartEntryScreenId(target.id)
        return successResult(
            action = "set",
            message = textProvider.string(
                R.string.agent_tool_start_entry_setting_set_message,
                textProvider.string(target.title),
            ),
            extra = mapOf(
                "current" to screenSummary(target),
                "isDefault" to (target.id == defaultHomeScreen().id),
            ),
        )
    }

    private fun defaultHomeScreen(): Screen = Screen.NewApp()

    private fun screenSummary(screen: Screen): Map<String, Any?> {
        return mapOf(
            "id" to screen.id,
            "title" to textProvider.string(screen.title),
        )
    }

    private fun parseArguments(arguments: String): StartEntrySettingParams {
        if (arguments.isBlank()) return StartEntrySettingParams()
        return runCatching { AppJson.decodeFromString<StartEntrySettingParams>(arguments) }.getOrNull() ?: StartEntrySettingParams()
    }

    private fun successResult(
        action: String,
        message: String,
        extra: Map<String, Any?> = emptyMap(),
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "ok",
            "action" to action,
            "message" to message,
            "effectiveHint" to textProvider.string(R.string.agent_tool_start_entry_setting_effective_hint),
        )
        payload.putAll(extra)
        return AgentToolResult(content = jsonStringOf(payload))
    }

    private fun errorResult(
        action: String,
        reasonCode: String,
        message: String,
        validOptions: Map<String, Any?>? = null,
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "error",
            "action" to action,
            "reasonCode" to reasonCode,
            "message" to message,
        )
        if (validOptions != null) payload["validOptions"] = validOptions
        return AgentToolResult(content = jsonStringOf(payload), isError = true)
    }

    @Serializable
    private data class StartEntrySettingParams(
        val action: String? = null,
        val screen_id: String? = null,
    )

    private companion object {
        val DEFAULT_KEYWORDS = listOf("default", "home", "首页", "默认", "主页")
    }
}
