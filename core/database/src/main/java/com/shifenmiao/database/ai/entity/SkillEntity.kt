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

package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI 技能（SKILL.md）：
 * 元数据与正文整体入库（body 为带 YAML frontmatter 的完整 SKILL.md 文本，保持生态兼容），
 * prompt 只注入 name/description 清单，正文由 agent 经 use_skill 工具按需加载。
 */
@Entity(tableName = "skill")
data class SkillEntity(
    /** slug（kebab-case），与 frontmatter 的 name 一致。 */
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    /** 完整 SKILL.md 文本（含 frontmatter）。 */
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "version", defaultValue = "1.0.0")
    val version: String = "1.0.0",
    @ColumnInfo(name = "source")
    val source: String = SOURCE_LOCAL,
    /** 预留：对齐 Strapi 同步惯例（参照 PromptEntity）；预置技能填稳定的预置 key。 */
    @ColumnInfo(name = "document_id")
    val documentId: String? = null,
    @ColumnInfo(name = "enabled", defaultValue = "1")
    val enabled: Boolean = true,
    /** 使用频次，超 1000 全体归一化到 0–100（照 OpenMinis normalizeUseCounts）。 */
    @ColumnInfo(name = "use_count", defaultValue = "0")
    val useCount: Double = 0.0,
    @ColumnInfo(name = "installed_at")
    val installedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SOURCE_BUNDLED = "BUNDLED"
        const val SOURCE_LOCAL = "LOCAL"
        /** 预留：未来 Strapi 技能市场下发。 */
        const val SOURCE_REMOTE = "REMOTE"

        /** 预置技能标识（document_id）：公众号写作风格示例技能。 */
        const val SKILL_PRESET_KEY_WECHAT_ARTICLE = "bundled_wechat_article_style"
    }
}
