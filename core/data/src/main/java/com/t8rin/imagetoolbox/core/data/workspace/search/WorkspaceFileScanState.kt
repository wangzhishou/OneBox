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
