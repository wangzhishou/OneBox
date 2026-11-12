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

package com.wanbaohe.teleprompter.service

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity
import com.shifenmiao.database.teleprompter.repo.TeleprompterRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.teleprompter.R
import com.wanbaohe.teleprompter.util.countScriptWords
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * 提词器文稿业务服务
 *
 * 承载文稿的校验、字数统计与 CRUD 编排，供页面 Component 与 AI 工具共用。
 * 写操作成功后自动记录活动日志（「历史」Tab 时间线可见）。
 */
@Singleton
class TeleprompterService @Inject constructor(
    private val repository: TeleprompterRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    fun observeScripts(): Flow<List<TeleprompterScriptEntity>> = repository.observeAll()

    suspend fun getScript(id: String): Result<TeleprompterScriptEntity> = runCatching {
        repository.getById(id) ?: throw NoSuchElementException("script not found: $id")
    }

    suspend fun saveScript(
        scriptId: String?,
        title: String,
        content: String,
        source: String,
    ): Result<TeleprompterScriptEntity> = runCatching {
        require(title.isNotBlank()) { "title must not be blank" }

        val isCreate = scriptId == null
        val now = System.currentTimeMillis()
        val entity = TeleprompterScriptEntity(
            id = scriptId ?: UUID.randomUUID().toString(),
            title = title.trim(),
            content = content,
            wordCount = countScriptWords(content),
            createdAt = now,
            updatedAt = now,
        )
        repository.upsert(entity)

        activityLogRecorder.recordTeleprompter(
            scriptId = entity.id,
            actionType = if (isCreate) "CREATE" else "UPDATE",
            source = source,
            title = AppContext.getString(
                if (isCreate) R.string.teleprompter_log_created_title
                else R.string.teleprompter_log_updated_title,
                entity.title
            ),
            description = AppContext.getContext().getString(
                if (isCreate) R.string.teleprompter_log_created_desc
                else R.string.teleprompter_log_updated_desc,
                entity.title,
                entity.wordCount
            ),
            screenRoute = Screen.Teleprompter().id.toString(),
        )
        entity
    }

    suspend fun deleteScript(id: String, source: String): Result<Unit> = runCatching {
        val deleted = repository.getById(id)
        repository.deleteById(id)

        activityLogRecorder.recordTeleprompter(
            scriptId = id,
            actionType = "DELETE",
            source = source,
            title = AppContext.getString(
                R.string.teleprompter_log_deleted_title,
                deleted?.title ?: id
            ),
            description = AppContext.getString(
                R.string.teleprompter_log_deleted_desc,
                deleted?.title ?: id
            ),
            screenRoute = Screen.Teleprompter().id.toString(),
        )
    }
}
