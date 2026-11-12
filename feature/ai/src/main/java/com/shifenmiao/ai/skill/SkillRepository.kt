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

package com.shifenmiao.ai.skill

import androidx.room.withTransaction
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.context.TokenEstimator
import com.shifenmiao.ai.prompt.SystemPromptRepository
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.SkillLocalStore
import com.shifenmiao.database.ai.SkillUsagePolicy
import com.shifenmiao.database.ai.dao.SkillDao
import com.shifenmiao.database.ai.entity.SkillEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import com.shifenmiao.database.R as DatabaseR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 技能（SKILL.md）仓库（agent 链路侧）。
 *
 * - prompt 只注入 `<available_skills>` 清单（name/description），
 *   正文由 agent 经 use_skill 工具按需加载（[loadSkillBody]）；
 * - use_count 超 [SkillUsagePolicy.NORMALIZE_THRESHOLD] 时全体归一化到 0–100；
 * - 导入/编辑/删除等管理操作走 feature/settings 的管理页
 *  （与这里共用 core/database 的 [SkillImportValidator] / [SkillUsagePolicy] 规则）。
 */
@Singleton
class SkillRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val skillDao: SkillDao,
    private val textProvider: AgentToolTextProvider,
    private val systemPromptRepository: SystemPromptRepository,
    dispatchersHolder: DispatchersHolder,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + dispatchersHolder.ioDispatcher)

    /** 全部技能（含停用），工具内查询可用清单用。 */
    val skills: StateFlow<List<SkillEntity>> = skillDao.observeAll()
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    // ─── use_skill 工具入口 ─────────────────────────────────────────────

    /**
     * 按 id（slug）或 name 取启用技能的完整正文，命中即记录使用频次。
     * 找不到 / 已停用返回 null，由工具层回传可用清单。
     */
    suspend fun loadSkillBody(nameOrSlug: String): String? {
        val key = nameOrSlug.trim()
        if (key.isEmpty()) return null
        val skill = skillDao.getById(key)
            ?: skillDao.getAll().firstOrNull { it.name.equals(key, ignoreCase = true) }
        if (skill == null || !skill.enabled) return null
        recordSkillUse(skill.id)
        return skill.body
    }

    /**
     * use_count 自增；任一技能超过阈值时全体归一化到 0–100（事务批量写回）。
     * 自增与归一化都不刷 updated_at：usage 不是内容变更，
     * 不能污染"7 天内有更新"的清单遴选信号。
     */
    suspend fun recordSkillUse(id: String) {
        skillDao.incrementUseCount(id)
        val all = skillDao.getAll()
        val max = all.maxOfOrNull { it.useCount } ?: return
        if (max <= SkillUsagePolicy.NORMALIZE_THRESHOLD) return
        appDatabase.withTransaction {
            all.forEach { skill ->
                skillDao.updateUseCount(
                    id = skill.id,
                    useCount = skill.useCount / max * 100.0
                )
            }
        }
    }

    /**
     * 从 SKILL.md 文本导入 LOCAL 技能（未来 REMOTE 同步/服务下发的本地落库入口）。
     * 语义与设置页导入/新建完全一致（[SkillLocalStore.import]）。
     */
    suspend fun importFromContent(content: String): Result<SkillEntity> =
        SkillLocalStore.import(skillDao, content)

    // ─── prompt 注入 ────────────────────────────────────────────────────

    /**
     * 组装 `<available_skills>` 清单 fragment。
     *
     * enabled 技能 ≤ [SkillUsagePolicy.MAX_LISTED_SKILLS] 直接全列；超出按
     * bundled > 7 天内更新 > useCount 遴选。清单总量再按 [tokenBudget] 的
     * [SkillUsagePolicy.LIST_BUDGET_FRACTION] 动态裁剪（从优先级最低的末尾开始丢，
     * 附"还有 N 个技能未列出"）。name/description 进 prompt 前做截断 + XML 转义 + 单行化。
     *
     * @return null 表示无启用技能（或预算连一条都装不下）
     */
    suspend fun buildPromptFragment(tokenBudget: Int): String? {
        val enabled = skillDao.getAll().filter { it.enabled }
        if (enabled.isEmpty()) return null
        val prioritized = if (enabled.size <= SkillUsagePolicy.MAX_LISTED_SKILLS) {
            enabled.sortedBy { it.name }
        } else {
            selectSkillsForListing(enabled)
        }

        // 引导语从 item_prompt 预置读取（SystemPromptManagement 可编辑），fallback 到 raw
        val guidance = systemPromptRepository.getSystemPrompt(
            title = PromptEntity.SYSTEM_PROMPT_KEY_SKILLS_LIST_GUIDANCE,
            fallback = textProvider.rawAsync(DatabaseR.raw.prompt_skills_list_guidance)
        )
        val budgetTokens = (tokenBudget * SkillUsagePolicy.LIST_BUDGET_FRACTION).toInt()
        var usedTokens = TokenEstimator.estimateText(guidance)
        val listedEntries = mutableListOf<String>()
        for (skill in prioritized) {
            // 按最终渲染内容（截断 + XML 转义后）估算，长描述不会挤爆预算
            val rendered = "<skill>\n" +
                "<name>${skill.name.toXmlSafe()}</name>\n" +
                "<description>${skill.description.toXmlSafe()}</description>\n" +
                "</skill>"
            val entryTokens = TokenEstimator.estimateText(rendered) + 1
            // 单条放不下就跳过该条继续后面的（不 break）
            if (budgetTokens > 0 && usedTokens + entryTokens > budgetTokens) continue
            usedTokens += entryTokens
            listedEntries.add(rendered)
        }
        if (listedEntries.isEmpty()) {
            "buildPromptFragment: no skill fits budget=$budgetTokens (${enabled.size} enabled)"
                .makeLog("SkillRepository")
            return null
        }

        val unlisted = enabled.size - listedEntries.size
        return buildString {
            appendLine("<available_skills>")
            appendLine(guidance)
            listedEntries.forEach { appendLine(it) }
            if (unlisted > 0) {
                appendLine(textProvider.string(R.string.agent_skills_prompt_unlisted, unlisted))
            }
            append("</available_skills>")
        }
    }

    /** 清单超长时的遴选：bundled 优先，其次 7 天内有更新，再按使用频次。 */
    private fun selectSkillsForListing(enabled: List<SkillEntity>): List<SkillEntity> {
        val now = System.currentTimeMillis()
        return enabled.sortedWith(
            compareByDescending<SkillEntity> { it.source == SkillEntity.SOURCE_BUNDLED }
                .thenByDescending { now - it.updatedAt <= SkillUsagePolicy.RECENT_UPDATE_WINDOW_MS }
                .thenByDescending { it.useCount }
                .thenBy { it.name }
        ).take(SkillUsagePolicy.MAX_LISTED_SKILLS)
    }

    /** 先截断原文再 XML 转义（避免截出半个实体），最后单行化 */
    private fun String.toXmlSafe(): String =
        take(SkillUsagePolicy.MAX_META_CHARS)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace(Regex("\\s+"), " ")
}
