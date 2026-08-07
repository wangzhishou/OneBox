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
