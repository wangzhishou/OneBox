package com.shifenmiao.ai.service

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.shifenmiao.base.utils.FileUtils
import com.t8rin.imagetoolbox.core.data.workspace.AppWorkspaceResolver
import com.shifenmiao.model.file.AgentBrowseFilesData
import com.shifenmiao.model.file.AgentBrowseFilesParams
import com.shifenmiao.model.file.AgentApplyTextPatchData
import com.shifenmiao.model.file.AgentApplyRangePatchData
import com.shifenmiao.model.file.AgentApplyRangePatchParams
import com.shifenmiao.model.file.AgentApplyTextPatchParams
import com.shifenmiao.model.file.AgentEditFileData
import com.shifenmiao.model.file.AgentEditFileParams
import com.shifenmiao.model.file.AgentFileContextLine
import com.shifenmiao.model.file.AgentFileContextRange
import com.shifenmiao.model.file.AgentFileItem
import com.shifenmiao.model.file.AgentFileLine
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileSearchMatch
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.file.AgentGlobFileMatch
import com.shifenmiao.model.file.AgentGlobFilesData
import com.shifenmiao.model.file.AgentGlobFilesParams
import com.shifenmiao.model.file.AgentGrepFileMatch
import com.shifenmiao.model.file.AgentGrepFilesData
import com.shifenmiao.model.file.AgentGrepFilesParams
import com.shifenmiao.model.file.AgentLocateFileData
import com.shifenmiao.model.file.AgentManageFileData
import com.shifenmiao.model.file.AgentManageFileParams
import com.shifenmiao.model.file.AgentReadFileData
import com.shifenmiao.model.file.AgentReadFileParams
import com.shifenmiao.model.file.AgentReadMultipleFilesData
import com.shifenmiao.model.file.AgentReadMultipleFilesItemData
import com.shifenmiao.model.file.AgentReadMultipleFilesParams
import com.shifenmiao.model.file.AgentSearchContextData
import com.shifenmiao.model.file.AgentSearchContextParams
import com.shifenmiao.model.file.AgentSearchFileData
import com.shifenmiao.model.file.AgentSearchFileParams
import com.shifenmiao.model.file.AgentStatFileData
import com.shifenmiao.model.file.AgentWorkspaceRootItem
import com.shifenmiao.model.file.AgentWorkspaceRootsData
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.wanbaohe.file.browser.model.FileItem
import com.wanbaohe.file.browser.utils.FileHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentFileServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileHelper: FileHelper,
    private val appWorkspaceResolver: AppWorkspaceResolver,
) : AgentFileService {

    override suspend fun workspaceRoots(): AgentFileOperationResult<AgentWorkspaceRootsData> {
        return withContext(Dispatchers.IO) {
            val workspaceRoot = appWorkspaceResolver.resolve()
            val roots = listOf(workspaceRoot)

            AgentFileOperationResult.Success(
                AgentWorkspaceRootsData(
                    returnedCount = roots.size,
                    items = roots.map { root ->
                        AgentWorkspaceRootItem(
                            name = root.name,
                            uri = root.file.toFileUriString(),
                            displayPath = root.file.absolutePath,
                            exists = root.file.exists(),
                            writable = root.file.canWrite() || root.file.parentFile?.canWrite() == true,
                            description = root.description,
                        )
                    },
                )
            )
        }
    }

    override suspend fun browseFiles(params: AgentBrowseFilesParams): AgentFileOperationResult<AgentBrowseFilesData> {
        val directoryFile = resolveDirectoryOrDefault(params.directoryUri)
            ?: return errorResult("Unable to resolve directory URI to local file path")
        val directoryUri = directoryFile.toFileUri()
        val items = fileHelper.loadFilesFromUri(directoryUri).getOrElse {
            return errorResult(it.message ?: "Unable to browse directory")
        }
        val filteredItems = items
            .filter { (it.isDirectory && params.includeDirectories) || (!it.isDirectory && params.includeFiles) }
            .sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase(Locale.getDefault()) })
            .take(params.limit.coerceIn(1, 100))

        return AgentFileOperationResult.Success(
            AgentBrowseFilesData(
                directoryUri = directoryUri.toString(),
                displayPath = directoryFile.absolutePath,
                parentDirectoryUri = directoryFile.parentFile?.toFileUriString(),
                returnedCount = filteredItems.size,
                items = filteredItems.map { it.toPayload() },
            )
        )
    }

    override suspend fun locateFile(targetUri: String): AgentFileOperationResult<AgentLocateFileData> {
        val targetFile = resolveExistingFile(targetUri)
            ?: return errorResult("Unable to resolve target URI to local file path")
        val directoryFile = if (targetFile.isDirectory) targetFile else targetFile.parentFile ?: targetFile
        return AgentFileOperationResult.Success(
            AgentLocateFileData(
                targetUri = targetFile.toFileUriString(),
                fileName = targetFile.name,
                directoryUri = directoryFile.toFileUriString(),
                displayPath = directoryFile.absolutePath,
                parentDirectoryUri = directoryFile.parentFile?.toFileUriString(),
            )
        )
    }

    override suspend fun globFiles(params: AgentGlobFilesParams): AgentFileOperationResult<AgentGlobFilesData> {
        return withContext(Dispatchers.IO) {
            val root = resolveDirectoryOrDefault(params.directoryUri)
                ?: return@withContext errorResult("Unable to resolve directory URI to local file path")
            if (!root.exists()) return@withContext errorResult("Directory does not exist")
            if (!root.isDirectory) return@withContext errorResult("Target is not a directory")
            val regex = runCatching {
                Pattern.compile(globToRegex(params.globPattern))
            }.getOrElse {
                return@withContext errorResult(it.message ?: "Invalid glob pattern")
            }
            val limit = params.maxResults.coerceIn(1, 2000)
            val matches = mutableListOf<AgentGlobFileMatch>()
            var truncated = false
            root.walkTopDown().drop(1).forEach { file ->
                val relativePath = file.relativeTo(root).invariantSeparatorsPath
                val isIncluded = (file.isDirectory && params.includeDirectories) || (!file.isDirectory && params.includeFiles)
                if (isIncluded && regex.matcher(relativePath).matches()) {
                    if (matches.size >= limit) {
                        truncated = true
                        return@forEach
                    }
                    matches += AgentGlobFileMatch(
                        uri = file.toFileUriString(),
                        path = file.absolutePath,
                        relativePath = relativePath,
                        isDirectory = file.isDirectory,
                    )
                }
            }
            AgentFileOperationResult.Success(
                AgentGlobFilesData(
                    directoryUri = root.toFileUriString(),
                    displayPath = root.absolutePath,
                    globPattern = params.globPattern,
                    returnedCount = matches.size,
                    truncated = truncated,
                    matches = matches,
                )
            )
        }
    }

    override suspend fun grepFiles(params: AgentGrepFilesParams): AgentFileOperationResult<AgentGrepFilesData> {
        return withContext(Dispatchers.IO) {
            val root = resolveDirectoryOrDefault(params.directoryUri)
                ?: return@withContext errorResult("Unable to resolve directory URI to local file path")
            if (!root.exists()) return@withContext errorResult("Directory does not exist")
            if (!root.isDirectory) return@withContext errorResult("Target is not a directory")
            if (params.query.isBlank()) return@withContext errorResult("query is blank")

            val globMatcher = params.globPattern?.takeIf { it.isNotBlank() }?.let {
                runCatching { Pattern.compile(globToRegex(it)) }.getOrElse { error ->
                    return@withContext errorResult(error.message ?: "Invalid glob pattern")
                }
            }
            val regex = if (params.isRegex) {
                runCatching {
                    Pattern.compile(
                        params.query,
                        if (params.caseSensitive) 0 else Pattern.CASE_INSENSITIVE,
                    )
                }.getOrElse { error ->
                    return@withContext errorResult(error.message ?: "Invalid regex pattern")
                }
            } else {
                null
            }
            val keyword = if (params.caseSensitive) params.query else params.query.lowercase()
            val limit = params.maxMatches.coerceIn(1, 5000)
            val matches = mutableListOf<AgentGrepFileMatch>()
            var scannedFiles = 0
            var totalMatches = 0
            var truncated = false

            root.walkTopDown().filter { it.isFile }.forEach { file ->
                if (truncated) return@forEach
                val relativePath = file.relativeTo(root).invariantSeparatorsPath
                if (globMatcher != null && !globMatcher.matcher(relativePath).matches()) return@forEach
                val sample = runCatching { file.inputStream().use { input -> input.readSampleBytes(TEXT_SAMPLE_BYTES) } }.getOrNull()
                    ?: return@forEach
                if (!looksLikeText(sample, file)) return@forEach
                scannedFiles += 1
                val lines = runCatching { file.readText(Charsets.UTF_8).toLineList() }.getOrNull() ?: return@forEach
                lines.forEachIndexed { index, line ->
                    val isMatch = if (regex != null) {
                        regex.matcher(line).find()
                    } else {
                        val candidate = if (params.caseSensitive) line else line.lowercase()
                        candidate.contains(keyword)
                    }
                    if (isMatch) {
                        totalMatches += 1
                        if (matches.size < limit) {
                            matches += AgentGrepFileMatch(
                                fileUri = file.toFileUriString(),
                                displayPath = file.absolutePath,
                                relativePath = relativePath,
                                lineNumber = index + 1,
                                line = line.take(500),
                            )
                        } else {
                            truncated = true
                        }
                    }
                }
            }

            AgentFileOperationResult.Success(
                AgentGrepFilesData(
                    directoryUri = root.toFileUriString(),
                    displayPath = root.absolutePath,
                    query = params.query,
                    isRegex = params.isRegex,
                    caseSensitive = params.caseSensitive,
                    scannedFiles = scannedFiles,
                    totalMatches = totalMatches,
                    returnedMatches = matches.size,
                    truncated = truncated,
                    matches = matches,
                )
            )
        }
    }

    override suspend fun statFile(targetUri: String): AgentFileOperationResult<AgentStatFileData> {
        return withContext(Dispatchers.IO) {
            val file = resolveFile(targetUri)
                ?: return@withContext errorResult("Unable to resolve target URI to local file path")
            val exists = file.exists()
            val isDirectory = exists && file.isDirectory
            val sizeBytes = if (exists && !isDirectory) file.length() else 0L
            AgentFileOperationResult.Success(
                AgentStatFileData(
                    fileUri = file.toFileUriString(),
                    displayPath = file.absolutePath,
                    parentDirectoryUri = file.parentFile?.toFileUriString(),
                    name = file.name,
                    exists = exists,
                    isDirectory = isDirectory,
                    sizeBytes = sizeBytes,
                    formattedSize = formatSize(sizeBytes),
                    mimeType = guessMimeType(file),
                    lastModified = timestampFormatter.format(file.lastModified()),
                )
            )
        }
    }

    override suspend fun readFile(params: AgentReadFileParams): AgentFileOperationResult<AgentReadFileData> {
        return withTextFile(params.fileUri) { file, lines, mimeType ->
            val snapshot = buildReadSnapshot(
                lines = lines,
                startLine = params.startLine,
                endLine = params.endLine,
                maxLength = params.maxLength,
            )
            AgentFileOperationResult.Success(
                AgentReadFileData(
                    fileUri = file.toFileUriString(),
                    displayPath = file.absolutePath,
                    mimeType = mimeType,
                    totalLines = snapshot.totalLines,
                    startLine = snapshot.startLine,
                    endLine = snapshot.endLine,
                    returnedLineCount = snapshot.returnedLineCount,
                    totalCharacters = snapshot.totalCharacters,
                    isTruncated = snapshot.isTruncated,
                    lines = snapshot.lines,
                )
            )
        }
    }

    override suspend fun readMultipleFiles(params: AgentReadMultipleFilesParams): AgentFileOperationResult<AgentReadMultipleFilesData> {
        if (params.files.isEmpty()) return errorResult("files is empty")
        val maxLength = params.maxLengthPerFile.coerceAtLeast(1)
        val items = mutableListOf<AgentReadMultipleFilesItemData>()
        params.files.forEachIndexed { index, item ->
            val result = withTextFile(item.fileUri) { file, lines, mimeType ->
                val snapshot = buildReadSnapshot(
                    lines = lines,
                    startLine = item.startLine,
                    endLine = item.endLine,
                    maxLength = maxLength,
                )
                AgentFileOperationResult.Success(
                    AgentReadMultipleFilesItemData(
                        fileUri = file.toFileUriString(),
                        displayPath = file.absolutePath,
                        mimeType = mimeType,
                        totalLines = snapshot.totalLines,
                        startLine = snapshot.startLine,
                        endLine = snapshot.endLine,
                        returnedLineCount = snapshot.returnedLineCount,
                        totalCharacters = snapshot.totalCharacters,
                        isTruncated = snapshot.isTruncated,
                        lines = snapshot.lines,
                    )
                )
            }
            when (result) {
                is AgentFileOperationResult.Success -> items += result.data
                is AgentFileOperationResult.Error -> return errorResult("File ${index + 1}: ${result.message}")
            }
        }
        return AgentFileOperationResult.Success(
            AgentReadMultipleFilesData(
                returnedCount = items.size,
                items = items,
            )
        )
    }

    override suspend fun searchInFile(params: AgentSearchFileParams): AgentFileOperationResult<AgentSearchFileData> {
        if (params.keyword.isBlank()) return errorResult("keyword is blank")
        return withTextFile(params.fileUri) { file, lines, _ ->
            val matches = findMatches(
                lines = lines,
                keyword = params.keyword,
                caseSensitive = params.caseSensitive,
            )
            val returnedMatches = matches.take(params.maxMatches.coerceIn(1, 100))
            AgentFileOperationResult.Success(
                AgentSearchFileData(
                    fileUri = file.toFileUriString(),
                    displayPath = file.absolutePath,
                    keyword = params.keyword,
                    caseSensitive = params.caseSensitive,
                    totalLines = lines.size,
                    totalMatches = matches.size,
                    returnedMatches = returnedMatches.size,
                    matches = returnedMatches.map { match ->
                        AgentFileSearchMatch(
                            lineNumber = match.lineNumber,
                            line = match.content,
                        )
                    },
                )
            )
        }
    }

    override suspend fun readSearchContext(params: AgentSearchContextParams): AgentFileOperationResult<AgentSearchContextData> {
        if (params.keyword.isBlank()) return errorResult("keyword is blank")
        return withTextFile(params.fileUri) { file, lines, _ ->
            val matches = findMatches(
                lines = lines,
                keyword = params.keyword,
                caseSensitive = params.caseSensitive,
            )
            val limitedMatches = matches.take(params.maxMatches.coerceIn(1, 100))
            val ranges = mergeRanges(
                ranges = limitedMatches.map { match ->
                    val start = (match.lineNumber - params.contextLines.coerceAtLeast(0)).coerceAtLeast(1)
                    val end = (match.lineNumber + params.contextLines.coerceAtLeast(0)).coerceAtMost(lines.size)
                    start..end
                }
            )
            val highlightedLines = limitedMatches.mapTo(mutableSetOf()) { it.lineNumber }

            AgentFileOperationResult.Success(
                AgentSearchContextData(
                    fileUri = file.toFileUriString(),
                    displayPath = file.absolutePath,
                    keyword = params.keyword,
                    caseSensitive = params.caseSensitive,
                    totalLines = lines.size,
                    totalMatches = matches.size,
                    returnedMatches = limitedMatches.size,
                    ranges = ranges.map { range ->
                        AgentFileContextRange(
                            startLine = range.first,
                            endLine = range.last,
                            lines = range.map { lineNumber ->
                                AgentFileContextLine(
                                    lineNumber = lineNumber,
                                    content = lines[lineNumber - 1],
                                    isMatch = highlightedLines.contains(lineNumber),
                                )
                            },
                        )
                    },
                )
            )
        }
    }

    override suspend fun editFile(params: AgentEditFileParams): AgentFileOperationResult<AgentEditFileData> {
        if (params.action.isBlank()) return errorResult("action is blank")
        return withTextFile(params.fileUri) { file, _, _ ->
            val originalText = file.readText(Charsets.UTF_8)
            val lineSeparator = detectLineSeparator(originalText)
            val result = when (params.action) {
                "replace_text" -> replaceText(
                    originalText = originalText,
                    oldText = params.oldText,
                    newText = params.newText,
                    replaceAll = params.replaceAll,
                )
                "replace_lines" -> replaceLines(
                    originalText = originalText,
                    newText = params.newText,
                    startLine = params.startLine,
                    endLine = params.endLine,
                    lineSeparator = lineSeparator,
                )
                "insert_before_line" -> insertAtLine(
                    originalText = originalText,
                    newText = params.newText,
                    line = params.line,
                    insertAfter = false,
                    lineSeparator = lineSeparator,
                )
                "insert_after_line" -> insertAtLine(
                    originalText = originalText,
                    newText = params.newText,
                    line = params.line,
                    insertAfter = true,
                    lineSeparator = lineSeparator,
                )
                "append" -> appendOrPrepend(
                    originalText = originalText,
                    newText = params.newText,
                    append = true,
                )
                "prepend" -> appendOrPrepend(
                    originalText = originalText,
                    newText = params.newText,
                    append = false,
                )
                else -> return@withTextFile errorResult("Unsupported edit action: ${params.action}")
            }

            when (result) {
                is EditResult.Error -> result.toOperationError()
                is EditResult.Success -> {
                    file.parentFile?.mkdirs()
                    file.writeText(result.updatedText, Charsets.UTF_8)
                    AgentFileOperationResult.Success(
                        AgentEditFileData(
                            action = params.action,
                            fileUri = file.toFileUriString(),
                            displayPath = file.absolutePath,
                            parentDirectoryUri = file.parentFile?.toFileUriString(),
                            replacedCount = result.replacedCount,
                            startLine = params.startLine,
                            endLine = params.endLine,
                            line = params.line,
                            preview = result.preview.take(500),
                        )
                    )
                }
            }
        }
    }

    override suspend fun applyTextPatch(params: AgentApplyTextPatchParams): AgentFileOperationResult<AgentApplyTextPatchData> {
        if (params.hunks.isEmpty()) return errorResult("hunks is empty")
        return withTextFile(params.fileUri) { file, _, _ ->
            var content = file.readText(Charsets.UTF_8)
            var appliedHunks = 0
            var replacementCount = 0
            params.hunks.forEachIndexed { index, hunk ->
                if (hunk.oldText.isEmpty()) {
                    return@withTextFile errorResult("Hunk ${index + 1} oldText is blank")
                }
                val occurrences = content.windowed(
                    size = hunk.oldText.length,
                    step = 1,
                    partialWindows = false,
                ).count { it == hunk.oldText }
                if (occurrences == 0) {
                    return@withTextFile errorResult("Hunk ${index + 1} oldText not found")
                }
                content = if (hunk.replaceAll) {
                    content.replace(hunk.oldText, hunk.newText)
                } else {
                    content.replaceFirst(hunk.oldText, hunk.newText)
                }
                appliedHunks += 1
                replacementCount += if (hunk.replaceAll) occurrences else 1
            }
            file.writeText(content, Charsets.UTF_8)
            AgentFileOperationResult.Success(
                AgentApplyTextPatchData(
                    fileUri = file.toFileUriString(),
                    displayPath = file.absolutePath,
                    parentDirectoryUri = file.parentFile?.toFileUriString(),
                    appliedHunkCount = appliedHunks,
                    totalReplacementCount = replacementCount,
                    preview = content.take(1000),
                )
            )
        }
    }

    override suspend fun applyRangePatch(params: AgentApplyRangePatchParams): AgentFileOperationResult<AgentApplyRangePatchData> {
        if (params.hunks.isEmpty()) return errorResult("hunks is empty")
        return withTextFile(params.fileUri) { file, _, _ ->
            val originalText = file.readText(Charsets.UTF_8)
            val lineSeparator = detectLineSeparator(originalText)
            var currentText = originalText
            var appliedHunks = 0
            var replacedLines = 0

            params.hunks.sortedByDescending { it.startLine }.forEachIndexed { index, hunk ->
                val lines = currentText.toMutableLineList()
                if (hunk.startLine <= 0 || hunk.endLine < hunk.startLine) {
                    return@withTextFile errorResult("Hunk ${index + 1} has invalid line range")
                }
                val boundedStart = hunk.startLine.coerceAtMost(lines.size.coerceAtLeast(1))
                val boundedEnd = hunk.endLine.coerceAtMost(lines.size.coerceAtLeast(boundedStart))
                if (lines.isNotEmpty() && boundedStart > lines.size) {
                    return@withTextFile errorResult("Hunk ${index + 1} line range out of bounds")
                }
                if (hunk.oldText != null) {
                    val existing = if (lines.isEmpty()) {
                        ""
                    } else {
                        lines.subList(boundedStart - 1, boundedEnd).joinToString(lineSeparator)
                    }
                    if (existing != hunk.oldText) {
                        return@withTextFile errorResult("Hunk ${index + 1} oldText mismatch")
                    }
                }
                val replacementLines = hunk.newText.splitToLinesPreserveEmpty()
                val updatedLines = buildList {
                    addAll(lines.take((boundedStart - 1).coerceAtLeast(0)))
                    addAll(replacementLines)
                    addAll(lines.drop(boundedEnd))
                }
                currentText = updatedLines.joinToString(lineSeparator)
                appliedHunks += 1
                replacedLines += boundedEnd - boundedStart + 1
            }

            file.writeText(currentText, Charsets.UTF_8)
            AgentFileOperationResult.Success(
                AgentApplyRangePatchData(
                    fileUri = file.toFileUriString(),
                    displayPath = file.absolutePath,
                    parentDirectoryUri = file.parentFile?.toFileUriString(),
                    appliedHunkCount = appliedHunks,
                    totalReplacementCount = replacedLines,
                    preview = currentText.take(1000),
                )
            )
        }
    }

    override suspend fun manageFile(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        return withContext(Dispatchers.IO) {
            runCatching {
                when (params.action) {
                    "delete" -> deletePath(params)
                    "rename" -> renamePath(params)
                    "copy" -> copyPath(params)
                    "move" -> movePath(params)
                    "create_file" -> createFile(params)
                    "create_folder" -> createFolder(params)
                    "write_file" -> writeFile(params)
                    else -> AgentFileOperationResult.Error("Unsupported manage action: ${params.action}")
                }
            }.getOrElse {
                AgentFileOperationResult.Error(it.message ?: "File operation failed")
            }
        }
    }

    private suspend fun <T> withTextFile(
        rawUri: String,
        block: suspend (file: File, lines: List<String>, mimeType: String?) -> AgentFileOperationResult<T>,
    ): AgentFileOperationResult<T> {
        val file = resolveExistingFile(rawUri)
            ?: return errorResult("Unable to resolve file URI to local file path")
        if (file.isDirectory) return errorResult("Target is a directory, expected a file")
        val sample = file.inputStream().use { input -> input.readSampleBytes(TEXT_SAMPLE_BYTES) }
        if (!looksLikeText(sample, file)) {
            return errorResult("Binary or unsupported file type")
        }
        val content = file.readText(Charsets.UTF_8)
        return block(file, content.toLineList(), guessMimeType(file))
    }

    private fun deletePath(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        val source = resolveExistingFile(params.sourceUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve source_uri to local file path")
        val parent = source.parentFile
        deleteRecursively(source)
        return AgentFileOperationResult.Success(
            AgentManageFileData(
                action = "delete",
                affectedUri = source.toFileUriString(),
                targetUri = null,
                created = false,
                displayPath = parent?.absolutePath,
                parentDirectoryUri = parent?.toFileUriString(),
            )
        )
    }

    private fun renamePath(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        val source = resolveExistingFile(params.sourceUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve source_uri to local file path")
        val newName = params.newName?.takeIf { it.isNotBlank() }
            ?: return AgentFileOperationResult.Error("new_name is blank")
        if (!isValidSinglePathSegment(newName)) {
            return AgentFileOperationResult.Error("new_name must be a single path segment")
        }
        val parent = source.parentFile ?: return AgentFileOperationResult.Error("Source has no parent directory")
        val target = File(parent, newName).canonicalFile
        if (target.exists() && target != source.canonicalFile) {
            return AgentFileOperationResult.Error("Target already exists")
        }
        if (!source.renameTo(target)) {
            copyRecursively(source, target)
            deleteRecursively(source)
        }
        return AgentFileOperationResult.Success(
            AgentManageFileData(
                action = "rename",
                affectedUri = source.toFileUriString(),
                targetUri = target.toFileUriString(),
                created = false,
                displayPath = target.absolutePath,
                parentDirectoryUri = parent.toFileUriString(),
            )
        )
    }

    private fun copyPath(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        val source = resolveExistingFile(params.sourceUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve source_uri to local file path")
        val destinationDirectory = resolveDirectoryOrDefault(params.destinationDirectoryUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve destination_dir_uri to local file path")
        if (!destinationDirectory.exists()) destinationDirectory.mkdirs()
        val targetName = params.newName?.takeIf { it.isNotBlank() } ?: source.name
        if (!isValidSinglePathSegment(targetName)) {
            return AgentFileOperationResult.Error("new_name must be a single path segment")
        }
        val target = File(destinationDirectory, targetName).canonicalFile
        if (source.isDirectory && isSameOrDescendant(target, source)) {
            return AgentFileOperationResult.Error("Cannot copy a directory into itself or its child")
        }
        if (target.exists()) {
            return AgentFileOperationResult.Error("Target already exists")
        }
        copyRecursively(source, target)
        return AgentFileOperationResult.Success(
            AgentManageFileData(
                action = "copy",
                affectedUri = source.toFileUriString(),
                targetUri = target.toFileUriString(),
                created = true,
                displayPath = target.absolutePath,
                parentDirectoryUri = destinationDirectory.toFileUriString(),
            )
        )
    }

    private fun movePath(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        val source = resolveExistingFile(params.sourceUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve source_uri to local file path")
        val destinationDirectory = resolveDirectoryOrDefault(params.destinationDirectoryUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve destination_dir_uri to local file path")
        if (!destinationDirectory.exists()) destinationDirectory.mkdirs()
        val targetName = params.newName?.takeIf { it.isNotBlank() } ?: source.name
        if (!isValidSinglePathSegment(targetName)) {
            return AgentFileOperationResult.Error("new_name must be a single path segment")
        }
        val target = File(destinationDirectory, targetName).canonicalFile
        if (source.isDirectory && isSameOrDescendant(target, source)) {
            return AgentFileOperationResult.Error("Cannot move a directory into itself or its child")
        }
        if (target.exists() && target != source.canonicalFile) {
            return AgentFileOperationResult.Error("Target already exists")
        }
        if (!source.renameTo(target)) {
            copyRecursively(source, target)
            deleteRecursively(source)
        }
        return AgentFileOperationResult.Success(
            AgentManageFileData(
                action = "move",
                affectedUri = source.toFileUriString(),
                targetUri = target.toFileUriString(),
                created = false,
                displayPath = target.absolutePath,
                parentDirectoryUri = destinationDirectory.toFileUriString(),
            )
        )
    }

    private fun createFile(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        val fileName = params.fileName?.takeIf { it.isNotBlank() }
            ?: return AgentFileOperationResult.Error("file_name is blank")
        if (!isValidSinglePathSegment(fileName)) {
            return AgentFileOperationResult.Error("file_name must be a single path segment")
        }
        val destinationDirectory = resolveDirectoryOrDefault(params.destinationDirectoryUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve destination_dir_uri to local file path")
        if (!destinationDirectory.exists()) destinationDirectory.mkdirs()
        val target = File(destinationDirectory, fileName).canonicalFile
        target.parentFile?.mkdirs()
        if (!target.exists()) target.createNewFile()
        return AgentFileOperationResult.Success(
            AgentManageFileData(
                action = "create_file",
                affectedUri = null,
                targetUri = target.toFileUriString(),
                created = true,
                displayPath = target.absolutePath,
                parentDirectoryUri = destinationDirectory.toFileUriString(),
            )
        )
    }

    private fun createFolder(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        val folderName = params.folderName?.takeIf { it.isNotBlank() }
            ?: return AgentFileOperationResult.Error("folder_name is blank")
        if (!isValidSinglePathSegment(folderName)) {
            return AgentFileOperationResult.Error("folder_name must be a single path segment")
        }
        val destinationDirectory = resolveDirectoryOrDefault(params.destinationDirectoryUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve destination_dir_uri to local file path")
        val target = File(destinationDirectory, folderName).canonicalFile
        if (!target.exists() && !target.mkdirs()) {
            return AgentFileOperationResult.Error("Unable to create folder")
        }
        return AgentFileOperationResult.Success(
            AgentManageFileData(
                action = "create_folder",
                affectedUri = null,
                targetUri = target.toFileUriString(),
                created = true,
                displayPath = target.absolutePath,
                parentDirectoryUri = destinationDirectory.toFileUriString(),
            )
        )
    }

    private fun writeFile(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData> {
        val source = resolveFile(params.sourceUri)
            ?: return AgentFileOperationResult.Error("Unable to resolve source_uri to local file path")
        val content = params.content ?: return AgentFileOperationResult.Error("content is null")
        source.parentFile?.mkdirs()
        if (params.append) {
            source.appendText(content, Charsets.UTF_8)
        } else {
            source.writeText(content, Charsets.UTF_8)
        }
        return AgentFileOperationResult.Success(
            AgentManageFileData(
                action = "write_file",
                affectedUri = source.toFileUriString(),
                targetUri = source.toFileUriString(),
                created = false,
                displayPath = source.absolutePath,
                parentDirectoryUri = source.parentFile?.toFileUriString(),
            )
        )
    }

    private fun replaceText(
        originalText: String,
        oldText: String?,
        newText: String?,
        replaceAll: Boolean,
    ): EditResult {
        val oldValue = oldText?.takeIf { it.isNotEmpty() } ?: return EditResult.Error("old_text is blank")
        val newValue = newText ?: return EditResult.Error("new_text is null")
        val occurrences = originalText.windowed(size = oldValue.length, step = 1, partialWindows = false)
            .count { it == oldValue }
        if (occurrences == 0) return EditResult.Error("old_text not found")
        val updated = if (replaceAll) {
            originalText.replace(oldValue, newValue)
        } else {
            originalText.replaceFirst(oldValue, newValue)
        }
        return EditResult.Success(
            updatedText = updated,
            replacedCount = if (replaceAll) occurrences else 1,
            preview = newValue,
        )
    }

    private fun replaceLines(
        originalText: String,
        newText: String?,
        startLine: Int?,
        endLine: Int?,
        lineSeparator: String,
    ): EditResult {
        val replacement = newText ?: return EditResult.Error("new_text is null")
        val lines = originalText.toMutableLineList()
        if (lines.isEmpty() && startLine != 1 && endLine != 1) {
            return EditResult.Error("line range out of bounds")
        }
        val start = startLine ?: return EditResult.Error("start_line is null")
        val end = endLine ?: return EditResult.Error("end_line is null")
        if (start <= 0 || end < start) return EditResult.Error("Invalid line range")
        val boundedStart = start.coerceAtMost(lines.size.coerceAtLeast(1))
        val boundedEnd = end.coerceAtMost(lines.size.coerceAtLeast(boundedStart))
        if (boundedStart > lines.size && lines.isNotEmpty()) return EditResult.Error("line range out of bounds")
        val replacementLines = replacement.splitToLinesPreserveEmpty()
        val newLines = buildList {
            addAll(lines.take((boundedStart - 1).coerceAtLeast(0)))
            addAll(replacementLines)
            addAll(lines.drop(boundedEnd))
        }
        return EditResult.Success(
            updatedText = newLines.joinToString(lineSeparator),
            replacedCount = boundedEnd - boundedStart + 1,
            preview = replacement,
        )
    }

    private fun insertAtLine(
        originalText: String,
        newText: String?,
        line: Int?,
        insertAfter: Boolean,
        lineSeparator: String,
    ): EditResult {
        val insertion = newText ?: return EditResult.Error("new_text is null")
        val targetLine = line ?: return EditResult.Error("line is null")
        if (targetLine <= 0) return EditResult.Error("line must be >= 1")
        val lines = originalText.toMutableLineList()
        val insertionLines = insertion.splitToLinesPreserveEmpty()
        val index = if (insertAfter) targetLine else targetLine - 1
        if (index > lines.size) {
            return EditResult.Error("line out of bounds")
        }
        lines.addAll(index, insertionLines)
        return EditResult.Success(
            updatedText = lines.joinToString(lineSeparator),
            replacedCount = insertionLines.size,
            preview = insertion,
        )
    }

    private fun appendOrPrepend(
        originalText: String,
        newText: String?,
        append: Boolean,
    ): EditResult {
        val value = newText ?: return EditResult.Error("new_text is null")
        return EditResult.Success(
            updatedText = if (append) originalText + value else value + originalText,
            replacedCount = 1,
            preview = value,
        )
    }

    private fun mergeRanges(ranges: List<IntRange>): List<IntRange> {
        if (ranges.isEmpty()) return emptyList()
        val sortedRanges = ranges.sortedBy { it.first }
        val merged = mutableListOf(sortedRanges.first())
        sortedRanges.drop(1).forEach { current ->
            val last = merged.last()
            if (current.first <= last.last + 1) {
                merged[merged.lastIndex] = last.first..maxOf(last.last, current.last)
            } else {
                merged += current
            }
        }
        return merged
    }

    private fun findMatches(
        lines: List<String>,
        keyword: String,
        caseSensitive: Boolean,
    ): List<LineMatch> {
        val searchKeyword = if (caseSensitive) keyword else keyword.lowercase()
        return buildList {
            lines.forEachIndexed { index, line ->
                val candidate = if (caseSensitive) line else line.lowercase()
                if (candidate.contains(searchKeyword)) {
                    add(LineMatch(index + 1, line.take(500)))
                }
            }
        }
    }

    private fun buildReadSnapshot(
        lines: List<String>,
        startLine: Int?,
        endLine: Int?,
        maxLength: Int,
    ): ReadSnapshot {
        val totalLines = lines.size
        val safeStartLine = startLine?.coerceAtLeast(1) ?: 1
        val resolvedStartLine = if (totalLines == 0) 1 else safeStartLine.coerceAtMost(totalLines)
        val safeEndLine = endLine ?: totalLines.coerceAtLeast(resolvedStartLine)
        val resolvedEndLine = if (totalLines == 0) {
            resolvedStartLine
        } else {
            safeEndLine.coerceIn(resolvedStartLine, totalLines)
        }
        val selectedLines = if (totalLines == 0) emptyList() else lines.subList(resolvedStartLine - 1, resolvedEndLine)
        val limitedLines = mutableListOf<AgentFileLine>()
        var remaining = maxLength.coerceAtLeast(1)
        var isTruncated = false

        selectedLines.forEachIndexed { index, line ->
            if (remaining <= 0) {
                isTruncated = true
                return@forEachIndexed
            }
            val separatorLength = if (limitedLines.isEmpty()) 0 else 1
            val budget = remaining - separatorLength
            if (budget <= 0) {
                isTruncated = true
                return@forEachIndexed
            }
            if (line.length <= budget) {
                limitedLines += AgentFileLine(
                    lineNumber = resolvedStartLine + index,
                    content = line,
                )
                remaining -= line.length + separatorLength
            } else {
                limitedLines += AgentFileLine(
                    lineNumber = resolvedStartLine + index,
                    content = line.take(budget),
                )
                remaining = 0
                isTruncated = true
            }
        }
        if (!isTruncated && limitedLines.size < selectedLines.size) {
            isTruncated = true
        }

        return ReadSnapshot(
            totalLines = totalLines,
            startLine = resolvedStartLine,
            endLine = resolvedEndLine,
            returnedLineCount = limitedLines.size,
            totalCharacters = selectedLines.sumOf { it.length },
            isTruncated = isTruncated,
            lines = limitedLines,
        )
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1fKB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1fMB", bytes / (1024.0 * 1024.0))
            else -> String.format(Locale.US, "%.1fGB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    private fun looksLikeText(sample: ByteArray, file: File): Boolean {
        val mimeType = guessMimeType(file)
        if (mimeType?.startsWith("text/") == true) return true
        if (mimeType in TEXT_MIME_TYPES) return true
        if (sample.isEmpty()) return true
        if (sample.any { it == 0.toByte() }) return false
        val suspicious = sample.count { byte ->
            val value = byte.toInt() and 0xFF
            value < 0x09 || (value in 0x0E..0x1F)
        }
        return suspicious * 5 < sample.size
    }

    private fun detectLineSeparator(text: String): String {
        return if (text.contains("\r\n")) "\r\n" else "\n"
    }

    private fun guessMimeType(file: File): String? {
        if (file.isDirectory) return null
        val extension = file.extension.lowercase(Locale.getDefault())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: when (extension) {
                "json" -> "application/json"
                "md" -> "text/markdown"
                "kt" -> "text/x-kotlin"
                "java" -> "text/x-java"
                "xml" -> "application/xml"
                "yaml", "yml" -> "application/yaml"
                "csv" -> "text/csv"
                "properties" -> "text/plain"
                else -> null
            }
    }

    private fun resolveDirectoryOrDefault(rawUri: String?): File? {
        return if (rawUri.isNullOrBlank()) {
            Environment.getExternalStorageDirectory().canonicalFile
        } else {
            resolveFile(rawUri)?.takeIf { !it.exists() || it.isDirectory }
        }
    }

    private fun resolveExistingFile(rawUri: String?): File? {
        return resolveFile(rawUri)?.takeIf { it.exists() }
    }

    private fun resolveFile(rawUri: String?): File? {
        return resolveLocalFile(rawUri)
    }

    private fun resolveLocalFile(rawUri: String?): File? {
        if (rawUri.isNullOrBlank()) return null
        val parsed = rawUri.toInputUri()
        return when (parsed.scheme) {
            null, "" -> File(rawUri).canonicalFile
            "file" -> parsed.path?.let(::File)?.canonicalFile
            "content" -> resolveContentUriToFile(parsed)
            else -> null
        }
    }

    private fun isSameOrDescendant(target: File, parent: File): Boolean {
        val parentPath = parent.absolutePath.trimEnd(File.separatorChar)
        val targetPath = target.absolutePath
        return targetPath == parentPath || targetPath.startsWith(parentPath + File.separator)
    }

    private fun isValidSinglePathSegment(name: String): Boolean {
        if (name == "." || name == "..") return false
        return name.none { it == '/' || it == '\\' }
    }

    override suspend fun resolveContentUriToFile(uri: String): String? {
        return withContext(Dispatchers.IO) {
            resolveLocalFile(uri)?.toFileUriString()
        }
    }

    private fun resolveContentUriToFile(uri: Uri): File? {
        val safUri = SafUriUtils.treeUriToFileUri(uri)
            ?: SafUriUtils.documentUriToFileUri(uri)
            ?: FileUtils.getPath(context, uri)?.let(::File)?.takeIf { it.exists() }?.toUri()
            ?: return null
        val path = safUri.path ?: return null
        return File(path).canonicalFile
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach(::deleteRecursively)
        }
        if (file.exists() && !file.delete()) {
            error("Unable to delete ${file.absolutePath}")
        }
    }

    private fun copyRecursively(source: File, target: File) {
        if (source.isDirectory) {
            if (!target.exists() && !target.mkdirs()) {
                error("Unable to create directory ${target.absolutePath}")
            }
            source.listFiles()?.forEach { child ->
                copyRecursively(child, File(target, child.name))
            }
            return
        }
        target.parentFile?.mkdirs()
        source.inputStream().buffered().use { input ->
            target.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }
        target.setLastModified(source.lastModified())
    }

    private fun File.toFileUri(): Uri = Uri.fromFile(canonicalFile)

    private fun File.toFileUriString(): String = toFileUri().toString()


    private fun String.toInputUri(): Uri {
        val uri = toUri()
        return if (uri.scheme.isNullOrBlank()) Uri.fromFile(File(this)) else uri
    }

    private fun String.toLineList(): List<String> {
        if (isEmpty()) return emptyList()
        return split("\r\n", "\n")
    }

    private fun String.toMutableLineList(): MutableList<String> {
        return toLineList().toMutableList()
    }

    private fun String.splitToLinesPreserveEmpty(): List<String> {
        if (isEmpty()) return emptyList()
        return split("\r\n", "\n")
    }

    private fun InputStream.readSampleBytes(limit: Int): ByteArray {
        val buffer = ByteArray(limit)
        var total = 0
        while (total < limit) {
            val count = read(buffer, total, limit - total)
            if (count <= 0) break
            total += count
        }
        return if (total == buffer.size) buffer else buffer.copyOf(total)
    }

    private fun globToRegex(glob: String): String {
        val regex = StringBuilder("^")
        var index = 0
        while (index < glob.length) {
            val char = glob[index]
            when (char) {
                '*' -> {
                    val isDoubleStar = index + 1 < glob.length && glob[index + 1] == '*'
                    if (isDoubleStar) {
                        regex.append(".*")
                        index += 1
                    } else {
                        regex.append("[^/]*")
                    }
                }

                '?' -> regex.append("[^/]")
                '.', '(', ')', '+', '|', '^', '$', '@', '%', '{', '}', '[', ']', '\\' -> {
                    regex.append('\\').append(char)
                }

                else -> regex.append(char)
            }
            index += 1
        }
        regex.append('$')
        return regex.toString()
    }

    private fun FileItem.toPayload(): AgentFileItem {
        return AgentFileItem(
            uri = uri.toFileUriString(),
            name = name,
            isDirectory = isDirectory,
            sizeBytes = size,
            formattedSize = getFormattedSize(),
            mimeType = mimeType,
            path = path,
            lastModified = timestampFormatter.format(lastModified),
        )
    }

    private fun Uri.toFileUriString(): String {
        val converted = SafUriUtils.toFileUri(context, this)
        return (converted ?: this).toString()
    }

    private fun errorResult(message: String): AgentFileOperationResult.Error {
        return AgentFileOperationResult.Error(message)
    }

    private sealed class EditResult {
        data class Success(
            val updatedText: String,
            val replacedCount: Int,
            val preview: String,
        ) : EditResult()

        data class Error(val message: String) : EditResult() {
            fun toOperationError(): AgentFileOperationResult.Error {
                return AgentFileOperationResult.Error(message)
            }
        }
    }

    private data class LineMatch(
        val lineNumber: Int,
        val content: String,
    )

    private data class ReadSnapshot(
        val totalLines: Int,
        val startLine: Int,
        val endLine: Int,
        val returnedLineCount: Int,
        val totalCharacters: Int,
        val isTruncated: Boolean,
        val lines: List<AgentFileLine>,
    )

    companion object {
        private val timestampFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        private const val TEXT_SAMPLE_BYTES = 8 * 1024
        private val TEXT_MIME_TYPES = setOf(
            "application/json",
            "application/xml",
            "application/javascript",
            "application/x-sh",
            "application/yaml",
            "text/markdown",
            "text/csv",
        )
    }
}

