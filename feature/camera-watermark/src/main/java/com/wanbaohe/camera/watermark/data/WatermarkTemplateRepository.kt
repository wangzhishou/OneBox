package com.wanbaohe.camera.watermark.data

import com.shifenmiao.database.watermark.dao.WatermarkTemplateDao
import com.shifenmiao.database.watermark.entity.WatermarkTemplateEntity
import com.wanbaohe.camera.watermark.domain.LogoType
import com.wanbaohe.camera.watermark.domain.WatermarkContent
import com.wanbaohe.camera.watermark.domain.WatermarkStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 水印模板仓库
 * 管理模板的持久化存储，包括预置模板
 */
@Singleton
class WatermarkTemplateRepository @Inject constructor(
    private val dao: WatermarkTemplateDao
) {

    /**
     * 初始化预置模板（首次启动时调用）
     * 如果数据库为空，则插入预置模板
     */
    suspend fun initPresetsIfNeeded() {
        val existingTemplates = dao.getAllTemplates().first()
        if (existingTemplates.isEmpty()) {
            // 数据库为空，插入预置模板
            getPresetTemplates().forEach { preset ->
                dao.insertTemplate(preset.toEntity().copy(isPreset = true))
            }
        }
    }

    /**
     * 恢复默认预置模板
     * 删除所有模板，重新插入预置模板
     */
    suspend fun resetToDefaults() {
        dao.deleteAllTemplates()
        getPresetTemplates().forEach { preset ->
            dao.insertTemplate(preset.toEntity().copy(isPreset = true))
        }
    }

    /**
     * 仅恢复预置模板的默认值
     * 删除所有预置模板，重新插入默认预置模板
     * 用户新增的模板不受影响
     */
    suspend fun resetPresetsOnly() {
        // 删除所有预置模板
        dao.deletePresetTemplates()
        // 重新插入默认预置模板
        getPresetTemplates().forEach { preset ->
            dao.insertTemplate(preset.toEntity().copy(isPreset = true))
        }
    }

    /**
     * 获取所有模板
     */
    fun getAllTemplates(): Flow<List<WatermarkStyle>> {
        return dao.getAllTemplates().map { entities ->
            entities.map { it.toWatermarkStyle() }
        }
    }

    /**
     * 获取最近使用的模板（按更新时间排序）
     */
    fun getRecentTemplates(limit: Int = 50): Flow<List<WatermarkStyle>> {
        return dao.getRecentTemplates(limit).map { entities ->
            entities.map { it.toWatermarkStyle() }
        }
    }

    /**
     * 保存新模板
     */
    suspend fun saveTemplate(style: WatermarkStyle): Long {
        return dao.insertTemplate(style.toEntity())
    }

    /**
     * 更新模板
     */
    suspend fun updateTemplate(style: WatermarkStyle) {
        dao.updateTemplate(style.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * 保存或更新模板
     */
    suspend fun saveOrUpdate(style: WatermarkStyle): WatermarkStyle {
        val existing = dao.getTemplateById(style.id)
        return if (existing != null) {
            // 更新现有模板
            dao.updateTemplate(style.toEntity().copy(updatedAt = System.currentTimeMillis()))
            style
        } else {
            // 新建模板
            val newId = dao.insertTemplate(style.toEntity())
            style.copy(id = newId)
        }
    }

    /**
     * 删除模板
     */
    suspend fun deleteTemplate(id: Long) {
        dao.deleteTemplateById(id)
    }

    /**
     * 批量删除模板
     */
    suspend fun deleteTemplates(ids: List<Long>) {
        ids.forEach { dao.deleteTemplateById(it) }
    }

    /**
     * 记录模板使用（更新时间）
     */
    suspend fun recordTemplateUsage(id: Long) {
        dao.updateTemplateTime(id)
    }

    /**
     * 根据 ID 获取模板
     */
    suspend fun getTemplateById(id: Long): WatermarkStyle? {
        return dao.getTemplateById(id)?.toWatermarkStyle()
    }

    /**
     * 获取预置模板列表（内存中的默认值）
     */
    private fun getPresetTemplates(): List<WatermarkStyle> = listOf(
        WatermarkStyle(
            name = "徕卡经典",
            logoType = LogoType.LEICA,
        ),
        WatermarkStyle(
            name = "万宝盒",
            logoType = LogoType.WANBAOHE,
        ),
        WatermarkStyle(
            name = "Apple",
            logoType = LogoType.APPLE,
        ),
        WatermarkStyle(
            name = "Google Pixel",
            logoType = LogoType.GOOGLE,
        ),
        WatermarkStyle(
            name = "华为",
            logoType = LogoType.HUAWEI,
        ),
        WatermarkStyle(
            name = "OPPO",
            logoType = LogoType.OPPO,
        ),
        WatermarkStyle(
            name = "vivo",
            logoType = LogoType.VIVO,
        ),
        WatermarkStyle(
            name = "小米",
            logoType = LogoType.XIAOMI,
        ),
    )
}

/**
 * Entity 转换为 Domain 模型
 */
private fun WatermarkTemplateEntity.toWatermarkStyle(): WatermarkStyle {
    return WatermarkStyle(
        id = id,
        name = name,
        backgroundColor = backgroundColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        logoType = LogoType.valueOf(logoType),
        customLogoPath = customLogoPath,
        showDivider = showDivider,
        dividerColor = dividerColor,
        watermarkHeight = watermarkHeight,
        paddingHorizontal = paddingHorizontal,
        paddingVertical = paddingVertical,
        primaryFontSize = primaryFontSize,
        secondaryFontSize = secondaryFontSize,
        createdAt = createdAt,
        isPreset = isPreset,
        customContent = WatermarkContent(
            topLeft = customTopLeft,
            topRight = customTopRight,
            bottomLeft = customBottomLeft,
            bottomRight = customBottomRight,
        ),
    )
}

/**
 * Domain 模型转换为 Entity
 */
private fun WatermarkStyle.toEntity(): WatermarkTemplateEntity {
    return WatermarkTemplateEntity(
        id = id,
        name = name,
        backgroundColor = backgroundColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        logoType = logoType.name,
        customLogoPath = customLogoPath,
        showDivider = showDivider,
        dividerColor = dividerColor,
        watermarkHeight = watermarkHeight,
        paddingHorizontal = paddingHorizontal,
        paddingVertical = paddingVertical,
        primaryFontSize = primaryFontSize,
        secondaryFontSize = secondaryFontSize,
        customTopLeft = customContent.topLeft,
        customTopRight = customContent.topRight,
        customBottomLeft = customContent.bottomLeft,
        customBottomRight = customContent.bottomRight,
        isPreset = isPreset,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
    )
}

