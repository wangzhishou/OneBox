package com.t8rin.imagetoolbox.core.data.workspace.search

import com.t8rin.imagetoolbox.core.data.workspace.AppWorkspaceResolver
import com.t8rin.imagetoolbox.core.data.workspace.AppWorkspaceSearchTarget
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchPage
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WorkspaceFileSearchServiceImpl @Inject constructor(
    private val workspaceResolver: AppWorkspaceResolver,
    private val localScanner: LocalWorkspaceFileScanner,
    private val safScanner: SafWorkspaceFileScanner,
    private val mediaStoreScanner: MediaStoreWorkspaceFileScanner,
) : WorkspaceFileSearchService {

    override suspend fun search(query: String, limit: Int): WorkspaceFileSearchPage =
        withContext(Dispatchers.IO) {
            currentCoroutineContext().ensureActive()
            val normalizedQuery = query.trim()
            if (normalizedQuery.isEmpty() || limit <= 0) {
                return@withContext WorkspaceFileSearchPage(emptyList())
            }

            val state = WorkspaceFileScanState(normalizedQuery, limit)
            try {
                when (val target = workspaceResolver.resolveSearchTarget()) {
                    is AppWorkspaceSearchTarget.Local -> {
                        if (localScanner.canScanDirectly(target.root)) {
                            localScanner.scan(target.root, state)
                        } else {
                            mediaStoreScanner.scan(target.root, state)
                        }
                    }

                    is AppWorkspaceSearchTarget.Saf -> safScanner.scan(target.treeUri, state)
                    AppWorkspaceSearchTarget.Unavailable -> state.toPage()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                state.markTruncated()
                state.toPage()
            }
        }
}
