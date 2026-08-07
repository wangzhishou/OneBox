package com.shifenmiao.database.teleprompter.repo

import com.shifenmiao.database.teleprompter.dao.TeleprompterScriptDao
import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeleprompterRepository @Inject constructor(
    private val dao: TeleprompterScriptDao,
) {

    fun observeAll(): Flow<List<TeleprompterScriptEntity>> = dao.observeAll()

    suspend fun getById(id: String): TeleprompterScriptEntity? = dao.getById(id)

    suspend fun upsert(entity: TeleprompterScriptEntity) = dao.upsert(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}

