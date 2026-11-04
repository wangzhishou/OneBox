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
