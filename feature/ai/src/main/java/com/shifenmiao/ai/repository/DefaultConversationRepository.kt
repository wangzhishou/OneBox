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
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.model.ai.Conversation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultConversationRepository @Inject constructor(
    private val appDatabase: AppDatabase,
) : ConversationRepository {

    override suspend fun getConversationByConversationId(conversationId: String): Conversation? {
        return appDatabase.conversationDao()
            .getConversationByConversationId(conversationId)
            ?.let(ConversationEntity::toConversation)
    }

    override suspend fun updateConversationTitle(
        conversationId: String,
        title: String,
        titleSource: String,
    ): Int {
        return appDatabase.conversationDao().updateTitle(
            conversationId = conversationId,
            title = title,
            titleSource = titleSource,
        )
    }
}

