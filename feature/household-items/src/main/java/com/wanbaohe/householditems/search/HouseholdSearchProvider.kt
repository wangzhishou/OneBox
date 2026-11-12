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
