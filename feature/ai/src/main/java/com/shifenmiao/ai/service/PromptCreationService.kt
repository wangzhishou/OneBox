/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.ai.service

import android.content.Context
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.base.longtask.LongTaskCenter
import com.shifenmiao.base.longtask.LongTaskKind
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AiLanguagePrompt
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.chat_prompt.LocalPromptRepository
import com.shifenmiao.database.chat_prompt.PromptSaveInput
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ChatPrompt
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

data class PromptGenerationPayload(
    val prompt: ChatPrompt? = null,
    val suggestedCategoryNames: List<String> = emptyList(),
    val suggestedToolNames: List<String> = emptyList(),
)

data class PromptGenerationResult(
    val payload: PromptGenerationPayload,
    val cleanedJson: String,
    val errorMessage: String? = null,
    val rawContent: String = "",
    val engineName: String = "",
    val modelName: String = "",
)

data class PromptSavedResult(
    val prompt: ChatPrompt,
    val itemId: Int,
    val selectedCategoryIds: Set<Int>,
    val selectedCategoryNames: List<String>,
    val selectedToolNames: Set<String>,
    val selectedToolSummaries: List<CreatedToolSummary>,
)

@Singleton
class PromptCreationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val creationMetaService: CreationMetaService,
    private val agentToolRegistry: AgentToolRegistry,
    private val aiPromptExecutor: AIPromptExecutor,
    private val activityLog: ActivityLogRecorder,
    private val longTaskCenter: LongTaskCenter,
    private val promptRepository: LocalPromptRepository,
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
            appendLine(
                AiLanguagePrompt.outputInCurrentLanguage(
                    "the title, description, placeholder, prompt text and suggested category names"
                )
            )
        }
    }

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
            rawContent = rawJson,
        )
    }

    suspend fun resolveSuggestedMeta(
        inputText: String,
        prompt: ChatPrompt?,
        aiSuggestedCategoryNames: List<String>,
        aiSuggestedToolNames: List<String>,
    ): CreationMetaSuggestion {
        val heuristicSuggestion = creationMetaService.suggest(
            inputText = inputText,
            title = prompt?.title,
            description = prompt?.description,
        )
        val aiCategoryIds = creationMetaService.resolveSuggestedCategoryIds(aiSuggestedCategoryNames)
        val aiToolNames = creationMetaService.resolveSuggestedToolNames(aiSuggestedToolNames)
        return CreationMetaSuggestion(
            categoryIds = aiCategoryIds + heuristicSuggestion.categoryIds,
            toolNames = aiToolNames + heuristicSuggestion.toolNames,
        )
    }

    private suspend fun savePrompt(
        parsedPrompt: ChatPrompt,
        fallbackInputText: String,
        selectedCategoryIds: Set<Int>,
        selectedToolNames: Set<String>,
    ): PromptSavedResult {
        val finalPrompt = parsedPrompt.copy(
            id = 0,
            title = parsedPrompt.title?.takeIf { it.isNotBlank() } ?: fallbackInputText.trim().take(30),
            description = parsedPrompt.description?.takeIf { it.isNotBlank() } ?: fallbackInputText,
        )
        val saved = promptRepository.save(
            PromptSaveInput(
                title = finalPrompt.title.orEmpty(),
                description = finalPrompt.description.orEmpty(),
                prompt = finalPrompt.prompt.orEmpty(),
                placeholder = finalPrompt.placeholder.orEmpty(),
                templates = finalPrompt.templates,
                selectedCategoryIds = selectedCategoryIds,
                selectedToolNames = selectedToolNames,
                defaultCategoryName = context.getString(R.string.create_ai_chat_prompt_default_category),
            )
        )
        val toolSummaries = saved.selectedToolNames.mapNotNull { toolName ->
            agentToolRegistry.getToolByName(toolName)?.let { tool ->
                CreatedToolSummary(
                    name = tool.name,
                    title = tool.title,
                    summary = tool.summary,
                )
            }
        }
        activityLog.recordPrompt(
            saved.itemId,
            finalPrompt.title.orEmpty(),
            context.getString(R.string.prompt_create),
            Screen.EditPromptItem().id.toString(),
        )
        return PromptSavedResult(
            prompt = DataBaseUtils.promptEntityToPrompt(saved.prompt),
            itemId = saved.itemId,
            selectedCategoryIds = saved.selectedCategories.mapTo(linkedSetOf()) { it.id },
            selectedCategoryNames = saved.selectedCategories.map { it.name },
            selectedToolNames = saved.selectedToolNames,
            selectedToolSummaries = toolSummaries,
        )
    }

    suspend fun createAndSaveFromRequirement(
        userGoal: String,
        categoryHints: List<String> = emptyList(),
        toolHints: List<String> = emptyList(),
    ): PromptSavedResult = longTaskCenter.withTask(
        id = TASK_ID_GENERATION,
        kind = LongTaskKind.AI_GENERATION,
        title = context.getString(R.string.ai_prompt_creating_foreground_title),
    ) {
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
            aiSuggestedToolNames = parsed.payload.suggestedToolNames + toolHints,
        )
        savePrompt(
            parsedPrompt = prompt,
            fallbackInputText = userGoal,
            selectedCategoryIds = meta.categoryIds,
            selectedToolNames = meta.toolNames,
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
            aiSuggestedToolNames = parsed.payload.suggestedToolNames,
        )
        return savePrompt(
            parsedPrompt = prompt,
            fallbackInputText = fallbackText,
            selectedCategoryIds = meta.categoryIds,
            selectedToolNames = meta.toolNames,
        )
    }

    private fun parsePayload(json: String): PromptGenerationPayload {
        if (json.isBlank()) return PromptGenerationPayload()
        return try {
            val root = AppJson.parseToJsonElement(json).jsonObject
            val promptRoot = root["prompt_template"] as? JsonObject ?: root
            val chatPrompt = AppJson.decodeFromJsonElement(ChatPrompt.serializer(), promptRoot)
            PromptGenerationPayload(
                prompt = chatPrompt.takeIf { !it.prompt.isNullOrBlank() },
                suggestedCategoryNames = extractStringList(root, "suggested_categories"),
                suggestedToolNames = extractStringList(root, "suggested_tools"),
            )
        } catch (_: kotlinx.serialization.SerializationException) {
            PromptGenerationPayload()
        } catch (_: Exception) {
            PromptGenerationPayload()
        }
    }

    private fun buildErrorMessage(json: String): String {
        if (json.isBlank()) return context.getString(R.string.create_ai_chat_prompt_error_no_response)
        return try {
            val jsonElement = AppJson.parseToJsonElement(json)
            if (jsonElement is JsonObject) {
                val contentObj = jsonElement["prompt_template"] as? JsonObject ?: jsonElement
                if (!contentObj.containsKey("prompt")) {
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
        toolHints: List<String>,
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

    companion object {
        private const val TASK_ID_GENERATION = "ai.generation.prompt"

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
