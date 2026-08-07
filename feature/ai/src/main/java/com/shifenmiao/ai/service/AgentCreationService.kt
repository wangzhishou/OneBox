package com.shifenmiao.ai.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.shifenmiao.database.item.entity.ItemAgentLink
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.Agent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

data class AgentGenerationPayload(
    val agent: Agent? = null,
    val suggestedCategoryNames: List<String> = emptyList(),
    val suggestedToolNames: List<String> = emptyList()
)

data class AgentGenerationResult(
    val payload: AgentGenerationPayload,
    val cleanedJson: String,
    val errorMessage: String? = null,
    val rawContent: String = "",
    val engineName: String = "",
    val modelName: String = ""
)

data class AgentSavedResult(
    val agent: Agent,
    val itemId: Int,
    val selectedCategoryIds: Set<Int>,
    val selectedCategoryNames: List<String>,
    val selectedToolNames: Set<String>,
    val selectedToolSummaries: List<CreatedToolSummary>
)

data class CreatedToolSummary(
    val name: String,
    val title: String,
    val summary: String
)

@Singleton
class AgentCreationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val dataDraftHelper: DataDraftHelper,
    private val creationMetaService: CreationMetaService,
    private val toolBindingRepository: ToolBindingRepository,
    private val agentToolRegistry: AgentToolRegistry,
    private val aiPromptExecutor: AIPromptExecutor,
    private val gson: Gson
) {

    suspend fun buildSystemPrompt(): String {
        return appDatabase.chatPromptDao()
            .getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_AGENT_CREATE)
            ?.prompt
            ?: loadRawPrompt()
    }

    private fun loadRawPrompt(): String {
        return context.resources
            .openRawResource(com.shifenmiao.database.R.raw.prompt_agent_create)
            .bufferedReader()
            .use { it.readText() }
    }

    fun extractJson(raw: String): String {
        var result = raw.trim()
        if (result.startsWith("```json")) {
            result = result.removePrefix("```json").trimStart()
        } else if (result.startsWith("```")) {
            result = result.removePrefix("```").trimStart()
        }
        if (result.endsWith("```")) {
            result = result.removeSuffix("```").trimEnd()
        }
        return result
    }

    fun parseGenerationResult(rawJson: String, inputText: String): AgentGenerationResult {
        val cleanedJson = extractJson(rawJson)
        val payload = parsePayload(cleanedJson, inputText)
        val errorMessage = if (payload.agent?.dynamicBody.isNullOrBlank()) {
            buildErrorMessage(cleanedJson)
        } else {
            null
        }
        return AgentGenerationResult(
            payload = payload,
            cleanedJson = cleanedJson,
            errorMessage = errorMessage,
            rawContent = rawJson
        )
    }

    suspend fun resolveSuggestedMeta(
        inputText: String,
        agent: Agent?,
        aiSuggestedCategoryNames: List<String>,
        aiSuggestedToolNames: List<String>
    ): CreationMetaSuggestion {
        val heuristicSuggestion = creationMetaService.suggest(
            inputText = inputText,
            title = agent?.title,
            description = agent?.description
        )
        val aiCategoryIds = creationMetaService.resolveSuggestedCategoryIds(aiSuggestedCategoryNames)
        val aiToolNames = creationMetaService.resolveSuggestedToolNames(aiSuggestedToolNames)
        return CreationMetaSuggestion(
            categoryIds = aiCategoryIds + heuristicSuggestion.categoryIds,
            toolNames = aiToolNames + heuristicSuggestion.toolNames
        )
    }

    suspend fun saveDraft(
        draftId: Long?,
        description: String,
        rawJson: String,
        isSuccess: Boolean,
        generatedTitle: String?,
        selectedCategoryIds: Set<Int> = emptySet(),
        selectedToolNames: Set<String> = emptySet(),
        itemId: Int? = null,
        relatedEntityId: Int? = null,
    ): Long {
        val title = generatedTitle?.takeIf { it.isNotBlank() } ?: description.take(30)
        return dataDraftHelper.upsertDraft(
            draftId = draftId ?: 0L,
            draftType = ListItemType.AGENT.id,
            title = title,
            description = description,
            url = gson.toJson(selectedToolNames.toList()),
            data = rawJson,
            selectedCategoryIds = selectedCategoryIds,
            status = if (isSuccess) {
                DataDraftEntity.STATUS_SUCCESS
            } else {
                DataDraftEntity.STATUS_FAILED
            },
            itemId = itemId,
            relatedEntityId = relatedEntityId,
        )
    }

    suspend fun saveAgent(
        parsedAgent: Agent,
        fallbackInputText: String,
        selectedCategoryIds: Set<Int>,
        selectedToolNames: Set<String>,
        draftId: Long? = null,
        source: Source = Source.LOCAL
    ): AgentSavedResult {
        val finalAgent = parsedAgent.copy(
            id = 0,
            title = parsedAgent.title?.takeIf { it.isNotBlank() } ?: fallbackInputText.trim().take(30),
            description = parsedAgent.description?.takeIf { it.isNotBlank() } ?: fallbackInputText,
        )

        val selectedCategories = creationMetaService.ensureCategories(
            selectedCategoryIds = selectedCategoryIds,
            fallbackCategoryName = context.getString(R.string.create_ai_agent_default_category)
        )

        val draft = draftId?.let { dataDraftHelper.getById(it) }
        val existingItemId = draft?.itemId?.takeIf { it > 0 }
        val agentDao = appDatabase.agentDao()
        val itemDao = appDatabase.itemEntityDao()
        val categoryDao = appDatabase.categoryDao()

        var itemId = 0
        var agentResourceId = 0
        if (source == Source.LOCAL) {
            itemId = itemDao.upsertItem(
                buildItemEntity(
                    itemId = existingItemId ?: 0,
                    title = finalAgent.title,
                    description = finalAgent.description,
                )
            )
            val now = System.currentTimeMillis()
            itemDao.upsertUserState(
                ItemUserState(
                    itemId = itemId,
                    isPinned = true,
                    pinnedAt = now,
                    canEdit = true,
                    updatedAt = now,
                )
            )
            val existingAgentId = agentDao.getAgentLinkByItemId(itemId)
            val agentEntity = DataBaseUtils.agentToAgentEntity(
                finalAgent.copy(id = existingAgentId ?: 0),
                source = source
            )
            agentResourceId = agentDao.upsertLocalAgent(agentEntity)
            agentDao.insertAgentLink(ItemAgentLink(itemId = itemId, agentId = agentResourceId))
            categoryDao.deleteCategoriesByItemId(itemId)
            selectedCategories.forEach { category ->
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(itemId = itemId, categoryId = category.id)
                )
            }
        } else {
            // 非 LOCAL（如纯远端推送）：item 与 agent 资源解耦，
            // 必须先建/更新 item，再用返回的 itemId 建 link。
            // 不能直接把 agentResourceId 当作 itemId。
            itemId = itemDao.upsertItem(
                buildItemEntity(
                    itemId = existingItemId ?: 0,
                    title = finalAgent.title,
                    description = finalAgent.description,
                )
            )
            val agentEntity = DataBaseUtils.agentToAgentEntity(finalAgent, source = source)
            agentResourceId = agentDao.upsertLocalAgent(agentEntity)
            agentDao.insertAgentLink(ItemAgentLink(itemId = itemId, agentId = agentResourceId))
        }

        toolBindingRepository.replaceAgentBindings(
            agentId = agentResourceId,
            toolNames = selectedToolNames.toList()
        )

        draftId?.let {
            dataDraftHelper.updateDraft(
                draftId = it,
                url = gson.toJson(selectedToolNames.toList()),
                selectedCategoryIds = selectedCategoryIds,
                itemId = itemId.takeIf { savedItemId -> savedItemId > 0 },
                relatedEntityId = agentResourceId.takeIf { it > 0 }
            )
        }

        val toolSummaries = selectedToolNames.mapNotNull { toolName ->
            agentToolRegistry.getToolByName(toolName)?.let { tool ->
                CreatedToolSummary(
                    name = tool.name,
                    title = tool.title,
                    summary = tool.summary
                )
            }
        }

        return AgentSavedResult(
            agent = finalAgent.copy(id = agentResourceId),
            itemId = itemId,
            selectedCategoryIds = selectedCategories.map { it.id }.toSet(),
            selectedCategoryNames = selectedCategories.map { it.name },
            selectedToolNames = selectedToolNames,
            selectedToolSummaries = toolSummaries
        )
    }

    suspend fun savePreviewAgent(
        parsedAgent: Agent,
        fallbackInputText: String,
        selectedToolNames: Set<String>
    ): Agent {
        val baseTitle = parsedAgent.title?.takeIf { it.isNotBlank() } ?: fallbackInputText.trim().take(30)
        val previewTitle = context.getString(R.string.create_ai_agent_preview_prefix, baseTitle)
        val finalAgent = parsedAgent.copy(
            title = previewTitle,
            description = parsedAgent.description?.takeIf { it.isNotBlank() } ?: fallbackInputText,
            source = Source.PREVIEW,
        )
        val agentDao = appDatabase.agentDao()
        // 相同标题的预览复用同一行，避免同一 Agent 反复预览无限膨胀；
        // 不同标题的预览各自独立，保证历史记录指向正确的 Agent 快照。
        val existingPreview = agentDao.getAgentByTitleAndSource(previewTitle, Source.PREVIEW)
        val previewId = existingPreview?.id ?: 0
        val insertedId = if (previewId > 0) {
            agentDao.upsertLocalAgent(
                DataBaseUtils.agentToAgentEntity(
                    finalAgent.copy(id = previewId),
                    source = Source.PREVIEW
                )
            )
            previewId
        } else {
            agentDao.upsertLocalAgent(
                DataBaseUtils.agentToAgentEntity(finalAgent, source = Source.PREVIEW)
            )
        }
        toolBindingRepository.replaceAgentBindings(
            agentId = insertedId,
            toolNames = selectedToolNames.toList()
        )
        return finalAgent.copy(id = insertedId)
    }

    suspend fun ensureEditDraft(
        agent: Agent,
        fallbackSelectedToolNames: Set<String> = emptySet()
    ): Long {
        val agentResourceId = agent.id
        if (agentResourceId > 0) {
            dataDraftHelper.getLatestByTypeAndRelatedEntityId(
                draftType = ListItemType.AGENT.id,
                relatedEntityId = agentResourceId,
            )?.let { return it.id }
        }

        val agentEntity = agentResourceId.takeIf { it > 0 }
            ?.let { appDatabase.agentDao().getAgentById(it) }
        val editableAgent = agentEntity?.let(DataBaseUtils::agentEntityToAgent)
            ?: agent.takeIf { !it.dynamicBody.isNullOrBlank() }
            ?: error("Agent ${agent.id} not found")

        // 通过资源 ID 反查关联的 item，用于读取分类
        val itemId = agentResourceId.takeIf { it > 0 }
            ?.let { appDatabase.agentDao().getItemIdsByAgentId(it).firstOrNull() }
        val itemWithCategories = itemId?.let {
            appDatabase.itemEntityDao().getItemWithCategoriesById(it).firstOrNull()
        }
        val selectedCategoryIds = itemWithCategories
            ?.categories
            ?.map { it.id }
            ?.toSet()
            .orEmpty()
        val selectedToolNames = agentResourceId.takeIf { it > 0 }
            ?.let { toolBindingRepository.getAgentBoundToolNames(it).orEmpty().toSet() }
            ?.ifEmpty { fallbackSelectedToolNames }
            ?: fallbackSelectedToolNames
        val fallbackDescription = editableAgent.description
            ?.takeIf { it.isNotBlank() }
            ?: editableAgent.title.orEmpty()

        return saveDraft(
            draftId = null,
            description = fallbackDescription,
            rawJson = serializeAgentPayload(editableAgent),
            isSuccess = true,
            generatedTitle = editableAgent.title,
            selectedCategoryIds = selectedCategoryIds,
            selectedToolNames = selectedToolNames,
            itemId = itemWithCategories?.item?.id,
            relatedEntityId = agentResourceId.takeIf { it > 0 },
        )
    }

    fun serializeAgentPayload(
        agent: Agent,
        suggestedCategoryNames: List<String> = emptyList(),
        suggestedToolNames: List<String> = emptyList()
    ): String {
        val root = JsonObject().apply {
            add("agent", JsonObject().apply {
                addProperty("id", agent.id)
                addProperty("title", agent.title)
                addProperty("description", agent.description)
                addProperty("prompt", agent.prompt)
                val body = agent.dynamicBody?.takeIf { it.isNotBlank() }
                if (body != null) {
                    add("body", gson.fromJson(body, JsonElement::class.java))
                }
            })
            add("suggested_categories", gson.toJsonTree(suggestedCategoryNames))
            add("suggested_tools", gson.toJsonTree(suggestedToolNames))
        }
        return gson.toJson(root)
    }

    suspend fun createAndSaveFromRequirement(
        userGoal: String,
        categoryHints: List<String> = emptyList(),
        toolHints: List<String> = emptyList()
    ): AgentSavedResult {
        val requirement = buildRequirement(userGoal, categoryHints, toolHints)
        val result = aiPromptExecutor.execute(
            input = requirement,
            systemPrompt = buildSystemPrompt()
        )
        if (!result.isSuccess) {
            error(result.errorMessage ?: "AI generation failed")
        }
        val parsed = parseGenerationResult(result.content, userGoal)
        val agent = parsed.payload.agent
            ?: error(parsed.errorMessage ?: "AI generation failed")
        val meta = resolveSuggestedMeta(
            inputText = userGoal,
            agent = agent,
            aiSuggestedCategoryNames = parsed.payload.suggestedCategoryNames,
            aiSuggestedToolNames = parsed.payload.suggestedToolNames + toolHints
        )
        return saveAgent(
            parsedAgent = agent,
            fallbackInputText = userGoal,
            selectedCategoryIds = meta.categoryIds,
            selectedToolNames = meta.toolNames
        )
    }

    suspend fun createAndSaveFromPayloadJson(payloadJson: String): AgentSavedResult {
        val parsed = parseGenerationResult(payloadJson, "")
        val agent = parsed.payload.agent
            ?: error(parsed.errorMessage ?: "Invalid agent payload")
        val fallbackText = agent.title
            ?.takeIf { it.isNotBlank() }
            ?: agent.description?.takeIf { it.isNotBlank() }
            ?: "AI Agent"
        val meta = resolveSuggestedMeta(
            inputText = fallbackText,
            agent = agent,
            aiSuggestedCategoryNames = parsed.payload.suggestedCategoryNames,
            aiSuggestedToolNames = parsed.payload.suggestedToolNames
        )
        return saveAgent(
            parsedAgent = agent,
            fallbackInputText = fallbackText,
            selectedCategoryIds = meta.categoryIds,
            selectedToolNames = meta.toolNames
        )
    }

    private fun parsePayload(json: String, inputText: String): AgentGenerationPayload {
        if (json.isBlank()) return AgentGenerationPayload()
        return try {
            val root = gson.fromJson(json, JsonObject::class.java) ?: return AgentGenerationPayload()
            val agentRoot = root.getAsJsonObject("agent") ?: root
            val bodyJson = when {
                agentRoot.has("body") -> gson.toJson(agentRoot.get("body"))
                agentRoot.has("type") || agentRoot.has("component") || agentRoot.has("children") || agentRoot.has("props") -> gson.toJson(agentRoot)
                else -> null
            } ?: return AgentGenerationPayload()

            AgentGenerationPayload(
                agent = Agent(
                    id = agentRoot.get("id")?.takeIf { !it.isJsonNull }?.asInt ?: 0,
                    title = agentRoot.get("title")?.takeIf { !it.isJsonNull }?.asString ?: inputText.take(20),
                    description = agentRoot.get("description")?.takeIf { !it.isJsonNull }?.asString ?: inputText,
                    prompt = agentRoot.get("prompt")?.takeIf { !it.isJsonNull }?.asString,
                    dynamicBody = bodyJson
                ),
                suggestedCategoryNames = extractStringList(root, "suggested_categories"),
                suggestedToolNames = extractStringList(root, "suggested_tools")
            )
        } catch (_: JsonSyntaxException) {
            AgentGenerationPayload()
        } catch (_: Exception) {
            AgentGenerationPayload()
        }
    }

    private fun buildErrorMessage(json: String): String {
        if (json.isBlank()) return context.getString(R.string.create_ai_agent_error_no_response)
        return try {
            val jsonElement = gson.fromJson(json, JsonElement::class.java)
            if (jsonElement == null) {
                context.getString(R.string.create_ai_agent_error_unrecognized)
            } else if (jsonElement.isJsonObject) {
                val obj = jsonElement.asJsonObject
                val contentObj = obj.getAsJsonObject("agent") ?: obj
                if (!contentObj.has("body") && !contentObj.has("type")) {
                    context.getString(R.string.create_ai_agent_error_incomplete)
                } else {
                    context.getString(R.string.create_ai_agent_error_content)
                }
            } else {
                context.getString(R.string.create_ai_agent_error_format)
            }
        } catch (_: Exception) {
            context.getString(R.string.create_ai_agent_error_parse_fallback)
        }
    }

    private fun buildRequirement(
        userGoal: String,
        categoryHints: List<String>,
        toolHints: List<String>
    ): String {
        return buildString {
            appendLine(userGoal)
            if (categoryHints.isNotEmpty()) {
                appendLine("分类提示: ${categoryHints.joinToString()}")
            }
            if (toolHints.isNotEmpty()) {
                appendLine("工具提示: ${toolHints.joinToString()}")
            }
        }
    }

    private fun buildItemEntity(
        itemId: Int,
        title: String?,
        description: String?,
    ): ItemEntity {
        val now = System.currentTimeMillis()
        return ItemEntity(
            id = itemId,
            remoteId = null,
            source = Source.LOCAL,
            listType = ListItemType.AGENT.id,
            title = title.orEmpty(),
            description = description.orEmpty(),
            url = "",
            iconName = title?.firstOrNull()?.toString(),
            recommend = true,
            createdAt = now,
            updatedAt = now,
            publishedAt = now,
        )
    }

}

internal fun extractStringList(root: JsonObject, key: String): List<String> {
    val array = root.getAsJsonArray(key) ?: return emptyList()
    return array.mapNotNull { element ->
        element?.takeIf { it.isJsonPrimitive }?.asString?.trim()?.takeIf(String::isNotEmpty)
    }
}
