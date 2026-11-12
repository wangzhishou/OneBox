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

package com.wanbaohe.poem.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.RetryPolicy
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.poem.R
import com.wanbaohe.poem.model.Poem
import com.wanbaohe.poem.service.PoemService
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * AI Agent 工具:`search_poem` — 按关键词搜索中国古诗词。
 *
 * 搜索策略对齐 PoemSearchComponent:关键词 ≥3 字符走诗泉全文搜索;
 * 短关键词(如两字诗人名)走 char 筛选多次随机取样 + 本地 contains 过滤。
 */
class SearchPoemTool @Inject constructor(
    private val poemService: PoemService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "search_poem"

    override val description: String =
        textProvider.string(R.string.agent_tool_search_poem_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_search_poem_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_search_poem_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val retryPolicy: RetryPolicy = RetryPolicy.API_DEFAULT

    /** 短关键词路径需多次取样请求,给足超时 */
    override val executionTimeoutMs: Long = 60_000L

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_search_poem_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_search_poem_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "query" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_search_poem_param_query),
            ),
            "max_results" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_search_poem_param_max_results),
            ),
        ),
        required = listOf("query"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val query = json.optString("query").trim()
            require(query.isNotEmpty()) {
                textProvider.string(R.string.agent_tool_search_poem_query_required)
            }
            val maxResults = json.optInt("max_results")
                .takeIf { it > 0 }
                ?.coerceAtMost(MAX_RESULTS)
                ?: DEFAULT_MAX_RESULTS

            val poems = searchPoems(query).take(maxResults)

            val deeplink = poemSearchDeeplink(query)
            val deepLinks = listOf(
                toolDeepLinkJson(
                    uri = deeplink,
                    label = textProvider.string(R.string.agent_tool_poem_open_search_link),
                    primary = true,
                )
            )

            poemSuccessResult(
                markdown = buildMarkdown(query, poems, deeplink),
                deepLinks = deepLinks,
            ) {
                put("action", "search")
                put("query", query)
                put("count", poems.size)
                put("poems", JSONArray().apply { poems.forEach { put(poemSummaryJson(it)) } })
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_search_poem_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_poem_unknown_error),
                ),
                isError = true,
            )
        }
    }

    /** ≥3 字符走全文搜索;短关键词走 char 取样 + 本地 contains 过滤(对齐搜索页短词逻辑) */
    private suspend fun searchPoems(query: String): List<Poem> {
        if (query.length >= MIN_FULL_TEXT_QUERY_LENGTH) {
            return poemService.searchPoems(query).getOrThrow()
        }
        return poemService.fetchRandomPoems(
            char = query.first().toString(),
            count = SHORT_QUERY_SAMPLE_COUNT,
        ).getOrThrow().filter { poem ->
            poem.title.contains(query) ||
                poem.author.contains(query) ||
                poem.content.any { it.contains(query) }
        }
    }

    private fun buildMarkdown(query: String, poems: List<Poem>, deeplink: String): String {
        return buildString {
            appendLine("# ${sanitizeMarkdownText(title)}")
            appendLine()
            if (poems.isEmpty()) {
                appendLine(textProvider.string(R.string.agent_tool_search_poem_empty, query))
            } else {
                appendLine(textProvider.string(R.string.poem_result_count, poems.size))
                appendLine()
                poems.forEach { poem ->
                    val meta = listOf(poem.authorWithDynasty, poem.type)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                    appendLine(
                        "- **${sanitizeMarkdownText(poem.title)}** · ${sanitizeMarkdownText(meta)}" +
                            " · ${sanitizeMarkdownText(poem.excerpt())} (id: ${poem.id})"
                    )
                }
            }
            appendLine()
            appendLine(
                "- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_poem_open_search_link), deeplink)}"
            )
        }.trimEnd()
    }

    private fun poemSummaryJson(poem: Poem): JSONObject = JSONObject().apply {
        put("id", poem.id)
        put("title", poem.title)
        put("author", poem.author)
        put("dynasty", poem.dynasty)
        put("type", poem.type)
        put("excerpt", poem.excerpt())
    }

    companion object {
        /** 诗泉 search API 要求 q≥3 字符 */
        private const val MIN_FULL_TEXT_QUERY_LENGTH = 3
        private const val DEFAULT_MAX_RESULTS = 10
        private const val MAX_RESULTS = 20

        /** 短关键词取样次数(对齐搜索页默认) */
        private const val SHORT_QUERY_SAMPLE_COUNT = 12
    }
}
