package com.shifenmiao.database.idphoto.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 证件照尺寸模板实体
 */
@Entity(tableName = "id_photo_size")
data class IdPhotoSizeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                           // 名称
    val widthMm: Float,                         // 宽度（毫米）
    val heightMm: Float,                        // 高度（毫米）
    val widthPx: Int,                           // 宽度（像素）
    val heightPx: Int,                          // 高度（像素）
    val description: String = "",               // 描述
    val isPreset: Boolean = false,              // 是否为预设
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

