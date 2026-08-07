package com.wanbaohe.imageviewer.screenLogic

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.image.ImageViewerInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent

class ImageViewerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val imageViewerInfo: ImageViewerInfo?,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext val applicationContext: Context,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        Log.i("ImageViewerComponent", "ImageViewerComponent init")
    }

    fun exit() {
        onGoBack()
    }
    
    fun downloadImage(imageUrl: String) {
        componentScope.launch {
            try {
                val fileName = imageUrl.toUri().lastPathSegment ?: "image_${System.currentTimeMillis()}.jpg"
                
                val request = DownloadManager.Request(imageUrl.toUri())
                    .setTitle("图片下载中")
                    .setDescription("正在下载图片...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Wanbaohe/$fileName")
                
                val downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
                
                Toast.makeText(applicationContext, "开始下载图片", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ImageViewerComponent", "Download error", e)
                Toast.makeText(applicationContext, "下载失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            imageViewerInfo: ImageViewerInfo?,
            onGoBack: () -> Unit,
        ): ImageViewerComponent
    }
}
