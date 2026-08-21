package com.shifenmiao.database.utils

import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemDataKind
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemWithRelation
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.DataItem
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ModelProvider
import com.shifenmiao.model.Source
import com.shifenmiao.model.StrapiImage
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.model.ai.ChatPrompt

object DataBaseUtils {

    fun agentToAgentEntity(
        agent: Agent,
        source: Source = Source.REMOTE,
    ): ItemAgentEntity {
        val remoteId = agent.remoteId?.takeIf { it > 0 } ?: agent.id.takeIf { it > 0 }
        return ItemAgentEntity(
            id = if (source == Source.REMOTE) 0 else agent.id,
            remoteId = remoteId.takeIf { source == Source.REMOTE },
            documentId = agent.documentId?.takeIf { it.isNotBlank() },
            title = agent.title.orEmpty(),
            description = agent.description,
            body = agent.dynamicBody ?: "",
            prompt = agent.prompt ?: "",
            source = source,
        )
    }

    fun agentEntityToAgent(it: ItemAgentEntity): Agent {
        return Agent(
            id = it.id,
            remoteId = it.remoteId,
            documentId = it.documentId,
            title = it.title,
            description = it.description,
            dynamicBody = it.body,
            prompt = it.prompt,
            source = it.source
        )
    }

    fun promptEntityToPrompt(it: PromptEntity): ChatPrompt {
        return ChatPrompt(
            id = it.id,
            remoteId = it.remoteId,
            documentId = it.documentId,
            title = it.title,
            description = it.description,
            prompt = it.prompt,
            templates = it.templates,
            placeholder = it.placeholder,
            source = it.source
        )
    }

    fun promptToPromptEntity(
        chatPrompt: ChatPrompt,
        source: Source = Source.REMOTE
    ): PromptEntity {
        val remoteId = chatPrompt.remoteId?.takeIf { it > 0 } ?: chatPrompt.id.takeIf { it > 0 }
        return PromptEntity(
            id = if (source == Source.REMOTE) 0 else chatPrompt.id,
            remoteId = remoteId.takeIf { source == Source.REMOTE },
            documentId = chatPrompt.documentId?.takeIf { it.isNotBlank() },
            title = chatPrompt.title.orEmpty(),
            description = chatPrompt.description,
            prompt = chatPrompt.prompt,
            templates = chatPrompt.templates,
            placeholder = chatPrompt.placeholder,
            source = source
        )
    }

    private fun dataItemToItemEntity(dataItem: DataItem): ItemEntity {
        val now = System.currentTimeMillis()
        val parseInstant: (String?) -> Long? = { value ->
            value?.takeIf { it.isNotBlank() }
                ?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() }
        }
        val publishedAt = parseInstant(dataItem.publishedAt)
        val updatedAt = parseInstant(dataItem.updatedAt) ?: now
        return ItemEntity(
            id = 0,
            remoteId = dataItem.id.takeIf { it > 0 },
            source = Source.REMOTE,
            listType = dataItem.listType ?: 0,
            title = dataItem.title.orEmpty(),
            description = dataItem.description.orEmpty(),
            url = dataItem.url.orEmpty(),
            miniProgramId = dataItem.miniProgramId.orEmpty(),
            placeholder = dataItem.placeholder.orEmpty(),
            iconPath = getImageThumbnailPath(dataItem.icon),
            iconName = dataItem.iconName.orEmpty(),
            recommend = dataItem.recommend,
            vipLevel = dataItem.vipLevel ?: 0,
            isHighlighted = dataItem.isHighlighted,
            isOnline = dataItem.isOnline,
            isAi = dataItem.isAi,
            createdAt = now,
            updatedAt = updatedAt,
            publishedAt = publishedAt,
            documentId = dataItem.documentId?.takeIf { it.isNotBlank() },
            commentCount = dataItem.commentCount?.takeIf { it >= 0 },
        )
    }

    /**
     * 从网络 DataItem 提取大文本内容到独立的 ItemDataEntity。
     */
    fun dataItemToItemDataEntity(dataItem: DataItem): ItemDataEntity {
        val kind = inferKind(dataItem)
        val data = dataItem.data
        val url = dataItem.url
        return ItemDataEntity(
            remoteId = dataItem.id.takeIf { it > 0 },
            documentId = dataItem.documentId?.takeIf { it.isNotBlank() },
            title = dataItem.title.orEmpty().ifBlank { dataItem.description.orEmpty().take(30) },
            kind = kind,
            data = if (kind == ItemDataKind.URL) null else data,
            url = if (kind == ItemDataKind.URL) url else url?.takeIf { !data.isNullOrBlank() },
            sizeBytes = ((data?.toByteArray()?.size ?: 0) + (url?.toByteArray()?.size ?: 0)).toLong(),
            source = Source.REMOTE,
        )
    }

    /**
     * 仅当远程条目包含有效 item_data 内容时，才生成 ItemDataEntity。
     */
    fun dataItemToItemDataEntityOrNull(dataItem: DataItem): ItemDataEntity? {
        val hasData = !dataItem.data.isNullOrBlank()
        val hasUrl = !dataItem.url.isNullOrBlank()
        if (!hasData && !hasUrl) return null
        return dataItemToItemDataEntity(dataItem)
    }

    fun dataItemToItemWithRelation(dataItem: DataItem): ItemWithRelation {
        return ItemWithRelation(
            item = dataItemToItemEntity(dataItem),
            categories = dataItem.categories?.map { category ->
                com.shifenmiao.database.item.entity.Category(
                    id = 0,
                    name = category.name.orEmpty(),
                    canEdit = category.canEdit,
                    source = Source.REMOTE,
                    updatedAt = System.currentTimeMillis(),
                    documentId = category.documentId?.takeIf { it.isNotBlank() },
                )
            } ?: emptyList()
        ).apply {
            agent = dataItem.agent?.let { agent ->
                ItemAgentEntity(
                    remoteId = (agent.remoteId ?: agent.id).takeIf { it > 0 },
                    documentId = agent.documentId?.takeIf { it.isNotBlank() },
                    title = agent.title.orEmpty(),
                    description = agent.description,
                    header = ModelProvider.provideGson().toJson(agent.header),
                    body = agent.dynamicBody ?: "",
                    prompt = agent.prompt,
                    source = Source.REMOTE,
                )
            }
            prompt = dataItem.prompt?.let { prompt ->
                PromptEntity(
                    remoteId = (prompt.remoteId ?: prompt.id).takeIf { it > 0 },
                    documentId = prompt.documentId?.takeIf { it.isNotBlank() },
                    title = prompt.title.orEmpty(),
                    description = prompt.description,
                    prompt = prompt.prompt,
                    templates = prompt.templates,
                    placeholder = prompt.placeholder,
                    source = Source.REMOTE,
                )
            }
            data = dataItemToItemDataEntityOrNull(dataItem)
        }
    }

    /** 从 listType + 字段特征推断 item_data 的 kind。 */
    private fun inferKind(dataItem: DataItem): ItemDataKind {
        // 1. URL-only
        if (dataItem.data.isNullOrBlank() && !dataItem.url.isNullOrBlank()) return ItemDataKind.URL
        // 2. 按 listType 推断
        val listType = ListItemType.fromId(dataItem.listType)
        return when (listType) {
            ListItemType.HTML -> ItemDataKind.HTML
            ListItemType.NOTE, ListItemType.BLOG -> ItemDataKind.MARKDOWN
            else -> ItemDataKind.HTML
        }
    }

    private fun getImageThumbnailPath(strapiImage: StrapiImage?): String {
        if (strapiImage == null) return ""
        return when {
            strapiImage.formats?.small?.url?.isNotEmpty() == true -> strapiImage.formats?.small?.url!!
            strapiImage.formats?.thumbnail?.url?.isNotEmpty() == true -> strapiImage.formats?.thumbnail?.url!!
            else -> strapiImage.url
        }
    }
}
