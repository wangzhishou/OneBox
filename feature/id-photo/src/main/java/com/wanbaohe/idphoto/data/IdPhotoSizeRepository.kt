package com.wanbaohe.idphoto.data

import com.shifenmiao.database.idphoto.dao.IdPhotoSizeDao
import com.shifenmiao.database.idphoto.entity.IdPhotoSizeEntity
import com.wanbaohe.idphoto.domain.IdPhotoSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 证件照尺寸仓库
 */
@Singleton
class IdPhotoSizeRepository @Inject constructor(
    private val dao: IdPhotoSizeDao
) {
    /**
     * 获取所有尺寸（作为 Flow）
     */
    fun getAllSizes(): Flow<List<IdPhotoSize>> {
        return dao.getAllSizes().map { entities ->
            entities.map { it.toIdPhotoSize() }
        }
    }

    /**
     * 获取最近使用的尺寸
     */
    fun getRecentSizes(limit: Int = 20): Flow<List<IdPhotoSize>> {
        return dao.getRecentSizes(limit).map { entities ->
            entities.map { it.toIdPhotoSize() }
        }
    }

    /**
     * 初始化预置尺寸（仅首次启动时）
     */
    suspend fun initPresetsIfNeeded() {
        val count = dao.getCount()
        if (count == 0) {
            // 插入预置尺寸
            IdPhotoSize.PRESETS.forEach { preset ->
                dao.insertSize(preset.toEntity().copy(isPreset = true))
            }
        }
    }

    /**
     * 恢复默认预置尺寸（删除所有预置，重新插入）
     */
    suspend fun resetPresetsOnly() {
        dao.deletePresetSizes()
        IdPhotoSize.PRESETS.forEach { preset ->
            dao.insertSize(preset.toEntity().copy(isPreset = true))
        }
    }

    /**
     * 保存或更新尺寸
     */
    suspend fun saveOrUpdate(size: IdPhotoSize): Long {
        return if (size.id > 0) {
            dao.updateSize(size.toEntity())
            size.id
        } else {
            dao.insertSize(size.toEntity())
        }
    }

    /**
     * 删除尺寸
     */
    suspend fun deleteSize(id: Long) {
        dao.deleteSizeById(id)
    }

    /**
     * 批量删除尺寸
     */
    suspend fun deleteSizes(ids: List<Long>) {
        ids.forEach { dao.deleteSizeById(it) }
    }

    /**
     * 记录尺寸使用（更新时间）
     */
    suspend fun recordSizeUsage(id: Long) {
        dao.updateSizeTime(id)
    }

    /**
     * 根据 ID 获取尺寸
     */
    suspend fun getSizeById(id: Long): IdPhotoSize? {
        return dao.getSizeById(id)?.toIdPhotoSize()
    }
}

/**
 * Entity 转换为 Domain 模型
 */
private fun IdPhotoSizeEntity.toIdPhotoSize(): IdPhotoSize {
    return IdPhotoSize(
        id = id,
        name = name,
        widthMm = widthMm,
        heightMm = heightMm,
        widthPx = widthPx,
        heightPx = heightPx,
        description = description,
        isPreset = isPreset,
        createdAt = createdAt,
    )
}

/**
 * Domain 模型转换为 Entity
 */
private fun IdPhotoSize.toEntity(): IdPhotoSizeEntity {
    return IdPhotoSizeEntity(
        id = id,
        name = name,
        widthMm = widthMm,
        heightMm = heightMm,
        widthPx = widthPx,
        heightPx = heightPx,
        description = description,
        isPreset = isPreset,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
    )
}

