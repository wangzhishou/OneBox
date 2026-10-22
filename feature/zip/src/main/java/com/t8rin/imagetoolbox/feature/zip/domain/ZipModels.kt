package com.t8rin.imagetoolbox.feature.zip.domain

/**
 * 待压缩的源文件（已解析好的展示信息）。
 */
data class ZipSourceFile(
    val uri: String,
    val name: String,
    val size: Long,
    val extension: String,
    val mimeType: String?
)

/**
 * 压缩包里的一个条目（文件或目录）。
 *
 * [path] 是压缩包内的相对路径，例如 `原图/IMG_2041.jpg`。
 */
data class ZipArchiveEntry(
    val path: String,
    val size: Long,
    val compressedSize: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val mimeType: String?
) {
    val name: String
        get() = path.trimEnd('/').substringAfterLast('/', path)

    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "").lowercase()

    val parentPath: String
        get() = path.trimEnd('/').substringBeforeLast('/', "")
}

/** 用户选中的不是 zip（如 rar/7z）时抛出，页面据此提示"仅支持 zip"。 */
class UnsupportedArchiveFormatException : Exception("Unsupported archive format")

/** 压缩包条目使用了密码加密，Java 的 ZipFile 无法解密。 */
class EncryptedArchiveException : Exception("Encrypted archive entry")

/** 组件已经释放/压缩包已关闭时仍尝试解压。 */
class ArchiveUnavailableException : Exception("Archive is not available")

/** file:// 直开但没有读权限（openInputStream 返回 null 或抛 SecurityException），页面据此走授权引导。 */
class ArchiveAccessDeniedException : Exception("Archive access denied")

/**
 * 压缩/解压进度。优先按字节算百分比（大文件也能连续走动），
 * 拿不到字节总量时退回按文件数算。
 */
data class ZipProgress(
    val doneBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val doneFiles: Int = 0,
    val totalFiles: Int = 0
) {
    val fraction: Float
        get() = when {
            totalBytes > 0L -> (doneBytes.toDouble() / totalBytes).toFloat().coerceIn(0f, 1f)
            totalFiles > 0 -> (doneFiles.toFloat() / totalFiles).coerceIn(0f, 1f)
            else -> 0f
        }
}

/**
 * 已打开（并解析完目录）的压缩包。实现需要持有临时副本，
 * 用完调用 [close] 释放。
 */
interface ZipArchive {

    /** 压缩包显示名，例如 `照片备份.zip`。 */
    val name: String

    /** 压缩包文件本身的大小（字节）。 */
    val archiveSize: Long

    /** 压缩包内全部条目（含目录条目）。 */
    val entries: List<ZipArchiveEntry>

    /** 解压后总大小。 */
    val totalSize: Long

    /** 压缩后总大小（不含目录条目）。 */
    val totalCompressedSize: Long

    /**
     * 全部解压到 [destinationTreeUri] 下以压缩包命名的子文件夹，
     * 返回新建文件夹的 URI。
     */
    suspend fun extractAll(
        destinationTreeUri: String,
        onProgress: (ZipProgress) -> Unit
    ): String

    /**
     * 把单个条目写到 [destinationUri]（由系统文件选择器创建的目标文件）。
     */
    suspend fun extractEntry(
        entry: ZipArchiveEntry,
        destinationUri: String,
        onProgress: (ZipProgress) -> Unit = {}
    )

    fun close()
}
