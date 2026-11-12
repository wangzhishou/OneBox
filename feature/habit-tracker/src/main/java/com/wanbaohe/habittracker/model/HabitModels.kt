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

package com.wanbaohe.habittracker.model

import com.shifenmiao.database.habit.entity.HabitEntity
import java.time.LocalDate

/** 主页底部 tab */
enum class HabitTab {
    CHECKIN,
    STATS,
}

/** 单个习惯 + 选中日的打卡状态 */
data class HabitWithStatus(
    val habit: HabitEntity,
    val isChecked: Boolean,
)

/** 打卡主页 UI 状态 */
data class CheckInUiState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val currentTab: HabitTab = HabitTab.CHECKIN,
    val isEditMode: Boolean = false,
    val habits: List<HabitWithStatus> = emptyList(),
    val doneCount: Int = 0,
    val dueCount: Int = 0,
)

/** 近 N 天打卡率趋势点 */
data class HabitTrendPoint(
    val epochDay: Long,
    /** 0f..1f */
    val rate: Float,
)

/** 数据 tab UI 状态 */
data class HabitStatsUiState(
    /** 本周一~周日每日打卡率(0f..1f) */
    val weekRates: List<Float> = emptyList(),
    val distributionDone: Int = 0,
    val distributionMissed: Int = 0,
    val distributionNotStarted: Int = 0,
    val totalHabits: Int = 0,
    val trendDays: Int = 7,
    val trendPoints: List<HabitTrendPoint> = emptyList(),
)

/** 新增/编辑页表单状态 */
data class HabitEditUiState(
    val habitId: String? = null,
    val name: String = "",
    val iconKey: String = HabitIcons.DEFAULT_KEY,
    /** null = 自动主题色 */
    val colorArgb: Long? = null,
    val repeatType: String = HabitRepeat.DAILY,
    val repeatTarget: Int = 1,
    val weekdaysMask: Int = 0,
    /** 一天内分钟数,null = 不提醒 */
    val remindMinutes: Int? = null,
    val note: String = "",
    val statsEnabled: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
)
