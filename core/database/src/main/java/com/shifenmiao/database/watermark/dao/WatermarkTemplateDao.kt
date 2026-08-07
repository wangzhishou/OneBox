package com.shifenmiao.database.watermark.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.watermark.entity.WatermarkTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * 水印模板 DAO
 */
@Dao
interface WatermarkTemplateDao {

    /**
     * 获取所有模板（按更新时间降序）
     */
    @Query("SELECT * FROM watermark_template ORDER BY updatedAt DESC")
    fun getAllTemplates(): Flow<List<WatermarkTemplateEntity>>

    /**
     * 获取所有自定义模板（非预设）
     */
    @Query("SELECT * FROM watermark_template WHERE isPreset = 0 ORDER BY updatedAt DESC")
    fun getCustomTemplates(): Flow<List<WatermarkTemplateEntity>>

    /**
     * 获取最近使用的模板
     */
    @Query("SELECT * FROM watermark_template ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentTemplates(limit: Int = 10): Flow<List<WatermarkTemplateEntity>>

    /**
     * 根据 ID 获取模板
     */
    @Query("SELECT * FROM watermark_template WHERE id = :id")
    suspend fun getTemplateById(id: Long): WatermarkTemplateEntity?

    /**
     * 插入模板
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WatermarkTemplateEntity): Long

    /**
     * 更新模板
     */
    @Update
    suspend fun updateTemplate(template: WatermarkTemplateEntity)

    /**
     * 删除模板
     */
    @Delete
    suspend fun deleteTemplate(template: WatermarkTemplateEntity)

    /**
     * 根据 ID 删除模板
     */
    @Query("DELETE FROM watermark_template WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)

    /**
     * 删除所有模板
     */
    @Query("DELETE FROM watermark_template")
    suspend fun deleteAllTemplates()

    /**
     * 删除所有预置模板（isPreset = true）
     */
    @Query("DELETE FROM watermark_template WHERE isPreset = 1")
    suspend fun deletePresetTemplates()

    /**
     * 更新模板的更新时间（用于记录最近使用）
     */
    @Query("UPDATE watermark_template SET updatedAt = :time WHERE id = :id")
    suspend fun updateTemplateTime(id: Long, time: Long = System.currentTimeMillis())
}

