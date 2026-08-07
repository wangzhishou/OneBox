package com.shifenmiao.database.theme.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户自建主题持久化实体。
 *
 * 内置主题不入库，仅用户通过"新建主题"创建的主题写入此表。
 */
@Entity(tableName = "theme_preset")
data class ThemePresetEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color_tuple") val colorTupleString: String,
    @ColumnInfo(name = "night_mode") val nightMode: Int = 2,
    @ColumnInfo(name = "is_dynamic_colors") val isDynamicColors: Boolean = false,
    @ColumnInfo(name = "is_glassmorphism_enabled") val isGlassmorphismEnabled: Boolean = true,
    @ColumnInfo(name = "is_liquid_glass_enabled") val isLiquidGlassEnabled: Boolean = false,
    @ColumnInfo(name = "is_mesh_gradient_bg_enabled") val isMeshGradientBackgroundEnabled: Boolean = true,
    @ColumnInfo(name = "gradient_bg_style") val gradientBackgroundStyle: Int = 3, // Sunset ordinal
    @ColumnInfo(name = "glass_base_alpha") val glassBaseAlpha: Float = 1.0f,
    @ColumnInfo(name = "custom_bg_image_uri") val customBackgroundImageUri: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

