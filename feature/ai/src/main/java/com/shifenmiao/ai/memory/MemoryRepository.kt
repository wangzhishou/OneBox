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

package com.shifenmiao.ai.memory

import androidx.room.withTransaction
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.context.TokenEstimator
import com.shifenmiao.ai.prompt.SystemPromptRepository
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.MemoryLimits
import com.shifenmiao.database.ai.dao.MemoryEntryDao
import com.shifenmiao.database.ai.entity.MemoryEntryEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.t8rin.logger.makeLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import com.shifenmiao.database.R as DatabaseR

/**
 * AI 记忆仓库：两级记忆（profile 全局档案 + log 时间序日志）的写入、检索与注入组装。
 *
 * 保留 OpenMinis 的语义（关键词检索、注入上限、滚动淘汰），载体落到 Room：
 * - 检索不用 SQL LIKE（要转义 %/_，且 SQLite LIKE 仅 ASCII 大小写不敏感），
 *   DAO 取出 enabled 候选后在 Kotlin 侧 lowercase() 过滤（Unicode 感知）；
 * - log 滚动淘汰（超 [MemoryLimits.MAX_LOG_ENTRIES] 删最旧），profile 不参与；
 * - 注入 = 全部 enabled profile + 最近 3 个"有内容的日期桶"的 log，再按预算逐条截断装入。
 *
 * 容量与策略常量在 [MemoryLimits]（core/database，feature/settings 管理页共用）。
 */
@Singleton
class MemoryRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val memoryEntryDao: MemoryEntryDao,
    private val textProvider: AgentToolTextProvider,
    private val systemPromptRepository: SystemPromptRepository,
) {
    /** 搜索范围：仅 log / 全部（profile + log） */
    enum class SearchScope { LOG, ALL }

    data class MemorySearchResult(
        val entries: List<MemoryEntryEntity>,
        /** 命中数超过条数上限被省略的条数 */
        val omittedByCount: Int,
        /** 因字节预算耗尽被整条省略的条数 */
        val omittedByBytes: Int,
        /** 因超预算被截断展示（仍保留）的条数 */
        val truncatedEntries: Int,
    )

    /**
     * 写入一条 log 记忆（memory_write 工具入口），写后执行滚动淘汰。
     * insert + 计数 + 淘汰在同一事务内，避免并发写入时淘汰水位失真。
     *
     * @return 新条目 id
     */
    suspend fun writeMemory(content: String, sourceConversationId: String?): Long {
        val trimmed = content.trim()
        require(trimmed.length <= MemoryLimits.MAX_ENTRY_CHARS) {
            "Memory entry exceeds ${MemoryLimits.MAX_ENTRY_CHARS} chars"
        }
        return appDatabase.withTransaction {
            val now = System.currentTimeMillis()
            val id = memoryEntryDao.insert(
                MemoryEntryEntity(
                    kind = MemoryEntryEntity.KIND_LOG,
                    content = trimmed,
                    sourceConversationId = sourceConversationId?.takeIf { it.isNotBlank() },
                    createdAt = now,
                    updatedAt = now,
                )
            )
            // 滚动淘汰：log 超上限删最旧，profile 不参与
            val total = memoryEntryDao.countByKind(MemoryEntryEntity.KIND_LOG)
            if (total > MemoryLimits.MAX_LOG_ENTRIES) {
                memoryEntryDao.deleteOldestByKind(
                    MemoryEntryEntity.KIND_LOG,
                    total - MemoryLimits.MAX_LOG_ENTRIES
                )
            }
            id
        }
    }

    /**
     * 条目级关键词搜索：全部关键词命中即返回整条，按时间倒序。
     * 无关键词时为 dump（取最新条目）。上限：搜索 [MemoryLimits.SEARCH_RESULT_LIMIT] 条 /
     * dump [MemoryLimits.DUMP_RESULT_LIMIT] 条 / 总输出 [MemoryLimits.MAX_OUTPUT_BYTES] 字节。
     *
     * 单条超预算时截断该条并继续后续条目（不再整体 break），
     * 调用方可据 [MemorySearchResult] 的计数区分"无匹配"与"N 条被截断/省略"。
     */
    suspend fun search(keywords: List<String>, scope: SearchScope): MemorySearchResult {
        val normalizedKeywords = keywords
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
        val candidates = buildList {
            if (scope == SearchScope.ALL) {
                addAll(memoryEntryDao.getByKind(MemoryEntryEntity.KIND_PROFILE, enabled = true))
            }
            addAll(
                memoryEntryDao.getRecentByKind(
                    kind = MemoryEntryEntity.KIND_LOG,
                    enabled = true,
                    limit = MemoryLimits.DUMP_RESULT_LIMIT
                )
            )
        }
        val limit = if (normalizedKeywords.isEmpty()) {
            MemoryLimits.DUMP_RESULT_LIMIT
        } else {
            MemoryLimits.SEARCH_RESULT_LIMIT
        }
        val matched = candidates
            .filter { entry ->
                normalizedKeywords.all { keyword -> keyword in entry.content.lowercase() }
            }
            .sortedByDescending { it.createdAt }

        val entries = mutableListOf<MemoryEntryEntity>()
        var usedBytes = 0
        var omittedByBytes = 0
        var truncatedEntries = 0
        for (entry in matched.take(limit)) {
            val remaining = MemoryLimits.MAX_OUTPUT_BYTES - usedBytes
            // 标题/结构开销与正文一起计入预算（端到端对齐工具 maxResultLength）
            val entryBytes = entry.content.toByteArray(Charsets.UTF_8).size +
                MemoryLimits.PER_ENTRY_OVERHEAD_BYTES
            when {
                entryBytes <= remaining -> {
                    usedBytes += entryBytes
                    entries.add(entry)
                }
                // 剩余预算还够放一条截断版：截断该条并继续
                remaining > MIN_ENTRY_BYTES -> {
                    entries.add(
                        entry.copy(
                            content = entry.content.truncateToUtf8Bytes(
                                remaining - MemoryLimits.PER_ENTRY_OVERHEAD_BYTES
                            )
                        )
                    )
                    usedBytes = MemoryLimits.MAX_OUTPUT_BYTES
                    truncatedEntries++
                }
                // 预算耗尽：整条省略，继续统计（不再 break）
                else -> omittedByBytes++
            }
        }
        return MemorySearchResult(
            entries = entries,
            omittedByCount = (matched.size - limit).coerceAtLeast(0),
            omittedByBytes = omittedByBytes,
            truncatedEntries = truncatedEntries,
        )
    }

    /**
     * 组装注入 system prompt 的记忆 fragment（`<memory>` 标签包裹 + 防御性引导语）。
     *
     * 内容：全部 enabled profile + 最近 [MemoryLimits.PROMPT_LOG_BUCKETS] 个有内容日期桶的 log。
     * 预算 = max([tokenBudget] × [MemoryLimits.PROMPT_BUDGET_FRACTION],
     * [MemoryLimits.MIN_PROMPT_BUDGET_TOKENS])：桶内条目逐条装入，单条过长时截断装入，
     * 预算耗尽时保留已装条目并附省略提示（不整桶丢弃）；profile 与引导语恒定保留。
     *
     * @param includeSearchHint 省略提示是否带"用 memory_get 搜索"指引
     * （模型不支持工具调用时传 false）
     * @return null 表示无任何可注入内容
     */
    suspend fun buildPromptFragment(tokenBudget: Int, includeSearchHint: Boolean = true): String? {
        val profiles = memoryEntryDao.getByKind(MemoryEntryEntity.KIND_PROFILE, enabled = true)
        val logs = memoryEntryDao.getRecentByKind(
            kind = MemoryEntryEntity.KIND_LOG,
            enabled = true,
            limit = MemoryLimits.MAX_LOG_ENTRIES
        )
        if (profiles.isEmpty() && logs.isEmpty()) return null

        val budgetTokens = maxOf(
            (tokenBudget * MemoryLimits.PROMPT_BUDGET_FRACTION).toInt(),
            MemoryLimits.MIN_PROMPT_BUDGET_TOKENS
        )

        // logs 已按 created_at 倒序，groupBy 保持首次出现顺序 = 日期桶倒序
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val buckets = logs.groupBy { dateFormat.format(Date(it.createdAt)) }
            .entries.take(MemoryLimits.PROMPT_LOG_BUCKETS)

        // 引导语从 item_prompt 预置读取（SystemPromptManagement 可编辑），fallback 到 raw
        val profileGuidance = if (profiles.isNotEmpty()) {
            systemPromptRepository.getSystemPrompt(
                title = PromptEntity.SYSTEM_PROMPT_KEY_MEMORY_GLOBAL_GUIDANCE,
                fallback = textProvider.rawAsync(DatabaseR.raw.prompt_memory_global_guidance)
            )
        } else {
            ""
        }
        var usedTokens = TokenEstimator.estimateText(profileGuidance)
        var omitted = false

        val profileBlock = StringBuilder()
        if (profiles.isNotEmpty()) {
            profileBlock.appendLine()
            profileBlock.appendLine(textProvider.string(R.string.agent_memory_prompt_profile_header))
            profiles.forEach { entry ->
                profileBlock.appendLine("- ${entry.content.trim()}")
            }
            usedTokens += TokenEstimator.estimateText(profileBlock.toString())
        }

        // 近期日志引导语：支持工具调用时用含 memory_get 提示的版本，否则用降级版
        val logGuidance = if (logs.isNotEmpty()) {
            systemPromptRepository.getSystemPrompt(
                title = if (includeSearchHint) {
                    PromptEntity.SYSTEM_PROMPT_KEY_MEMORY_RECENT_GUIDANCE
                } else {
                    PromptEntity.SYSTEM_PROMPT_KEY_MEMORY_RECENT_GUIDANCE_NO_TOOL
                },
                fallback = textProvider.rawAsync(
                    if (includeSearchHint) DatabaseR.raw.prompt_memory_recent_guidance
                    else DatabaseR.raw.prompt_memory_recent_guidance_no_tool
                )
            )
        } else {
            ""
        }
        val logGuidanceTokens = TokenEstimator.estimateText(logGuidance) + 1

        // 日期桶逐条装入：装得下的装，单条过长截断装，预算耗尽即止并标记省略
        val logBlock = StringBuilder()
        var packedLogs = 0
        if (logs.isNotEmpty() && usedTokens + logGuidanceTokens < budgetTokens) {
            logBlock.appendLine()
            logBlock.appendLine(logGuidance)
            usedTokens += logGuidanceTokens
            bucketLoop@ for ((date, entries) in buckets) {
                val header = "\n" + textProvider.string(R.string.agent_memory_prompt_log_header, date) + "\n"
                val headerTokens = TokenEstimator.estimateText(header)
                if (usedTokens + headerTokens >= budgetTokens) {
                    omitted = true
                    break
                }
                val bucketStart = logBlock.length
                val bucketStartTokens = usedTokens
                logBlock.append(header)
                usedTokens += headerTokens
                var packedInBucket = 0
                for (entry in entries) {
                    val line = "- ${entry.content.trim()}"
                    val lineTokens = TokenEstimator.estimateText(line) + 1
                    when {
                        usedTokens + lineTokens <= budgetTokens -> {
                            logBlock.appendLine(line)
                            usedTokens += lineTokens
                            packedInBucket++
                        }
                        budgetTokens - usedTokens > MIN_LINE_TOKENS -> {
                            // 单条过长：按剩余预算截断装入
                            val keepChars = (entry.content.length *
                                ((budgetTokens - usedTokens).toDouble() / lineTokens))
                                .toInt().coerceIn(0, entry.content.length)
                            logBlock.appendLine("- ${entry.content.trim().take(keepChars)}…")
                            usedTokens = budgetTokens
                            packedInBucket++
                            omitted = true
                            break@bucketLoop
                        }
                        else -> {
                            omitted = true
                            break@bucketLoop
                        }
                    }
                }
                if (packedInBucket == 0) {
                    // 桶内一条都没装：回退桶头，避免空桶标题
                    logBlock.setLength(bucketStart)
                    usedTokens = bucketStartTokens
                    omitted = true
                    break
                }
                packedLogs += packedInBucket
            }
            if (packedLogs == 0) {
                // 一条日志都没装下：回退日志引导语
                logBlock.setLength(0)
                usedTokens -= logGuidanceTokens
            }
        }
        if (logs.isNotEmpty() && packedLogs == 0) {
            "buildPromptFragment: no log entries fit budget=$budgetTokens, fallback to profile-only"
                .makeLog("MemoryRepository")
        }
        // 极端预算下什么都装不下：不注入空壳 fragment
        if (profileBlock.isEmpty() && logBlock.isEmpty()) return null

        return buildString {
            appendLine("<memory>")
            if (profileGuidance.isNotBlank()) appendLine(profileGuidance)
            append(profileBlock)
            append(logBlock)
            if (omitted) {
                appendLine()
                appendLine(
                    textProvider.string(
                        if (includeSearchHint) R.string.agent_memory_prompt_omitted_hint
                        else R.string.agent_memory_prompt_omitted_hint_no_search
                    )
                )
            }
            append("</memory>")
        }
    }

    private companion object {
        /** 剩余预算低于该字节数时整条省略（截断后信息过少无意义） */
        const val MIN_ENTRY_BYTES = 64

        /** 剩余预算低于该 token 数时不再截断装入 */
        const val MIN_LINE_TOKENS = 24
    }
}

/**
 * 真按 UTF-8 字节截断（回退到字符边界，末尾补省略号）。
 * 不能按"全串平均字节比"估算前缀长度——中英混排时比例失真会超预算。
 */
private fun String.truncateToUtf8Bytes(maxBytes: Int): String {
    val bytes = toByteArray(Charsets.UTF_8)
    if (bytes.size <= maxBytes) return this
    var end = maxBytes.coerceAtLeast(0)
    // UTF-8 续字节形如 10xxxxxx：回退到某个字符的起始字节
    while (end > 0 && end < bytes.size && (bytes[end].toInt() and 0xC0) == 0x80) {
        end--
    }
    return String(bytes, 0, end, Charsets.UTF_8) + "…"
}
