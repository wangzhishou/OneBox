/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.shifenmiao.model.file

import kotlinx.serialization.Serializable

sealed class AgentFileOperationResult<out T> {
    data class Success<T>(val data: T) : AgentFileOperationResult<T>()
    data class Error(val message: String) : AgentFileOperationResult<Nothing>()
}

@Serializable
data class AgentFileLine(
    val lineNumber: Int,
    val content: String,
)

@Serializable
data class AgentFileContextLine(
    val lineNumber: Int,
    val content: String,
    val isMatch: Boolean,
)

@Serializable
data class AgentFileSearchMatch(
    val lineNumber: Int,
    val line: String,
)

@Serializable
data class AgentFileContextRange(
    val startLine: Int,
    val endLine: Int,
    val lines: List<AgentFileContextLine>,
)

@Serializable
data class AgentFileItem(
    val uri: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val formattedSize: String,
    val mimeType: String?,
    val path: String,
    val lastModified: String,
)

@Serializable
data class AgentBrowseFilesParams(
    val directoryUri: String? = null,
    val limit: Int = 20,
    val includeDirectories: Boolean = true,
    val includeFiles: Boolean = true,
)

@Serializable
data class AgentBrowseFilesData(
    val directoryUri: String,
    val displayPath: String,
    val parentDirectoryUri: String?,
    val returnedCount: Int,
    val items: List<AgentFileItem>,
)

@Serializable
data class AgentLocateFileData(
    val targetUri: String,
    val fileName: String,
    val directoryUri: String,
    val displayPath: String,
    val parentDirectoryUri: String?,
)

@Serializable
data class AgentReadFileParams(
    val fileUri: String,
    val startLine: Int? = null,
    val endLine: Int? = null,
    val maxLength: Int = 4096,
)

@Serializable
data class AgentReadFileData(
    val fileUri: String,
    val displayPath: String,
    val mimeType: String?,
    val totalLines: Int,
    val startLine: Int,
    val endLine: Int,
    val returnedLineCount: Int,
    val totalCharacters: Int,
    val isTruncated: Boolean,
    val lines: List<AgentFileLine>,
)

@Serializable
data class AgentReadMultipleFilesItemParams(
    val fileUri: String,
    val startLine: Int? = null,
    val endLine: Int? = null,
)

@Serializable
data class AgentReadMultipleFilesParams(
    val files: List<AgentReadMultipleFilesItemParams>,
    val maxLengthPerFile: Int = 4096,
)

@Serializable
data class AgentReadMultipleFilesItemData(
    val fileUri: String,
    val displayPath: String,
    val mimeType: String?,
    val totalLines: Int,
    val startLine: Int,
    val endLine: Int,
    val returnedLineCount: Int,
    val totalCharacters: Int,
    val isTruncated: Boolean,
    val lines: List<AgentFileLine>,
)

@Serializable
data class AgentReadMultipleFilesData(
    val returnedCount: Int,
    val items: List<AgentReadMultipleFilesItemData>,
)

@Serializable
data class AgentStatFileData(
    val fileUri: String,
    val displayPath: String,
    val parentDirectoryUri: String?,
    val name: String,
    val exists: Boolean,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val formattedSize: String,
    val mimeType: String?,
    val lastModified: String,
)

@Serializable
data class AgentWorkspaceRootItem(
    val name: String,
    val uri: String,
    val displayPath: String,
    val exists: Boolean,
    val writable: Boolean,
    val description: String,
)

@Serializable
data class AgentWorkspaceRootsData(
    val returnedCount: Int,
    val items: List<AgentWorkspaceRootItem>,
)

@Serializable
data class AgentSearchFileParams(
    val fileUri: String,
    val keyword: String,
    val maxMatches: Int = 20,
    val caseSensitive: Boolean = false,
)

@Serializable
data class AgentSearchFileData(
    val fileUri: String,
    val displayPath: String,
    val keyword: String,
    val caseSensitive: Boolean,
    val totalLines: Int,
    val totalMatches: Int,
    val returnedMatches: Int,
    val matches: List<AgentFileSearchMatch>,
)

@Serializable
data class AgentSearchContextParams(
    val fileUri: String,
    val keyword: String,
    val contextLines: Int = 50,
    val maxMatches: Int = 10,
    val caseSensitive: Boolean = false,
)

@Serializable
data class AgentSearchContextData(
    val fileUri: String,
    val displayPath: String,
    val keyword: String,
    val caseSensitive: Boolean,
    val totalLines: Int,
    val totalMatches: Int,
    val returnedMatches: Int,
    val ranges: List<AgentFileContextRange>,
)

@Serializable
data class AgentEditFileParams(
    val fileUri: String,
    val action: String,
    val newText: String? = null,
    val oldText: String? = null,
    val startLine: Int? = null,
    val endLine: Int? = null,
    val line: Int? = null,
    val replaceAll: Boolean = false,
)

@Serializable
data class AgentEditFileData(
    val action: String,
    val fileUri: String,
    val displayPath: String,
    val parentDirectoryUri: String?,
    val replacedCount: Int,
    val startLine: Int?,
    val endLine: Int?,
    val line: Int?,
    val preview: String,
)

@Serializable
data class AgentManageFileParams(
    val action: String,
    val sourceUri: String? = null,
    val destinationDirectoryUri: String? = null,
    val newName: String? = null,
    val fileName: String? = null,
    val folderName: String? = null,
    val mimeType: String? = null,
    val content: String? = null,
    val append: Boolean = false,
)

@Serializable
data class AgentManageFileData(
    val action: String,
    val affectedUri: String?,
    val targetUri: String?,
    val created: Boolean,
    val displayPath: String?,
    val parentDirectoryUri: String?,
)

@Serializable
data class AgentGlobFilesParams(
    val directoryUri: String? = null,
    val globPattern: String,
    val includeDirectories: Boolean = false,
    val includeFiles: Boolean = true,
    val maxResults: Int = 200,
)

@Serializable
data class AgentGlobFileMatch(
    val uri: String,
    val path: String,
    val relativePath: String,
    val isDirectory: Boolean,
)

@Serializable
data class AgentGlobFilesData(
    val directoryUri: String,
    val displayPath: String,
    val globPattern: String,
    val returnedCount: Int,
    val truncated: Boolean,
    val matches: List<AgentGlobFileMatch>,
)

@Serializable
data class AgentGrepFilesParams(
    val directoryUri: String? = null,
    val globPattern: String? = null,
    val query: String,
    val isRegex: Boolean = false,
    val caseSensitive: Boolean = false,
    val maxMatches: Int = 200,
)

@Serializable
data class AgentGrepFileMatch(
    val fileUri: String,
    val displayPath: String,
    val relativePath: String,
    val lineNumber: Int,
    val line: String,
)

@Serializable
data class AgentGrepFilesData(
    val directoryUri: String,
    val displayPath: String,
    val query: String,
    val isRegex: Boolean,
    val caseSensitive: Boolean,
    val scannedFiles: Int,
    val totalMatches: Int,
    val returnedMatches: Int,
    val truncated: Boolean,
    val matches: List<AgentGrepFileMatch>,
)

@Serializable
data class AgentTextPatchHunk(
    val oldText: String,
    val newText: String,
    val replaceAll: Boolean = false,
)

@Serializable
data class AgentApplyTextPatchParams(
    val fileUri: String,
    val hunks: List<AgentTextPatchHunk>,
)

@Serializable
data class AgentApplyTextPatchData(
    val fileUri: String,
    val displayPath: String,
    val parentDirectoryUri: String?,
    val appliedHunkCount: Int,
    val totalReplacementCount: Int,
    val preview: String,
)

@Serializable
data class AgentRangePatchHunk(
    val startLine: Int,
    val endLine: Int,
    val newText: String,
    val oldText: String? = null,
)

@Serializable
data class AgentApplyRangePatchParams(
    val fileUri: String,
    val hunks: List<AgentRangePatchHunk>,
)

@Serializable
data class AgentApplyRangePatchData(
    val fileUri: String,
    val displayPath: String,
    val parentDirectoryUri: String?,
    val appliedHunkCount: Int,
    val totalReplacementCount: Int,
    val preview: String,
)

@Serializable
data class AgentUnzipFileParams(
    val sourceUri: String,
    val destinationDirectoryUri: String? = null,
)

@Serializable
data class AgentUnzipFileItem(
    val uri: String,
    val path: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
)

@Serializable
data class AgentUnzipFileData(
    val sourceUri: String,
    val destinationDirectoryUri: String,
    val displayPath: String,
    val extractedCount: Int,
    val skippedCount: Int,
    val truncated: Boolean,
    val items: List<AgentUnzipFileItem>,
)

