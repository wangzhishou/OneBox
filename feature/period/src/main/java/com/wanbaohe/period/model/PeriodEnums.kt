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

package com.wanbaohe.period.model

/** 流量强度 */
enum class PeriodFlow {
    NONE,
    LIGHT,
    MEDIUM,
    HEAVY;

    companion object {
        fun fromName(name: String?): PeriodFlow =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NONE
    }
}

/** 心情五档 */
enum class PeriodMood {
    VERY_HAPPY,
    HAPPY,
    NORMAL,
    SAD,
    VERY_SAD;

    companion object {
        fun fromName(name: String?): PeriodMood =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NORMAL
    }
}

/** 常见症状（与设计稿 chips 一致） */
enum class PeriodSymptom {
    ABDOMINAL_PAIN,
    HEADACHE,
    BACK_PAIN,
    MOOD_SWINGS,
    BREAST_TENDERNESS,
    FATIGUE,
    NAUSEA,
    INSOMNIA,
    APPETITE_CHANGE,
    ACNE,
    EDEMA;

    companion object {
        fun fromName(name: String?): PeriodSymptom? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

        fun parse(csv: String?): List<PeriodSymptom> {
            if (csv.isNullOrBlank()) return emptyList()
            return csv.split(',')
                .mapNotNull { fromName(it.trim()) }
                .distinct()
        }

        fun toCsv(items: Collection<PeriodSymptom>): String =
            items.joinToString(",") { it.name }
    }
}

/** 列表页底部 tab */
enum class PeriodTab {
    RECORDS,
    STATS,
}

/** 统计页时间范围 */
enum class PeriodStatsRange {
    SIX_MONTHS,
    DAY,
    WEEK,
    MONTH,
}

/** 记录卡片右侧状态 */
enum class PeriodStatus {
    PERIOD,
    OVULATION,
    NORMAL,
}

/** 编辑页「经期状态」三选一：开始 / 经期中 / 结束(经期中 = 开始、结束标记都不置位) */
enum class PeriodDayStatus {
    START,
    MID,
    END,
}
