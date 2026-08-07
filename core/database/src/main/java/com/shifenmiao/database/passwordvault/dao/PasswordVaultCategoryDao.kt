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
