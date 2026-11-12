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

import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.ChatPrompt

interface PromptRepository {
    suspend fun getPromptRecordById(id: Int): PromptRecord?

    /** 通过 itemId 查询（资源表 1:1 关联后专用）。 */
    suspend fun getPromptRecordByItemId(itemId: Int): PromptRecord?

    suspend fun upsertPrompt(
        prompt: ChatPrompt,
        source: Source = prompt.source ?: Source.REMOTE
    ): Int

    /** 1:1 关联下，把 prompt 写入指定 itemId 对应的位置（PK 复用 itemId）。 */
    suspend fun upsertPromptAtItemId(
        prompt: ChatPrompt,
        itemId: Int,
        source: Source = prompt.source ?: Source.REMOTE
    ): Int
}

data class PromptRecord(
    val id: Int,
    val remoteId: Int?,
    /** Strapi v5 documentId；远端回查 / 刷新定位优先于 [remoteId]。 */
    val documentId: String? = null,
    val prompt: ChatPrompt,
    val source: Source,
    val updatedAtMillis: Long,
    val isSystemPrompt: Boolean,
)

