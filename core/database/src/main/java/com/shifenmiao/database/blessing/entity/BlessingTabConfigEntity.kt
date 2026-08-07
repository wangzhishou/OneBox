package com.shifenmiao.database.blessing.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 祈福 tab 文案自定义快照。
 * 每次编辑写入编辑发生当天（[date]）的一条记录；
 * 某日期生效的文案 = date 不晚于该日期的最近一条快照。
 */
@Entity(
    tableName = "blessing_tab_config",
    primaryKeys = ["date", "type"],
)
data class BlessingTabConfigEntity(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "subtitle") val subtitle: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
