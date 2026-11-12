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
 * AI Agent 工具:`get_poem_detail` — 获取一首诗词的全文。
 *
 * 解析顺序:poem_id 先查本地库;查不到(或未传 id)且给了 title 时走诗泉搜索,
 * 取标题精确匹配项(可用 author 消歧)。线上解析到的诗词会落库(对齐搜索页
 * openPoem 行为),保证详情页 deeplink 可打开;本地已有同 id 记录时优先用本地
 * (可能带 AI 翻译/解读/收藏状态)。
 */
class GetPoemDetailTool @Inject constructor(
    private val poemService: PoemService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "get_poem_detail"

    override val description: String =
        textProvider.string(R.string.agent_tool_get_poem_detail_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_get_poem_detail_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_get_poem_detail_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val retryPolicy: RetryPolicy = RetryPolicy.API_DEFAULT

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_get_poem_detail_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_get_poem_detail_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "poem_id" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_get_poem_detail_param_poem_id),
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_get_poem_detail_param_title),
            ),
            "author" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_get_poem_detail_param_author),
            ),
        ),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val poemId = json.optLong("poem_id")
            val poemTitle = json.optString("title").trim()
            val author = json.optString("author").trim()
            require(poemId > 0 || poemTitle.isNotEmpty()) {
                textProvider.string(R.string.agent_tool_get_poem_detail_id_or_title_required)
            }

            val poem = resolvePoem(poemId, poemTitle, author)
                ?: error(
                    textProvider.string(
                        R.string.agent_tool_get_poem_detail_not_found,
                        poemTitle.ifBlank { poemId.toString() },
                    )
                )

            val deeplink = poemDetailDeeplink(poem.id)
            poemSuccessResult(
                markdown = buildMarkdown(poem, deeplink),
                deepLinks = listOf(
                    toolDeepLinkJson(
                        uri = deeplink,
                        label = textProvider.string(R.string.agent_tool_poem_open_detail_link),
                        primary = true,
                    )
                ),
            ) {
                put("action", "detail")
                put("poem", poemDetailJson(poem))
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_get_poem_detail_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_poem_unknown_error),
                ),
                isError = true,
            )
        }
    }

    /** id 优先查本地;否则按标题搜索精确匹配。线上结果落库后再返回,本地同 id 记录优先 */
    private suspend fun resolvePoem(poemId: Long, title: String, author: String): Poem? {
        if (poemId > 0) {
            poemService.getPoem(poemId)?.let { return it }
            if (title.isBlank()) return null
        }
        if (title.isBlank()) return null

        val candidates = poemService.searchPoems(title).getOrThrow()
        val exactMatches = candidates.filter { it.title == title }
        val resolved = exactMatches.firstOrNull { author.isNotBlank() && it.author.contains(author) }
            ?: exactMatches.firstOrNull()
            ?: return null
        return poemService.getPoem(resolved.id) ?: resolved.also { poemService.upsertPoem(it) }
    }

    private fun buildMarkdown(poem: Poem, deeplink: String): String {
        return buildString {
            appendLine("# 《${sanitizeMarkdownText(poem.title)}》")
            appendLine()
            appendLine(
                "- **${textProvider.string(R.string.poem_filter_author)}**: ${sanitizeMarkdownText(poem.authorWithDynasty)}"
            )
            if (poem.type.isNotBlank()) {
                appendLine(
                    "- **${textProvider.string(R.string.poem_filter_type)}**: ${sanitizeMarkdownText(poem.type)}"
                )
            }
            if (poem.isFavorite) {
                appendLine("- ${textProvider.string(R.string.poem_favorited)}")
            }
            appendLine()
            poem.content.forEach { appendLine(sanitizeMarkdownText(it)) }

            poem.translation?.takeIf { it.isNotBlank() }?.let { translation ->
                appendLine()
                appendLine("## ${textProvider.string(R.string.poem_translation_title)}")
                appendLine()
                appendLine(sanitizeMarkdownText(translation))
            }
            poem.aiInsight?.takeIf { it.isNotBlank() }?.let { insight ->
                appendLine()
                appendLine("## ${textProvider.string(R.string.poem_insight_title)}")
                appendLine()
                appendLine(sanitizeMarkdownText(insight))
            }
            appendLine()
            appendLine(
                "- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_poem_open_detail_link), deeplink)}"
            )
        }.trimEnd()
    }

    private fun poemDetailJson(poem: Poem): JSONObject = JSONObject().apply {
        put("id", poem.id)
        put("title", poem.title)
        put("author", poem.author)
        put("dynasty", poem.dynasty)
        put("type", poem.type)
        put("content", JSONArray().apply { poem.content.forEach { put(it) } })
        put("isFavorite", poem.isFavorite)
        poem.translation?.takeIf { it.isNotBlank() }?.let { put("translation", it) }
        poem.aiInsight?.takeIf { it.isNotBlank() }?.let { put("insight", it) }
    }
}
