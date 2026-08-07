package com.t8rin.imagetoolbox.core.data.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

/**
 * SAF (Storage Access Framework) URI / MediaStore URI → file:// URI 转换工具。
 *
 * SAF 的 content URI 没有持久权限，app 重启后失效；
 * MediaStore URI 也可能在权限回收后不可访问。
 * app 有 MANAGE_EXTERNAL_STORAGE 时，把 URI 转为 file:// 后可直接访问，
 * 避免 agent / 后台任务执行时遇到 SAF 权限过期的问题。
 */
object SafUriUtils {

    /**
     * 将 tree URI 转为 file:// URI（目录）。
     *
     * 例: `content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FOneBox`
     * → `file:///storage/emulated/0/Documents/OneBox`
     */
    fun treeUriToFileUri(treeUri: Uri): Uri? {
        return runCatching {
            if (!DocumentsContract.isTreeUri(treeUri)) return@runCatching null
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            docIdToFileUri(docId)
        }.getOrNull()
    }

    /**
     * 将 document URI 转为 file:// URI（文件或目录）。
     *
     * 例: `content://com.android.externalstorage.documents/document/primary%3ADocuments%2FOneBox%2Ffile.webp`
     * → `file:///storage/emulated/0/Documents/OneBox/file.webp`
     */
    fun documentUriToFileUri(documentUri: Uri): Uri? {
        return runCatching {
            if (DocumentsContract.isTreeUri(documentUri)) return@runCatching null
            val docId = DocumentsContract.getDocumentId(documentUri)
            docIdToFileUri(docId)
        }.getOrNull()
    }

    /**
     * 将 document URI 转为父目录的 file:// URI。
     *
     * 例: `content://.../document/primary%3ADocuments%2FOneBox%2Ffile.webp`
     * → `file:///storage/emulated/0/Documents/OneBox`
     */
    fun documentUriToParentFileUri(documentUri: Uri): Uri? {
        return runCatching {
            if (DocumentsContract.isTreeUri(documentUri)) return@runCatching null
            val docId = DocumentsContract.getDocumentId(documentUri)
            val parentId = docId.substringBeforeLast('/', docId)
            docIdToFileUri(parentId)
        }.getOrNull()
    }

    /**
     * 将 MediaStore URI 转为 file:// URI。
     *
     * 通过查询 [MediaStore.MediaColumns.DATA] 取出真实路径。
     * 若文件已被删除 / 列不存在，返回 null。
     */
    fun mediaStoreUriToFileUri(context: Context, mediaUri: Uri): Uri? {
        return runCatching {
            val cursor = context.contentResolver.query(
                mediaUri,
                arrayOf(MediaStore.MediaColumns.DATA),
                null,
                null,
                null
            ) ?: return@runCatching null
            cursor.use { c ->
                if (!c.moveToFirst()) return@runCatching null
                val dataIndex = c.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (dataIndex < 0) return@runCatching null
                val path = c.getString(dataIndex)
                if (path.isNullOrBlank()) return@runCatching null
                val file = File(path)
                if (file.exists()) Uri.fromFile(file) else null
            }
        }.getOrNull()
    }

    /**
     * 通用：任意 URI 字符串 → file:// URI 字符串。
     *
     * 转换失败或 URI 为空时返回 null，方便调用方回退到原始字符串。
     */
    fun toFileUri(context: Context, uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        return runCatching { toFileUri(context, Uri.parse(uriString))?.toString() }.getOrNull()
    }

    /**
     * 通用：任意 URI → file:// URI。
     *
     *  - `file://` 直接返回；
     *  - MediaStore URI 走 [mediaStoreUriToFileUri]；
     *  - SAF tree/document URI 走 [treeUriToFileUri] / [documentUriToFileUri]；
     *  - 其他 content provider（云盘 / 远程）返回 null，调用方应回退到原始 URI。
     */
    fun toFileUri(context: Context, uri: Uri?): Uri? {
        if (uri == null) return null
        if (uri.scheme == "file") return uri
        if (uri.scheme != "content") return null
        val authority = uri.authority.orEmpty()
        return when {
            authority.startsWith("media") -> mediaStoreUriToFileUri(context, uri)
            authority.contains("externalstorage") ||
                authority.contains("documents") -> {
                if (DocumentsContract.isTreeUri(uri)) treeUriToFileUri(uri)
                else documentUriToFileUri(uri)
            }
            else -> null
        }
    }

    /**
     * 通用：DocumentsProvider document ID → file:// URI。
     *
     * 支持 `primary:Documents/OneBox` 格式（externalstorage provider）。
     * 其他 provider 返回 null。
     */
    private fun docIdToFileUri(docId: String): Uri? {
        val split = docId.split(":")
        if (split.size < 2) return null
        val type = split[0]
        val relativePath = split[1]
        val base = if (type.equals("primary", ignoreCase = true)) {
            Environment.getExternalStorageDirectory()
        } else {
            return null
        }
        val file = File(base, relativePath)
        return if (file.exists()) Uri.fromFile(file) else null
    }
}
