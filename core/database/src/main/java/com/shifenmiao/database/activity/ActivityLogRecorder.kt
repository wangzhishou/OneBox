package com.shifenmiao.database.activity

import android.content.Context
import com.shifenmiao.database.R
import com.shifenmiao.database.activity.repository.ActivityLogRepository
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.interfaces.logging.ImageSaveLogger
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.activity.ActivityCategory
import com.shifenmiao.model.activity.ActivityLogEntry
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 活动日志记录器 — 所有 feature 模块唯一的写入门面。
 *
 * 通过语义化的方法（[recordAiChat]、[recordImageSave] 等）构造正确的
 * [ActivityLogEntry] 并委托给 [ActivityLogRepository]。
 *
 * 新增一种活动类型只需：
 * 1. 在 [ActivityCategory] 加枚举值
 * 2. 在本类加一个 record* 方法
 * 3. 在对应 feature 的 Component 里调用它
 */
@Singleton
class ActivityLogRecorder @Inject constructor(
    private val repository: ActivityLogRepository,
    @ApplicationContext private val context: Context
) : ImageSaveLogger {

    // ── AI 对话 ──────────────────────────────────────

    /**
     * 记录 AI 单聊 / Prompt 对话。
     *
     * @param conversationId   对话 ID（用作 dedupKey，同一对话只保留最新摘要）
     * @param title            对话标题
     * @param description      最后一条消息摘要
     * @param screenRoute      跳转的 Screen.id
     * @param questionId       用户消息 ID
     * @param answerId         AI 消息 ID
     * @param completionId     AI completion ID
     */
    suspend fun recordAiChat(
        conversationId: String,
        appTitle: String,
        title: String,
        description: String,
        screenRoute: String = "",
        questionId: String = "",
        answerId: String = "",
        completionId: String = "",
        entryType: String = "",
        entryRefId: String = "",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("questionId", questionId)
            put("answerId", answerId)
            put("completionId", completionId)
            put("entryType", entryType)
            put("entryRefId", entryRefId)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.AI_CHAT,
                appTitle = appTitle,
                title = title,
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "ai_chat_$conversationId",
                createdAt = timestamp
            )
        )
    }

    // ── AI 对战 ──────────────────────────────────────

    suspend fun recordAiDuel(
        conversationId: String,
        title: String,
        appTitle: String,
        description: String,
        screenRoute: String = "",
        questionId: String = "",
        answerId: String = "",
        completionId: String = "",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("questionId", questionId)
            put("answerId", answerId)
            put("completionId", completionId)
            put("entryType", "DUEL")
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.AI_DUEL,
                title = title,
                appTitle = appTitle,
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "ai_duel_$conversationId",
                createdAt = timestamp
            )
        )
    }

    // ── AI 智能体 ────────────────────────────────────
    suspend fun recordAiAgent(
        conversationId: String,
        agentId: String,
        title: String,
        appTitle: String,
        description: String,
        screenRoute: String = "",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("agentId", agentId)
            put("entryType", "AGENT")
            put("entryRefId", agentId)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.AI_AGENT,
                title = title,
                appTitle = appTitle,
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "ai_agent_$conversationId",
                createdAt = timestamp
            )
        )
    }

    // ── 图片编辑 / 保存 ─────────────────────────────
    /**
     * 图片操作每次保存都是独立记录（dedupKey = 时间戳），不做去重。
     */
    override suspend fun recordImageSave(
        screenId: String,
        screenName: String,
        description: String,
        fileUri: String,
        fileName: String,
        savePath: String
    ) {
        val timestamp = Date()
        val logFileUri = fileUri.toLogUri()
        val logSavePath = savePath.toLogUri()
        val payload = JSONObject().apply {
            put("fileUri", logFileUri)
            put("fileName", fileName)
            put("savePath", logSavePath)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.IMAGE_EDIT,
                title = screenName,
                appTitle = screenName,
                description = description,
                screenRoute = screenId,
                payload = payload,
                thumbnailUri = logFileUri.ifEmpty { null },
                dedupKey = "image_${timestamp.time}",
                createdAt = timestamp
            )
        )
    }

    // ── 压缩包制作 / 解压 ────────────────────────────

    /**
     * 制作压缩包成功。压缩包保存到 SAF 后授权可能过期，操作记录里仍保留原 URI，
     * 点开打不开时由页面自行提示，不影响记录本身。
     */
    suspend fun recordArchiveCreated(
        archiveName: String,
        fileCount: Int,
        archiveUri: String,
        timestamp: Date = Date()
    ) {
        val displayName = archiveName.ifBlank {
            context.getString(R.string.activity_log_archive_unnamed)
        }
        val count = fileCount.coerceAtLeast(0)
        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.ARCHIVE,
                appTitle = context.getString(R.string.activity_log_archive_app_title),
                title = context.getString(R.string.activity_log_archive_created_title),
                description = context.resources.getQuantityString(
                    R.plurals.activity_log_archive_created_description,
                    count,
                    displayName,
                    count
                ),
                payload = archivePayload(
                    actionType = ARCHIVE_ACTION_CREATE,
                    archiveName = displayName,
                    archiveUri = archiveUri,
                    destinationUri = archiveUri,
                    fileCount = count
                ),
                dedupKey = "archive_create_${timestamp.time}",
                createdAt = timestamp
            )
        )
    }

    /**
     * 整包解压成功，[folderUri] 是新建出来的目标文件夹（document URI），供点击回跳。
     */
    suspend fun recordArchiveExtracted(
        archiveName: String,
        fileCount: Int,
        folderName: String,
        archiveUri: String,
        folderUri: String,
        timestamp: Date = Date()
    ) {
        val count = fileCount.coerceAtLeast(0)
        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.ARCHIVE,
                appTitle = context.getString(R.string.activity_log_archive_app_title),
                title = context.getString(R.string.activity_log_archive_extracted_title),
                description = context.resources.getQuantityString(
                    R.plurals.activity_log_archive_extracted_description,
                    count,
                    count,
                    archiveFolderLabel(folderName)
                ),
                payload = archivePayload(
                    actionType = ARCHIVE_ACTION_EXTRACT,
                    archiveName = archiveName,
                    archiveUri = archiveUri,
                    destinationUri = folderUri,
                    fileCount = count
                ),
                dedupKey = "archive_extract_${timestamp.time}",
                createdAt = timestamp
            )
        )
    }

    /**
     * 单个条目解压成功，[fileUri] 是写出的目标文件，点击回跳到它所在的目录。
     */
    suspend fun recordArchiveEntryExtracted(
        fileName: String,
        folderName: String,
        archiveUri: String,
        fileUri: String,
        timestamp: Date = Date()
    ) {
        val displayName = fileName.ifBlank {
            context.getString(R.string.activity_log_archive_unnamed)
        }
        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.ARCHIVE,
                appTitle = context.getString(R.string.activity_log_archive_app_title),
                title = context.getString(R.string.activity_log_archive_entry_extracted_title),
                description = context.getString(
                    R.string.activity_log_archive_entry_extracted_description,
                    displayName,
                    archiveFolderLabel(folderName)
                ),
                payload = archivePayload(
                    actionType = ARCHIVE_ACTION_EXTRACT_FILE,
                    archiveName = context.getString(R.string.activity_log_archive_unnamed),
                    archiveUri = archiveUri,
                    destinationUri = fileUri,
                    fileName = displayName,
                    fileCount = 1
                ),
                dedupKey = "archive_extract_file_${timestamp.time}",
                createdAt = timestamp
            )
        )
    }

    private fun archiveFolderLabel(folderName: String): String =
        folderName.ifBlank { context.getString(R.string.activity_log_archive_unknown_folder) }

    private fun archivePayload(
        actionType: String,
        archiveName: String,
        archiveUri: String,
        destinationUri: String = "",
        fileName: String = "",
        fileCount: Int = 0
    ): String = JSONObject().apply {
        put("actionType", actionType)
        put("archiveName", archiveName)
        // 保留原始 content URI：操作记录跳转走 FileBrowser/ZipBrowser，能直接吃 SAF URI；
        // 转成 file:// 会丢掉「解压出的那个子文件夹」，只剩父目录。
        put("archiveUri", archiveUri)
        put("destinationUri", destinationUri)
        put("fileName", fileName)
        put("fileCount", fileCount)
    }.toString()

    // ── 笔记 ────────────────────────────────────────

    suspend fun recordNote(
        itemId: Int,
        title: String,
        appTitle: String,
        description: String,
        screenRoute: String,
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("itemId", itemId)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.NOTE_EDIT,
                title = title,
                appTitle = appTitle,
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "note_$itemId",
                createdAt = timestamp
            )
        )
    }

    // ── HTML 文章 ────────────────────────────────────

    suspend fun recordHtml(
        itemId: Int,
        title: String,
        appTitle: String,
        description: String,
        screenRoute: String,
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("itemId", itemId)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.HTML_EDIT,
                title = title,
                appTitle = appTitle,
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "html_$itemId",
                createdAt = timestamp
            )
        )
    }

    // ── 记账 ────────────────────────────────────────

    /**
     * 记录一次账目变更。每次写入都是独立日志（审计场景不去重）。
     *
     * @param entityId      关联实体 ID（recordId / categoryId / "*" 表示批量）
     * @param entityType    "BookkeepingRecord" / "BookkeepingCategory" / "BookkeepingBackup"
     * @param actorType     USER / AGENT / SYSTEM
     * @param actionType    CREATE / UPDATE / DELETE / IMPORT / RESTORE
     * @param source        触发源（UI 路径 / AgentTool 名）
     * @param title         标题（如 "新增支出 ¥12.50"）
     * @param description   详细描述
     * @param snapshot      实体 JSON 快照（可选）
     */
    suspend fun recordBookkeeping(
        entityId: String,
        entityType: String,
        actorType: String,
        actionType: String,
        source: String,
        title: String,
        description: String,
        snapshot: String? = null,
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("entityId", entityId)
            put("entityType", entityType)
            put("actorType", actorType)
            put("actionType", actionType)
            put("source", source)
            if (snapshot != null) put("snapshot", snapshot)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.BOOKKEEPING,
                title = title,
                appTitle = context.getString(R.string.activity_log_bookkeeping_app_title),
                description = description,
                screenRoute = "",
                payload = payload,
                // 每次都唯一,避免同一 entityId 的旧日志被覆盖
                dedupKey = "bookkeeping_${timestamp.time}_${java.util.UUID.randomUUID()}",
                createdAt = timestamp
            )
        )
    }

    // ── 中国古诗词 ──────────────────────────────────

    /**
     * 记录一次古诗词操作。每次写入都是独立日志（审计场景不去重）。
     *
     * @param entityId      关联实体 ID（诗词 id / "*" 表示批量）
     * @param entityType    "Poem"
     * @param actorType     USER / AGENT / SYSTEM
     * @param actionType    CREATE / UPDATE / DELETE / FAVORITE / UNFAVORITE
     * @param source        触发源（UI 路径 / AgentTool 名）
     * @param title         标题（如 "收藏《静夜思》"）
     * @param description   详细描述
     * @param snapshot      实体 JSON 快照（可选）
     * @param screenRoute   跳转的 Screen.id
     */
    suspend fun recordPoem(
        entityId: String,
        entityType: String = "Poem",
        actorType: String,
        actionType: String,
        source: String,
        title: String,
        description: String? = null,
        snapshot: String? = null,
        screenRoute: String? = null,
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("entityId", entityId)
            put("entityType", entityType)
            put("actorType", actorType)
            put("actionType", actionType)
            put("source", source)
            if (snapshot != null) put("snapshot", snapshot)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.POEM,
                title = title,
                appTitle = context.getString(R.string.activity_log_poem_app_title),
                description = description ?: title,
                screenRoute = screenRoute ?: "",
                payload = payload,
                // 每次都唯一,避免同一 entityId 的旧日志被覆盖
                dedupKey = "poem_${timestamp.time}_${java.util.UUID.randomUUID()}",
                createdAt = timestamp
            )
        )
    }

    // ── 待办清单 ────────────────────────────────────

    /**
     * 记录待办清单变更。每次写入都是独立日志（覆盖式，同一实体保留最新状态）。
     *
     * @param entityId      关联实体 ID（categoryId / taskId）
     * @param entityType    "CATEGORY" / "TASK"
     * @param actionType    "CREATE" / "UPDATE" / "DELETE" / "TOGGLE_COMPLETE" / "TOGGLE_STAR" / "REORDER"
     * @param source        触发源（UI 路径 / AgentTool 名）
     * @param title         标题（如 "新增分类: 工作"）
     * @param description   详细描述
     */
    suspend fun recordMarkTodo(
        entityId: String,
        entityType: String,
        actionType: String,
        source: String,
        title: String,
        description: String,
        screenRoute: String = "marktodo",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("entityId", entityId)
            put("entityType", entityType)
            put("actionType", actionType)
            put("source", source)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.TODO,
                title = title,
                appTitle = context.getString(R.string.activity_log_todo_app_title),
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "todo_${entityType.lowercase()}_$entityId",
                createdAt = timestamp
            )
        )
    }

    // ── 象棋对局 ────────────────────────────────────

    /**
     * 记录象棋对局操作。
     *
     * @param gameId        对局 ID
     * @param actionType    "CREATE" / "DELETE" / "RESIGN" / "RENAME"
     * @param title         标题（如 "新建人机对局"）
     * @param description   详细描述
     * @param screenRoute   跳转 Screen.id
     */
    suspend fun recordXiangqi(
        gameId: String,
        actionType: String,
        title: String,
        description: String,
        screenRoute: String = "",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("gameId", gameId)
            put("actionType", actionType)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.XIANGQI,
                title = title,
                appTitle = context.getString(R.string.activity_log_xiangqi_app_title),
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "xiangqi_${actionType.lowercase()}_$gameId",
                createdAt = timestamp
            )
        )
    }

    // ── 提词器文稿 ──────────────────────────────────

    /**
     * 记录提词器文稿变更。
     *
     * @param scriptId      文稿 ID
     * @param actionType    "CREATE" / "UPDATE" / "DELETE"
     * @param source        触发源（UI 路径 / AgentTool 名）
     * @param title         标题（如 "新建提词文稿: xxx"）
     * @param description   详细描述
     * @param screenRoute   跳转 Screen.id
     */
    suspend fun recordTeleprompter(
        scriptId: String,
        actionType: String,
        source: String,
        title: String,
        description: String,
        screenRoute: String = "",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("scriptId", scriptId)
            put("actionType", actionType)
            put("source", source)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.TELEPROMPTER,
                title = title,
                appTitle = context.getString(R.string.activity_log_teleprompter_app_title),
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "teleprompter_${actionType.lowercase()}_$scriptId",
                createdAt = timestamp
            )
        )
    }

    // ── 习惯打卡 ────────────────────────────────────

    /**
     * 记录一次习惯创建。同一习惯只保留最新一条(按 habitId 去重)。
     *
     * 历史卡片正文渲染的是 description,主文案放在 description(title 保留同内容)。
     *
     * @param habitId     习惯 ID
     * @param habitName   习惯名称
     * @param actor       USER / AGENT / SYSTEM
     * @param screenRoute 跳转目标 Screen.id 字符串(core/database 不依赖 core/ui,由调用方传)
     */
    suspend fun recordHabitCreated(
        habitId: String,
        habitName: String,
        actor: String,
        screenRoute: String = "",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("habitId", habitId)
            put("habitName", habitName)
            put("actionType", "CREATE")
            put("actor", actor)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.HABIT,
                title = context.getString(R.string.activity_log_habit_created, habitName),
                appTitle = context.getString(R.string.activity_log_habit_app_title),
                description = context.getString(R.string.activity_log_habit_created, habitName),
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "habit_created_$habitId",
                createdAt = timestamp
            )
        )
    }

    /**
     * 记录一次习惯打卡。同一习惯同一天只保留最新一条。
     *
     * 历史卡片正文渲染的是 description,主文案放在 description(title 保留同内容)。
     *
     * @param habitId     习惯 ID
     * @param habitName   习惯名称
     * @param epochDay    打卡日期(LocalDate.toEpochDay())
     * @param actor       USER / AGENT / SYSTEM
     * @param screenRoute 跳转目标 Screen.id 字符串(core/database 不依赖 core/ui,由调用方传)
     */
    suspend fun recordHabitCheckIn(
        habitId: String,
        habitName: String,
        epochDay: Long,
        actor: String,
        screenRoute: String = "",
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("habitId", habitId)
            put("habitName", habitName)
            put("epochDay", epochDay)
            put("actionType", "CHECK_IN")
            put("actor", actor)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.HABIT,
                title = context.getString(R.string.activity_log_habit_check_in, habitName),
                appTitle = context.getString(R.string.activity_log_habit_app_title),
                description = context.getString(R.string.activity_log_habit_check_in, habitName),
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "habit_checkin_${habitId}_$epochDay",
                createdAt = timestamp
            )
        )
    }

    // ── 家庭物品 ────────────────────────────────────

    /**
     * 记录一次家庭物品/位置变更。每次写入都是独立日志（审计场景不去重）。
     *
     * @param entityId      关联实体 ID（itemId / locationId）
     * @param entityType    "HouseholdItem" / "HouseholdLocation"
     * @param actorType     USER / AGENT / SYSTEM
     * @param actionType    CREATE / UPDATE / DELETE / MOVE
     * @param source        触发源（UI 路径 / AgentTool 名）
     * @param title         标题（如 "新增物品: 感冒药"）
     * @param description   详细描述（通常为位置路径）
     * @param appTitle      来源功能名（由调用方提供本地化文案）
     * @param snapshot      实体 JSON 快照（可选）
     */
    suspend fun recordHousehold(
        entityId: String,
        entityType: String,
        actorType: String,
        actionType: String,
        source: String,
        title: String,
        description: String,
        appTitle: String,
        snapshot: String? = null,
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("entityId", entityId)
            put("entityType", entityType)
            put("actorType", actorType)
            put("actionType", actionType)
            put("source", source)
            if (snapshot != null) put("snapshot", snapshot)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.HOUSEHOLD,
                title = title,
                appTitle = appTitle,
                description = description.ifEmpty { title },
                screenRoute = "",
                payload = payload,
                // 每次都唯一,避免同一 entityId 的旧日志被覆盖
                dedupKey = "household_${timestamp.time}_${java.util.UUID.randomUUID()}",
                createdAt = timestamp
            )
        )
    }

    /**
     * 经期记录审计日志。
     *
     * @param entityId      记录 ID
     * @param entityType    "PeriodRecord"
     * @param actorType     USER / AGENT / SYSTEM
     * @param actionType    CREATE / UPDATE / DELETE
     * @param source        触发源（UI 路径 / AgentTool 名）
     * @param title         标题
     * @param description   详细描述（日期摘要等）
     * @param appTitle      来源功能名（由调用方提供本地化文案）
     * @param snapshot      实体 JSON 快照（可选）
     */
    suspend fun recordPeriod(
        entityId: String,
        entityType: String,
        actorType: String,
        actionType: String,
        source: String,
        title: String,
        description: String,
        appTitle: String,
        snapshot: String? = null,
        timestamp: Date = Date()
    ) {
        val payload = JSONObject().apply {
            put("entityId", entityId)
            put("entityType", entityType)
            put("actorType", actorType)
            put("actionType", actionType)
            put("source", source)
            if (snapshot != null) put("snapshot", snapshot)
        }.toString()

        repository.record(
            ActivityLogEntry(
                category = ActivityCategory.PERIOD,
                title = title,
                appTitle = appTitle,
                description = description.ifEmpty { title },
                screenRoute = "",
                payload = payload,
                dedupKey = "period_${timestamp.time}_${java.util.UUID.randomUUID()}",
                createdAt = timestamp
            )
        )
    }

    // ── 通用记录 ────────────────────────────────────
    /**
     * 通用记录方法 — 当上面没有覆盖到的类型时使用。
     */
    suspend fun record(
        category: ActivityCategory,
        title: String,
        appTitle: String,
        description: String,
        screenRoute: String = "",
        payload: String = "",
        dedupKey: String = "${category.name}_${System.currentTimeMillis()}",
        thumbnailUri: String? = null,
        timestamp: Date = Date()
    ) {
        repository.record(
            ActivityLogEntry(
                category = category,
                title = title,
                appTitle = appTitle,
                description = description,
                screenRoute = screenRoute,
                payload = payload,
                thumbnailUri = thumbnailUri,
                dedupKey = dedupKey,
                createdAt = timestamp
            )
        )
    }

    // ── 按 conversationId 删除（AI 场景） ────────────

    suspend fun deleteAiChatLog(conversationId: String) {
        repository.deleteByDedupKey("ai_chat_$conversationId")
    }

    suspend fun deleteAiDuelLog(conversationId: String) {
        repository.deleteByDedupKey("ai_duel_$conversationId")
    }

    suspend fun deleteAiAgentLog(conversationId: String) {
        repository.deleteByDedupKey("ai_agent_$conversationId")
    }

    suspend fun deleteNoteLog(itemId: Int): Int {
        return repository.deleteByDedupKey("note_$itemId")
    }

    suspend fun deleteHtmlLog(itemId: Int): Int {
        return repository.deleteByDedupKey("html_$itemId")
    }

    suspend fun deleteAiAgentLogsByAgentId(agentId: Int): Int {
        val agentIdValue = agentId.toString()
        return repository.deleteByCategoryAndAnyPayloadContains(
            category = ActivityCategory.AI_AGENT,
            primaryPayloadFragment = "\"agentId\":\"$agentIdValue\"",
            secondaryPayloadFragment = "\"entryRefId\":\"$agentIdValue\"",
        )
    }

    suspend fun deleteRelatedLogsForItem(item: ItemEntity) {
        val type = ListItemType.fromId(item.listType)
        when (type) {
            ListItemType.NOTE -> deleteNoteLog(item.id)
            ListItemType.HTML -> deleteHtmlLog(item.id)
            ListItemType.AGENT -> item.id.takeIf { it > 0 }?.let { deleteAiAgentLogsByAgentId(it) }
            else -> Unit
        }
    }

    // ── 日志 URI 持久化 ───────────────────────────────

    /**
     * 将可能过期的 content:// URI 转为 file:// URI 字符串。
     * 转换失败时回退到原始字符串，避免丢失信息。
     */
    private fun String?.toLogUri(): String {
        if (this.isNullOrBlank()) return this ?: ""
        return SafUriUtils.toFileUri(context, this) ?: this
    }

    companion object {
        /** 压缩包制作（payload.actionType） */
        const val ARCHIVE_ACTION_CREATE = "CREATE"

        /** 整个压缩包解压 */
        const val ARCHIVE_ACTION_EXTRACT = "EXTRACT"

        /** 单个条目解压 */
        const val ARCHIVE_ACTION_EXTRACT_FILE = "EXTRACT_FILE"
    }
}
