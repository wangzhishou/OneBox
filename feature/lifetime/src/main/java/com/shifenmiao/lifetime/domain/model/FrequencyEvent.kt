package com.shifenmiao.lifetime.domain.model

import androidx.compose.runtime.Immutable

enum class FrequencyType {
    ONE_TIME,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

@Immutable
data class FrequencyEvent(
    val id: Long = 0,
    val name: String,
    val iconKey: String,
    val frequencyType: FrequencyType,
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

@Immutable
data class FrequencyEventStats(
    val event: FrequencyEvent,
    val completedCount: Long,
    val remainingCount: Long,
    val yearsSinceEvent: Long = 0,
    val daysSinceEvent: Long = 0
)
