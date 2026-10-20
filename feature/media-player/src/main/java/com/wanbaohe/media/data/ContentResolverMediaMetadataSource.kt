package com.wanbaohe.media.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.net.Uri
import android.provider.OpenableColumns
import android.util.LruCache
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaMetadata
import com.wanbaohe.media.domain.MediaMetadataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用 [MediaMetadataRetriever] 读取本地媒体的标题/作者/时长/尺寸与封面。
 *
 * 读取结果是"锦上添花"：任何一步失败都退回空元数据 / null 封面，
 * 页面用文件名和占位图继续渲染，不因为某个文件元数据坏了就整页打不开。
 *
 * 列表里同一项会被缩略图、时长徽标各问一次，所以两级缓存都放在这里，
 * 按 URI 记，容量按"最近看过的几十项"取，不跟库规模线性增长。
 */
@Singleton
class ContentResolverMediaMetadataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : MediaMetadataSource {

    private val ioDispatcher = dispatchersHolder.ioDispatcher

    private val metadataCache = LruCache<String, MediaMetadata>(MAX_METADATA_ENTRIES)

    /** 按字节算缓存：按条数计的话 24 张 512px 封面就可能吃掉二十多 MB */
    private val artworkCache = object :
        LruCache<String, Bitmap>(MAX_ARTWORK_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    override suspend fun metadata(entry: MediaEntry): MediaMetadata = withContext(ioDispatcher) {
        metadataCache.get(entry.uri) ?: readMetadata(entry).also {
            metadataCache.put(entry.uri, it)
        }
    }

    override suspend fun artwork(entry: MediaEntry, maxSize: Int): Bitmap? =
        withContext(ioDispatcher) {
            val key = "${entry.uri}@$maxSize"
            artworkCache.get(key) ?: readArtwork(entry, maxSize)?.also {
                artworkCache.put(key, it)
            }
        }

    /** 收藏/最近列表刷新后旧封面可能过期，这里给一个显式的失效入口。 */
    fun clearCaches() {
        metadataCache.evictAll()
        artworkCache.evictAll()
    }

    private fun readMetadata(entry: MediaEntry): MediaMetadata = runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(entry.uri))
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0
            MediaMetadata(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L,
                width = width,
                height = height,
                sizeBytes = readSize(entry.uri),
            )
        } finally {
            runCatching { retriever.release() }
        }
    }.getOrDefault(MediaMetadata())

    /**
     * 封面优先用内嵌图（音频专辑图、带封面的视频），没有再取视频首帧。
     *
     * 时间点取 1 秒：0 秒经常是黑场或片头渐入，缩略图几乎是纯黑的一张。
     */
    private fun readArtwork(entry: MediaEntry, maxSize: Int): Bitmap? = runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(entry.uri))
            val embedded = retriever.embeddedPicture?.let { decodeSampled(it, maxSize) }
            // API 27+ 直接按目标尺寸取帧，避免先解一张全尺寸 4K 帧再缩放
            val frame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                retriever.getScaledFrameAtTime(
                    FIRST_FRAME_US,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    maxSize.coerceAtLeast(1),
                    maxSize.coerceAtLeast(1),
                )
            } else {
                retriever.getFrameAtTime(FIRST_FRAME_US, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
            (embedded ?: frame)?.let { scaleDown(it, maxSize) }
        } finally {
            runCatching { retriever.release() }
        }
    }.getOrNull()

    /** 先读尺寸再按 inSampleSize 解码，避免相册封面直接解出整张原图。 */
    private fun decodeSampled(bytes: ByteArray, maxSize: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSize)
        }
        return runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) }
            .getOrNull()
    }

    private fun sampleSizeFor(width: Int, height: Int, maxSize: Int): Int {
        if (maxSize <= 0) return 1
        var sample = 1
        while (width / (sample * 2) >= maxSize && height / (sample * 2) >= maxSize) {
            sample *= 2
        }
        return sample
    }

    /** 文件大小：优先 OpenableColumns.SIZE，取不到再从文件描述符长度兜底。 */
    private fun readSize(uri: String): Long = runCatching {
        val parsed = Uri.parse(uri)
        val fromColumns = context.contentResolver.query(
            parsed,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0 && !cursor.isNull(index)) cursor.getLong(index) else null
            } else null
        }
        fromColumns ?: context.contentResolver.openAssetFileDescriptor(parsed, "r")?.use { it.length }
    }.getOrNull() ?: 0L

    private fun scaleDown(source: Bitmap, maxSize: Int): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (maxSize <= 0 || longest <= maxSize) return source
        val ratio = maxSize.toFloat() / longest
        return Bitmap.createScaledBitmap(
            source,
            (source.width * ratio).toInt().coerceAtLeast(1),
            (source.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    }

    private companion object {
        const val MAX_METADATA_ENTRIES = 96

        /** 封面缓存上限：12MB，超出按最近最少使用淘汰 */
        const val MAX_ARTWORK_BYTES = 12 * 1024 * 1024
        const val FIRST_FRAME_US = 1_000_000L
    }
}
