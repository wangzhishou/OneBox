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

package com.shifenmiao.database.ai

import androidx.room.withTransaction
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.dao.SkillDao
import com.shifenmiao.database.ai.entity.SkillEntity

/**
 * 技能 LOCAL 行的写入编排（import 语义 + 另存副本）。
 *
 * feature/ai 的 SkillRepository、feature/settings 的技能管理页与技能编辑页
 * 共用这一处实现，保证"导入 / 新建 / 另存副本"在任何入口行为一致。
 */
object SkillLocalStore {

    /**
     * 导入 / 新建 LOCAL 技能：
     * - 已存在同名 BUNDLED → 拒绝（[SkillImportValidator.Rejection.BUNDLED_NAME_CONFLICT]）；
     * - 已存在 LOCAL → 更新语义：保留 enabled / use_count / installed_at，
     *   只更新 name/description/body/updated_at（不 REPLACE 整行清零状态）。
     */
    suspend fun import(skillDao: SkillDao, content: String): Result<SkillEntity> {
        val body = content.trim()
        val meta = SkillFrontMatterParser.parse(body)
        val existing = meta?.let { skillDao.getById(it.first) }
        return SkillImportValidator.validate(body, existing).mapCatching { validated ->
            val now = System.currentTimeMillis()
            if (existing != null) {
                val updated = existing.copy(
                    name = validated.slug,
                    description = validated.description,
                    body = validated.body,
                    updatedAt = now,
                )
                skillDao.update(updated)
                updated
            } else {
                val entity = SkillEntity(
                    id = validated.slug,
                    name = validated.slug,
                    description = validated.description,
                    body = validated.body,
                    source = SkillEntity.SOURCE_LOCAL,
                    installedAt = now,
                    updatedAt = now,
                )
                skillDao.upsert(entity)
                entity
            }
        }
    }

    /**
     * 另存为 LOCAL 副本：id 冲突追加序号；副本 body 的 frontmatter name 同步改写，
     * 改写结果再过一次解析校验，失败返回 null（不生成坏副本）。
     */
    suspend fun saveAsCopy(skillDao: SkillDao, skill: SkillEntity): SkillEntity? {
        val now = System.currentTimeMillis()
        var copyId = "${skill.id}-copy"
        var sequence = 2
        while (skillDao.getById(copyId) != null) {
            copyId = "${skill.id}-copy$sequence"
            sequence++
        }
        val copyBody = SkillFrontMatterParser.rewriteName(skill.body, copyId)
        if (SkillFrontMatterParser.parse(copyBody)?.first != copyId) return null
        val copy = skill.copy(
            id = copyId,
            name = copyId,
            body = copyBody,
            source = SkillEntity.SOURCE_LOCAL,
            documentId = null,
            useCount = 0.0,
            installedAt = now,
            updatedAt = now,
        )
        skillDao.upsert(copy)
        return copy
    }

    /** 元数据更新结果 */
    enum class MetadataResult { SUCCESS, NOT_EDITABLE, INVALID_NAME, INVALID_DESCRIPTION, NAME_CONFLICT }

    /**
     * 更新技能元数据（name/description，不动正文），仅 LOCAL 可改。
     *
     * - newName 先 slugify 再校验（kebab-case、≤100、非空）；description 单行化且非空；
     * - slug 不变 → 简单 update（同时同步 body frontmatter 里的 description）；
     * - slug 变了 → 查重（新 slug 已存在即拒绝，BUNDLED/LOCAL 一视同仁），
     *   可用则在**同一事务内** insert 新行（保留 enabled/use_count/installed_at，
     *   body 的 frontmatter name/description 同步改写）+ delete 旧行，完成主键迁移。
     */
    suspend fun updateMetadata(
        appDatabase: AppDatabase,
        skillDao: SkillDao,
        skill: SkillEntity,
        newName: String,
        newDescription: String,
    ): MetadataResult {
        if (skill.source != SkillEntity.SOURCE_LOCAL) return MetadataResult.NOT_EDITABLE
        val slug = SkillImportValidator.slugify(newName) ?: return MetadataResult.INVALID_NAME
        val description = newDescription.trim().replace(Regex("\\s+"), " ")
        if (description.isEmpty()) return MetadataResult.INVALID_DESCRIPTION
        val now = System.currentTimeMillis()

        if (slug == skill.id) {
            val newBody = SkillFrontMatterParser.rewriteDescription(skill.body, description)
            if (SkillFrontMatterParser.parse(newBody) == null) return MetadataResult.INVALID_NAME
            skillDao.update(
                skill.copy(description = description, body = newBody, updatedAt = now)
            )
            return MetadataResult.SUCCESS
        }

        if (skillDao.getById(slug) != null) return MetadataResult.NAME_CONFLICT
        val newBody = SkillFrontMatterParser.rewriteDescription(
            SkillFrontMatterParser.rewriteName(skill.body, slug),
            description
        )
        if (SkillFrontMatterParser.parse(newBody)?.first != slug) {
            return MetadataResult.INVALID_NAME
        }
        appDatabase.withTransaction {
            skillDao.upsert(
                skill.copy(
                    id = slug,
                    name = slug,
                    description = description,
                    body = newBody,
                    updatedAt = now,
                )
            )
            skillDao.deleteById(skill.id)
        }
        return MetadataResult.SUCCESS
    }
}
