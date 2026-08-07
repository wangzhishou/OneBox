package com.wanbaohe.file_transfer.util

import android.webkit.MimeTypeMap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.shifenmiao.model.transfer.FileItem
import java.io.File
import java.text.DecimalFormat

/**
 * 文件操作工具类
 */
object FileUtils {

    private val sizeFormat = DecimalFormat("#.##")

    /**
     * 获取文件的MIME类型
     */
    fun getMimeType(file: File): String? {
        return getMimeType(file.name)
    }

    /**
     * 根据文件名获取MIME类型
     */
    fun getMimeType(fileName: String): String? {
        val extension = getFileExtension(fileName)
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else {
            null
        }
    }

    /**
     * 获取文件扩展名
     */
    fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot >= 0 && lastDot < fileName.length - 1) {
            fileName.substring(lastDot + 1)
        } else {
            ""
        }
    }

    /**
     * 格式化文件大小
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${sizeFormat.format(size / 1024.0)} KB"
            size < 1024 * 1024 * 1024 -> "${sizeFormat.format(size / (1024.0 * 1024.0))} MB"
            else -> "${sizeFormat.format(size / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }

    /**
     * 将File转换为FileItem
     */
    fun fileToFileItem(file: File): FileItem {
        return FileItem(
            name = file.name,
            path = file.absolutePath,
            size = if (file.isFile) file.length() else 0L,
            isDirectory = file.isDirectory,
            mimeType = if (file.isFile) getMimeType(file) else null,
            lastModified = file.lastModified(),
            canRead = file.canRead(),
            canWrite = file.canWrite()
        )
    }

    /**
     * 列出目录中的文件
     * @param directory 目录
     * @param showHidden 是否显示隐藏文件
     * @return 文件列表，目录在前，文件在后，按名称排序
     */
    fun listFiles(directory: File, showHidden: Boolean = false): List<FileItem> {
        if (!directory.exists() || !directory.isDirectory || !directory.canRead()) {
            return emptyList()
        }

        val files = directory.listFiles() ?: return emptyList()

        return files
            .filter { showHidden || !it.name.startsWith(".") }
            .map { fileToFileItem(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    /**
     * 安全路径验证，防止路径穿越攻击
     * @param rootPath 根目录路径
     * @param requestPath 请求的路径
     * @return 验证后的安全路径，如果验证失败返回null
     */
    fun validatePath(rootPath: String, requestPath: String): String? {
        val root = File(rootPath).canonicalFile
        val requestFile = File(requestPath)

        val requested = if (requestFile.isAbsolute) {
            requestFile.canonicalFile
        } else {
            File(rootPath, requestPath).canonicalFile
        }

        // 检查请求的路径是否在根目录下
        return if (requested.absolutePath.startsWith(root.absolutePath)) {
            requested.absolutePath
        } else {
            null
        }
    }

    /**
     * 检查路径是否可以返回上级目录
     */
    fun canGoUp(rootPath: String, currentPath: String): Boolean {
        val root = File(rootPath).canonicalPath
        val current = File(currentPath).canonicalPath
        return current != root && current.startsWith(root)
    }

    /**
     * 获取父目录路径
     */
    fun getParentPath(rootPath: String, currentPath: String): String? {
        if (!canGoUp(rootPath, currentPath)) {
            return null
        }
        val parent = File(currentPath).parentFile
        return parent?.absolutePath
    }

    /**
     * 获取相对路径
     */
    fun getRelativePath(rootPath: String, absolutePath: String): String {
        val root = File(rootPath).canonicalPath
        val path = File(absolutePath).canonicalPath
        return if (path.startsWith(root)) {
            path.removePrefix(root).removePrefix("/")
        } else {
            absolutePath
        }
    }

    /**
     * 判断是否为图片文件
     */
    fun isImage(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }

    /**
     * 判断是否为视频文件
     */
    fun isVideo(mimeType: String?): Boolean {
        return mimeType?.startsWith("video/") == true
    }

    /**
     * 判断是否为音频文件
     */
    fun isAudio(mimeType: String?): Boolean {
        return mimeType?.startsWith("audio/") == true
    }

    /**
     * 判断是否为文本文件
     */
    fun isText(mimeType: String?): Boolean {
        return mimeType?.startsWith("text/") == true
    }

    /**
     * 生成唯一文件名（避免覆盖）
     */
    fun generateUniqueFileName(directory: File, originalName: String): String {
        var file = File(directory, originalName)
        if (!file.exists()) {
            return originalName
        }

        val nameWithoutExtension = originalName.substringBeforeLast(".")
        val extension = originalName.substringAfterLast(".", "")
        var counter = 1

        while (file.exists()) {
            val newName = if (extension.isNotEmpty()) {
                "${nameWithoutExtension}_$counter.$extension"
            } else {
                "${nameWithoutExtension}_$counter"
            }
            file = File(directory, newName)
            counter++
        }

        return file.name
    }

    /**
     * 生成图片缩略图
     * @param sourceFile 源图片文件
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 缩略图的Bitmap，如果失败返回null
     */
    fun generateThumbnail(sourceFile: File, maxWidth: Int = 300, maxHeight: Int = 300): Bitmap? {
        if (!sourceFile.exists() || !sourceFile.isFile) {
            return null
        }

        return try {
            // 首先获取图片尺寸，不加载到内存
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // 计算缩放比例
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight
            var inSampleSize = 1

            if (imageHeight > maxHeight || imageWidth > maxWidth) {
                val halfHeight = imageHeight / 2
                val halfWidth = imageWidth / 2

                while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                    inSampleSize *= 2
                }
            }

            // 使用计算出的缩放比例解码图片
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 递归删除文件或目录
     * @param file 要删除的文件或目录
     * @return 是否删除成功
     */
    fun deleteRecursively(file: File): Boolean {
        if (!file.exists()) {
            return true
        }

        if (file.isDirectory) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    if (!deleteRecursively(child)) {
                        return false
                    }
                }
            }
        }

        return file.delete()
    }

    /**
     * 批量删除文件
     * @param files 要删除的文件列表
     * @return 删除结果，包含成功数量和失败的文件路径
     */
    fun deleteMultipleFiles(files: List<File>): Pair<Int, List<String>> {
        var successCount = 0
        val failedPaths = mutableListOf<String>()

        for (file in files) {
            if (deleteRecursively(file)) {
                successCount++
            } else {
                failedPaths.add(file.absolutePath)
            }
        }

        return Pair(successCount, failedPaths)
    }

    /**
     * 创建目录
     * @param parentDir 父目录
     * @param dirName 目录名称
     * @return 创建的目录，如果失败返回null
     */
    fun createDirectory(parentDir: File, dirName: String): File? {
        if (!parentDir.exists() || !parentDir.isDirectory || !parentDir.canWrite()) {
            return null
        }

        // 验证目录名称（不允许特殊字符）
        if (!isValidFileName(dirName)) {
            return null
        }

        // 生成唯一目录名
        val uniqueName = generateUniqueFileName(parentDir, dirName)
        val newDir = File(parentDir, uniqueName)

        return if (newDir.mkdir()) {
            newDir
        } else {
            null
        }
    }

    /**
     * 重命名文件或目录
     * @param file 要重命名的文件
     * @param newName 新名称
     * @return 重命名后的文件，如果失败返回null
     */
    fun renameFile(file: File, newName: String): File? {
        if (!file.exists() || !isValidFileName(newName)) {
            return null
        }

        val parentDir = file.parentFile ?: return null
        if (!parentDir.canWrite()) {
            return null
        }

        val newFile = File(parentDir, newName)

        // 如果新文件名已存在
        if (newFile.exists()) {
            return null
        }

        return if (file.renameTo(newFile)) {
            newFile
        } else {
            null
        }
    }

    /**
     * 验证文件名是否有效
     */
    fun isValidFileName(fileName: String): Boolean {
        if (fileName.isEmpty() || fileName == "." || fileName == "..") {
            return false
        }

        // 检查非法字符
        val invalidChars = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|', '\u0000')
        return invalidChars.none { fileName.contains(it) }
    }
}
