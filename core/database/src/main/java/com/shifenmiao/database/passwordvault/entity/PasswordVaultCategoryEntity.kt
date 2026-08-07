package com.shifenmiao.database.passwordvault.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 密码保险箱分类实体。
 *
 * 分类名称本身不属于敏感数据（数据库其他字段不加密），但条目的 [PasswordVaultEntryEntity.categoryId]
 * 通过外键语义关联到本表，修改分类名称后旧条目会自动反映新名称。
 */
@Entity(
    tableName = "password_vault_category",
    indices = [Index(value = ["sort_order"])]
)
data class PasswordVaultCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
