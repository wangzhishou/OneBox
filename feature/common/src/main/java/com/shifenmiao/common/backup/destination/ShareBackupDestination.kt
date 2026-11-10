package com.shifenmiao.common.backup.destination

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.t8rin.imagetoolbox.core.utils.fileProviderAuthority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 走系统分享面板。
 *
 * 国产网盘（百度网盘 / 阿里云盘）大多没有实现 DocumentsProvider，系统保存器里看不到它们，
 * 只能从这里进：分享到网盘 App，或分享进微信 / QQ 的收藏。
 * 这条路径拿不到可回读的 URI，恢复时仍需用户手动把文件选回来。
 */
@Singleton
class ShareBackupDestination @Inject constructor(
    @ApplicationContext private val context: Context,
) : BackupDestination {

    override val id: String = BackupDestinationIds.SHARE

    override suspend fun store(backup: File, documentUri: Uri?): Result<StoredBackup> =
        runCatching {
            val uri = FileProvider.getUriForFile(context, context.fileProviderAuthority, backup)
            withContext(Dispatchers.Main) {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(send, null)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
            StoredBackup(destinationId = id)
        }
}
