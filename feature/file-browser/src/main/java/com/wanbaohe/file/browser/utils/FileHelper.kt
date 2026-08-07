package com.wanbaohe.file.browser.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import com.wanbaohe.file.browser.model.FileItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for file operations and URI handling
 *
 * This class provides utilities for:
 * - Loading files from URIs (content:// and file://)
 * - Converting between different URI schemes
 * - File path resolution and display
 * - File metadata extraction
 * - Basic file operations (copy/move/delete/rename)
 */
@Singleton
class FileHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Loads files from the specified URI
     *
     * @param uri The directory URI to load from. If null, loads from primary storage
     * @return Result containing list of FileItem or an error
     */
    suspend fun loadFilesFromUri(uri: Uri?): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            if (uri == null) {
                // Load from default directory (primary storage)
                loadFromDefaultDirectory()
            } else {
                when (uri.scheme) {
                    "content" -> loadFromContentUri(uri)
                    "file" -> loadFromFileUri(uri)
                    else -> Result.failure(IllegalArgumentException("Unsupported URI scheme: ${uri.scheme}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Finds the directory containing a file and returns both the directory URI and file name
     *
     * @param fileUri The URI of the file to locate
     * @return Result containing a Pair of (directory URI, file name) or an error
     */
    suspend fun findFileDirectory(fileUri: Uri): Result<Pair<Uri, String>> = withContext(Dispatchers.IO) {
        try {
            when (fileUri.scheme) {
                "content" -> findContentUriDirectory(fileUri)
                "file" -> findFileUriDirectory(fileUri)
                else -> Result.failure(IllegalArgumentException("Unsupported URI scheme: ${fileUri.scheme}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the parent URI of the given URI
     *
     * @param uri The current URI
     * @return The parent directory URI, or null if at root
     */
    fun getParentUri(uri: Uri?): Uri? {
        if (uri == null) return null

        return when (uri.scheme) {
            "content" -> getParentContentUri(uri)
            "file" -> getParentFileUri(uri)
            else -> null
        }
    }

    /**
     * Gets a display-friendly path string for the given URI
     *
     * @param uri The URI to convert to a display path
     * @return A human-readable path string
     */
    fun getDisplayPath(uri: Uri?): String {
        if (uri == null) {
            return Environment.getExternalStorageDirectory().absolutePath
        }

        return when (uri.scheme) {
            "content" -> getContentUriDisplayPath(uri)
            "file" -> uri.path ?: "/"
            else -> uri.toString()
        }
    }

    suspend fun isDirectoryUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        when (uri.scheme) {
            "content" -> runCatching {
                context.contentResolver.getType(uri) == DocumentsContract.Document.MIME_TYPE_DIR
            }.getOrDefault(false)

            "file" -> uri.path
                ?.let { runCatching { File(it).isDirectory }.getOrDefault(false) }
                ?: false

            else -> false
        }
    }

    /**
     * Loads files from the default directory (primary external storage)
     */
    private fun loadFromDefaultDirectory(): Result<List<FileItem>> {
        val defaultDir = Environment.getExternalStorageDirectory()
        return loadFromFile(defaultDir)
    }

    /**
     * Deletes a document/file.
     */
    suspend fun delete(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            when (uri.scheme) {
                "content" -> {
                    val ok = DocumentsContract.deleteDocument(context.contentResolver, uri)
                    if (!ok) error("deleteDocument returned false")
                }

                "file" -> {
                    val file = File(uri.path ?: error("Invalid file uri"))
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                }

                else -> error("Unsupported Uri scheme: ${uri.scheme}")
            }
            Unit
        }
    }

    /**
     * Renames a document/file and returns the new Uri.
     */
    suspend fun rename(uri: Uri, newName: String): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            require(newName.isNotBlank()) { "newName is blank" }
            when (uri.scheme) {
                "content" -> {
                    DocumentsContract.renameDocument(context.contentResolver, uri, newName)
                        ?: error("renameDocument returned null")
                }

                "file" -> {
                    val file = File(uri.path ?: error("Invalid file uri"))
                    val parent = file.parentFile ?: error("No parent")
                    val dest = File(parent, newName)
                    if (!file.renameTo(dest)) error("renameTo failed")
                    Uri.fromFile(dest)
                }

                else -> error("Unsupported Uri scheme: ${uri.scheme}")
            }
        }
    }

    /**
     * Copy a file into a destination directory Uri. Returns Uri of the new file.
     * Notes:
     * - For content:// destinations we use createDocument.
     * - For file:// destinations we use java.io.File.
     */
    suspend fun copy(source: Uri, destDir: Uri, newName: String? = null): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val actualName = newName ?: (source.lastPathSegment ?: "file")

                // Open source stream
                val input: InputStream = context.contentResolver.openInputStream(source)
                    ?: error("Cannot open source input stream")

                input.use { inStream ->
                    when (destDir.scheme) {
                        "content" -> {
                            val mime = context.contentResolver.getType(source) ?: "application/octet-stream"
                            val newDoc = DocumentsContract.createDocument(
                                context.contentResolver,
                                destDir,
                                mime,
                                actualName
                            ) ?: error("createDocument returned null")

                            context.contentResolver.openOutputStream(newDoc)?.use { out ->
                                inStream.copyTo(out)
                            } ?: error("Cannot open output stream")

                            newDoc
                        }

                        "file" -> {
                            val dirFile = File(destDir.path ?: error("Invalid destDir"))
                            if (!dirFile.exists()) dirFile.mkdirs()
                            val outFile = File(dirFile, actualName)
                            FileOutputStream(outFile).use { out ->
                                inStream.copyTo(out)
                            }
                            Uri.fromFile(outFile)
                        }

                        else -> error("Unsupported destination scheme: ${destDir.scheme}")
                    }
                }
            }
        }

    /**
     * Move a file into a destination directory Uri. Returns Uri of the new file.
     * Implementation: try DocumentsContract.moveDocument for content://; otherwise copy+delete.
     */
    suspend fun move(source: Uri, destDir: Uri, newName: String? = null): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val actualName = newName ?: (source.lastPathSegment ?: "file")

                if (source.scheme == "content" && destDir.scheme == "content") {
                    // moveDocument requires both doc and parent doc uris.
                    val sourceParent = getParentUri(source) ?: error("No source parent")
                    DocumentsContract.moveDocument(
                        context.contentResolver,
                        source,
                        sourceParent,
                        destDir
                    ) ?: error("moveDocument returned null")
                } else if (source.scheme == "file" && destDir.scheme == "file") {
                    val file = File(source.path ?: error("Invalid source"))
                    val dir = File(destDir.path ?: error("Invalid dest"))
                    if (!dir.exists()) dir.mkdirs()
                    val destFile = File(dir, actualName)
                    if (!file.renameTo(destFile)) {
                        // cross-device: fallback copy + delete
                        val copied = copy(source, destDir, actualName).getOrThrow()
                        delete(source).getOrThrow()
                        copied
                    } else {
                        Uri.fromFile(destFile)
                    }
                } else {
                    val copied = copy(source, destDir, actualName).getOrThrow()
                    delete(source).getOrThrow()
                    copied
                }
            }
        }

    /**
     * Writes text content to a file URI. Supports overwrite and append modes.
     *
     * @param uri The file URI to write to
     * @param content The text content to write
     * @param append If true, append to existing content; if false, overwrite
     * @return Result containing the file URI on success
     */
    suspend fun writeTextToFile(uri: Uri, content: String, append: Boolean = false): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                when (uri.scheme) {
                    "content" -> {
                        val mode = if (append) "wa" else "w"
                        context.contentResolver.openOutputStream(uri, mode)?.use { out ->
                            out.write(content.toByteArray(Charsets.UTF_8))
                        } ?: error("Cannot open output stream for $uri")
                    }

                    "file" -> {
                        val file = File(uri.path ?: error("Invalid file uri"))
                        file.parentFile?.let { if (!it.exists()) it.mkdirs() }
                        FileOutputStream(file, append).use { out ->
                            out.write(content.toByteArray(Charsets.UTF_8))
                        }
                    }

                    else -> error("Unsupported Uri scheme: ${uri.scheme}")
                }
                uri
            }
        }

    /**
     * Creates a new empty file in the given directory.
     */
    suspend fun createEmptyFile(destDir: Uri?, fileName: String, mimeType: String = "application/octet-stream"): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(fileName.isNotBlank()) { "fileName is blank" }
                val dir = destDir
                if (dir == null) {
                    // default directory is file:// based
                    val base = Environment.getExternalStorageDirectory()
                    val outFile = File(base, fileName)
                    if (!outFile.exists()) outFile.createNewFile()
                    Uri.fromFile(outFile)
                } else {
                    when (dir.scheme) {
                        "content" -> DocumentsContract.createDocument(context.contentResolver, dir, mimeType, fileName)
                            ?: error("createDocument returned null")

                        "file" -> {
                            val dirFile = File(dir.path ?: error("Invalid dir"))
                            if (!dirFile.exists()) dirFile.mkdirs()
                            val outFile = File(dirFile, fileName)
                            if (!outFile.exists()) outFile.createNewFile()
                            Uri.fromFile(outFile)
                        }

                        else -> error("Unsupported Uri scheme: ${dir.scheme}")
                    }
                }
            }
        }

    /**
     * Creates a folder in the given directory.
     */
    suspend fun createFolder(destDir: Uri?, folderName: String): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            require(folderName.isNotBlank()) { "folderName is blank" }
            val dir = destDir
            if (dir == null) {
                val base = Environment.getExternalStorageDirectory()
                val folder = File(base, folderName)
                if (!folder.exists()) folder.mkdirs()
                Uri.fromFile(folder)
            } else {
                when (dir.scheme) {
                    "content" -> {
                        DocumentsContract.createDocument(
                            context.contentResolver,
                            dir,
                            DocumentsContract.Document.MIME_TYPE_DIR,
                            folderName
                        ) ?: error("createDocument returned null")
                    }

                    "file" -> {
                        val dirFile = File(dir.path ?: error("Invalid dir"))
                        val folder = File(dirFile, folderName)
                        if (!folder.exists()) folder.mkdirs()
                        Uri.fromFile(folder)
                    }

                    else -> error("Unsupported Uri scheme: ${dir.scheme}")
                }
            }
        }
    }

    /**
     * Loads files from a content:// URI
     */
    private fun loadFromContentUri(uri: Uri): Result<List<FileItem>> {
        return try {
            val fileItems = mutableListOf<FileItem>()
            // tree URI 用 getTreeDocumentId，document URI 用 getDocumentId
            val docId = if (DocumentsContract.isTreeUri(uri)) {
                DocumentsContract.getTreeDocumentId(uri)
            } else {
                DocumentsContract.getDocumentId(uri)
            }
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, docId)

            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val modifiedColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext()) {
                    val documentId = cursor.getString(idColumn)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val lastModified = cursor.getLong(modifiedColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)

                    val itemUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)
                    val isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR

                    // Creation time isn't consistently exposed by all providers.
                    // If not available, keep it null and UI can fall back.
                    val createdAt: Date? = null

                    fileItems.add(
                        FileItem(
                            uri = itemUri,
                            name = name,
                            isDirectory = isDirectory,
                            size = if (isDirectory) 0 else size,
                            lastModified = Date(lastModified),
                            createdAt = createdAt,
                            mimeType = mimeType,
                            path = getDisplayPath(itemUri)
                        )
                    )
                }
            }

            Result.success(fileItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads files from a file:// URI
     */
    private fun loadFromFileUri(uri: Uri): Result<List<FileItem>> {
        val path = uri.path ?: return Result.failure(IllegalArgumentException("Invalid file URI"))
        val file = File(path)
        return loadFromFile(file)
    }

    /**
     * Loads files from a File object
     */
    private fun loadFromFile(file: File): Result<List<FileItem>> {
        return try {
            if (!file.exists()) {
                return Result.failure(IllegalArgumentException("File does not exist: ${file.path}"))
            }

            if (!file.isDirectory) {
                return Result.failure(IllegalArgumentException("Not a directory: ${file.path}"))
            }

            val files = file.listFiles()?.mapNotNull { childFile ->
                try {
                    val mimeType = getMimeType(childFile)

                    val createdAt = runCatching {
                        val path = childFile.toPath()
                        java.nio.file.Files.readAttributes(
                            path,
                            java.nio.file.attribute.BasicFileAttributes::class.java
                        ).creationTime().toMillis().let { millis ->
                            if (millis > 0) Date(millis) else null
                        }
                    }.getOrNull()

                    FileItem(
                        uri = Uri.fromFile(childFile),
                        name = childFile.name,
                        isDirectory = childFile.isDirectory,
                        size = if (childFile.isDirectory) 0 else childFile.length(),
                        lastModified = Date(childFile.lastModified()),
                        createdAt = createdAt,
                        mimeType = mimeType,
                        path = childFile.absolutePath
                    )
                } catch (_: Exception) {
                    null // Skip files that can't be read
                }
            } ?: emptyList()

            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Finds the directory for a content:// URI
     */
    private fun findContentUriDirectory(fileUri: Uri): Result<Pair<Uri, String>> {
        return try {
            // Check if this is a MediaStore URI (content://media/...)
            if (fileUri.authority?.startsWith("media") == true) {
                return findMediaStoreDirectory(fileUri)
            }

            // Check if this is a Downloads URI (content://com.android.providers.downloads.documents/...)
            if (fileUri.authority?.contains("downloads") == true) {
                return findDownloadsDirectory(fileUri)
            }

            // Try to get file info from content resolver
            val fileName = context.contentResolver.query(
                fileUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else null
            } ?: fileUri.lastPathSegment ?: "Unknown"

            // For content URIs, try to get the parent by manipulating the document ID
            val documentId = DocumentsContract.getDocumentId(fileUri)
            val parentId = documentId.substringBeforeLast('/', documentId)

            val treeUri = DocumentsContract.buildTreeDocumentUri(
                fileUri.authority,
                parentId
            )

            Result.success(treeUri to fileName)
        } catch (e: Exception) {
            // If we can't find parent, try to use the URI as-is
            Result.failure(IllegalArgumentException("Cannot find parent directory: ${e.message}"))
        }
    }

    /**
     * Finds the directory for a Downloads content:// URI
     * Downloads URIs use numeric document IDs (e.g., document/40) which don't have path hierarchy
     * We need to query for the actual file path and resolve the parent directory
     */
    private fun findDownloadsDirectory(fileUri: Uri): Result<Pair<Uri, String>> {
        return try {
            // Try to get file info from content resolver
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                android.provider.MediaStore.MediaColumns.DATA  // Try to get actual path
            )

            var fileName: String? = null
            var filePath: String? = null

            context.contentResolver.query(
                fileUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val dataIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA)

                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                    if (dataIndex >= 0) {
                        filePath = cursor.getString(dataIndex)
                    }
                }
            }

            // If we got actual file path, use it to find parent directory
            if (filePath != null) {
                val file = File(filePath)
                val parentFile = file.parentFile
                if (parentFile != null && parentFile.exists()) {
                    return Result.success(Uri.fromFile(parentFile) to (fileName ?: file.name))
                }
            }

            // Fallback: if file is in standard Download folder, navigate there
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir.exists()) {
                val resolvedFileName = fileName ?: fileUri.lastPathSegment ?: "Unknown"
                return Result.success(Uri.fromFile(downloadDir) to resolvedFileName)
            }

            Result.failure(IllegalArgumentException("Cannot find parent directory for Downloads URI"))
        } catch (e: Exception) {
            // Last resort fallback to Download folder
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir.exists()) {
                val resolvedFileName = fileUri.lastPathSegment ?: "Unknown"
                return Result.success(Uri.fromFile(downloadDir) to resolvedFileName)
            }
            Result.failure(IllegalArgumentException("Cannot find parent directory for Downloads URI: ${e.message}"))
        }
    }

    /**
     * Finds the directory for a MediaStore content:// URI
     */
    private fun findMediaStoreDirectory(fileUri: Uri): Result<Pair<Uri, String>> {
        return try {
            // Query MediaStore to get the actual file path
            val projection = arrayOf(
                android.provider.MediaStore.MediaColumns.DATA,
                android.provider.MediaStore.MediaColumns.DISPLAY_NAME
            )

            context.contentResolver.query(
                fileUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dataIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DATA)
                    val nameIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)

                    val filePath = cursor.getString(dataIndex)
                    val fileName = cursor.getString(nameIndex)

                    if (filePath != null) {
                        val file = File(filePath)
                        val parentFile = file.parentFile

                        if (parentFile != null && parentFile.exists()) {
                            // Return the parent directory as a file:// URI
                            return Result.success(Uri.fromFile(parentFile) to fileName)
                        }
                    }
                }
                Result.failure(IllegalArgumentException("Cannot find file path from MediaStore URI"))
            } ?: Result.failure(IllegalArgumentException("Cannot query MediaStore URI"))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Cannot find parent directory for MediaStore URI: ${e.message}"))
        }
    }

    /**
     * Finds the directory for a file:// URI
     */
    private fun findFileUriDirectory(fileUri: Uri): Result<Pair<Uri, String>> {
        return try {
            val path = fileUri.path ?: return Result.failure(IllegalArgumentException("Invalid file URI"))
            val file = File(path)

            val (directoryFile, fileName) = if (file.isDirectory) {
                file to file.name
            } else {
                (file.parentFile ?: file) to file.name
            }

            Result.success(Uri.fromFile(directoryFile) to fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the parent content URI
     */
    private fun getParentContentUri(uri: Uri): Uri? {
        return try {
            val documentId = if (DocumentsContract.isTreeUri(uri)) {
                DocumentsContract.getTreeDocumentId(uri)
            } else {
                DocumentsContract.getDocumentId(uri)
            }
            if (!documentId.contains('/')) {
                // Already at root
                return null
            }

            val parentId = documentId.substringBeforeLast('/')
            DocumentsContract.buildTreeDocumentUri(uri.authority, parentId)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Gets the parent file URI
     */
    private fun getParentFileUri(uri: Uri): Uri? {
        return try {
            val path = uri.path ?: return null
            val file = File(path)
            val parent = file.parentFile ?: return null
            Uri.fromFile(parent)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Gets a display path for content URIs
     */
    private fun getContentUriDisplayPath(uri: Uri): String {
        return try {
            // Try to resolve the path from DocumentsContract
            val documentId = if (DocumentsContract.isTreeUri(uri)) {
                DocumentsContract.getTreeDocumentId(uri)
            } else {
                DocumentsContract.getDocumentId(uri)
            }

            // For common providers, extract readable path
            when {
                uri.authority?.contains("com.android.externalstorage") == true -> {
                    val split = documentId.split(':')
                    if (split.size >= 2) {
                        val type = split[0]
                        val relativePath = split[1]
                        if (type == "primary") {
                            "/storage/emulated/0/$relativePath"
                        } else {
                            "/storage/$type/$relativePath"
                        }
                    } else {
                        "/$documentId"
                    }
                }
                uri.authority?.contains("downloads") == true -> {
                    "/Download/$documentId"
                }
                else -> {
                    // Fallback to document ID
                    "/$documentId"
                }
            }
        } catch (_: Exception) {
            uri.toString()
        }
    }

    /**
     * Gets the MIME type for a file
     */
    private fun getMimeType(file: File): String? {
        return if (file.isDirectory) {
            DocumentsContract.Document.MIME_TYPE_DIR
        } else {
            val extension = file.extension.lowercase()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                ?: when (extension) {
                    "json" -> "application/json"
                    "txt" -> "text/plain"
                    "log" -> "text/plain"
                    "md" -> "text/markdown"
                    else -> "application/octet-stream"
                }
        }
    }
}
