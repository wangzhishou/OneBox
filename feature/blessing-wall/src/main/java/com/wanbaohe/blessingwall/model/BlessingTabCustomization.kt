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

package com.wanbaohe.blessingwall.model

/**
 * 用户对单个祈福 tab 的自定义文案。
 * 空串表示未自定义，展示时回退到远程下发 / 本地兜底文案。
 */
data class BlessingTabCustomization(
    val title: String = "",
    val subtitle: String = "",
)

/**
 * 一次标题/副标题编辑产生的按日期快照。
 */
data class BlessingTabCustomizationSnapshot(
    val date: String,
    val type: BlessingType,
    val title: String,
    val subtitle: String,
)

/**
 * 计算 [date] 当天生效的自定义文案：每个类型取快照日期不晚于 [date] 的最近一条。
 * 日期均为 `yyyy-MM-dd` 格式，可直接按字典序比较。
 */
fun List<BlessingTabCustomizationSnapshot>.effectiveAt(
    date: String,
): Map<BlessingType, BlessingTabCustomization> {
    return filter { it.date <= date }
        .groupBy { it.type }
        .mapValues { (_, snapshots) ->
            val latest = snapshots.maxBy { it.date }
            BlessingTabCustomization(title = latest.title, subtitle = latest.subtitle)
        }
}
