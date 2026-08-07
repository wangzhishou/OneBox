package com.shifenmiao.database.authcode.repo

import com.shifenmiao.database.authcode.dao.AuthCodeDao
import com.shifenmiao.database.authcode.entity.AuthCodeEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 授权码表仓库。
 *
 * - [save] 写入或替换单行记录
 * - [get] / [hasCode] 用于判断是否已设置 / 读取已存的 hash + salt
 * - [delete] 清空授权码(回到未设置状态)
 */
@Singleton
class AuthCodeRepository @Inject constructor(
    private val dao: AuthCodeDao,
) {
    suspend fun save(entity: AuthCodeEntity) = dao.upsert(entity)

    suspend fun get(): AuthCodeEntity? = dao.getById()

    suspend fun hasCode(): Boolean = dao.count() > 0

    suspend fun delete() = dao.deleteAll()
}
