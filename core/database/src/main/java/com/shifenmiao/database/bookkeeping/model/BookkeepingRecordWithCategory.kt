package com.shifenmiao.database.bookkeeping.model

import androidx.room.ColumnInfo

data class BookkeepingRecordWithCategory(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "happened_at") val happenedAt: Long,
    @ColumnInfo(name = "exclude_from_stats") val excludeFromStats: Boolean,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_icon_key") val categoryIconKey: String?
)

data class BookkeepingCategorySum(
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_icon_key") val categoryIconKey: String?,
    @ColumnInfo(name = "total_cents") val totalCents: Long
)

data class BookkeepingTimeTotal(
    @ColumnInfo(name = "time_key") val timeKey: String,
    @ColumnInfo(name = "total_cents") val totalCents: Long
)

