package com.wanbaohe.media.data

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import com.wanbaohe.media.domain.MediaDeviceLibrarySource
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaKind
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本机媒体库：直接查 MediaStore。
 *
 * 这一层负责"全部视频 / 全部歌曲"的真实数据；"最近打开"仍来自公共访问记录表。
 * 没有读媒体权限时返回空表，页面会退回只看最近记录，并提示用户授权。
 *
 * 元数据（标题/作者/专辑/时长/位置）一次查全，列表行就不用再为每一项跑
 * MediaMetadataRetriever：几百条时那会是几百次 IO。
 */
@Singleton
class MediaStoreDeviceLibrarySource @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : MediaDeviceLibrarySource {

    private val ioDispatcher = dispatchersHolder.ioDispatcher

    override suspend fun load(kind: MediaKind): List<MediaEntry> = withContext(ioDispatcher) {
        runCatching { query(kind) }
            .onFailure { com.t8rin.logger.makeLog(it, "MediaStoreDeviceLibrary", com.t8rin.logger.Logger.Level.Error) }
            .getOrDefault(emptyList())
    }

    private fun query(kind: MediaKind): List<MediaEntry> {
        val collection = when (kind) {
            MediaKind.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            MediaKind.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val includeRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        // 标题/作者/专辑/时长在 AudioColumns / VideoColumns 下各有一套常量
        val baseColumns = when (kind) {
            MediaKind.VIDEO -> arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.TITLE,
                MediaStore.Video.VideoColumns.ARTIST,
                MediaStore.Video.VideoColumns.ALBUM,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.MediaColumns.DATA,
            )

            MediaKind.AUDIO -> arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.MediaColumns.DATA,
            )
        }
        val columns = if (includeRelativePath) {
            baseColumns + MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            baseColumns
        }

        // 隐藏未完成/回收站里的条目；音频再排除铃声、通知音、闹钟
        val selectionParts = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add("${MediaStore.MediaColumns.IS_PENDING}=0")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add("${MediaStore.MediaColumns.IS_TRASHED}=0")
            }
            if (kind == MediaKind.AUDIO) {
                add("${MediaStore.Audio.AudioColumns.IS_RINGTONE}=0")
                add("${MediaStore.Audio.AudioColumns.IS_NOTIFICATION}=0")
                add("${MediaStore.Audio.AudioColumns.IS_ALARM}=0")
            }
        }
        val selection = selectionParts.takeIf { it.isNotEmpty() }?.joinToString(" AND ")

        val result = ArrayList<MediaEntry>(DEFAULT_LIMIT)
        context.contentResolver.query(
            collection,
            columns,
            selection,
            null,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            val titleIndex = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)
            val artistIndex = cursor.getColumnIndex(MediaStore.MediaColumns.ARTIST)
            val albumIndex = cursor.getColumnIndex(MediaStore.MediaColumns.ALBUM)
            val durationIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION)
            val dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)

            while (cursor.moveToNext() && result.size < DEFAULT_LIMIT) {
                val id = cursor.getLong(idIndex)
                val displayName = nameIndex.takeIf { it >= 0 }?.let(cursor::getString)
                    ?.takeIf { it.isNotBlank() }
                    ?: continue
                val location = relativePathIndex.takeIf { it >= 0 }?.let(cursor::getString)
                    ?.takeIf { it.isNotBlank() }
                    ?.trimEnd('/')
                    ?: dataIndex.takeIf { it >= 0 }?.let(cursor::getString)
                        ?.takeIf { it.isNotBlank() }
                        ?.substringBeforeLast('/')
                result += MediaEntry(
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    displayName = displayName,
                    mediaTitle = titleIndex.takeIf { it >= 0 }?.let(cursor::getString)
                        ?.takeUnless(::isUnknown),
                    artist = artistIndex.takeIf { it >= 0 }?.let(cursor::getString)
                        ?.takeUnless(::isUnknown),
                    album = albumIndex.takeIf { it >= 0 }?.let(cursor::getString)
                        ?.takeUnless(::isUnknown),
                    durationMs = durationIndex.takeIf { it >= 0 }?.let(cursor::getLong) ?: 0L,
                    location = location,
                    identityUri = ContentUris.withAppendedId(collection, id).toString(),
                )
            }
        }
        return result
    }

    /** MediaStore 对未知标签返回 "<unknown>"，不能当成作者显示 */
    private fun isUnknown(value: String): Boolean =
        value.isBlank() || value.equals("<unknown>", ignoreCase = true)

    private companion object {
        /** 列表不分页，但也不该把几万条全端进内存 */
        const val DEFAULT_LIMIT = 1000
    }
}
