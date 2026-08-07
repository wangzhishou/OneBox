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
