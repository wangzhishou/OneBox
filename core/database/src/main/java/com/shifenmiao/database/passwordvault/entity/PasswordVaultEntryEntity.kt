package com.shifenmiao.database.passwordvault.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 密码保险箱条目实体。
 *
 * 所有敏感字段（密码、备注等）均以加密后的字节数组形式存储。
 * 加密密钥由用户在运行时输入的主密码通过 PBKDF2 派生，不会持久化。
 */
@Entity(
    tableName = "password_vault_entry",
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["updated_at"])
    ]
)
data class PasswordVaultEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "account") val account: String? = null,
    @ColumnInfo(name = "encrypted_payload") val encryptedPayload: ByteArray,
    @ColumnInfo(name = "salt") val salt: ByteArray,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasswordVaultEntryEntity

        if (id != other.id) return false
        if (title != other.title) return false
        if (categoryId != other.categoryId) return false
        if (account != other.account) return false
        if (!encryptedPayload.contentEquals(other.encryptedPayload)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + categoryId.hashCode()
        result = 31 * result + (account?.hashCode() ?: 0)
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}
