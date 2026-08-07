package com.shifenmiao.database.idphoto.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.idphoto.entity.IdPhotoSizeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 证件照尺寸 DAO
 */
@Dao
interface IdPhotoSizeDao {

    /**
     * 获取所有尺寸（按更新时间降序）
     */
    @Query("SELECT * FROM id_photo_size ORDER BY updatedAt DESC")
    fun getAllSizes(): Flow<List<IdPhotoSizeEntity>>

    /**
     * 获取所有自定义尺寸（非预设）
     */
    @Query("SELECT * FROM id_photo_size WHERE isPreset = 0 ORDER BY updatedAt DESC")
    fun getCustomSizes(): Flow<List<IdPhotoSizeEntity>>

    /**
     * 获取最近使用的尺寸
     */
    @Query("SELECT * FROM id_photo_size ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentSizes(limit: Int = 20): Flow<List<IdPhotoSizeEntity>>

    /**
     * 根据 ID 获取尺寸
     */
    @Query("SELECT * FROM id_photo_size WHERE id = :id")
    suspend fun getSizeById(id: Long): IdPhotoSizeEntity?

    /**
     * 插入尺寸
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSize(size: IdPhotoSizeEntity): Long

    /**
     * 更新尺寸
     */
    @Update
    suspend fun updateSize(size: IdPhotoSizeEntity)

    /**
     * 删除尺寸
     */
    @Delete
    suspend fun deleteSize(size: IdPhotoSizeEntity)

    /**
     * 根据 ID 删除尺寸
     */
    @Query("DELETE FROM id_photo_size WHERE id = :id")
    suspend fun deleteSizeById(id: Long)

    /**
     * 删除所有尺寸
     */
    @Query("DELETE FROM id_photo_size")
    suspend fun deleteAllSizes()

    /**
     * 删除所有预置尺寸
     */
    @Query("DELETE FROM id_photo_size WHERE isPreset = 1")
    suspend fun deletePresetSizes()

    /**
     * 更新尺寸的更新时间（用于记录最近使用）
     */
    @Query("UPDATE id_photo_size SET updatedAt = :time WHERE id = :id")
    suspend fun updateSizeTime(id: Long, time: Long = System.currentTimeMillis())

    /**
     * 检查是否有数据
     */
    @Query("SELECT COUNT(*) FROM id_photo_size")
    suspend fun getCount(): Int
}

