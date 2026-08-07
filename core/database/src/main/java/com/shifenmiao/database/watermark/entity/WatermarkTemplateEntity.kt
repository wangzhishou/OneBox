package com.shifenmiao.database.watermark.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 水印模板历史记录实体
 */
@Entity(tableName = "watermark_template")
data class WatermarkTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                           // 模板名称
    val backgroundColor: Long,                   // 背景色
    val primaryTextColor: Long,                  // 主文字颜色
    val secondaryTextColor: Long,                // 次要文字颜色
    val logoType: String,                        // Logo 类型
    val customLogoPath: String? = null,          // 自定义 Logo 路径
    val showDivider: Boolean = true,             // 是否显示分隔线
    val dividerColor: Long,                      // 分隔线颜色
    val watermarkHeight: Int = 120,              // 水印高度
    val paddingHorizontal: Int = 24,             // 水平内边距
    val paddingVertical: Int = 16,               // 垂直内边距
    val primaryFontSize: Int = 35,               // 主文字大小
    val secondaryFontSize: Int = 22,             // 次要文字大小
    val showGps: Boolean = true,                 // 是否显示 GPS
    val showDateTime: Boolean = true,            // 是否显示时间
    val showParams: Boolean = true,              // 是否显示拍摄参数
    // 自定义内容字段（为 null 时使用 EXIF 元数据）
    val customTopLeft: String? = null,           // 左上角自定义内容
    val customTopRight: String? = null,          // 右上角自定义内容
    val customBottomLeft: String? = null,        // 左下角自定义内容
    val customBottomRight: String? = null,       // 右下角自定义内容
    val isPreset: Boolean = false,               // 是否为预设模板
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

