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

package com.shifenmiao.lifetime.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.model.CountdownStatus
import com.shifenmiao.lifetime.domain.model.MilestoneStatus

/**
 * 预置数据展示名本地化。
 *
 * 预置频率事件与预置倒数日（节日）在播种时以中文 name/unit 写入数据库
 * （见 FrequencyEventRepository.initializePresetEvents 与 CountdownSeedService），
 * 这些中文值同时作为稳定的映射 key；展示层统一在此映射到多语言字符串资源。
 * 用户自建内容（isPreset = false）不在映射表内，直接回退展示数据库中的值。
 */

/**
 * 预置事件/节日展示名：按播种时的中文 name 映射到字符串资源。
 * 映射表覆盖 [com.shifenmiao.lifetime.data.FrequencyEventRepository] 的预置频率事件、
 * feature:calendar 的 SOLAR_FESTIVALS / LUNAR_FESTIVALS 全部节日名，
 * 以及 LunarHolidayProvider 为非中文环境自建的国际节日（万圣节/感恩节/黑色星期五/母亲节/父亲节/跨年夜）。
 */
@Composable
fun localizedPresetEventName(name: String, isPreset: Boolean): String {
    if (!isPreset) return name
    val resId = presetEventNameRes(name) ?: return name
    return stringResource(resId)
}

/**
 * 预置事件/节日中文 name → 字符串资源 id 的映射表（非 Composable）。
 * 供展示层 Composable 与 AI 工具（AgentToolTextProvider）等非 Compose 场景复用；
 * 表外取值返回 null，调用方回退展示数据库中的原始值。
 */
fun presetEventNameRes(name: String): Int? = when (name) {
    "春节" -> R.string.lifetime_preset_name_spring_festival
    "元宵节" -> R.string.lifetime_preset_name_lantern_festival
    "龙抬头" -> R.string.lifetime_preset_name_longtaitou
    "上巳节" -> R.string.lifetime_preset_name_shangsi
    "端午节" -> R.string.lifetime_preset_name_dragon_boat
    "七夕" -> R.string.lifetime_preset_name_qixi
    "中元节" -> R.string.lifetime_preset_name_zhongyuan
    "中秋节" -> R.string.lifetime_preset_name_mid_autumn
    "重阳节" -> R.string.lifetime_preset_name_double_ninth
    "寒衣节" -> R.string.lifetime_preset_name_hanyi
    "下元节" -> R.string.lifetime_preset_name_xiayuan
    "腊八节" -> R.string.lifetime_preset_name_laba
    "小年" -> R.string.lifetime_preset_name_little_new_year
    "除夕" -> R.string.lifetime_preset_name_chinese_new_years_eve
    "元旦" -> R.string.lifetime_preset_name_new_years_day
    "情人节" -> R.string.lifetime_preset_name_valentines
    "妇女节" -> R.string.lifetime_preset_name_womens_day
    "植树节" -> R.string.lifetime_preset_name_arbor_day
    "消费者权益日" -> R.string.lifetime_preset_name_consumer_rights_day
    "愚人节" -> R.string.lifetime_preset_name_april_fools
    "地球日" -> R.string.lifetime_preset_name_earth_day
    "劳动节" -> R.string.lifetime_preset_name_labor_day
    "青年节" -> R.string.lifetime_preset_name_youth_day
    "护士节" -> R.string.lifetime_preset_name_nurses_day
    "儿童节" -> R.string.lifetime_preset_name_childrens_day
    "环境日" -> R.string.lifetime_preset_name_environment_day
    "建党节" -> R.string.lifetime_preset_name_party_founding_day
    "建军节" -> R.string.lifetime_preset_name_army_day
    "抗战胜利" -> R.string.lifetime_preset_name_victory_day
    "教师节" -> R.string.lifetime_preset_name_teachers_day
    "国庆节" -> R.string.lifetime_preset_name_national_day
    "光棍节" -> R.string.lifetime_preset_name_singles_day
    "国家公祭日" -> R.string.lifetime_preset_name_national_memorial_day
    "平安夜" -> R.string.lifetime_preset_name_christmas_eve
    "圣诞节" -> R.string.lifetime_preset_name_christmas
    "万圣节" -> R.string.lifetime_preset_name_halloween
    "感恩节" -> R.string.lifetime_preset_name_thanksgiving
    "黑色星期五" -> R.string.lifetime_preset_name_black_friday
    "母亲节" -> R.string.lifetime_preset_name_mothers_day
    "父亲节" -> R.string.lifetime_preset_name_fathers_day
    "跨年夜" -> R.string.lifetime_preset_name_new_years_eve
    "吃饭" -> R.string.lifetime_preset_name_eating
    "睡觉" -> R.string.lifetime_preset_name_sleeping
    "写日记" -> R.string.lifetime_preset_name_journaling
    "运动" -> R.string.lifetime_preset_name_exercise
    "看书" -> R.string.lifetime_preset_name_reading
    "看电影" -> R.string.lifetime_preset_name_movies
    "旅行" -> R.string.lifetime_preset_name_travel
    else -> null
}

/**
 * 事件单位展示名本地化。
 *
 * 单位只能来自新增事件页的固定下拉列表（见 LifeTimeAddEventScreen.UNIT_OPTIONS）
 * 或预置事件播种值，因此不按 isPreset 区分，统一按中文 key 映射；
 * 表外取值（历史数据）回退展示原始字符串。
 */
@Composable
fun localizedEventUnit(unit: String): String = when (unit) {
    "次" -> stringResource(R.string.lifetime_event_unit_times)
    "顿" -> stringResource(R.string.lifetime_event_unit_meals)
    "篇" -> stringResource(R.string.lifetime_event_unit_entries)
    "本" -> stringResource(R.string.lifetime_event_unit_books)
    "部" -> stringResource(R.string.lifetime_event_unit_movies)
    "集" -> stringResource(R.string.lifetime_event_unit_episodes)
    "公里" -> stringResource(R.string.lifetime_event_unit_km)
    "米" -> stringResource(R.string.lifetime_event_unit_meters)
    "步" -> stringResource(R.string.lifetime_event_unit_steps)
    "分钟" -> stringResource(R.string.lifetime_event_unit_minutes)
    "小时" -> stringResource(R.string.lifetime_event_unit_hours)
    "个" -> stringResource(R.string.lifetime_event_unit_items)
    "杯" -> stringResource(R.string.lifetime_event_unit_cups)
    "瓶" -> stringResource(R.string.lifetime_event_unit_bottles)
    "包" -> stringResource(R.string.lifetime_event_unit_packs)
    "盒" -> stringResource(R.string.lifetime_event_unit_boxes)
    else -> unit
}

/**
 * 倒数日状态标签：今天 / 已过 X 天 / 还有 X 天。
 * 文案集中在资源中，domain 层只产出结构化 [CountdownStatus]。
 */
@Composable
fun countdownStatusLabel(status: CountdownStatus): String = when {
    status.nextOccurrence == null -> "—"
    status.isToday -> stringResource(R.string.lifetime_status_today)
    status.isPast -> stringResource(R.string.lifetime_status_days_past, status.daysUntil)
    else -> stringResource(R.string.lifetime_status_days_until, status.daysUntil)
}

/**
 * 纪念日状态标签：今天 / 已过(还有) X 年/个月/天。
 * 文案集中在资源中，domain 层只产出结构化 [MilestoneStatus]。
 */
@Composable
fun milestoneStatusLabel(status: MilestoneStatus): String {
    return if (status.isReached) {
        when {
            status.daysSince >= 365 ->
                stringResource(R.string.lifetime_status_years_past, status.daysSince / 365)
            status.daysSince >= 30 ->
                stringResource(R.string.lifetime_status_months_past, status.daysSince / 30)
            status.daysSince > 0 ->
                stringResource(R.string.lifetime_status_days_past, status.daysSince)
            else -> stringResource(R.string.lifetime_status_today)
        }
    } else {
        when {
            status.daysUntil > 365 ->
                stringResource(R.string.lifetime_status_years_until, status.daysUntil / 365)
            status.daysUntil > 30 ->
                stringResource(R.string.lifetime_status_months_until, status.daysUntil / 30)
            status.daysUntil > 0 ->
                stringResource(R.string.lifetime_status_days_until, status.daysUntil)
            else -> stringResource(R.string.lifetime_status_today)
        }
    }
}
