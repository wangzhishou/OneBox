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
import androidx.room.PrimaryKey

@Entity(tableName = "frequency_events")
data class FrequencyEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val iconKey: String,

    val frequencyType: String,

    val timesPerPeriod: Int,

    val unit: String = "次",

    val specificDate: Long? = null,

    val sortOrder: Int = 999,

    val color: String? = null,

    val isEnabled: Boolean = true,

    val isPreset: Boolean = false,

    val isRecommended: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
