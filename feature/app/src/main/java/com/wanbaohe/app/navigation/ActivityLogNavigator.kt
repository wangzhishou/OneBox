package com.wanbaohe.app.navigation

import android.net.Uri
import com.shifenmiao.common.handle.AIConversationNavigation
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.activity.ActivityCategory
import com.shifenmiao.model.activity.ActivityLogEntry
import com.shifenmiao.model.ai.AIConversationEntryType
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.logger.makeLog
import com.wanbaohe.app.navigation.ActivityLogNavigator.resolve
import org.json.JSONObject

/**
 * 活动日志导航解析器。
 *
 * 根据 [ActivityLogEntry.category] + [ActivityLogEntry.payload] 构造跳转目标 Screen。
 * 不再依赖 HandleEvent 中的大 when-block，也不需要 runBlocking。
 *
 * 如需扩展新类型，只需在 [resolve] 的 when 里加一个分支。
 */
object ActivityLogNavigator {

    /**
     * 解析一条活动日志 → 要跳转的 Screen（可能为 null 表示无法跳转）。
     */
    suspend fun resolve(entry: ActivityLogEntry, dataDraftHelper: DataDraftHelper? = null): Screen? {
        val payload = runCatching { JSONObject(entry.payload) }.getOrNull()


        return when (entry.category) {
            ActivityCategory.AI_CHAT -> resolveAiChat(payload)
            ActivityCategory.AI_DUEL -> resolveAiDuel(payload)
            ActivityCategory.AI_AGENT -> resolveAiAgent(payload)
            ActivityCategory.IMAGE_EDIT -> resolveImageEdit(entry)
            ActivityCategory.NOTE_EDIT -> resolveNote(payload, dataDraftHelper)
            ActivityCategory.HTML_EDIT -> resolveHtml(entry, payload)
            ActivityCategory.BOOKKEEPING -> Screen.Bookkeeping()
            ActivityCategory.RECORD_CENTER -> Screen.RecordCenter()
            ActivityCategory.TODO -> Screen.MarkTodoRouter()
            ActivityCategory.XIANGQI -> resolveXiangqi(payload)
            ActivityCategory.TELEPROMPTER -> resolveTeleprompter(payload)
            ActivityCategory.HABIT -> resolveHabit(payload)
            ActivityCategory.HOUSEHOLD -> Screen.HouseholdItems()
            ActivityCategory.PERIOD -> Screen.Period()
            ActivityCategory.POEM -> resolvePoem(payload)
            ActivityCategory.ARCHIVE -> resolveArchive(payload)
            else -> resolveByScreenRoute(entry)
        }
    }

    // ── AI 单聊 / Prompt ─────────────────────────────

    private fun resolveAiChat(payload: JSONObject?): Screen? {
        payload ?: return null
        val conversationId = payload.optString("conversationId")
        if (conversationId.isEmpty()) return null
        val entryType = payload.optString("entryType")
        val entryRefId = payload.optString("entryRefId").ifBlank { null }
        val resolved = runCatching { enumValueOf<AIConversationEntryType>(entryType) }
            .getOrDefault(AIConversationEntryType.CHAT)
        return AIConversationNavigation.buildHistoryDetailScreen(
            conversationId = conversationId,
            entryType = resolved,
            entryRefId = entryRefId,
        )
    }

    // ── AI 对战 ──────────────────────────────────────

    private fun resolveAiDuel(payload: JSONObject?): Screen? {
        payload ?: return null
        val conversationId = payload.optString("conversationId")
        if (conversationId.isEmpty()) return null
        return AIConversationNavigation.buildHistoryDetailScreen(
            conversationId = conversationId,
            entryType = AIConversationEntryType.DUEL,
        )
    }

    // ── AI 智能体 ────────────────────────────────────

    private fun resolveAiAgent(payload: JSONObject?): Screen? {
        payload ?: return null
        val conversationId = payload.optString("conversationId")
        val agentId = payload.optString("entryRefId").ifBlank {
            payload.optString("agentId")
        }
        if (agentId.isEmpty()) return null
        return AIConversationNavigation.buildHistoryDetailScreen(
            conversationId = conversationId,
            entryType = AIConversationEntryType.AGENT,
            entryRefId = agentId,
        )
    }

    // ── 图片编辑 ─────────────────────────────────────

    private fun resolveImageEdit(entry: ActivityLogEntry): Screen? {
        return when {
            entry.screenRoute == "browser" -> Screen.WebBrowser()
            else -> {
                val screenId = entry.screenRoute.toIntOrNull() ?: return null
                Screen.entries.find { it.id == screenId }
            }
        }
    }

    // ── 笔记 ────────────────────────────────────────

    private suspend fun resolveNote(payload: JSONObject?, dataDraftHelper: DataDraftHelper?): Screen? {
        payload ?: run { "resolveNote: payload is null".makeLog(TAG); return null }
        val itemId = payload.optInt("itemId", 0)
        if (itemId == 0) { "resolveNote: invalid itemId=0".makeLog(TAG); return null }
        if (dataDraftHelper != null) {
            val draftId = dataDraftHelper.createDraft(
                draftType = ListItemType.NOTE.id,
                itemId = itemId
            )
            return Screen.CreateNote(draftId = draftId)
        }
        return Screen.CreateNote()
    }

    // ── 网址收藏 ─────────────────────────────────────
    //
    // 和「我的」里点同一条走同一条路：内置浏览器打开收藏（有快照就离线看快照）。
    // 以前这里建草稿进编辑页，点操作记录打不开已收藏的网页。

    private fun resolveHtml(entry: ActivityLogEntry, payload: JSONObject?): Screen? {
        val itemId = payload?.optInt("itemId", 0)?.takeIf { it > 0 }
            ?: entry.dedupKey.removePrefix("html_").toIntOrNull()?.takeIf { it > 0 }
            ?: return null
        return Screen.WebBrowser(bookmarkItemId = itemId)
    }

    // ── 象棋 ────────────────────────────────────────

    private fun resolveXiangqi(payload: JSONObject?): Screen? {
        payload ?: return null
        val gameId = payload.optString("gameId")
        if (gameId.isEmpty()) return null
        return Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Game(gameId))
    }

    // ── 提词器 ──────────────────────────────────────

    private fun resolveTeleprompter(payload: JSONObject?): Screen? {
        payload ?: return Screen.Teleprompter()
        val scriptId = payload.optString("scriptId")
        val actionType = payload.optString("actionType")
        // 已删除的文稿或无有效 id 时回到列表页，否则直达编辑页
        return when {
            scriptId.isEmpty() || actionType.equals("DELETE", ignoreCase = true) ->
                Screen.Teleprompter()

            else -> Screen.Teleprompter(Screen.Teleprompter.Type.Edit(scriptId))
        }
    }

    // ── 习惯打卡 ────────────────────────────────────

    private fun resolveHabit(payload: JSONObject?): Screen? {
        val habitId = payload?.optString("habitId").orEmpty()
        val actionType = payload?.optString("actionType").orEmpty()
        // 新建记录且带 habitId → 直达编辑页;打卡/无 id → 习惯主页
        return when {
            habitId.isNotEmpty() && actionType.equals("CREATE", ignoreCase = true) ->
                Screen.HabitTracker(Screen.HabitTracker.Type.Edit(habitId))

            else -> Screen.HabitTracker()
        }
    }

    // ── 诗词 ────────────────────────────────────────

    private fun resolvePoem(payload: JSONObject?): Screen? {
        val poemId = payload?.optString("entityId")?.toLongOrNull()
        val actionType = payload?.optString("actionType").orEmpty()
        // 已删除的诗词或无有效 id 时回到诗词主页,否则直达对应诗词
        return when {
            poemId == null || actionType.equals("DELETE", ignoreCase = true) -> Screen.Poem()
            else -> Screen.Poem(poemId = poemId)
        }
    }

    // ── 压缩包制作 / 解压 ────────────────────────────

    private fun resolveArchive(payload: JSONObject?): Screen? {
        payload ?: return null
        val destinationUri = payload.optString("destinationUri").takeIf { it.isNotBlank() }
        val archiveUri = payload.optString("archiveUri").takeIf { it.isNotBlank() }
        return when (payload.optString("actionType")) {
            // 解压：回到解压结果所在的文件夹/文件（FileBrowser 对文件 URI 会打开父目录并高亮）
            ActivityLogRecorder.ARCHIVE_ACTION_EXTRACT,
            ActivityLogRecorder.ARCHIVE_ACTION_EXTRACT_FILE -> {
                destinationUri?.let { destination ->
                    runCatching {
                        Screen.FileBrowser(initialUri = Uri.parse(destination))
                    }.getOrNull()
                } ?: archiveUri?.let { archive ->
                    runCatching { Screen.ZipBrowser(uri = Uri.parse(archive)) }.getOrNull()
                }
            }

            // 制作压缩包：打开刚保存的压缩包浏览
            else -> archiveUri?.let { archive ->
                runCatching { Screen.ZipBrowser(uri = Uri.parse(archive)) }.getOrNull()
            } ?: Screen.Zip()
        }
    }

    // ── 通用回退：根据 screenRoute 查 Screen 枚举 ──
    private fun resolveByScreenRoute(entry: ActivityLogEntry): Screen? {
        val screenId = entry.screenRoute.toIntOrNull() ?: return null
        return Screen.entries.find { it.id == screenId }
    }

    private const val TAG = "ActivityLogNavigator"
}
