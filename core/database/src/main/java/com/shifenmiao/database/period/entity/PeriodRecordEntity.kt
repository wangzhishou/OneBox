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

package com.shifenmiao.database.period.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "period_record",
    indices = [
        Index(value = ["record_date"]),
        Index(value = ["is_period_start"]),
    ]
)
data class PeriodRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    /** LocalDate 存 epochDay */
    @ColumnInfo(name = "record_date") val recordDate: Long,
    @ColumnInfo(name = "is_period_start") val isPeriodStart: Boolean = false,
    @ColumnInfo(name = "is_period_end") val isPeriodEnd: Boolean = false,
    /** NONE / LIGHT / MEDIUM / HEAVY */
    @ColumnInfo(name = "flow_intensity") val flowIntensity: String = "NONE",
    /** 逗号分隔的症状枚举名，空串 = 无症状 */
    @ColumnInfo(name = "symptoms") val symptoms: String = "",
    /** VERY_HAPPY / HAPPY / NORMAL / SAD / VERY_SAD */
    @ColumnInfo(name = "mood") val mood: String = "NORMAL",
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
