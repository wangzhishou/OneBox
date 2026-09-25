package com.shifenmiao.ai.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AiLanguagePrompt
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemPromptLink
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.ChatPrompt
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class PromptGenerationPayload(
    val prompt: ChatPrompt? = null,
    val suggestedCategoryNames: List<String> = emptyList(),
    val suggestedToolNames: List<String> = emptyList()
)

data class PromptGenerationResult(
    val payload: PromptGenerationPayload,
    val cleanedJson: String,
    val errorMessage: String? = null,
    val rawContent: String = "",
    val engineName: String = "",
    val modelName: String = ""
)

data class PromptSavedResult(
    val prompt: ChatPrompt,
    val itemId: Int,
    val selectedCategoryIds: Set<Int>,
    val selectedCategoryNames: List<String>,
    val selectedToolNames: Set<String>,
    val selectedToolSummaries: List<CreatedToolSummary>
)

@Singleton
class PromptCreationService @Inject constructor(
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
        val preset = appDatabase.chatPromptDao()
            .getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_CHAT_PROMPT_CREATE)
        val basePrompt = preset?.prompt ?: loadRawPrompt()
        return buildString {
            appendLine(basePrompt)
            appendLine()
            appendLine(PROMPT_OUTPUT_CONTRACT)
            appendLine()
            // 生成的标题/说明/提示词与分类名都要跟用户语言:
            // 分类名会经 resolveSuggestedCategoryIds 直接落库, 写死中文会在外语库里
            // 凭空建出一批中文分类, 也对不上本地已有的本地化分类。
            appendLine(
                AiLanguagePrompt.outputInCurrentLanguage(
                    "the title, description, placeholder, prompt text and suggested category names"
                )
            )
        }
    }

    /** 与 AgentCreationService 一致: 预设缺失时读 raw(默认英文, zh 有中文变体)。 */
    private fun loadRawPrompt(): String {
        return context.resources
            .openRawResource(com.shifenmiao.database.R.raw.prompt_prompt_create)
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

    fun parseGenerationResult(rawJson: String): PromptGenerationResult {
        val cleanedJson = extractJson(rawJson)
        val payload = parsePayload(cleanedJson)
        val errorMessage = if (payload.prompt?.prompt.isNullOrBlank()) {
            buildErrorMessage(cleanedJson)
        } else {
            null
        }
        return PromptGenerationResult(
            payload = payload,
            cleanedJson = cleanedJson,
            errorMessage = errorMessage,
            rawContent = rawJson
        )
    }

    suspend fun resolveSuggestedMeta(
        inputText: String,
        prompt: ChatPrompt?,
        aiSuggestedCategoryNames: List<String>,
        aiSuggestedToolNames: List<String>
    ): CreationMetaSuggestion {
        val heuristicSuggestion = creationMetaService.suggest(
            inputText = inputText,
            title = prompt?.title,
            description = prompt?.description
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
            draftType = ListItemType.PROMPT.id,
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

    suspend fun savePrompt(
        parsedPrompt: ChatPrompt,
        fallbackInputText: String,
        selectedCategoryIds: Set<Int>,
        selectedToolNames: Set<String>,
        draftId: Long? = null,
        source: Source = Source.LOCAL
    ): PromptSavedResult {
        val finalPrompt = parsedPrompt.copy(
            id = 0,
            title = parsedPrompt.title?.takeIf { it.isNotBlank() } ?: fallbackInputText.trim().take(30),
            description = parsedPrompt.description?.takeIf { it.isNotBlank() } ?: fallbackInputText
        )

        val selectedCategories = creationMetaService.ensureCategories(
            selectedCategoryIds = selectedCategoryIds,
            fallbackCategoryName = context.getString(R.string.create_ai_chat_prompt_default_category)
        )

        val draft = draftId?.let { dataDraftHelper.getById(it) }
        val existingItemId = draft?.itemId?.takeIf { it > 0 }
        val promptDao = appDatabase.chatPromptDao()
        val itemDao = appDatabase.itemEntityDao()
        val categoryDao = appDatabase.categoryDao()

        var itemId = 0
        var promptResourceId = 0
        if (source == Source.LOCAL) {
            itemId = itemDao.upsertItem(
                buildItemEntity(
                    itemId = existingItemId ?: 0,
                    title = finalPrompt.title,
                    description = finalPrompt.description
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
            val existingPromptId = promptDao.getPromptLinkByItemId(itemId)
            val promptEntity = DataBaseUtils.promptToPromptEntity(
                finalPrompt.copy(id = existingPromptId ?: 0),
                source = source
            )
            promptResourceId = promptDao.upsertLocalPrompt(promptEntity)
            promptDao.insertPromptLink(ItemPromptLink(itemId = itemId, promptId = promptResourceId))
            categoryDao.deleteCategoriesByItemId(itemId)
            selectedCategories.forEach { category ->
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(itemId = itemId, categoryId = category.id)
                )
            }
        } else {
            // 非 LOCAL（如纯远端推送）：item 与 prompt 资源解耦，
            // 必须先建/更新 item，再用返回的 itemId 建 link。
            // 不能直接把 promptResourceId 当作 itemId。
            itemId = itemDao.upsertItem(
                buildItemEntity(
                    itemId = existingItemId ?: 0,
                    title = finalPrompt.title,
                    description = finalPrompt.description,
                )
            )
            promptResourceId = promptDao.upsertLocalPrompt(
                DataBaseUtils.promptToPromptEntity(finalPrompt, source = source)
            )
            promptDao.insertPromptLink(ItemPromptLink(itemId = itemId, promptId = promptResourceId))
        }

        toolBindingRepository.replacePromptBindings(
            promptId = promptResourceId,
            toolNames = selectedToolNames.toList()
        )

        draftId?.let {
            dataDraftHelper.updateDraft(
                draftId = it,
                url = gson.toJson(selectedToolNames.toList()),
                selectedCategoryIds = selectedCategoryIds,
                itemId = itemId.takeIf { savedItemId -> savedItemId > 0 },
                relatedEntityId = promptResourceId.takeIf { it > 0 }
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

        return PromptSavedResult(
            prompt = finalPrompt.copy(id = promptResourceId),
            itemId = itemId,
            selectedCategoryIds = selectedCategories.map { it.id }.toSet(),
            selectedCategoryNames = selectedCategories.map { it.name },
            selectedToolNames = selectedToolNames,
            selectedToolSummaries = toolSummaries
        )
    }

    /**
     * 预览：写到本地 prompt（之前用 Source.PREVIEW 区分，现统一用 LOCAL）。
     * 复用现有 LOCAL 行，保证每次预览覆盖前次结果，不留垃圾行。
     */
    suspend fun savePreviewPrompt(
        parsedPrompt: ChatPrompt,
        fallbackInputText: String,
        selectedToolNames: Set<String>
    ): ChatPrompt {
        val finalPrompt = parsedPrompt.copy(
            title = parsedPrompt.title?.takeIf { it.isNotBlank() } ?: fallbackInputText.trim().take(30),
            description = parsedPrompt.description?.takeIf { it.isNotBlank() } ?: fallbackInputText
        )
        val promptDao = appDatabase.chatPromptDao()
        val existingPreview = promptDao.getPromptBySource(Source.LOCAL)
        val previewId = existingPreview?.id ?: 0
        val insertedId = if (previewId > 0) {
            promptDao.upsertLocalPrompt(
                DataBaseUtils.promptToPromptEntity(
                    finalPrompt.copy(id = previewId),
                    source = Source.LOCAL
                )
            )
            previewId
        } else {
            promptDao.upsertLocalPrompt(
                DataBaseUtils.promptToPromptEntity(finalPrompt, source = Source.LOCAL)
            )
        }
        toolBindingRepository.replacePromptBindings(
            promptId = insertedId,
            toolNames = selectedToolNames.toList()
        )
        return finalPrompt.copy(id = insertedId)
    }

    fun serializePromptPayload(
        prompt: ChatPrompt,
        suggestedCategoryNames: List<String> = emptyList(),
        suggestedToolNames: List<String> = emptyList()
    ): String {
        return gson.toJson(
            mapOf(
                "prompt_template" to prompt,
                "suggested_categories" to suggestedCategoryNames,
                "suggested_tools" to suggestedToolNames
            )
        )
    }

    suspend fun createAndSaveFromRequirement(
        userGoal: String,
        categoryHints: List<String> = emptyList(),
        toolHints: List<String> = emptyList()
    ): PromptSavedResult {
        val result = aiPromptExecutor.execute(
            input = buildRequirement(userGoal, categoryHints, toolHints),
            systemPrompt = buildSystemPrompt(),
            billingDesc = context.getString(R.string.create_ai_prompt_consume_points_desc),
        )
        if (!result.isSuccess) {
            error(result.errorMessage ?: "AI generation failed")
        }
        val parsed = parseGenerationResult(result.content)
        val prompt = parsed.payload.prompt
            ?: error(parsed.errorMessage ?: "AI generation failed")
        val meta = resolveSuggestedMeta(
            inputText = userGoal,
            prompt = prompt,
            aiSuggestedCategoryNames = parsed.payload.suggestedCategoryNames,
            aiSuggestedToolNames = parsed.payload.suggestedToolNames + toolHints
        )
        return savePrompt(
            parsedPrompt = prompt,
            fallbackInputText = userGoal,
            selectedCategoryIds = meta.categoryIds,
            selectedToolNames = meta.toolNames
        )
    }

    suspend fun createAndSaveFromPayloadJson(payloadJson: String): PromptSavedResult {
        val parsed = parseGenerationResult(payloadJson)
        val prompt = parsed.payload.prompt
            ?: error(parsed.errorMessage ?: "Invalid prompt payload")
        val fallbackText = prompt.title
            ?.takeIf { it.isNotBlank() }
            ?: prompt.description?.takeIf { it.isNotBlank() }
            ?: "AI Prompt"
        val meta = resolveSuggestedMeta(
            inputText = fallbackText,
            prompt = prompt,
            aiSuggestedCategoryNames = parsed.payload.suggestedCategoryNames,
            aiSuggestedToolNames = parsed.payload.suggestedToolNames
        )
        return savePrompt(
            parsedPrompt = prompt,
            fallbackInputText = fallbackText,
            selectedCategoryIds = meta.categoryIds,
            selectedToolNames = meta.toolNames
        )
    }

    private fun parsePayload(json: String): PromptGenerationPayload {
        if (json.isBlank()) return PromptGenerationPayload()
        return try {
            val root = gson.fromJson(json, JsonObject::class.java) ?: return PromptGenerationPayload()
            val promptRoot = root.getAsJsonObject("prompt_template") ?: root
            val chatPrompt = gson.fromJson(promptRoot, ChatPrompt::class.java)
            PromptGenerationPayload(
                prompt = chatPrompt?.takeIf { !it.prompt.isNullOrBlank() },
                suggestedCategoryNames = extractStringList(root, "suggested_categories"),
                suggestedToolNames = extractStringList(root, "suggested_tools")
            )
        } catch (_: JsonSyntaxException) {
            PromptGenerationPayload()
        } catch (_: Exception) {
            PromptGenerationPayload()
        }
    }

    private fun buildErrorMessage(json: String): String {
        if (json.isBlank()) return context.getString(R.string.create_ai_chat_prompt_error_no_response)
        return try {
            val jsonElement = gson.fromJson(json, JsonElement::class.java)
            if (jsonElement == null) {
                context.getString(R.string.create_ai_chat_prompt_error_unrecognized)
            } else if (jsonElement.isJsonObject) {
                val obj = jsonElement.asJsonObject
                val contentObj = obj.getAsJsonObject("prompt_template") ?: obj
                if (!contentObj.has("prompt")) {
                    context.getString(R.string.create_ai_chat_prompt_error_incomplete)
                } else {
                    context.getString(R.string.create_ai_chat_prompt_error_content)
                }
            } else {
                context.getString(R.string.create_ai_chat_prompt_error_format)
            }
        } catch (_: Exception) {
            context.getString(R.string.create_ai_chat_prompt_error_parse_fallback)
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
                appendLine("Category hints: ${categoryHints.joinToString()}")
            }
            if (toolHints.isNotEmpty()) {
                appendLine("Tool hints: ${toolHints.joinToString()}")
            }
        }
    }

    private fun buildItemEntity(
        itemId: Int,
        title: String?,
        description: String?
    ): ItemEntity {
        val now = System.currentTimeMillis()
        return ItemEntity(
            id = itemId,
            remoteId = null,
            source = Source.LOCAL,
            listType = ListItemType.PROMPT.id,
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

    companion object {
        private val PROMPT_OUTPUT_CONTRACT = """
Use the following top-level structure and output JSON only:
{
  "prompt_template": {
    "id": 0,
    "title": "<prompt title>",
    "description": "<prompt description>",
    "prompt": "<full system prompt>",
    "placeholder": "<input placeholder>",
    "templates": "{}"
  },
  "suggested_categories": ["<category 1>", "<category 2>"],
  "suggested_tools": []
}

Requirements:
1. `suggested_categories`: 1-3 short category names. When one of the category hints in the user's request already fits, reuse that exact name instead of inventing a new one.
2. `suggested_tools`: always return an empty array []. Do not recommend any tool; the user binds tools manually after creation.
3. `prompt_template.prompt` must be complete and directly usable as a conversation template.
4. Do not output comments, explanations or markdown code fences.
""".trimIndent()
    }
}
