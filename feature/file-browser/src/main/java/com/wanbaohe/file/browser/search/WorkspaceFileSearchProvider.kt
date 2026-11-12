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

package com.wanbaohe.file.browser.search

import com.shifenmiao.common.search.GlobalSearchDestination
import com.shifenmiao.common.search.GlobalSearchProvider
import com.shifenmiao.common.search.GlobalSearchRelevance
import com.shifenmiao.common.search.GlobalSearchResult
import com.shifenmiao.common.search.GlobalSearchResultPage
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceFileSearchProvider @Inject constructor(
    private val searchService: WorkspaceFileSearchService,
) : GlobalSearchProvider {

    override val sourceId: String = SOURCE_ID

    override suspend fun search(query: String, limit: Int): GlobalSearchResultPage {
        if (query.isBlank() || limit <= 0) return GlobalSearchResultPage(emptyList())

        val page = searchService.search(query, limit)
        return GlobalSearchResultPage(
            results = page.results.map { match ->
                GlobalSearchResult(
                    sourceId = sourceId,
                    id = match.uri,
                    title = match.name,
                    description = match.relativePath,
                    labelRes = R.string.file_explorer,
                    destination = GlobalSearchDestination.OpenContent(match.uri),
                    iconName = if (match.isDirectory) "folder" else "description",
                    relevance = GlobalSearchRelevance.score(
                        query = query,
                        title = match.name,
                        description = match.relativePath,
                    ),
                )
            },
            isTruncated = page.isTruncated,
        )
    }

    companion object {
        private const val SOURCE_ID = "workspace_files"
    }
}
