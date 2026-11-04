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
