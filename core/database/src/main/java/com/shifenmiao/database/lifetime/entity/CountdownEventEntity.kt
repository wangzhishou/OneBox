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

package com.shifenmiao.database.lifetime.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 倒数日实体。
 *
 * - [targetDate] 存储为 epochDay(Long)，仅在 [isLunarTarget] = false 时使用
 * - [lunarMonth] / [lunarDay] 仅在 [isLunarTarget] = true 时使用，跨年自动滚动到下一年
 * - [isFromHoliday] 标识是否由节日兜底生成；用户可以删除但不能再 "恢复"
 */
@Entity(
    tableName = "countdown_events",
    indices = [Index("targetDate"), Index("isPreset")]
)
data class CountdownEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val iconKey: String = "Event",

    /** epochDay (days since 1970-01-01)；公历倒计时使用 */
    val targetDate: Long? = null,

    val isLunarTarget: Boolean = false,

    val lunarMonth: Int? = null,

    val lunarDay: Int? = null,

    val note: String? = null,

    val color: String? = null,

    val sortOrder: Int = 999,

    val isPreset: Boolean = false,

    val isFromHoliday: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
