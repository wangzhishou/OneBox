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

package com.shifenmiao.database.ai.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.ai.entity.ToolBindingEntity

@Dao
interface ToolBindingDao {

    @Query(
        """
        SELECT tool_name
        FROM tool_binding
        WHERE owner_type = :ownerType AND owner_id = :ownerId
        ORDER BY sort_order ASC, tool_name ASC
        """
    )
    suspend fun getToolNames(ownerType: String, ownerId: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bindings: List<ToolBindingEntity>)

    @Query("DELETE FROM tool_binding WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun deleteByOwner(ownerType: String, ownerId: Int)
}
