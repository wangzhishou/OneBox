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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 里程碑 AI 文案历史。
 *
 * 每次进入里程碑详情页都会生成一条新文案，自动追加到此表。
 * 通过 [milestoneId] 关联到 [PersonalMilestoneEntity]，删除里程碑时级联清理。
 */
@Entity(
    tableName = "milestone_ai_insights",
    foreignKeys = [
        ForeignKey(
            entity = PersonalMilestoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["milestoneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("milestoneId")]
)
data class MilestoneAiInsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val milestoneId: Long,

    val content: String,

    val generatedAt: Long = System.currentTimeMillis()
)
