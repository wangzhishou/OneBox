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
 * 技能使用频次与清单展示策略（feature/ai 与 feature/settings 共用）。
 */
object SkillUsagePolicy {

    /** prompt 清单最多列出的技能数 */
    const val MAX_LISTED_SKILLS = 20

    /** use_count 超过该值时触发全体归一化到 0–100 */
    const val NORMALIZE_THRESHOLD = 1000.0

    /** 清单遴选时"近期更新"的时间窗 */
    const val RECENT_UPDATE_WINDOW_MS = 7L * 24 * 60 * 60 * 1000

    /** name/description 进 prompt 前的字符上限（防 `</available_skills>` 逃逸） */
    const val MAX_META_CHARS = 200

    /** 技能清单占 prompt 预算的比例上限 */
    const val LIST_BUDGET_FRACTION = 0.10

    /** 使用频次分档（阈值照 OpenMinis：20 / 60） */
    enum class UsageFrequency { NEVER, LOW, REGULAR, HIGH }

    fun frequencyOf(useCount: Double): UsageFrequency = when {
        useCount <= 0.0 -> UsageFrequency.NEVER
        useCount < 20.0 -> UsageFrequency.LOW
        useCount < 60.0 -> UsageFrequency.REGULAR
        else -> UsageFrequency.HIGH
    }
}
