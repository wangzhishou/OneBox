package com.t8rin.imagetoolbox.core.domain.workspace

interface WorkspaceFileSearchService {
    suspend fun search(query: String, limit: Int): WorkspaceFileSearchPage
}

data class WorkspaceFileSearchPage(
    val results: List<WorkspaceFileMatch>,
    val isTruncated: Boolean = false,
)

data class WorkspaceFileMatch(
    val uri: String,
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
)
