/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.database.idphoto.dao

import androidx.room.Dao
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
     * 根据 ID 删除尺寸
     */
    @Query("DELETE FROM id_photo_size WHERE id = :id")
    suspend fun deleteSizeById(id: Long)

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
     * 预置尺寸条数（判断库里播的是哪一版预置目录，见 IdPhotoSizeRepository）
     */
    @Query("SELECT COUNT(*) FROM id_photo_size WHERE isPreset = 1")
    suspend fun getPresetCount(): Int
}
