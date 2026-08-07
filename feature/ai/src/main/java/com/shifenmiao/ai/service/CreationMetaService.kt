package com.shifenmiao.ai.service

import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.database.item.dao.CategoryDao
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import javax.inject.Inject
import javax.inject.Singleton

data class CreationMetaSuggestion(
    val categoryIds: Set<Int> = emptySet(),
    val toolNames: Set<String> = emptySet()
)

data class CategoryResolution(
    val category: Category,
    val created: Boolean
)

@Singleton
class CreationMetaService @Inject constructor(
    private val categoryDao: CategoryDao,
    private val agentToolRegistry: AgentToolRegistry
) {

    suspend fun getAllCategoryNames(): List<String> {
        return categoryDao.getAllCategoriesList()
            .map { it.name.trim() }
            .filter(String::isNotEmpty)
            .sorted()
    }

    suspend fun suggest(
        inputText: String,
        title: String?,
        description: String?
    ): CreationMetaSuggestion {
        val query = buildQuery(inputText, title, description)
        val categories = categoryDao.getAllCategoriesList()
        val matchedCategoryIds = categories
            .filter { category ->
                val name = category.name.trim()
                name.isNotBlank() && query.contains(name, ignoreCase = true)
            }
            .map { it.id }
            .toSet()

        val tools = agentToolRegistry.getVisibleTools()
            .filter { ChatWorkingMode.AGENT in it.bootstrapModes }
            .map { it.name }
            .toSet()

        return CreationMetaSuggestion(
            categoryIds = matchedCategoryIds,
            toolNames = tools
        )
    }

    suspend fun resolveSuggestedCategoryIds(categoryNames: List<String>): Set<Int> {
        val normalizedNames = categoryNames
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
        val categoryIds = linkedSetOf<Int>()
        normalizedNames.forEach { name ->
            categoryIds += ensureCategoryByName(name).id
        }
        return categoryIds
    }

    suspend fun resolveSuggestedToolNames(toolNames: List<String>): Set<String> {
        if (toolNames.isEmpty()) return emptySet()

        val visibleToolNames = agentToolRegistry.getVisibleTools()
            .map { it.name }
            .toSet()
        return toolNames
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .filter { it in visibleToolNames }
            .toSet()
    }

    suspend fun ensureCategories(
        selectedCategoryIds: Set<Int>,
        fallbackCategoryName: String
    ): List<Category> {
        val categories = categoryDao.getAllCategoriesList()
        val existingById = categories.associateBy { it.id }
        val selected = selectedCategoryIds.mapNotNull(existingById::get)
        if (selected.isNotEmpty()) return selected

        return listOf(ensureCategoryByName(fallbackCategoryName))
    }

    suspend fun ensureCategoryByName(name: String): Category {
        return resolveCategoryByName(name).category
    }

    suspend fun resolveCategoryByName(name: String): CategoryResolution {
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) { "Category name cannot be blank" }

        categoryDao.getCategoryByName(normalizedName)?.let {
            return CategoryResolution(
                category = it,
                created = false
            )
        }

        val category = Category(
            id = 0,
            name = normalizedName,
            canEdit = true,
            source = Source.LOCAL,
        )
        categoryDao.insertOrUpdateCategory(category)
        return CategoryResolution(
            category = categoryDao.getCategoryByName(normalizedName) ?: category,
            created = true
        )
    }

    private fun buildQuery(
        inputText: String,
        title: String?,
        description: String?
    ): String {
        return listOf(inputText, title.orEmpty(), description.orEmpty())
            .map(String::trim)
            .filter(String::isNotEmpty)
            .joinToString(separator = " ")
    }
}
