package com.wanbaohe.boardgame.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.GifAnimationEncoder
import com.t8rin.imagetoolbox.core.domain.image.GifAnimationFrame
import com.t8rin.imagetoolbox.core.domain.saving.io.Writeable
import com.t8rin.imagetoolbox.core.utils.fileProviderAuthority
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class BoardGameExportFile(
    val uri: String,
    val filename: String,
    val mimeType: String,
    val byteCount: Long,
    val width: Int,
    val height: Int,
    val durationMillis: Long,
)

class BoardGameGifExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gifEncoder: GifAnimationEncoder<Bitmap>,
    private val dispatchers: DispatchersHolder,
) {
    suspend fun generate(
        plan: BoardGifPlan<*>,
        filenamePrefix: String,
        frameProvider: suspend (index: Int) -> Bitmap,
        onProgress: suspend (completedFrames: Int) -> Unit,
    ): BoardGameExportFile = withContext(dispatchers.ioDispatcher) {
        require(filenamePrefix.matches(Regex("[a-zA-Z0-9_-]+")))
        val directory = File(context.cacheDir, CACHE_DIRECTORY)
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Cannot create the board replay export directory")
        }
        removeExpiredFiles(directory)
        val extension = if (plan.isStatic) "png" else "gif"
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "${filenamePrefix}_${timestamp}_${UUID.randomUUID().toString().take(8)}.$extension"
        val pending = File.createTempFile("replay-", ".part", directory)
        val finished = File(directory, filename)
        var published = false
        try {
            pending.outputStream().buffered(BUFFER_SIZE).use { output ->
                if (plan.isStatic) {
                    withContext(dispatchers.defaultDispatcher) {
                        val frame = frameProvider(0)
                        try {
                            require(frame.width == plan.size.width && frame.height == plan.size.height)
                            currentCoroutineContext().ensureActive()
                            if (!frame.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                                throw IOException("Cannot encode the board image")
                            }
                            onProgress(1)
                        } finally {
                            frame.recycle()
                        }
                    }
                } else {
                    gifEncoder.encode(
                        size = plan.size,
                        frameCount = plan.frameCount,
                        output = object : Writeable {
                            override fun writeBytes(byteArray: ByteArray) = output.write(byteArray)
                            override fun close() = Unit
                        },
                        frameProvider = { index ->
                            GifAnimationFrame(
                                image = frameProvider(index),
                                durationMillis = plan.frameAt(index).durationMillis,
                            )
                        },
                        onProgress = onProgress,
                    )
                }
            }
            currentCoroutineContext().ensureActive()
            if (pending.length() == 0L || !pending.renameTo(finished)) {
                throw IOException("Cannot finish the board replay export")
            }
            val uri = FileProvider.getUriForFile(context, context.fileProviderAuthority, finished)
            currentCoroutineContext().ensureActive()
            val result = BoardGameExportFile(
                uri = uri.toString(),
                filename = filename,
                mimeType = if (plan.isStatic) "image/png" else "image/gif",
                byteCount = finished.length(),
                width = plan.size.width,
                height = plan.size.height,
                durationMillis = plan.durationMillis,
            )
            published = true
            result
        } finally {
            if (!published) {
                deleteCacheFile(pending)
                deleteCacheFile(finished)
            }
        }
    }

    suspend fun save(file: BoardGameExportFile, destination: Uri) = withContext(dispatchers.ioDispatcher) {
        val input = context.contentResolver.openInputStream(file.uri.toUri())
            ?: throw IOException("Cannot read the generated board replay")
        input.use { source ->
            val output = context.contentResolver.openOutputStream(destination, "wt")
                ?: throw IOException("Cannot open the save destination")
            output.use { target ->
                val buffer = ByteArray(BUFFER_SIZE)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = source.read(buffer)
                    if (count < 0) break
                    target.write(buffer, 0, count)
                }
                target.flush()
            }
        }
    }

    suspend fun share(file: BoardGameExportFile) = withContext(dispatchers.uiDispatcher) {
        val uri = file.uri.toUri()
        val send = Intent(Intent.ACTION_SEND).apply {
            type = file.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, file.filename, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun removeExpiredFiles(directory: File) {
        val oldestAllowed = System.currentTimeMillis() - CACHE_LIFETIME_MILLIS
        val files = directory.listFiles() ?: throw IOException("Cannot read the board replay export directory")
        files.filter { it.isFile && it.lastModified() < oldestAllowed }.forEach(::deleteCacheFile)
    }

    private fun deleteCacheFile(file: File) {
        if (file.exists() && !file.delete()) {
            IOException("Cannot remove replay cache file: ${file.name}").makeLog("BoardGameGifExporter")
        }
    }

    private companion object {
        const val CACHE_DIRECTORY = "boardgame-exports"
        const val BUFFER_SIZE = 64 * 1024
        // Share receivers may read the file after the chooser and export dialog have closed.
        const val CACHE_LIFETIME_MILLIS = 3L * 24 * 60 * 60 * 1000
    }
}
