package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.webview.browser.BookmarkItem
import com.shifenmiao.webview.browser.BrowserRepository
import com.shifenmiao.webview.browser.toFaviconUrl
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

class ManageBookmarkTool @Inject constructor(
    private val repository: BrowserRepository
) : AgentTool {

    override val name = "manage_bookmark"
    override val description = "管理浏览器书签：添加/删除书签、创建/删除书签分类文件夹、列出书签和分类"
    override val title = "管理书签"
    override val summary = "添加收藏网址、创建收藏夹分类"
    override val category = ToolCategory.BUSINESS
    override val riskLevel = ToolRiskLevel.SAFE
    override val keywords = listOf("书签", "收藏", "bookmark", "收藏夹", "添加书签", "删除书签", "整理收藏")
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
                description = "所属分类文件夹 ID（add_bookmark 时可选）"
            ),
            "folder_name" to ToolParameterProperty(
                type = "string",
                description = "分类文件夹名称（create_folder 时必填）"
            ),
            "bookmark_id" to ToolParameterProperty(
                type = "string",
                description = "书签 ID（remove_bookmark 时必填）"
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

    private fun addBookmark(json: JSONObject): AgentToolResult {
        val url = json.optString("url").takeIf { it.isNotBlank() }
            ?: return AgentToolResult("缺少 url 参数", isError = true)
        val title = json.optString("title").takeIf { it.isNotBlank() } ?: url
        val folderId = json.optString("folder_id").orEmpty()

        if (folderId.isNotEmpty()) {
            val folders = repository.loadBookmarkFolders()
            if (folders.none { it.id == folderId }) {
                return AgentToolResult("文件夹 $folderId 不存在，可用: ${folders.map { "${it.id}(${it.name})" }}", isError = true)
            }
        }

        val existing = repository.loadBookmarks().find { it.url == url }
        if (existing != null) {
            return AgentToolResult("书签已存在: ${existing.title} (${existing.id})", isError = true)
        }

        val item = BookmarkItem(
            id = UUID.randomUUID().toString(),
            url = url,
            title = title,
            folderId = folderId,
            favicon = url.toFaviconUrl()
        )
        val bookmarks = repository.loadBookmarks() + item
        repository.saveBookmarks(bookmarks)

        return AgentToolResult("已添加书签:\n- id: ${item.id}\n- 标题: $title\n- URL: $url\n- 分类: ${folderId.ifEmpty { "未分类" }}")
    }

    private fun removeBookmark(json: JSONObject): AgentToolResult {
        val bookmarkId = json.optString("bookmark_id").takeIf { it.isNotBlank() }
            ?: return AgentToolResult("缺少 bookmark_id 参数", isError = true)

        val bookmarks = repository.loadBookmarks()
        val target = bookmarks.find { it.id == bookmarkId }
            ?: return AgentToolResult("书签 $bookmarkId 不存在", isError = true)

        repository.saveBookmarks(bookmarks.filter { it.id != bookmarkId })
        return AgentToolResult("已删除书签: ${target.title} (${target.url})")
    }

    private fun listBookmarks(): AgentToolResult {
        val bookmarks = repository.loadBookmarks()
        val folders = repository.loadBookmarkFolders()
        if (bookmarks.isEmpty()) return AgentToolResult("暂无书签")

        val grouped = bookmarks.groupBy { item ->
            val name = folders.find { it.id == item.folderId }?.name
            name ?: "未分类"
        }
        val sb = StringBuilder("书签列表:\n")
        grouped.forEach { (folder, items) ->
            sb.appendLine("[$folder]")
            items.forEach { item ->
                sb.appendLine("  · ${item.title} - ${item.url} (id: ${item.id})")
            }
        }
        return AgentToolResult(sb.toString().trimEnd())
    }

    private fun createFolder(json: JSONObject): AgentToolResult {
        val folderName = json.optString("folder_name").takeIf { it.isNotBlank() }
            ?: return AgentToolResult("缺少 folder_name 参数", isError = true)

        val folders = repository.loadBookmarkFolders()
        if (folders.any { it.name == folderName }) {
            return AgentToolResult("分类「$folderName」已存在", isError = true)
        }

        val folder = com.shifenmiao.webview.browser.BookmarkFolder(
            id = UUID.randomUUID().toString(),
            name = folderName,
            order = folders.size
        )
        repository.saveBookmarkFolders(folders + folder)
        return AgentToolResult("已创建书签分类: $folderName (id: ${folder.id})")
    }

    private fun deleteFolder(json: JSONObject): AgentToolResult {
        val folderId = json.optString("folder_id").takeIf { it.isNotBlank() }
            ?: return AgentToolResult("缺少 folder_id 参数", isError = true)

        val folders = repository.loadBookmarkFolders()
        val target = folders.find { it.id == folderId }
            ?: return AgentToolResult("分类 $folderId 不存在", isError = true)

        repository.saveBookmarkFolders(folders.filter { it.id != folderId })
        val bookmarks = repository.loadBookmarks().map {
            if (it.folderId == folderId) it.copy(folderId = "") else it
        }
        repository.saveBookmarks(bookmarks)

        return AgentToolResult("已删除分类: ${target.name}，其中书签已移至未分类")
    }

    private fun listFolders(): AgentToolResult {
        val folders = repository.loadBookmarkFolders()
        if (folders.isEmpty()) return AgentToolResult("暂无书签分类")

        val sb = StringBuilder("书签分类:\n")
        folders.forEach { folder ->
            val count = repository.loadBookmarks().count { it.folderId == folder.id }
            sb.appendLine("· ${folder.name} (id: ${folder.id}, $count 个书签)")
        }
        return AgentToolResult(sb.toString().trimEnd())
    }
}
