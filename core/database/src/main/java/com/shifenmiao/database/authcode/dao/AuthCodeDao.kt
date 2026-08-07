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
