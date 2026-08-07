package com.shifenmiao.database.lifetime.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_milestones")
data class PersonalMilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val iconKey: String = "EmojiEvents",

    val targetDate: Long? = null,

    val targetDays: Long? = null,

    val startDate: Long? = null,

    val note: String? = null,

    val color: String? = null,

    val sortOrder: Int = 999,

    val createdAt: Long = System.currentTimeMillis()
)
