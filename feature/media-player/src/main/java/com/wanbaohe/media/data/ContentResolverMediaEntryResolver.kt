package com.wanbaohe.media.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaEntryResolver
import com.wanbaohe.media.domain.MediaKind
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * URI → [MediaEntry] 的默认实现。
 *
 * 对 MediaStore 的音频/视频 URI 会顺手读出标题/作者/专辑/时长/目录，
 * 这样"最近打开"里的条目也能按标题搜索、副标题有作者；其它 URI 只取显示名，
 * 额外的元数据留给播放页按需读。
 */
@Singleton
class ContentResolverMediaEntryResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : MediaEntryResolver {

    private val ioDispatcher = dispatchersHolder.ioDispatcher

    override suspend fun resolve(uri: String): MediaEntry? = withContext(ioDispatcher) {
        val parsed = runCatching { Uri.parse(uri) }.getOrNull() ?: return@withContext null
        val kind = kindOf(parsed)
        if (kind != null) {
            resolveMediaStore(parsed, kind)?.let { return@withContext it }
        }
        val name = displayName(parsed) ?: parsed.lastPathSegment?.substringAfterLast('/')
        if (name.isNullOrBlank()) {
            null
        } else {
            MediaEntry(
                uri = uri,
                displayName = name,
                // 系统选择器给的是文档 URI；归一化后用于识别"同一个文件"，不影响播放用的 uri
                identityUri = identityOf(parsed),
            )
        }
    }

    /**
     * 同一份内容的规范化 URI。
     *
     * Android 10+ 可以直接用 [MediaStore.getMediaUri] 把 DocumentsProvider 的
     * `document/video:123` 映射成 `content://media/external/video/media/123`，
     * 收藏/最近里同一文件就不会出现两条。
     */
    private fun identityOf(uri: Uri): String? {
        if (kindOf(uri) != null) return uri.toString()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return runCatching { MediaStore.getMediaUri(context, uri)?.toString() }.getOrNull()
    }

    private fun kindOf(uri: Uri): MediaKind? = when {
        uri.path?.contains("/audio/") == true -> MediaKind.AUDIO
        uri.path?.contains("/video/") == true -> MediaKind.VIDEO
        else -> null
    }

    private fun resolveMediaStore(uri: Uri, kind: MediaKind): MediaEntry? = runCatching {
        val columns = when (kind) {
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
        context.contentResolver.query(uri, columns, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val displayName = stringOf(cursor, MediaStore.MediaColumns.DISPLAY_NAME) ?: return@use null
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            val collection = when (kind) {
                MediaKind.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                MediaKind.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val location = stringOf(cursor, MediaStore.MediaColumns.DATA)?.substringBeforeLast('/')
            MediaEntry(
                // 统一成 "external/<kind>/media/<id>" 形式，避免不同入口拿到不同的 document URI
                uri = ContentUris.withAppendedId(collection, id).toString(),
                displayName = displayName,
                mediaTitle = stringOf(cursor, MediaStore.Video.VideoColumns.TITLE)
                    ?.takeUnless(::isUnknown),
                artist = stringOf(cursor, MediaStore.Video.VideoColumns.ARTIST)
                    ?.takeUnless(::isUnknown),
                album = stringOf(cursor, MediaStore.Video.VideoColumns.ALBUM)
                    ?.takeUnless(::isUnknown),
                durationMs = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
                    .takeIf { it >= 0 }
                    ?.let { cursor.getLong(it) }
                    ?: 0L,
                location = location,
                identityUri = ContentUris.withAppendedId(collection, id).toString(),
            )
        }
    }.getOrNull()

    private fun stringOf(cursor: Cursor, column: String): String? {
        val index = cursor.getColumnIndex(column)
        return if (index >= 0 && !cursor.isNull(index)) cursor.getString(index) else null
    }

    private fun isUnknown(value: String): Boolean =
        value.isBlank() || value.equals(UNKNOWN_ARTIST, ignoreCase = true)

    private fun displayName(uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        return runCatching {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: uri.path?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
    }

    private companion object {
        /** MediaStore 对未知作者返回的字面量，不能当作者显示/搜索 */
        const val UNKNOWN_ARTIST = "<unknown>"
    }
}
