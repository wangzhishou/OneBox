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

package com.shifenmiao.database.chat_prompt

import androidx.room.withTransaction
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.ToolBindingEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemPromptLink
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.ChatPrompt
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPromptRepository @Inject constructor(
    private val database: AppDatabase,
    private val drafts: DataDraftHelper,
) {
    private val itemDao = database.itemEntityDao()
    private val promptDao = database.chatPromptDao()
    private val categoryDao = database.categoryDao()
    private val toolBindingDao = database.toolBindingDao()
    private val legacyJson = Json { ignoreUnknownKeys = true }

    fun observeCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun loadEditorSnapshot(draftId: Long): PromptEditorSnapshot {
        val draft = drafts.getById(draftId) ?: return PromptEditorSnapshot()
        val relatedPromptId = draft.relatedEntityId?.takeIf { it > 0 }
        val itemRelation = draft.itemId?.takeIf { it > 0 }
            ?.let { itemDao.getItemById(it).firstOrNull() }
            ?: relatedPromptId
                ?.let { promptDao.getItemIdByPromptId(it) }
                ?.let { itemDao.getItemById(it).firstOrNull() }
        val item = itemRelation?.item
        val linkedPromptId = item?.id?.let { promptDao.getPromptLinkByItemId(it) }
        val prompt = (linkedPromptId ?: relatedPromptId)?.let { promptDao.getPromptById(it) }
            ?: itemRelation?.prompt
        val legacy = decodeLegacyPrompt(draft.data)
        val draftToolNames = decodeStringList(draft.url)
        val promptId = prompt?.id ?: 0
        val selectedToolNames = when {
            draftToolNames != null -> draftToolNames.toSet()
            promptId > 0 -> getBoundToolNames(promptId)
            else -> emptySet()
        }
        val draftCategoryIds = DataDraftHelper.decodeCategoryIds(draft.selectedCategoryIds)

        return PromptEditorSnapshot(
            itemId = item?.id ?: 0,
            promptId = promptId,
            title = legacy?.title?.takeIf { it.isNotBlank() }
                ?: draft.title.ifEmpty { item?.title ?: prompt?.title.orEmpty() },
            description = legacy?.description?.takeIf { it.isNotBlank() }
                ?: draft.description.ifEmpty { item?.description ?: prompt?.description.orEmpty() },
            data = legacy?.prompt?.takeIf { it.isNotBlank() }
                ?: draft.data.ifEmpty { prompt?.prompt.orEmpty() },
            placeholder = legacy?.placeholder ?: prompt?.placeholder.orEmpty(),
            templates = legacy?.templates ?: prompt?.templates,
            selectedCategoryIds = draftCategoryIds.ifEmpty {
                itemRelation?.categories?.mapTo(linkedSetOf()) { it.id }.orEmpty()
            },
            selectedToolNames = selectedToolNames,
            isEditing = item != null || prompt != null,
        )
    }

    suspend fun save(input: PromptSaveInput): PromptSaveResult {
        val draft = input.draftId.takeIf { it > 0 }?.let { drafts.getById(it) }
        val result = database.withTransaction {
            val now = System.currentTimeMillis()
            val relatedPromptId = input.promptId.takeIf { it > 0 }
                ?: draft?.relatedEntityId?.takeIf { it > 0 }
            val linkedItemId = input.itemId.takeIf { it > 0 }
                ?: draft?.itemId?.takeIf { it > 0 }
                ?: relatedPromptId?.let { promptDao.getItemIdByPromptId(it) }
            val existingItem = linkedItemId?.let { itemDao.getItemEntityById(it) }
            val item = existingItem?.copy(
                title = input.title,
                description = input.description,
                iconName = input.title.firstOrNull()?.toString(),
                updatedAt = now,
            ) ?: ItemEntity(
                remoteId = null,
                source = Source.LOCAL,
                listType = ListItemType.PROMPT.id,
                title = input.title,
                description = input.description,
                url = "",
                iconName = input.title.firstOrNull()?.toString(),
                recommend = true,
                createdAt = now,
                updatedAt = now,
                publishedAt = now,
            )
            val itemId = itemDao.upsertItem(item)
            val existingUserState = itemDao.getUserStateByItemId(itemId)
            val userState = existingUserState?.copy(
                canEdit = true,
                updatedAt = now,
            ) ?: ItemUserState(
                itemId = itemId,
                isPinned = true,
                pinnedAt = now,
                canEdit = true,
                updatedAt = now,
            )
            itemDao.upsertUserState(userState)

            val existingPromptId = promptDao.getPromptLinkByItemId(itemId) ?: relatedPromptId
            val existingPrompt = existingPromptId?.let { promptDao.getPromptById(it) }
            val prompt = existingPrompt?.copy(
                title = input.title,
                description = input.description,
                prompt = input.prompt,
                placeholder = input.placeholder,
                templates = input.templates,
                updatedAt = now,
            ) ?: PromptEntity(
                title = input.title,
                description = input.description,
                prompt = input.prompt,
                placeholder = input.placeholder,
                templates = input.templates,
                source = Source.LOCAL,
                updatedAt = now,
            )
            val promptId = promptDao.upsertLocalPrompt(prompt)
            promptDao.insertPromptLink(ItemPromptLink(itemId = itemId, promptId = promptId))

            val categories = ensureCategories(input.selectedCategoryIds, input.defaultCategoryName)
            categoryDao.deleteCategoriesByItemId(itemId)
            categories.forEach { category ->
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(itemId = itemId, categoryId = category.id)
                )
            }

            val toolNames = input.selectedToolNames.toList().sorted()
            replaceToolBindings(promptId, toolNames)
            PromptSaveResult(
                itemId = itemId,
                prompt = prompt.copy(id = promptId),
                selectedCategories = categories,
                selectedToolNames = toolNames.toSet(),
            )
        }
        if (draft != null) {
            try {
                withContext(NonCancellable) {
                    drafts.updateDraft(
                        draftId = draft.id,
                        title = input.title,
                        description = input.description,
                        url = Json.encodeToString(result.selectedToolNames.toList()),
                        data = input.prompt,
                        selectedCategoryIds = result.selectedCategories.mapTo(linkedSetOf()) { it.id },
                        status = DataDraftEntity.STATUS_SUCCESS,
                        itemId = result.itemId,
                        relatedEntityId = result.prompt.id,
                    )
                }
            } catch (error: Exception) {
                android.util.Log.w("LocalPromptRepository", "Could not link prompt draft ${draft.id}", error)
            }
        }
        return result
    }

    suspend fun ensureCategoryByName(name: String): Category {
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) { "Category name cannot be blank" }
        categoryDao.getCategoryByName(normalizedName)?.let { return it }
        val category = Category(name = normalizedName, canEdit = true, source = Source.LOCAL)
        categoryDao.insertOrUpdateCategory(category)
        return categoryDao.getCategoryByName(normalizedName) ?: category
    }

    private suspend fun ensureCategories(
        selectedIds: Set<Int>,
        defaultCategoryName: String,
    ): List<Category> {
        val existingById = categoryDao.getAllCategoriesList().associateBy { it.id }
        val selected = selectedIds.mapNotNull(existingById::get)
        return selected.ifEmpty { listOf(ensureCategoryByName(defaultCategoryName)) }
    }

    private suspend fun getBoundToolNames(promptId: Int): Set<String> =
        toolBindingDao.getToolNames(
            ownerType = ToolBindingEntity.OwnerType.PROMPT,
            ownerId = promptId,
        ).toSet()

    private suspend fun replaceToolBindings(promptId: Int, toolNames: List<String>) {
        toolBindingDao.deleteByOwner(ToolBindingEntity.OwnerType.PROMPT, promptId)
        if (toolNames.isEmpty()) return
        toolBindingDao.insertAll(
            toolNames.distinct().mapIndexed { index, toolName ->
                ToolBindingEntity(
                    ownerType = ToolBindingEntity.OwnerType.PROMPT,
                    ownerId = promptId,
                    toolName = toolName,
                    sortOrder = index,
                )
            }
        )
    }

    private fun decodeLegacyPrompt(raw: String): ChatPrompt? {
        if (raw.isBlank()) return null
        return runCatching {
            val root = legacyJson.parseToJsonElement(raw).jsonObject
            val promptRoot = root["prompt_template"] as? JsonObject
                ?: root.takeIf {
                    "prompt" in it && "title" in it && "placeholder" in it && "templates" in it
                }
                ?: return null
            legacyJson.decodeFromJsonElement<ChatPrompt>(promptRoot).takeIf { !it.prompt.isNullOrBlank() }
        }.getOrNull()
    }

    private fun decodeStringList(raw: String): List<String>? {
        if (raw.isBlank()) return null
        return runCatching { Json.decodeFromString<List<String>>(raw) }.getOrNull()
    }
}

data class PromptEditorSnapshot(
    val itemId: Int = 0,
    val promptId: Int = 0,
    val title: String = "",
    val description: String = "",
    val data: String = "",
    val placeholder: String = "",
    val templates: String? = null,
    val selectedCategoryIds: Set<Int> = emptySet(),
    val selectedToolNames: Set<String> = emptySet(),
    val isEditing: Boolean = false,
)

data class PromptSaveInput(
    val draftId: Long = 0,
    val itemId: Int = 0,
    val promptId: Int = 0,
    val title: String,
    val description: String,
    val prompt: String,
    val placeholder: String = "",
    val templates: String? = null,
    val selectedCategoryIds: Set<Int> = emptySet(),
    val selectedToolNames: Set<String> = emptySet(),
    val defaultCategoryName: String,
)

data class PromptSaveResult(
    val itemId: Int,
    val prompt: PromptEntity,
    val selectedCategories: List<Category>,
    val selectedToolNames: Set<String>,
)
