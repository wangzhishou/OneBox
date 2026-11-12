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

package com.shifenmiao.ai.repository

import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.ChatPrompt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPromptRepository @Inject constructor(
    private val appDatabase: AppDatabase,
) : PromptRepository {

    override suspend fun getPromptRecordById(id: Int): PromptRecord? {
        return appDatabase.chatPromptDao().getPromptById(id)?.let { entity ->
            PromptRecord(
                id = entity.id,
                remoteId = entity.remoteId,
                documentId = entity.documentId,
                prompt = DataBaseUtils.promptEntityToPrompt(entity),
                source = entity.source,
                updatedAtMillis = entity.updatedAt,
                isSystemPrompt = entity.isSystemPreset(),
            )
        }
    }

    override suspend fun getPromptRecordByItemId(itemId: Int): PromptRecord? {
        // 通过 link 表查本地资源行
        val promptId = appDatabase.chatPromptDao().getPromptLinkByItemId(itemId) ?: return null
        return appDatabase.chatPromptDao().getPromptById(promptId)?.let { entity ->
            PromptRecord(
                id = entity.id,
                remoteId = entity.remoteId,
                documentId = entity.documentId,
                prompt = DataBaseUtils.promptEntityToPrompt(entity),
                source = entity.source,
                updatedAtMillis = entity.updatedAt,
                isSystemPrompt = entity.isSystemPreset(),
            )
        }
    }

    override suspend fun upsertPrompt(prompt: ChatPrompt, source: Source): Int {
        val entity = DataBaseUtils.promptToPromptEntity(prompt, source = source)
        return if (source == Source.REMOTE) {
            appDatabase.chatPromptDao().upsertRemotePrompt(entity)
        } else {
            appDatabase.chatPromptDao().upsertLocalPrompt(entity)
        }
    }

    override suspend fun upsertPromptAtItemId(
        prompt: ChatPrompt,
        itemId: Int,
        source: Source,
    ): Int {
        val entity = DataBaseUtils.promptToPromptEntity(
            prompt.copy(id = itemId),
            source = source,
        )
        return if (source == Source.REMOTE) {
            appDatabase.chatPromptDao().upsertRemotePrompt(entity)
        } else {
            appDatabase.chatPromptDao().upsertLocalPrompt(entity)
        }
    }
}
