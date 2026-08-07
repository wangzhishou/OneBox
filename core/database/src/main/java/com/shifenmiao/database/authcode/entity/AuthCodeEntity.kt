package com.shifenmiao.database.authcode.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 全局授权码持久化实体。
 *
 * 全局仅保留一条记录 ([SINGLETON_ID]),[codeHash] 与 [salt] 配合做 SHA-256 校验:
 * - [codeHash] = SHA-256(salt + code)
 * - [salt] 为首次设置时随机生成,防止明文 / 哈希撞库
 *
 * 表为单行 (SINGLETON_ID),修改授权码时直接 REPLACE 即可。
 */
@Entity(
    tableName = "auth_code",
)
data class AuthCodeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String = SINGLETON_ID,
    @ColumnInfo(name = "code_hash") val codeHash: String,
    @ColumnInfo(name = "salt") val salt: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID: String = "auth_code_singleton"
    }
}
