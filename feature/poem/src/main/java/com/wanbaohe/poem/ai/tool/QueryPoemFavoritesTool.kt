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
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.poem.R
import com.wanbaohe.poem.model.Poem
import com.wanbaohe.poem.service.PoemService
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * AI Agent 工具:`query_poem_favorites` — 查询本地收藏的诗词列表。
 *
 * 纯本地查询,无网络请求。收藏的诗词均在本地库中,每条结果都附带详情页 deeplink。
 */
class QueryPoemFavoritesTool @Inject constructor(
    private val poemService: PoemService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "query_poem_favorites"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_poem_favorites_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_query_poem_favorites_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_poem_favorites_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_query_poem_favorites_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_query_poem_favorites_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "max_results" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_query_poem_favorites_param_max_results),
            ),
        ),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val maxResults = json.optInt("max_results")
                .takeIf { it > 0 }
                ?.coerceAtMost(MAX_RESULTS)
                ?: DEFAULT_MAX_RESULTS

            val favorites = poemService.observeFavorites().first().take(maxResults)

            val deepLinks = listOf(
                toolDeepLinkJson(
                    uri = poemHomeDeeplink(),
                    label = textProvider.string(R.string.agent_tool_poem_open_home_link),
                    primary = true,
                )
            )

            poemSuccessResult(
                markdown = buildMarkdown(favorites),
                deepLinks = deepLinks,
            ) {
                put("action", "query_favorites")
                put("count", favorites.size)
                put("poems", JSONArray().apply { favorites.forEach { put(poemSummaryJson(it)) } })
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_poem_favorites_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_poem_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun buildMarkdown(favorites: List<Poem>): String {
        return buildString {
            appendLine("# ${sanitizeMarkdownText(title)}")
            appendLine()
            if (favorites.isEmpty()) {
                appendLine(textProvider.string(R.string.agent_tool_query_poem_favorites_empty))
            } else {
                appendLine(textProvider.string(R.string.poem_result_count, favorites.size))
                appendLine()
                favorites.forEach { poem ->
                    val link = buildMarkdownLink(
                        sanitizeMarkdownText(poem.title),
                        poemDetailDeeplink(poem.id),
                    )
                    appendLine(
                        "- **$link** · ${sanitizeMarkdownText(poem.authorWithDynasty)}" +
                            " · ${sanitizeMarkdownText(poem.excerpt())}"
                    )
                }
            }
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
        private const val DEFAULT_MAX_RESULTS = 20
        private const val MAX_RESULTS = 50
    }
}
