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

/**
 * SKILL.md 导入/保存校验规则（feature/ai 的 SkillRepository 与
 * feature/settings 的技能管理页共用，保证两处导入行为一致）。
 *
 * 规则：
 * - frontmatter 必须含单行 name/description（[SkillFrontMatterParser]）；
 * - name 必须是 kebab-case slug；
 * - body 体积不得超过 [MAX_BODY_BYTES]（与 use_skill 的 maxResultLength=16KB 对齐，
 *   避免"导得进但读不全"）；
 * - 与 BUNDLED 技能同名时拒绝（防止导入覆盖预置行）。
 */
object SkillImportValidator {

    /** SKILL.md 正文体积上限（UTF-8 字节） */
    const val MAX_BODY_BYTES = 16 * 1024

    /** name(slug) 字符上限：超长 slug 在清单里被截断后 use_skill 会按名找不到 */
    const val MAX_SLUG_CHARS = 100

    /** 文件导入的读取上限（字节，SAF 读取阶段限量，超出按文件过大拒绝） */
    const val MAX_IMPORT_FILE_BYTES = 64 * 1024

    private val SLUG_REGEX = Regex("[a-z0-9]+(-[a-z0-9]+)*")

    enum class Rejection {
        EMPTY_BODY,
        INVALID_FRONTMATTER,
        INVALID_SLUG,
        SLUG_TOO_LONG,
        BODY_TOO_LARGE,
        BUNDLED_NAME_CONFLICT,
    }

    class SkillImportException(val rejection: Rejection) : Exception(rejection.name)

    data class Validated(
        val body: String,
        val slug: String,
        val description: String,
    )

    /**
     * 校验导入/保存文本。
     *
     * @param content 原始 SKILL.md 文本
     * @param existing 同 slug 的既有行（调用方先按 slug 查）；为 BUNDLED 时拒绝
     * @return 成功为 [Validated]；失败为 [SkillImportException]，按 rejection 映射提示文案
     */
    fun validate(content: String, existing: com.shifenmiao.database.ai.entity.SkillEntity? = null): Result<Validated> {
        val body = content.trim()
        if (body.isEmpty()) return failure(Rejection.EMPTY_BODY)
        val (slug, description) = SkillFrontMatterParser.parse(body)
            ?: return failure(Rejection.INVALID_FRONTMATTER)
        if (!SLUG_REGEX.matches(slug)) return failure(Rejection.INVALID_SLUG)
        if (slug.length > MAX_SLUG_CHARS) return failure(Rejection.SLUG_TOO_LONG)
        if (body.toByteArray(Charsets.UTF_8).size > MAX_BODY_BYTES) {
            return failure(Rejection.BODY_TOO_LARGE)
        }
        if (existing?.source == com.shifenmiao.database.ai.entity.SkillEntity.SOURCE_BUNDLED) {
            return failure(Rejection.BUNDLED_NAME_CONFLICT)
        }
        return Result.success(Validated(body = body, slug = slug, description = description))
    }

    private fun failure(rejection: Rejection): Result<Validated> =
        Result.failure(SkillImportException(rejection))

    /**
     * 把用户输入宽松规范化为 kebab-case slug（小写、非法字符折叠为连字符）；
     * 无法得到合法 slug（空 / 超长 / 不含任何字母数字）时返回 null。
     */
    fun slugify(raw: String): String? {
        val slug = raw.trim().lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .replace(Regex("-{2,}"), "-")
            .trim('-')
        if (slug.isEmpty() || slug.length > MAX_SLUG_CHARS || !SLUG_REGEX.matches(slug)) {
            return null
        }
        return slug
    }
}
