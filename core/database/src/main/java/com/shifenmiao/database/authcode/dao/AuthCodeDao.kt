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

package com.shifenmiao.database.authcode.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.authcode.entity.AuthCodeEntity

@Dao
interface AuthCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AuthCodeEntity)

    @Query("SELECT * FROM auth_code WHERE id = :id LIMIT 1")
    suspend fun getById(id: String = AuthCodeEntity.SINGLETON_ID): AuthCodeEntity?

    @Query("SELECT COUNT(*) FROM auth_code")
    suspend fun count(): Int

    @Query("DELETE FROM auth_code")
    suspend fun deleteAll()
}
