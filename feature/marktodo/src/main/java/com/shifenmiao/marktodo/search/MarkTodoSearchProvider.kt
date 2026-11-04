package com.shifenmiao.marktodo.search

import com.shifenmiao.common.search.GlobalSearchDestination
import com.shifenmiao.common.search.GlobalSearchProvider
import com.shifenmiao.common.search.GlobalSearchRelevance
import com.shifenmiao.common.search.GlobalSearchResult
import com.shifenmiao.common.search.GlobalSearchResultPage
import com.shifenmiao.marktodo.R
import com.shifenmiao.model.todo.MarkTodoServiceInterface
import com.wanbaohe.core.ui.utils.navigation.Screen
import javax.inject.Inject

class MarkTodoSearchProvider @Inject constructor(
    private val service: MarkTodoServiceInterface,
) : GlobalSearchProvider {

    override val sourceId: String = SOURCE_ID

    override suspend fun search(query: String, limit: Int): GlobalSearchResultPage {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty() || limit <= 0) return GlobalSearchResultPage(emptyList())

        val tasks = service.searchTasks(normalizedQuery, limit + 1)
        return GlobalSearchResultPage(
            results = tasks.take(limit).map { task ->
                val description = listOfNotNull(
                    task.note?.takeIf { it.isNotBlank() },
                    task.tags.joinToString(", ").takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                GlobalSearchResult(
                    sourceId = sourceId,
                    id = task.id,
                    title = task.title,
                    description = description,
                    labelRes = R.string.marktodo,
                    destination = GlobalSearchDestination.Navigate(
                        Screen.MarkTodoRouter(
                            Screen.MarkTodoRouter.MarkTodoType.TodoDetail(
                                taskId = task.id,
                                categoryId = task.categoryId,
                            )
                        )
                    ),
                    relevance = GlobalSearchRelevance.score(normalizedQuery, task.title, description),
                )
            },
            isTruncated = tasks.size > limit,
        )
    }

    private companion object {
        const val SOURCE_ID = "marktodo"
    }
}
