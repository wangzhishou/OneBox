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

import android.os.SystemClock
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileMatch
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchPage
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield

internal const val MAX_VISITED = 6000
internal const val MAX_DEPTH = 12
internal const val TIME_BUDGET_MS = 1500L

private const val YIELD_INTERVAL = 32

internal class WorkspaceFileScanState(
    private val query: String,
    private val resultLimit: Int,
) {
    private val deadlineMs = SystemClock.elapsedRealtime() + TIME_BUDGET_MS
    private val results = mutableListOf<WorkspaceFileMatch>()
    private var visitedCount = 0
    private var isTruncated = false

    suspend fun canContinue(): Boolean {
        currentCoroutineContext().ensureActive()
        if (SystemClock.elapsedRealtime() >= deadlineMs) {
            isTruncated = true
            return false
        }
        return true
    }

    suspend fun visit(): Boolean {
        if (!canContinue()) return false
        if (visitedCount >= MAX_VISITED) {
            isTruncated = true
            return false
        }
        visitedCount++
        if (visitedCount % YIELD_INTERVAL == 0) yield()
        return true
    }

    fun matches(name: String, relativePath: String): Boolean {
        return name.contains(query, ignoreCase = true) ||
            relativePath.contains(query, ignoreCase = true)
    }

    fun addResult(result: WorkspaceFileMatch): Boolean {
        results += result
        if (results.size >= resultLimit) {
            isTruncated = true
            return false
        }
        return true
    }

    fun markTruncated() {
        isTruncated = true
    }

    fun toPage(): WorkspaceFileSearchPage {
        return WorkspaceFileSearchPage(
            results = results.toList(),
            isTruncated = isTruncated,
        )
    }
}
