package com.wanbaohe.imageviewer.screenLogic

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
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
