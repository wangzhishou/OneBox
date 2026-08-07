package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.service.BookkeepingService
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

/**
 * 内置工具:`add_bookkeeping_record` — 向记账本写入一条账目。
 *
 * 与 UI 共用 [BookkeepingService],写入会自动落 ActivityLog (actorType=AGENT)。
 */
class AddBookkeepingRecordTool @Inject constructor(
    private val service: BookkeepingService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "add_bookkeeping_record"

    override val description: String =
        textProvider.string(R.string.agent_tool_add_bookkeeping_record_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_add_bookkeeping_record_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_add_bookkeeping_record_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_add_bookkeeping_record_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_add_bookkeeping_record_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "records" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_add_bookkeeping_record_param_records),
            ),
            "amount" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_add_bookkeeping_record_param_amount),
            ),
            "type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_bookkeeping_record_param_type),
                enum = listOf("expense", "income", "excluded"),
            ),
            "category" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_bookkeeping_record_param_category),
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_bookkeeping_record_param_note),
            ),
            "date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_bookkeeping_record_param_date),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val records = json.optJSONArray("records")
            if (records != null) {
                executeBatch(records)
            } else {
                executeSingle(json)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_bookkeeping_record_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true,
            )
        }
    }

    private suspend fun executeSingle(json: JSONObject): AgentToolResult {
        val request = parseRecordPayload(json = json, index = 0)
        val recordId = service.addRecord(
            input = request.input,
            actor = BookkeepingService.ACTOR_AGENT,
            source = BookkeepingService.SOURCE_AGENT_ADD,
        ).getOrThrow()
        val bookkeepingDeeplink = bookkeepingDeeplink()
        val editDeeplink = bookkeepingAddRecordDeeplink(recordId)

        return AgentToolResult(
            content = buildString {
                appendLine("# ${sanitizeMarkdownText(title)}")
                appendLine()
                appendLine("- ${request.type.displayName(textProvider)} · ¥${request.amountText()} · ${sanitizeMarkdownText(request.categoryName)}")
                appendMarkdownBullet(
                    label = textProvider.string(R.string.agent_tool_bookkeeping_label_date),
                    value = request.date.toString(),
                )
                request.note?.let {
                    appendMarkdownBullet(
                        label = textProvider.string(R.string.agent_tool_bookkeeping_label_note),
                        value = it,
                    )
                }
                appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_quick_actions)) {
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_add_bookkeeping_record_open_link_label), bookkeepingDeeplink)}")
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_add_bookkeeping_record_edit_link_label), editDeeplink)}")
                }
            }.trimEnd(),
        )
    }

    private suspend fun executeBatch(records: JSONArray): AgentToolResult {
        require(records.length() > 0) { "records 不能为空" }

        val parsedRequests = mutableListOf<ResolvedRecordRequest>()
        val failures = mutableListOf<IndexedFailure>()

        for (index in 0 until records.length()) {
            runCatching {
                parseRecordPayload(json = records.getJSONObject(index), index = index)
            }.onSuccess { request ->
                parsedRequests += request
            }.onFailure { error ->
                failures += IndexedFailure(index = index, reason = error.message ?: "unknown_error")
            }
        }

        if (parsedRequests.isNotEmpty()) {
            val batchResult = service.addRecordsBatch(
                inputs = parsedRequests.map { it.input },
                actor = BookkeepingService.ACTOR_AGENT,
                source = BookkeepingService.SOURCE_AGENT_ADD_BATCH,
            )

            batchResult.failures.forEach { failure ->
                failures += IndexedFailure(
                    index = parsedRequests[failure.index].index,
                    reason = failure.reason,
                )
            }

            return AgentToolResult(
                content = buildBatchContent(
                    originalRequests = parsedRequests,
                    successes = batchResult.successes,
                    failures = failures.sortedBy { it.index },
                ),
                isError = batchResult.successes.isEmpty(),
            )
        }

        return AgentToolResult(
            content = buildBatchContent(
                originalRequests = emptyList(),
                successes = emptyList(),
                failures = failures.sortedBy { it.index },
            ),
            isError = true,
        )
    }

    private suspend fun parseRecordPayload(
        json: JSONObject,
        index: Int,
    ): ResolvedRecordRequest {
        val amountRaw = when {
            json.has("amount") -> json.get("amount").toString()
            else -> error("缺少 amount 参数")
        }
        val amountCents = service.parseAmountToCents(amountRaw)
            ?: error("amount 解析失败: $amountRaw")
        require(amountCents > 0L) { "amount 必须大于 0" }

        val typeStr = json.optString("type", "expense").lowercase()
        val type = when (typeStr) {
            "expense" -> BookkeepingRecordType.EXPENSE
            "income" -> BookkeepingRecordType.INCOME
            "excluded" -> BookkeepingRecordType.EXCLUDED
            else -> error("未知 type=$typeStr,允许 expense/income/excluded")
        }

        val categoryName = json.optString("category").trim()
        require(categoryName.isNotEmpty()) { "category 不能为空" }
        val category = service.findCategoryByName(categoryName, type)
            ?: run {
                val available = service.listCategoriesByType(type).joinToString(", ") { it.name }
                error("分类「$categoryName」未找到。当前可用 ${type.displayName(textProvider)} 分类:[$available]")
            }

        val note = json.optString("note").takeIf { it.isNotBlank() }
        val today = LocalDate.now()
        val date = json.optString("date").takeIf { it.isNotBlank() }?.let { dateStr ->
            runCatching { LocalDate.parse(dateStr) }.getOrElse { error("date 格式错误,需 yyyy-MM-dd") }
        } ?: today
        require(!date.isAfter(today)) { "date 不能晚于今天" }

        return ResolvedRecordRequest(
            index = index,
            input = BookkeepingService.RecordInput(
                categoryId = category.id,
                type = type,
                amountCents = amountCents,
                note = note,
                happenedDate = date,
            ),
            type = type,
            categoryName = category.name,
            note = note,
            date = date,
            amountCents = amountCents,
        )
    }

    private fun buildBatchContent(
        originalRequests: List<ResolvedRecordRequest>,
        successes: List<BookkeepingService.BatchAddSuccess>,
        failures: List<IndexedFailure>,
    ): String {
        val bookkeepingDeeplink = bookkeepingDeeplink()
        return buildString {
            appendLine("# ${sanitizeMarkdownText(title)}")
            appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_summary)) {
                appendMarkdownBullet(
                    label = textProvider.string(R.string.agent_tool_bookkeeping_label_success_count),
                    value = successes.size.toString(),
                )
                appendMarkdownBullet(
                    label = textProvider.string(R.string.agent_tool_bookkeeping_label_failure_count),
                    value = failures.size.toString(),
                )
            }
            if (successes.isNotEmpty()) {
                appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_success_records)) {
                    successes.forEachIndexed { order, success ->
                        val request = originalRequests[success.index]
                        val editDeeplink = bookkeepingAddRecordDeeplink(success.recordId)
                        appendLine("${order + 1}. ${request.date} · ${request.type.displayName(textProvider)} · ¥${request.amountText()} · ${sanitizeMarkdownText(request.categoryName)}")
                        request.note?.let {
                            appendLine("   - ${sanitizeMarkdownText(textProvider.string(R.string.agent_tool_bookkeeping_label_note))}：${sanitizeMarkdownText(it)}")
                        }
                        appendLine("   - ${buildMarkdownLink(textProvider.string(R.string.agent_tool_add_bookkeeping_record_edit_link_label), editDeeplink)}")
                    }
                }
            }
            if (failures.isNotEmpty()) {
                appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_failure_records)) {
                    failures.forEachIndexed { order, failure ->
                        appendLine("${order + 1}. #${failure.index + 1} ${sanitizeMarkdownText(failure.reason)}")
                    }
                }
            }
            appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_quick_actions)) {
                appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_add_bookkeeping_record_open_link_label), bookkeepingDeeplink)}")
            }
        }.trimEnd()
    }

    private data class ResolvedRecordRequest(
        val index: Int,
        val input: BookkeepingService.RecordInput,
        val type: BookkeepingRecordType,
        val categoryName: String,
        val note: String?,
        val date: LocalDate,
        val amountCents: Long,
    )

    private data class IndexedFailure(
        val index: Int,
        val reason: String,
    )

    private fun bookkeepingDeeplink(): String {
        return AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = Screen.Bookkeeping().routeKey,
        )
    }

    private fun bookkeepingAddRecordDeeplink(recordId: String? = null): String {
        val params = buildMap<String, String> {
            put("type", "add_record")
            recordId?.takeIf { it.isNotBlank() }?.let { put("editing_record_id", it) }
        }
        return AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = Screen.Bookkeeping().routeKey,
            params = params,
        )
    }

    private fun ResolvedRecordRequest.amountText(): String = "%.2f".format(amountCents / 100.0)
}
