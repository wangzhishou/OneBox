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

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileMatch
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchPage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

internal class SafWorkspaceFileScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun scan(treeUri: Uri, state: WorkspaceFileScanState): WorkspaceFileSearchPage {
        val rootDocumentId = try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (_: Exception) {
            null
        } ?: return state.toPage()

        val pending = ArrayDeque<PendingSafDirectory>()
        val visitedDirectories = mutableSetOf<String>()
        pending += PendingSafDirectory(rootDocumentId, relativePath = "", depth = 0)

        while (pending.isNotEmpty()) {
            if (!state.canContinue()) return state.toPage()
            val current = pending.removeFirst()
            if (!visitedDirectories.add(current.documentId)) continue
            if (current.depth >= MAX_DEPTH) {
                state.markTruncated()
                continue
            }

            val childrenUri = try {
                DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, current.documentId)
            } catch (_: Exception) {
                null
            } ?: continue
            val cursor = queryChildren(childrenUri) ?: continue

            try {
                cursor.use {
                    val idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    if (idColumn < 0 || nameColumn < 0 || mimeColumn < 0) return@use

                    while (cursor.moveToNext()) {
                        if (!state.visit()) return state.toPage()
                        val documentId = cursor.getString(idColumn) ?: continue
                        val name = cursor.getString(nameColumn)
                            ?.takeIf { it.isNotBlank() }
                            ?: documentId.substringAfterLast('/')
                        val relativePath = childRelativePath(current.relativePath, name)
                        val isDirectory = cursor.getString(mimeColumn) ==
                            DocumentsContract.Document.MIME_TYPE_DIR

                        if (state.matches(name, relativePath)) {
                            val match = WorkspaceFileMatch(
                                uri = DocumentsContract.buildDocumentUriUsingTree(
                                    treeUri,
                                    documentId,
                                ).toString(),
                                name = name,
                                relativePath = relativePath,
                                isDirectory = isDirectory,
                            )
                            if (!state.addResult(match)) return state.toPage()
                        }

                        if (isDirectory) {
                            pending += PendingSafDirectory(
                                documentId = documentId,
                                relativePath = relativePath,
                                depth = current.depth + 1,
                            )
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                continue
            }
        }

        return state.toPage()
    }

    private fun queryChildren(childrenUri: Uri): Cursor? {
        return try {
            context.contentResolver.query(
                childrenUri,
                PROJECTION,
                null,
                null,
                null,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }

    private fun childRelativePath(parent: String, name: String): String {
        return if (parent.isEmpty()) name else "$parent/$name"
    }

    companion object {
        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
    }
}

private data class PendingSafDirectory(
    val documentId: String,
    val relativePath: String,
    val depth: Int,
)
