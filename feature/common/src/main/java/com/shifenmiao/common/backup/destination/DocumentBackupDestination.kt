package com.shifenmiao.common.backup.destination

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 保存到系统文件选择器挑中的位置。
 *
 * 海外用户在这里会看到 Google Drive / OneDrive / Dropbox（它们实现了
 * DocumentsProvider），保存完就是一个「在用户自己网盘里的备份」；
 * 拿到的 URI 在申请持久授权后还能长期读回来。
 */
@Singleton
class DocumentBackupDestination @Inject constructor(
    @ApplicationContext private val context: Context,
) : BackupDestination {

    override val id: String = BackupDestinationIds.DOCUMENT

    override val requiresDocument: Boolean = true

    override suspend fun store(backup: File, documentUri: Uri?): Result<StoredBackup> =
        runCatching {
            val uri = requireNotNull(documentUri) { "document uri is required" }
            context.contentResolver.openOutputStream(uri)?.use { output ->
                backup.inputStream().use { input -> input.copyTo(output) }
            } ?: error("cannot open output stream")
            StoredBackup(destinationId = id, documentUri = uri.toString())
        }
}
