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

package com.shifenmiao.lifetime.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class PersonalMilestone(
    val id: Long = 0,
    val name: String,
    val iconKey: String = "EmojiEvents",
    val targetDate: LocalDate? = null,
    val targetDays: Long? = null,
    val startDate: LocalDate? = null,
    val note: String? = null,
    val color: String? = null,
    val sortOrder: Int = 999,
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
data class MilestoneStatus(
    val milestone: PersonalMilestone,
    val isReached: Boolean,
    val daysUntil: Long,
    val daysSince: Long,
    val progress: Float,
)
