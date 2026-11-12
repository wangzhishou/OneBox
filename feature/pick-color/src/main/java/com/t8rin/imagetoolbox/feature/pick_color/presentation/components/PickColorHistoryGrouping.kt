/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import com.shifenmiao.model.color.ColorPickHistoryEntry
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal data class ColorHistoryGroup(
    val title: String,
    val entries: List<ColorPickHistoryEntry>
)

/**
 * 把按时间倒序的历史记录按“自然日”分组。
 *
 * 今天/昨天用本地化标题，更早的日期交给 [dateFormatter]（MEDIUM 风格，中文即“2026年10月18日”）。
 * groupBy 会保留首次遇到日期的顺序，记录本身已是最新在前，因此不再额外排序。
 */
internal fun groupColorPickHistory(
    history: List<ColorPickHistoryEntry>,
    todayLabel: String,
    yesterdayLabel: String,
    dateFormatter: DateTimeFormatter,
    zoneId: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zoneId)
): List<ColorHistoryGroup> = history
    .groupBy { localDateOf(it.pickedAtMillis, zoneId) }
    .map { (date, entries) ->
        ColorHistoryGroup(
            title = when (date) {
                today -> todayLabel
                today.minusDays(1) -> yesterdayLabel
                else -> date.format(dateFormatter)
            },
            entries = entries
        )
    }
