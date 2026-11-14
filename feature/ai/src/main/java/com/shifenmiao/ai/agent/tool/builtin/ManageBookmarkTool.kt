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

package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.interfaces.bookmark.UrlBookmark
import com.shifenmiao.interfaces.bookmark.UrlBookmarkService
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

class ManageBookmarkTool @Inject constructor(
    private val bookmarkService: UrlBookmarkService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    private companion object {
        /** 报错时回给模型的白名单，模型据此重试（schema 里的 enum 有的模型不遵守）。 */
        val ALL_ACTIONS = listOf(
            "add_bookmark", "remove_bookmark", "list_bookmarks",
            "create_folder", "delete_folder", "list_folders",
        )
    }

    override val name = "manage_bookmark"
    override val description = textProvider.raw(R.raw.agent_tool_description_manage_bookmark)
    override val title = textProvider.string(R.string.agent_tool_manage_bookmark_title)
    override val summary = textProvider.string(R.string.agent_tool_manage_bookmark_summary)
    override val category = ToolCategory.BUSINESS
    override val riskLevel = ToolRiskLevel.SAFE

    /** 书签列表默认 20 条/页，最多 100 条；避免长列表撞上工具结果上限被硬截断。 */
    override val maxResultLength: Int = 16_384

    override val keywords = textProvider.array(R.array.agent_tool_manage_bookmark_keywords)
    override val examples = textProvider.array(R.array.agent_tool_manage_bookmark_examples)
    override val parallelizable = false

    override val parametersSchema = ToolParameters(
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_bookmark_param_action),
                enum = listOf("add_bookmark", "remove_bookmark", "list_bookmarks", "create_folder", "delete_folder", "list_folders")
            ),
            "url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_bookmark_param_url)
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_bookmark_param_title)
            ),
            "folder_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_bookmark_param_folder_id)
            ),
            "folder_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_bookmark_param_folder_name)
            ),
            "bookmark_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_bookmark_param_bookmark_id)
            ),
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_list_param_limit)
            ),
            "offset" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_list_param_offset)
            ),
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            when (val action = resolveAction(json)) {
                "add_bookmark" -> addBookmark(json)
                "remove_bookmark" -> removeBookmark(json)
                "list_bookmarks" -> listBookmarks(json)
                "create_folder" -> createFolder(json)
                "delete_folder" -> deleteFolder(json)
                "list_folders" -> listFolders()
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_bookmark_unknown_action,
                        action,
                        ALL_ACTIONS.joinToString(", ")
                    ),
                    isError = true
                )
            }
        }.getOrElse {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_bookmark_failed,
                    it.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    /**
     * 模型常按"用户怎么说"编动作名（add / create / new / remove …），收敛成真实动作。
     *
     * 裸动词按参数判意图：有 url 就是加书签，只有 folder_name 才是建分类 —— 不能一律当 add_bookmark，
     * 否则「新建一个收藏分类」会被当成缺 url 的收藏而报错（线上就是这类失败）。
     */
    private fun resolveAction(json: JSONObject): String {
        val key = json.optString("action")
            .trim()
            .lowercase(Locale.ROOT)
            .replace('-', '_')
            .replace(' ', '_')
        val hasUrl = json.optString("url").isNotBlank()
        val hasFolderName = json.optString("folder_name").isNotBlank()

        return when (key) {
            "add_bookmark", "add", "create", "new", "insert", "save", "save_bookmark",
            "add_favorite", "bookmark", "add_url" ->
                if (!hasUrl && hasFolderName) "create_folder" else "add_bookmark"

            "remove_bookmark", "delete_bookmark", "remove", "delete", "delete_url" -> "remove_bookmark"
            "list_bookmarks", "list", "get_list", "query", "browse", "list_favorite" -> "list_bookmarks"
            "create_folder", "add_folder", "new_folder", "create_category", "add_category",
            "create_collection" -> "create_folder"
            "delete_folder", "remove_folder", "delete_category", "remove_category" -> "delete_folder"
            "list_folders", "list_folder", "list_categories", "get_folders", "folders" -> "list_folders"
            else -> key
        }
    }

    private fun errorResult(resId: Int, vararg args: Any): AgentToolResult = AgentToolResult(
        content = textProvider.string(resId, *args),
        isError = true
    )

    private suspend fun addBookmark(json: JSONObject): AgentToolResult {
        val url = json.optString("url").takeIf { it.isNotBlank() }
            ?: return errorResult(R.string.agent_tool_bookmark_missing_url)
        val title = json.optString("title").takeIf { it.isNotBlank() } ?: url
        val folderId = json.optString("folder_id").takeIf { it.isNotBlank() }?.toLongOrNull()

        if (folderId != null) {
            val folders = bookmarkService.listCategories()
            if (folders.none { it.id == folderId }) {
                return AgentToolResult(
                    textProvider.string(
                        R.string.agent_tool_bookmark_folder_not_found,
                        folderId.toString(),
                        folders.joinToString(", ") { "${it.id}(${it.name})" }
                    ),
                    isError = true
                )
            }
        }

        bookmarkService.findByUrl(url)?.let {
            return AgentToolResult(
                textProvider.string(
                    R.string.agent_tool_bookmark_exists,
                    it.title.orEmpty(),
                    it.itemId.toString()
                ),
                isError = true
            )
        }

        val itemId = bookmarkService.saveBookmark(url, title, folderId)
        val folderName = folderId?.let { id ->
            bookmarkService.listCategories().find { it.id == id }?.name
        } ?: textProvider.string(R.string.agent_tool_bookmark_uncategorized)
        return AgentToolResult(
            textProvider.string(
                R.string.agent_tool_bookmark_added,
                itemId.toString(),
                title,
                url,
                folderName
            )
        )
    }

    private suspend fun removeBookmark(json: JSONObject): AgentToolResult {
        val bookmarkId = json.optString("bookmark_id").takeIf { it.isNotBlank() }
            ?: return errorResult(R.string.agent_tool_bookmark_missing_bookmark_id)

        val bookmarks = bookmarkService.listBookmarks()
        val target = bookmarks.find { it.itemId.toString() == bookmarkId }
            ?: return errorResult(R.string.agent_tool_bookmark_not_found, bookmarkId)

        bookmarkService.removeBookmark(target.itemId)
        return AgentToolResult(
            textProvider.string(
                R.string.agent_tool_bookmark_removed,
                target.title.orEmpty(),
                target.url
            )
        )
    }

    private suspend fun listBookmarks(json: JSONObject): AgentToolResult {
        // 书签按创建时间倒序，最近收藏的在前；分页返回，避免上千条书签一次性灌进上下文。
        val bookmarks = bookmarkService.listBookmarks()
            .sortedWith(compareByDescending<UrlBookmark> { it.createdAt }.thenBy { it.itemId })
        val limit = json.optInt("limit", 20).coerceIn(1, 100)
        val offset = json.optInt("offset", 0).coerceAtLeast(0)
        val page = bookmarks.drop(offset).take(limit)
        val categoryNames = bookmarkService.listCategories().associate { it.id to it.name }
        val data = page.map { item ->
            mapOf(
                "id" to item.itemId,
                "title" to item.title,
                "url" to item.url,
                "category" to (
                    item.categoryIds.firstOrNull()?.let { categoryNames[it] }
                        ?: textProvider.string(R.string.agent_tool_bookmark_uncategorized)
                    ),
            )
        }
        val hasMore = offset + page.size < bookmarks.size
        val result = mapOf(
            "action" to "list_bookmarks",
            "success" to true,
            "count" to bookmarks.size,
            "returned_count" to page.size,
            "offset" to offset,
            "limit" to limit,
            "has_more" to hasMore,
            "next_offset" to if (hasMore) offset + page.size else null,
            "bookmarks" to data,
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun createFolder(json: JSONObject): AgentToolResult {
        val folderName = json.optString("folder_name").takeIf { it.isNotBlank() }
            ?: return errorResult(R.string.agent_tool_bookmark_missing_folder_name)

        bookmarkService.listCategories().find { it.name == folderName }?.let {
            return AgentToolResult(
                textProvider.string(
                    R.string.agent_tool_bookmark_folder_exists,
                    folderName,
                    it.id.toString()
                ),
                isError = true
            )
        }

        val categoryId = bookmarkService.createCategory(folderName)
        return AgentToolResult(
            textProvider.string(
                R.string.agent_tool_bookmark_folder_created,
                folderName,
                categoryId.toString()
            )
        )
    }

    private suspend fun deleteFolder(json: JSONObject): AgentToolResult {
        val folderId = json.optString("folder_id").takeIf { it.isNotBlank() }?.toLongOrNull()
            ?: return errorResult(R.string.agent_tool_bookmark_missing_folder_id)

        val folders = bookmarkService.listCategories()
        val target = folders.find { it.id == folderId }
            ?: return errorResult(R.string.agent_tool_bookmark_not_found, folderId.toString())

        if (!bookmarkService.deleteCategory(folderId)) {
            return AgentToolResult(
                textProvider.string(
                    R.string.agent_tool_bookmark_folder_builtin,
                    target.name.orEmpty()
                ),
                isError = true
            )
        }
        return AgentToolResult(
            textProvider.string(
                R.string.agent_tool_bookmark_folder_deleted,
                target.name.orEmpty()
            )
        )
    }

    private suspend fun listFolders(): AgentToolResult {
        val folders = bookmarkService.listCategories()
        if (folders.isEmpty()) {
            return AgentToolResult(textProvider.string(R.string.agent_tool_bookmark_no_folders))
        }

        val bookmarks = bookmarkService.listBookmarks()
        val sb = StringBuilder(textProvider.string(R.string.agent_tool_bookmark_folder_list_header))
        sb.appendLine()
        folders.forEach { folder ->
            val count = bookmarks.count { folder.id in it.categoryIds }
            sb.appendLine(
                textProvider.string(
                    R.string.agent_tool_bookmark_folder_row,
                    folder.name.orEmpty(),
                    folder.id.toString(),
                    count.toString()
                )
            )
        }
        return AgentToolResult(sb.toString().trimEnd())
    }
}
