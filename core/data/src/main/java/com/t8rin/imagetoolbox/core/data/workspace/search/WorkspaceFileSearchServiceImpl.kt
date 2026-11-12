/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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
