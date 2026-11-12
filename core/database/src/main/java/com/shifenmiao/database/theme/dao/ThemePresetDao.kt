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

package com.shifenmiao.database.theme.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.theme.entity.ThemePresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemePresetDao {

    @Query("SELECT * FROM theme_preset ORDER BY created_at ASC")
    fun observeAll(): Flow<List<ThemePresetEntity>>

    @Query("SELECT * FROM theme_preset ORDER BY created_at ASC")
    suspend fun getAll(): List<ThemePresetEntity>

    @Query("SELECT * FROM theme_preset WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ThemePresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ThemePresetEntity)

    @Update
    suspend fun update(entity: ThemePresetEntity)

    @Query("DELETE FROM theme_preset WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM theme_preset")
    suspend fun count(): Int
}

