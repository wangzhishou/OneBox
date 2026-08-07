package com.shifenmiao.database.activity

import android.content.Context
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
                appTitle = "记账本",
                description = description,
                screenRoute = "",
                payload = payload,
                // 每次都唯一,避免同一 entityId 的旧日志被覆盖
                dedupKey = "bookkeeping_${timestamp.time}_${java.util.UUID.randomUUID()}",
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
                appTitle = "待办清单",
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
                appTitle = "中国象棋",
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
                appTitle = "提词器",
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
                title = "新建习惯: $habitName",
                appTitle = "习惯打卡",
                description = "新建习惯: $habitName",
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
                title = "打卡: $habitName",
                appTitle = "习惯打卡",
                description = "打卡: $habitName",
                screenRoute = screenRoute,
                payload = payload,
                dedupKey = "habit_checkin_${habitId}_$epochDay",
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
}
