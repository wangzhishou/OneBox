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
