package com.shifenmiao.common.handle

import com.shifenmiao.core.R
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemWithCategories
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.DefaultDataItem
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ScreenParams
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.item.ItemEntityParams
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.wechat.Wechat
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

enum class ItemScreenAction {
    OPEN,
    EDIT
}

data class ItemScreenResolution(
    val screen: Screen? = null,
    val message: String? = null,
    val listItemType: ListItemType? = null,
    val action: ItemScreenAction = ItemScreenAction.OPEN,
    val supportsResultCallback: Boolean = false,
    val routeKey: String? = null,
    val canonicalName: String? = null,
    val slug: String? = null
) {
    val isSupported: Boolean
        get() = screen != null
}

object ItemScreenResolver {

    fun buildDeeplink(
        itemId: Int,
        action: ItemScreenAction = ItemScreenAction.OPEN,
        slug: String? = null
    ): String {
        val base = buildString {
            append(UrlConstants.DEEP_LINKS_PREFIX)
            append(UrlConstants.DEEP_LINK_HOST_ITEM)
            append('/')
            append(itemId)
            slug
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    append('/')
                    append(it)
                }
        }
        return if (action == ItemScreenAction.OPEN) {
            base
        } else {
            "$base?${UrlConstants.DEEP_LINK_QUERY_ACTION}=${action.name.lowercase()}"
        }
    }

    fun buildDeeplink(
        item: ItemEntity,
        action: ItemScreenAction = ItemScreenAction.OPEN
    ): String = buildDeeplink(
        itemId = item.id,
        action = action,
        slug = buildSlug(item)
    )

    fun resolveForOpen(
        itemWithCategories: ItemWithCategories,
        agent: ItemAgentEntity? = null,
        prompt: PromptEntity? = null,
    ): ItemScreenResolution {
        val item = itemWithCategories.item
        return resolveForOpenInternal(item, agent, prompt)
    }

    fun resolveForOpen(
        itemWithStats: ItemWithCategoriesAndStats,
        agent: ItemAgentEntity? = null,
        prompt: PromptEntity? = null,
    ): ItemScreenResolution = resolveForOpen(
        itemWithCategories = itemWithStats.toItemWithCategories(),
        agent = agent,
        prompt = prompt,
    )

    /**
     * 由调用方预查资源（link → resource）后传入，resolver 本身不接触 DB。
     * - agent: AGENT 类型条目的预查资源行（可为 null，本地 miss）
     * - prompt: PROMPT 类型条目的预查资源行（可为 null，本地 miss）
     */
    fun resolveForOpen(
        item: ItemEntity,
        agent: ItemAgentEntity? = null,
        prompt: PromptEntity? = null,
    ): ItemScreenResolution = resolveForOpenInternal(item, agent, prompt)

    private fun resolveForOpenInternal(
        item: ItemEntity,
        agent: ItemAgentEntity?,
        prompt: PromptEntity?,
    ): ItemScreenResolution {
        val type = ListItemType.fromId(item.listType)
        return when (type) {
            ListItemType.NORMAL -> resolveNormalItem(item)
            ListItemType.WECHAT -> resolveWechatItem(item)
            ListItemType.HTML -> {
                val screen = Screen.PreviewHtml(itemEntityParams = item.toItemEntityParams())
                ItemScreenResolution(
                    screen = screen,
                    listItemType = ListItemType.HTML,
                    supportsResultCallback = true,
                    routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
                    canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
                    slug = buildSlug(item)
                )
            }

            ListItemType.PROMPT -> {
                val localPromptId = prompt?.id
                val remotePromptId = prompt?.remoteId
                if (localPromptId == null && remotePromptId == null) {
                    ItemScreenResolution(
                        message = AppContext.getString(R.string.item_jump_missing_prompt_id),
                        listItemType = ListItemType.PROMPT
                    )
                } else {
                    val screen = Screen.AiChatScreen(
                        conversation = Conversation(
                            promptId = localPromptId,
                            promptRemoteId = remotePromptId,
                            promptDocumentId = prompt?.documentId,
                            appTitle = item.title,
                            placeholder = item.placeholder,
                            entryType = AIConversationEntryType.PROMPT,
                            entryRefId = remotePromptId?.toString().orEmpty(),
                        )
                    )
                    ItemScreenResolution(
                        screen = screen,
                        listItemType = ListItemType.PROMPT,
                        routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
                        canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
                        slug = buildSlug(item)
                    )
                }
            }

            ListItemType.AGENT -> {
                val localAgentId = agent?.id
                val remoteAgentId = agent?.remoteId
                if (localAgentId == null && remoteAgentId == null) {
                    ItemScreenResolution(
                        message = AppContext.getString(R.string.item_jump_missing_agent_id),
                        listItemType = ListItemType.AGENT
                    )
                } else {
                    val screen = Screen.AgentScreen(
                        agent = Agent(
                            id = localAgentId ?: 0,
                            remoteId = remoteAgentId,
                            documentId = agent.documentId,
                            title = item.title,
                            description = item.description,
                            source = agent.source,
                        )
                    )
                    ItemScreenResolution(
                        screen = screen,
                        listItemType = ListItemType.AGENT,
                        routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
                        canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
                        slug = buildSlug(item)
                    )
                }
            }

            ListItemType.BLOG -> {
                val blogId = item.miniProgramId.toIntOrNull()
                if (blogId == null) {
                    ItemScreenResolution(
                        message = AppContext.getString(R.string.item_jump_missing_blog_id),
                        listItemType = ListItemType.BLOG
                    )
                } else {
                    val screen = Screen.BlogDetail(
                        ScreenParams(
                            id = blogId,
                            title = item.title,
                            description = item.description
                        )
                    )
                    ItemScreenResolution(
                        screen = screen,
                        listItemType = ListItemType.BLOG,
                        routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
                        canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
                        slug = buildSlug(item)
                    )
                }
            }

            ListItemType.NOTE -> {
                val screen = Screen.NoteItem(itemEntityParams = item.toItemEntityParams())
                ItemScreenResolution(
                    screen = screen,
                    listItemType = ListItemType.NOTE,
                    supportsResultCallback = true,
                    routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
                    canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
                    slug = buildSlug(item)
                )
            }

            else -> resolveNormalItem(item)
        }
    }

    suspend fun resolveForEdit(
        item: ItemEntity,
        dataDraftHelper: DataDraftHelper
    ): ItemScreenResolution {
        return when (ListItemType.fromId(item.listType)) {
            ListItemType.NOTE -> {
                val draftId = dataDraftHelper.createDraft(
                    draftType = ListItemType.NOTE.id,
                    itemId = item.id
                )
                val screen = Screen.CreateNote(draftId = draftId)
                ItemScreenResolution(
                    screen = screen,
                    listItemType = ListItemType.NOTE,
                    action = ItemScreenAction.EDIT,
                    supportsResultCallback = true,
                    routeKey = buildRouteKey(item, screen, ItemScreenAction.EDIT),
                    canonicalName = buildCanonicalName(item, screen, ItemScreenAction.EDIT),
                    slug = buildSlug(item)
                )
            }

            ListItemType.HTML -> {
                val draftId = dataDraftHelper.createDraft(
                    draftType = ListItemType.HTML.id,
                    itemId = item.id
                )
                val screen = Screen.CreateHtml(draftId = draftId)
                ItemScreenResolution(
                    screen = screen,
                    listItemType = ListItemType.HTML,
                    action = ItemScreenAction.EDIT,
                    supportsResultCallback = true,
                    routeKey = buildRouteKey(item, screen, ItemScreenAction.EDIT),
                    canonicalName = buildCanonicalName(item, screen, ItemScreenAction.EDIT),
                    slug = buildSlug(item)
                )
            }

            ListItemType.PROMPT -> {
                val draftId = dataDraftHelper.createDraft(
                    draftType = ListItemType.PROMPT.id,
                    itemId = item.id
                )
                val screen = Screen.EditPromptItem(draftId = draftId)
                ItemScreenResolution(
                    screen = screen,
                    listItemType = ListItemType.PROMPT,
                    action = ItemScreenAction.EDIT,
                    supportsResultCallback = true,
                    routeKey = buildRouteKey(item, screen, ItemScreenAction.EDIT),
                    canonicalName = buildCanonicalName(item, screen, ItemScreenAction.EDIT),
                    slug = buildSlug(item)
                )
            }

            else -> ItemScreenResolution(
                message = AppContext.getString(R.string.item_jump_edit_unsupported),
                listItemType = ListItemType.fromId(item.listType),
                action = ItemScreenAction.EDIT
            )
        }
    }

    private fun resolveNormalItem(item: ItemEntity): ItemScreenResolution {
        val screen = item.miniProgramId
            .toIntOrNull()
            ?.let { id -> Screen.entries.find { it.id == id } }
        if (screen != null) {
            return ItemScreenResolution(
                screen = screen,
                listItemType = ListItemType.fromId(item.listType),
                routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
                canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
                slug = buildSlug(item)
            )
        }

        if (item.url.isNotBlank()) {
            val screen = Screen.WebView(
                WebViewParams(
                    title = item.title,
                    url = item.url
                )
            )
            return ItemScreenResolution(
                screen = screen,
                listItemType = ListItemType.fromId(item.listType),
                routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
                canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
                slug = buildSlug(item)
            )
        }

        return ItemScreenResolution(
            message = AppContext.getString(R.string.item_jump_no_target),
            listItemType = ListItemType.fromId(item.listType)
        )
    }

    private fun resolveWechatItem(item: ItemEntity): ItemScreenResolution {
        if (!Wechat.isEnabled) {
            return ItemScreenResolution(
                message = AppContext.getString(R.string.item_jump_wechat_unsupported),
                listItemType = ListItemType.WECHAT
            )
        }
        if (!Wechat.isInstalled()) {
            return ItemScreenResolution(
                message = AppContext.getString(R.string.item_jump_wechat_not_installed),
                listItemType = ListItemType.WECHAT
            )
        }

        val screen = Screen.MiniProgram(
            dataItem = DefaultDataItem(
                customId = item.id,
                customTitle = item.title,
                customDescription = item.description,
                customUrl = item.url,
                customMiniProgramId = item.miniProgramId
            ).dataItem
        )
        return ItemScreenResolution(
            screen = screen,
            listItemType = ListItemType.WECHAT,
            routeKey = buildRouteKey(item, screen, ItemScreenAction.OPEN),
            canonicalName = buildCanonicalName(item, screen, ItemScreenAction.OPEN),
            slug = buildSlug(item)
        )
    }

    private fun buildRouteKey(
        item: ItemEntity,
        screen: Screen,
        action: ItemScreenAction
    ): String {
        val type = ListItemType.fromId(item.listType)?.name?.lowercase() ?: "unknown"
        return "item.$type.${screen.routeKey}.${action.name.lowercase()}"
    }

    private fun buildCanonicalName(
        item: ItemEntity,
        screen: Screen,
        action: ItemScreenAction
    ): String {
        return "${buildRouteKey(item, screen, action)}.${item.id}"
    }

    private fun buildSlug(item: ItemEntity): String {
        return item.title
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\u4e00-\\u9fa5]+"), "-")
            .trim('-')
            .ifBlank { "item-${item.id}" }
    }

    fun ItemEntity.toItemEntityParams(): ItemEntityParams {
        return ItemEntityParams(
            id = id,
            title = title,
            description = description,
            url = url.ifEmpty { null },
            listType = listType
        )
    }
}
