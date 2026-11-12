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

import android.net.Uri
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileMatch
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchPage
import java.io.File
import javax.inject.Inject

internal class LocalWorkspaceFileScanner @Inject constructor() {

    fun canScanDirectly(root: File): Boolean {
        val canonicalRoot = root.canonicalOrNull() ?: return false
        if (!canonicalRoot.isReadableDirectory()) return false
        return try {
            canonicalRoot.listFiles() != null
        } catch (_: Exception) {
            false
        }
    }

    suspend fun scan(root: File, state: WorkspaceFileScanState): WorkspaceFileSearchPage {
        val canonicalRoot = root.canonicalOrNull() ?: return state.toPage()
        if (!canonicalRoot.isReadableDirectory()) return state.toPage()

        val pending = ArrayDeque<PendingLocalDirectory>()
        val visitedDirectories = mutableSetOf<String>()
        pending += PendingLocalDirectory(canonicalRoot, relativePath = "", depth = 0)

        while (pending.isNotEmpty()) {
            if (!state.canContinue()) return state.toPage()
            val current = pending.removeFirst()
            val directory = current.file.canonicalOrNull() ?: continue
            if (!directory.isWithin(canonicalRoot)) continue
            if (!visitedDirectories.add(directory.path)) continue
            if (current.depth >= MAX_DEPTH) {
                state.markTruncated()
                continue
            }

            val children = try {
                directory.listFiles()
            } catch (_: Exception) {
                null
            } ?: continue

            for (child in children) {
                if (!state.visit()) return state.toPage()
                val canonicalChild = child.canonicalOrNull() ?: continue
                if (!canonicalChild.isWithin(canonicalRoot)) continue

                val relativePath = childRelativePath(current.relativePath, child.name)
                val isDirectory = canonicalChild.isDirectorySafely()
                if (state.matches(child.name, relativePath)) {
                    val match = WorkspaceFileMatch(
                        uri = Uri.fromFile(child.absoluteFile).toString(),
                        name = child.name,
                        relativePath = relativePath,
                        isDirectory = isDirectory,
                    )
                    if (!state.addResult(match)) return state.toPage()
                }

                if (isDirectory && canonicalChild.isReadableDirectory()) {
                    pending += PendingLocalDirectory(
                        file = canonicalChild,
                        relativePath = relativePath,
                        depth = current.depth + 1,
                    )
                }
            }
        }

        return state.toPage()
    }

    private fun File.canonicalOrNull(): File? {
        return try {
            canonicalFile
        } catch (_: Exception) {
            null
        }
    }

    private fun File.isReadableDirectory(): Boolean {
        return try {
            exists() && isDirectory() && canRead()
        } catch (_: Exception) {
            false
        }
    }

    private fun File.isDirectorySafely(): Boolean {
        return try {
            isDirectory()
        } catch (_: Exception) {
            false
        }
    }

    private fun File.isWithin(root: File): Boolean {
        val candidatePath = path
        val rootPath = root.path
        return candidatePath == rootPath || candidatePath.startsWith(rootPath + File.separator)
    }

    private fun childRelativePath(parent: String, name: String): String {
        return if (parent.isEmpty()) name else "$parent/$name"
    }
}

private data class PendingLocalDirectory(
    val file: File,
    val relativePath: String,
    val depth: Int,
)
