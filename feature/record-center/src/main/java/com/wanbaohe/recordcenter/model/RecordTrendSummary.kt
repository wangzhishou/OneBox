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

package com.wanbaohe.recordcenter.model

import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 趋势聚合页单个记录类型的汇总数据。
 *
 * 图表窗口固定为近 7 天(按自然日聚合,一天多条取均值),
 * 对比窗口为其前 7 天,用于"较上周"增量。
 */
data class RecordTrendSummary(
    val definition: RecordTypeDefinition,
    /** 全量历史里的最新一条(卡片大数字),null = 从未记录 */
    val latest: HealthRecordEntity?,
    /** 近 7 天每天的 day-start millis(旧 → 新),与 dailyPoints 的点位对齐 */
    val dayStarts: List<Long>,
    /** chartFieldKey → 近 7 天每日均值,null = 当日无记录(折线断点) */
    val dailyPoints: Map<String, List<Float?>>,
    /** chartFieldKey → 本周均值 - 上周均值;任一侧无数据为 null */
    val weekDeltas: Map<String, Float?>,
) {
    /** 近 7 天是否有任何记录(无 → 卡片展示空状态) */
    val hasRecentData: Boolean
        get() = dailyPoints.values.any { points -> points.any { it != null } }
}

/**
 * 由近 14 天记录构建全部类型的趋势汇总。
 *
 * @param recentRecords 近 14 天(本周 7 天 + 上周 7 天)的全部类型记录
 * @param latestByType  全量历史各类型最新一条(type → entity)
 */
fun buildRecordTrendSummaries(
    definitions: List<RecordTypeDefinition>,
    recentRecords: List<HealthRecordEntity>,
    latestByType: Map<String, HealthRecordEntity>,
    zoneId: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(),
): List<RecordTrendSummary> {
    val days = (6L downTo 0L).map { today.minusDays(it) }
    val dayStarts = days.map { it.atStartOfDay(zoneId).toInstant().toEpochMilli() }
    val thisWeekDates = days.toSet()
    val lastWeekDates = (13L downTo 7L).map { today.minusDays(it) }.toSet()

    val recordsByType = recentRecords.groupBy { it.type }

    return definitions.map { definition ->
        val decoded = recordsByType[definition.key].orEmpty()
            .map { record ->
                Instant.ofEpochMilli(record.happenedAt).atZone(zoneId).toLocalDate() to
                    RecordFieldsCodec.decode(record.fieldsJson)
            }

        val dailyPoints = definition.chartFieldKeys.associateWith { fieldKey ->
            days.map { day ->
                val values = decoded.filter { (date, _) -> date == day }
                    .mapNotNull { (_, fields) -> fields[fieldKey] }
                if (values.isEmpty()) null else values.average().toFloat()
            }
        }

        val weekDeltas = definition.chartFieldKeys.associateWith { fieldKey ->
            val thisWeek = decoded.filter { (date, _) -> date in thisWeekDates }
                .mapNotNull { (_, fields) -> fields[fieldKey] }
            val lastWeek = decoded.filter { (date, _) -> date in lastWeekDates }
                .mapNotNull { (_, fields) -> fields[fieldKey] }
            if (thisWeek.isEmpty() || lastWeek.isEmpty()) {
                null
            } else {
                (thisWeek.average() - lastWeek.average()).toFloat()
            }
        }

        RecordTrendSummary(
            definition = definition,
            latest = latestByType[definition.key],
            dayStarts = dayStarts,
            dailyPoints = dailyPoints,
            weekDeltas = weekDeltas,
        )
    }
}
