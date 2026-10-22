package com.wanbaohe.imageviewer.screenLogic

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.image.ImageViewerInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val DOWNLOAD_SUBDIR = "Wanbaohe"
private const val DOWNLOAD_WATCH_INTERVAL_MS = 500L
private const val DOWNLOAD_WATCH_TIMEOUT_MS = 120_000L

class ImageViewerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val imageViewerInfo: ImageViewerInfo?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onEditImage: (Uri) -> Unit,
    @ApplicationContext val applicationContext: Context,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        Log.i("ImageViewerComponent", "ImageViewerComponent init")
    }

    fun exit() {
        onGoBack()
    }

    /**
     * 跳图层创作编辑当前图片(仅本地图片入口会调到这,网络图先下载)
     */
    fun editImage(imageUrl: String) {
        onEditImage(imageUrl.toUri())
    }

    /**
     * SAF 授权文件夹后,在已授权的目录树里找回目标图片的可读 Uri。
     * onResult 返回 null 表示授权目录里找不到该文件。
     */
    fun resolveImageInGrantedTree(
        treeUri: Uri,
        imageUrl: String,
        onResult: (String?) -> Unit
    ) {
        componentScope.launch {
            val resolved = withContext(ioDispatcher) {
                runCatching {
                    applicationContext.contentResolver.takePersistableUriPermission(
                        treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                resolveDocumentInTree(treeUri, imageUrl)
            }
            onResult(resolved)
        }
    }

    /**
     * 已有持久授权(SAF)覆盖该图片时静默解析出可读 Uri,避免每次冷启动都重新引导授权;
     * 没有任何授权覆盖时返回 null(由 UI 引导用户授权)
     */
    fun resolveImageFromPersistedGrants(imageUrl: String, onResult: (String?) -> Unit) {
        componentScope.launch {
            val resolved = withContext(ioDispatcher) {
                applicationContext.contentResolver.persistedUriPermissions
                    .asSequence()
                    .filter { it.isReadPermission }
                    .mapNotNull { permission ->
                        runCatching {
                            resolveDocumentInTree(permission.uri, imageUrl)
                        }.getOrNull()
                    }
                    .firstOrNull()
            }
            onResult(resolved)
        }
    }

    private fun resolveDocumentInTree(treeUri: Uri, imageUrl: String): String? {
        val filePath = imageUrl.toUri().path ?: return null
        val tree = DocumentFile.fromTreeUri(applicationContext, treeUri) ?: return null

        // 授权目录是图片所在目录(或其上级):按相对路径逐段下钻
        val relative = relativePathInsideTree(treeUri, filePath)
        if (relative != null) {
            val segments = relative.split('/').filter { it.isNotEmpty() }
            var dir: DocumentFile? = tree
            for (segment in segments.dropLast(1)) {
                dir = dir?.listFiles()?.firstOrNull { it.isDirectory && it.name == segment }
            }
            val target = segments.lastOrNull()?.let { name ->
                dir?.listFiles()?.firstOrNull { it.isFile && it.name == name }
            }
            if (target != null) return target.uri.toString()
        }
        // 兜底:授权根下直接按文件名找
        val fileName = filePath.substringAfterLast('/')
        return tree.findFile(fileName)?.uri?.toString()
    }

    /**
     * 计算文件在授权目录树内的相对路径;不在树内返回 null。
     * treeDocumentId 形如 "primary:Pictures" 或 "<volume>:Pictures"
     */
    private fun relativePathInsideTree(treeUri: Uri, filePath: String): String? {
        val treeDocId = runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull() ?: return null

        val normalized = filePath.replaceFirst("^/sdcard/".toRegex(), "/storage/emulated/0/")
        val volumeRoot = when {
            treeDocId.startsWith("primary:") -> "/storage/emulated/0/"
            else -> {
                val volume = treeDocId.substringBefore(':', "")
                if (volume.isEmpty()) return null else "/storage/$volume/"
            }
        }
        if (!normalized.startsWith(volumeRoot)) return null

        val fileRelative = normalized.removePrefix(volumeRoot)
        val treeRelative = treeDocId.substringAfter(':', "")
        if (treeRelative.isEmpty()) return fileRelative
        if (!fileRelative.startsWith("$treeRelative/")) return null
        return fileRelative.removePrefix("$treeRelative/")
    }

    fun downloadImage(imageUrl: String) {
        componentScope.launch {
            val downloadManager =
                applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fileName: String
            val downloadId: Long
            try {
                fileName = imageUrl.toUri().lastPathSegment
                    ?: "image_${System.currentTimeMillis()}.jpg"

                val request = DownloadManager.Request(imageUrl.toUri())
                    .setTitle(fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_PICTURES,
                        "$DOWNLOAD_SUBDIR/$fileName"
                    )

                downloadId = downloadManager.enqueue(request)

                AppToastHost.showToast(applicationContext.getString(R.string.download_started))
            } catch (e: Exception) {
                Log.e("ImageViewerComponent", "Download error", e)
                AppToastHost.showFailureToast(e)
                return@launch
            }

            watchDownload(downloadManager, downloadId, fileName)
        }
    }

    /**
     * 轮询 DownloadManager 直到下载结束:成功走标准保存成功提示管线
     * (带「打开」按钮的 toast + 撒花 + ActivityLog),失败弹错误 toast。
     * 组件销毁时 componentScope 取消,轮询随 delay 自然终止。
     */
    private suspend fun watchDownload(
        downloadManager: DownloadManager,
        downloadId: Long,
        fileName: String
    ) {
        val deadline = System.currentTimeMillis() + DOWNLOAD_WATCH_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val status = runCatching {
                downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                    ?.use { cursor ->
                        if (!cursor.moveToFirst()) return@use null
                        val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (index >= 0) cursor.getInt(index) else null
                    }
            }.getOrNull()

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    SaveSuccessNotifier.notifyFileSaved(
                        fileUri = downloadManager.getUriForDownloadedFile(downloadId)
                            ?.toString().orEmpty(),
                        fileName = fileName,
                        savingPath = "${Environment.DIRECTORY_PICTURES}/$DOWNLOAD_SUBDIR"
                    )
                    return
                }

                DownloadManager.STATUS_FAILED -> {
                    AppToastHost.showFailureToast(R.string.something_went_wrong)
                    return
                }
            }
            delay(DOWNLOAD_WATCH_INTERVAL_MS)
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            imageViewerInfo: ImageViewerInfo?,
            onGoBack: () -> Unit,
            onEditImage: (Uri) -> Unit,
        ): ImageViewerComponent
    }
}
