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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileMatch
import com.t8rin.imagetoolbox.core.domain.workspace.WorkspaceFileSearchPage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import java.io.File
import javax.inject.Inject

internal class MediaStoreWorkspaceFileScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun scan(root: File, state: WorkspaceFileScanState): WorkspaceFileSearchPage {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return state.toPage()
        val workspacePrefix = root.toMediaStoreRelativePath() ?: return state.toPage()
        if (!state.canContinue()) return state.toPage()

        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val cursor = queryFiles(collection, workspacePrefix) ?: return state.toPage()
        try {
            cursor.use {
                val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                if (idColumn < 0 || nameColumn < 0 || pathColumn < 0) return@use

                while (cursor.moveToNext()) {
                    if (!state.visit()) return state.toPage()
                    val mediaStorePath = cursor.getString(pathColumn) ?: continue
                    if (!mediaStorePath.isWithinWorkspace(workspacePrefix)) continue
                    val name = cursor.getString(nameColumn)?.takeIf { it.isNotBlank() } ?: continue
                    val relativePath = mediaStorePath.removePrefix(workspacePrefix) + name
                    if (!state.matches(name, relativePath)) continue

                    val match = WorkspaceFileMatch(
                        uri = ContentUris.withAppendedId(collection, cursor.getLong(idColumn)).toString(),
                        name = name,
                        relativePath = relativePath,
                        isDirectory = false,
                    )
                    if (!state.addResult(match)) return state.toPage()
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return state.toPage()
        }

        return state.toPage()
    }

    private fun queryFiles(collection: android.net.Uri, workspacePrefix: String): Cursor? {
        return try {
            context.contentResolver.query(
                collection,
                PROJECTION,
                SELECTION,
                arrayOf(workspacePrefix, workspacePrefix.toLikePattern() + "%"),
                null,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun File.toMediaStoreRelativePath(): String? {
        val canonicalRoot = try {
            canonicalFile
        } catch (_: Exception) {
            return null
        }
        val externalRoot = try {
            Environment.getExternalStorageDirectory().canonicalFile
        } catch (_: Exception) {
            return null
        }

        val externalPrefix = externalRoot.path + File.separator
        if (!canonicalRoot.path.startsWith(externalPrefix)) return null
        val relativePath = canonicalRoot.path.removePrefix(externalPrefix)
        if (relativePath.isBlank()) return null

        val segments = relativePath.split(File.separatorChar)
        if (segments.any { it.isEmpty() || it == "." || it == ".." }) return null
        if (segments.first().equals(Environment.DIRECTORY_DOWNLOADS, ignoreCase = true)) return null
        return segments.joinToString("/").trimEnd('/') + "/"
    }

    private fun String.toLikePattern(): String = buildString(length) {
        this@toLikePattern.forEach { char ->
            if (char == LIKE_ESCAPE || char == '%' || char == '_') append(LIKE_ESCAPE)
            append(char)
        }
    }

    private fun String.isWithinWorkspace(workspacePrefix: String): Boolean {
        if (this != workspacePrefix && !startsWith(workspacePrefix)) return false
        return split('/').none { it == ".." }
    }

    companion object {
        private const val LIKE_ESCAPE = '\\'
        private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
        )
        private const val SELECTION = MediaStore.Files.FileColumns.RELATIVE_PATH +
            " = ? OR " + MediaStore.Files.FileColumns.RELATIVE_PATH +
            " LIKE ? ESCAPE '\\'"
    }
}
