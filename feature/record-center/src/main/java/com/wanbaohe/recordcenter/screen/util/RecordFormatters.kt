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

package com.wanbaohe.recordcenter.screen.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm")
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d")
private val chartDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")

private fun millisToDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

/**
 * 记录数值的紧凑展示,如血压 "120/80 mmHg"、血脂 "5.2 mmol/L …"。
 * 全部字段单位一致时合并为一个单位后缀,否则逐字段带单位。
 */
@Composable
fun formatRecordValues(definition: RecordTypeDefinition, fieldsJson: String): String {
    val values = RecordFieldsCodec.decode(fieldsJson)
    val present = definition.fields.filter { values.containsKey(it.key) }
    if (present.isEmpty()) return ""
    val units = present.map { it.unit }.distinct()
    return if (units.size == 1) {
        val joined = present.joinToString("/") { RecordFieldsCodec.formatValue(values.getValue(it.key)) }
        if (units.first().isEmpty()) joined else "$joined ${units.first()}"
    } else {
        present.joinToString("  ") { field ->
            val value = RecordFieldsCodec.formatValue(values.getValue(field.key))
            if (field.unit.isEmpty()) value else "$value ${field.unit}"
        }
    }
}

/**
 * 记录数值拆成 数值/单位 两部分,供"大数字 + 小单位"排版。
 * 单位不一致时取首个字段的单位;无单位(如 BMI)单位部分为 null。
 * 纯函数,调用方按需 remember。
 */
fun formatRecordValueParts(definition: RecordTypeDefinition, fieldsJson: String): Pair<String, String?> {
    val values = RecordFieldsCodec.decode(fieldsJson)
    val present = definition.fields.filter { values.containsKey(it.key) }
    if (present.isEmpty()) return "" to null
    val joined = present.joinToString("/") { RecordFieldsCodec.formatValue(values.getValue(it.key)) }
    val unit = present.map { it.unit }.distinct().firstOrNull()?.takeIf { it.isNotEmpty() }
    return joined to unit
}

/** 相对日期:今天 / 昨天 / N天前 / 具体日期 */
@Composable
fun formatRelativeDate(millis: Long): String {
    val date = millisToDate(millis)
    val days = ChronoUnit.DAYS.between(date, LocalDate.now()).toInt()
    return when {
        days <= 0 -> stringResource(R.string.record_center_today)
        days == 1 -> stringResource(R.string.record_center_yesterday)
        days < 30 -> stringResource(R.string.record_center_days_ago, days)
        else -> date.format(dateFormatter)
    }
}

/** 图表 X 轴标签:M/d */
fun formatChartDate(millis: Long): String = millisToDate(millis).format(chartDateFormatter)

/** 列表行时间:yyyy/M/d HH:mm */
fun formatRecordDateTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
