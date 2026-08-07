package com.shifenmiao.database.bookkeeping.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookkeeping_record",
    foreignKeys = [
        ForeignKey(
            entity = BookkeepingCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["happened_at"]),
        Index(value = ["category_id"]),
        Index(value = ["type"])
    ]
)
data class BookkeepingRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "happened_at") val happenedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "exclude_from_stats") val excludeFromStats: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

