package com.wanbaohe.householditems.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.service.HouseholdService
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`add_household_item` — 记录一件家庭物品。
 *
 * 与 UI 共用 [HouseholdService],写入自动落活动日志(actor=AGENT)。
 * 位置路径(「/」或「>」分隔)不存在时自动逐级创建。
 */
class AddHouseholdItemTool @Inject constructor(
    private val service: HouseholdService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "add_household_item"

    override val description: String =
        textProvider.string(R.string.agent_tool_add_household_item_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_add_household_item_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_add_household_item_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_add_household_item_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_add_household_item_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_household_item_param_name),
            ),
            "location_path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_household_item_param_location_path),
            ),
            "category" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_household_item_param_category),
            ),
            "expire_date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_household_item_param_expire_date),
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_household_item_param_note),
            ),
        ),
        required = listOf("name"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val itemName = json.optString("name").trim()
            require(itemName.isNotEmpty()) { "name 不能为空" }

            val locationPath = parseLocationPath(json.optString("location_path"))
            val expireAt = json.optString("expire_date")
                .takeIf { it.isNotBlank() }
                ?.let { parseExpireDate(it) }

            val itemId = service.addItem(
                input = HouseholdService.ItemInput(
                    name = itemName,
                    category = json.optString("category").takeIf { it.isNotBlank() },
                    locationPath = locationPath,
                    expireAt = expireAt,
                    note = json.optString("note").takeIf { it.isNotBlank() },
                ),
                actor = HouseholdService.ACTOR_AGENT,
                source = HouseholdService.SOURCE_AGENT_ADD,
            ).getOrThrow()

            val deeplink = householdItemsDeeplink()
            val editDeeplink = householdItemEditDeeplink(itemId)
            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine("- **${textProvider.string(R.string.agent_tool_household_label_name)}**: ${sanitizeMarkdownText(itemName)}")
                    if (!locationPath.isNullOrEmpty()) {
                        appendLine(
                            "- **${textProvider.string(R.string.agent_tool_household_label_location)}**: " +
                                sanitizeMarkdownText(locationPath.joinToString(" / "))
                        )
                    }
                    json.optString("category").takeIf { it.isNotBlank() }?.let {
                        appendLine("- **${textProvider.string(R.string.agent_tool_household_label_category)}**: ${sanitizeMarkdownText(it)}")
                    }
                    json.optString("expire_date").takeIf { it.isNotBlank() }?.let {
                        appendLine("- **${textProvider.string(R.string.agent_tool_household_label_expiry)}**: ${sanitizeMarkdownText(it)}")
                    }
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_household_open_link), deeplink)}")
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_household_edit_link), editDeeplink)}")
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_household_item_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_household_unknown_error),
                ),
                isError = true,
            )
        }
    }

    companion object {
        /** "主卧/衣柜/上层抽屉" 或 "主卧>衣柜" → 分段列表;空串返回 null */
        internal fun parseLocationPath(raw: String): List<String>? {
            return raw.split('/', '>', '\\')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .takeIf { it.isNotEmpty() }
        }

        /** yyyy-MM-dd → 当天 0 点时间戳 */
        internal fun parseExpireDate(raw: String): Long {
            val date = runCatching { LocalDate.parse(raw.trim()) }
                .getOrElse { error("expire_date 格式错误,需 yyyy-MM-dd") }
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        internal fun householdItemsDeeplink(): String {
            return AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.HouseholdItems().routeKey,
            )
        }

        /** 直达某物品的编辑页(params 由 AppNavigationRegistry 的 screenBuilder 解析) */
        internal fun householdItemEditDeeplink(itemId: String): String {
            return AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.HouseholdItems().routeKey,
                params = mapOf("type" to "edit_item", "item_id" to itemId),
            )
        }
    }
}
