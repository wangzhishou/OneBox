/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.database.chat_prompt

import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.Source
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SystemPromptRepository @Inject constructor(appDatabase: AppDatabase) {
    private val promptDao = appDatabase.chatPromptDao()
    private val creationMutex = Mutex()

    fun observeSystemPrompts(): Flow<List<PromptEntity>> =
        promptDao.getPromptsBySourceFlow(Source.SYSTEM)

    suspend fun getById(id: Int): PromptEntity? = promptDao.getPromptById(id)

    suspend fun getOrCreate(
        key: String,
        title: String,
        description: String,
        defaultContent: String,
    ): PromptEntity = creationMutex.withLock {
        promptDao.getSystemPromptByKey(key)?.let { return@withLock it }
        val prompt = PromptEntity(
            source = Source.SYSTEM,
            title = title,
            description = description,
            prompt = defaultContent.trim(),
            placeholder = key,
        )
        val id = promptDao.upsertLocalPrompt(prompt)
        prompt.copy(id = id)
    }

    suspend fun updateContent(id: Int, content: String): PromptEntity {
        val current = getById(id) ?: throw IllegalArgumentException("Unknown system prompt: $id")
        val updated = current.copy(
            prompt = content,
            updatedAt = System.currentTimeMillis(),
        )
        promptDao.upsertLocalPrompt(updated)
        return updated
    }
}
