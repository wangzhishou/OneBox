package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.interfaces.bookmark.UrlBookmarkService
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import org.json.JSONObject
import javax.inject.Inject

class ManageBookmarkTool @Inject constructor(
    private val bookmarkService: UrlBookmarkService
) : AgentTool {

    override val name = "manage_bookmark"
    override val description = "管理网址收藏（浏览器书签与「网址收藏」是同一个收藏库，分类与笔记等全局分类共享）：添加/删除收藏网址、创建/删除收藏分类、列出收藏和分类"
    override val title = "管理书签"
    override val summary = "添加收藏网址、创建收藏夹分类"
    override val category = ToolCategory.BUSINESS
    override val riskLevel = ToolRiskLevel.SAFE
    override val keywords = listOf("书签", "收藏", "bookmark", "收藏夹", "添加书签", "删除书签", "整理收藏", "网址收藏")
    override val examples = listOf(
        "把 https://example.com 添加到书签",
        "创建一个叫「技术博客」的书签分类",
        "列出所有书签分类",
    )
    override val parallelizable = false

    override val parametersSchema = ToolParameters(
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = "操作类型",
                enum = listOf("add_bookmark", "remove_bookmark", "list_bookmarks", "create_folder", "delete_folder", "list_folders")
            ),
            "url" to ToolParameterProperty(
                type = "string",
                description = "网址（add_bookmark / remove_bookmark 时必填）"
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = "书签标题（add_bookmark 时可选，默认使用 URL）"
            ),
            "folder_id" to ToolParameterProperty(
                type = "string",
                description = "所属分类 ID（add_bookmark / delete_folder 时使用，来自 list_folders）"
            ),
            "folder_name" to ToolParameterProperty(
                type = "string",
                description = "分类名称（create_folder 时必填）"
            ),
            "bookmark_id" to ToolParameterProperty(
                type = "string",
                description = "书签条目 ID（remove_bookmark 时必填，来自 list_bookmarks）"
            ),
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            when (json.optString("action")) {
                "add_bookmark" -> addBookmark(json)
                "remove_bookmark" -> removeBookmark(json)
                "list_bookmarks" -> listBookmarks()
                "create_folder" -> createFolder(json)
                "delete_folder" -> deleteFolder(json)
                "list_folders" -> listFolders()
                else -> AgentToolResult("未知 action，支持: add_bookmark, remove_bookmark, list_bookmarks, create_folder, delete_folder, list_folders", isError = true)
            }
        }.getOrElse { AgentToolResult("操作失败: ${it.message}", isError = true) }
    }

    private suspend fun addBookmark(json: JSONObject): AgentToolResult {
        val url = json.optString("url").takeIf { it.isNotBlank() }
            ?: return AgentToolResult("缺少 url 参数", isError = true)
        val title = json.optString("title").takeIf { it.isNotBlank() } ?: url
        val folderId = json.optString("folder_id").takeIf { it.isNotBlank() }?.toLongOrNull()

        if (folderId != null) {
            val folders = bookmarkService.listCategories()
            if (folders.none { it.id == folderId }) {
                return AgentToolResult("分类 $folderId 不存在，可用: ${folders.map { "${it.id}(${it.name})" }}", isError = true)
            }
        }

        bookmarkService.findByUrl(url)?.let {
            return AgentToolResult("书签已存在: ${it.title} (id: ${it.itemId})", isError = true)
        }

        val itemId = bookmarkService.saveBookmark(url, title, folderId)
        val folderName = folderId?.let { id ->
            bookmarkService.listCategories().find { it.id == id }?.name
        } ?: "未分类"
        return AgentToolResult("已添加书签:\n- id: $itemId\n- 标题: $title\n- URL: $url\n- 分类: $folderName")
    }

    private suspend fun removeBookmark(json: JSONObject): AgentToolResult {
        val bookmarkId = json.optString("bookmark_id").takeIf { it.isNotBlank() }
            ?: return AgentToolResult("缺少 bookmark_id 参数", isError = true)

        val bookmarks = bookmarkService.listBookmarks()
        val target = bookmarks.find { it.itemId.toString() == bookmarkId }
            ?: return AgentToolResult("书签 $bookmarkId 不存在", isError = true)

        bookmarkService.removeBookmark(target.itemId)
        return AgentToolResult("已删除书签: ${target.title} (${target.url})")
    }

    private suspend fun listBookmarks(): AgentToolResult {
        val bookmarks = bookmarkService.listBookmarks()
        if (bookmarks.isEmpty()) return AgentToolResult("暂无书签")

        val categoryNames = bookmarkService.listCategories().associate { it.id to it.name }
        val grouped = bookmarks.groupBy { item ->
            item.categoryIds.firstOrNull()?.let { categoryNames[it] } ?: "未分类"
        }
        val sb = StringBuilder("书签列表:\n")
        grouped.forEach { (folder, items) ->
            sb.appendLine("[$folder]")
            items.forEach { item ->
                sb.appendLine("  · ${item.title} - ${item.url} (id: ${item.itemId})")
            }
        }
        return AgentToolResult(sb.toString().trimEnd())
    }

    private suspend fun createFolder(json: JSONObject): AgentToolResult {
        val folderName = json.optString("folder_name").takeIf { it.isNotBlank() }
            ?: return AgentToolResult("缺少 folder_name 参数", isError = true)

        bookmarkService.listCategories().find { it.name == folderName }?.let {
            return AgentToolResult("分类「$folderName」已存在 (id: ${it.id})", isError = true)
        }

        val categoryId = bookmarkService.createCategory(folderName)
        return AgentToolResult("已创建书签分类: $folderName (id: $categoryId)")
    }

    private suspend fun deleteFolder(json: JSONObject): AgentToolResult {
        val folderId = json.optString("folder_id").takeIf { it.isNotBlank() }?.toLongOrNull()
            ?: return AgentToolResult("缺少 folder_id 参数", isError = true)

        val folders = bookmarkService.listCategories()
        val target = folders.find { it.id == folderId }
            ?: return AgentToolResult("分类 $folderId 不存在", isError = true)

        if (!bookmarkService.deleteCategory(folderId)) {
            return AgentToolResult("分类「${target.name}」是内置分类，不可删除", isError = true)
        }
        return AgentToolResult("已删除分类: ${target.name}，其中书签已移至未分类")
    }

    private suspend fun listFolders(): AgentToolResult {
        val folders = bookmarkService.listCategories()
        if (folders.isEmpty()) return AgentToolResult("暂无书签分类")

        val bookmarks = bookmarkService.listBookmarks()
        val sb = StringBuilder("书签分类:\n")
        folders.forEach { folder ->
            val count = bookmarks.count { folder.id in it.categoryIds }
            sb.appendLine("· ${folder.name} (id: ${folder.id}, $count 个书签)")
        }
        return AgentToolResult(sb.toString().trimEnd())
    }
}
