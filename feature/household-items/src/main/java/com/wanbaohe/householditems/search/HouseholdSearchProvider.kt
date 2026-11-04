package com.wanbaohe.householditems.search

import com.shifenmiao.common.search.GlobalSearchDestination
import com.shifenmiao.common.search.GlobalSearchProvider
import com.shifenmiao.common.search.GlobalSearchRelevance
import com.shifenmiao.common.search.GlobalSearchResult
import com.shifenmiao.common.search.GlobalSearchResultPage
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.service.HouseholdService
import javax.inject.Inject

class HouseholdSearchProvider @Inject constructor(
    private val service: HouseholdService,
) : GlobalSearchProvider {

    override val sourceId: String = SOURCE_ID

    override suspend fun search(query: String, limit: Int): GlobalSearchResultPage {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty() || limit <= 0) return GlobalSearchResultPage(emptyList())

        val items = service.searchItems(normalizedQuery, limit + 1)
        return GlobalSearchResultPage(
            results = items.take(limit).map { item ->
                val description = listOfNotNull(
                    item.locationPath.takeIf { it.isNotBlank() },
                    item.category?.takeIf { it.isNotBlank() },
                    item.note?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                GlobalSearchResult(
                    sourceId = sourceId,
                    id = item.id,
                    title = item.name,
                    description = description,
                    labelRes = R.string.household_title,
                    destination = GlobalSearchDestination.Navigate(
                        Screen.HouseholdItems(
                            Screen.HouseholdItems.Type.EditItem(itemId = item.id)
                        )
                    ),
                    relevance = GlobalSearchRelevance.score(normalizedQuery, item.name, description),
                )
            },
            isTruncated = items.size > limit,
        )
    }

    private companion object {
        const val SOURCE_ID = "household_items"
    }
}
