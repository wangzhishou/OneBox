package com.wanbaohe.teleprompter.service

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity
import com.shifenmiao.database.teleprompter.repo.TeleprompterRepository
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
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
            wordCount = countWords(content),
            createdAt = now,
            updatedAt = now,
        )
        repository.upsert(entity)

        activityLogRecorder.recordTeleprompter(
            scriptId = entity.id,
            actionType = if (isCreate) "CREATE" else "UPDATE",
            source = source,
            title = if (isCreate) "新建提词文稿: ${entity.title}" else "更新提词文稿: ${entity.title}",
            description = if (isCreate) {
                "创建了提词文稿「${entity.title}」（${entity.wordCount} 字）"
            } else {
                "更新了提词文稿「${entity.title}」（${entity.wordCount} 字）"
            },
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
            title = "删除提词文稿: ${deleted?.title ?: id}",
            description = "删除了提词文稿「${deleted?.title ?: id}」",
            screenRoute = Screen.Teleprompter().id.toString(),
        )
    }

    private fun countWords(content: String): Int =
        content.replace("\\s+".toRegex(), "").length
}
