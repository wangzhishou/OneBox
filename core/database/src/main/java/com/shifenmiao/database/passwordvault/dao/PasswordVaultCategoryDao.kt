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

package com.shifenmiao.database.passwordvault.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.passwordvault.entity.PasswordVaultCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordVaultCategoryDao {

    @Query("SELECT COUNT(*) FROM password_vault_category")
    suspend fun count(): Int

    @Query("SELECT * FROM password_vault_category ORDER BY sort_order ASC, created_at ASC")
    fun observeAll(): Flow<List<PasswordVaultCategoryEntity>>

    @Query("SELECT * FROM password_vault_category ORDER BY sort_order ASC, created_at ASC")
    suspend fun getAll(): List<PasswordVaultCategoryEntity>

    @Query("SELECT * FROM password_vault_category WHERE id = :categoryId LIMIT 1")
    suspend fun getById(categoryId: String): PasswordVaultCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PasswordVaultCategoryEntity)

    @Update
    suspend fun update(entity: PasswordVaultCategoryEntity)

    @Query("UPDATE password_vault_category SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :categoryId")
    suspend fun updateSortOrder(
        categoryId: String,
        sortOrder: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM password_vault_category WHERE id = :categoryId AND is_default = 0")
    suspend fun deleteCustomById(categoryId: String)

    @Query("DELETE FROM password_vault_category")
    suspend fun deleteAll()
}
