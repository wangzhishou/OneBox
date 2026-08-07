package com.shifenmiao.database.passwordvault.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.passwordvault.entity.PasswordVaultEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordVaultEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PasswordVaultEntryEntity)

    @Query("SELECT * FROM password_vault_entry ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<PasswordVaultEntryEntity>>

    @Query("SELECT * FROM password_vault_entry ORDER BY updated_at DESC")
    suspend fun getAll(): List<PasswordVaultEntryEntity>

    @Query("SELECT * FROM password_vault_entry WHERE id = :entryId LIMIT 1")
    suspend fun getById(entryId: String): PasswordVaultEntryEntity?

    @Query("DELETE FROM password_vault_entry WHERE id = :entryId")
    suspend fun deleteById(entryId: String)

    @Query("DELETE FROM password_vault_entry")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM password_vault_entry")
    suspend fun count(): Int
}
