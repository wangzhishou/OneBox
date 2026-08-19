package com.shifenmiao.database.poem.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poem",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["isFavorite"])
    ]
)
data class PoemEntity(
    /** 服务端诗词 id */
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "title") val title: String,
    /** 诗句 JSON 数组字符串 */
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "dynasty") val dynasty: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "aiInsight") val aiInsight: String? = null,
    /** AI 生成的逐字拼音:每句一行,行内空格分隔 */
    @ColumnInfo(name = "pinyin") val pinyin: String? = null,
    /** AI 生成的现代汉语翻译 */
    @ColumnInfo(name = "translation") val translation: String? = null,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
)
