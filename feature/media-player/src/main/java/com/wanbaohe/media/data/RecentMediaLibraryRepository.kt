package com.wanbaohe.media.data

import com.shifenmiao.common.recent.RecentAccessRepository
import com.t8rin.imagetoolbox.core.domain.content.ContentType
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.MediaLibraryRepository
import com.wanbaohe.media.domain.canPlay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 媒体库 = 公共"最近访问"表按类型过滤。
 *
 * 刻意不另建一张播放历史表：从文件浏览器、外部 Intent、最近文件打开的音频/视频
 * 都已经写进最近访问，媒体库读同一份就自动"打通"，不存在两套记录对不上的情况。
 *
 * 这张表也被别的地方写（文件浏览器记录历史时 displayName 可能只是 URI 的末段，
 * 例如 `content://media/external/audio/media/86` 会记成 "86"），所以这里不只看后缀，
 * 还会按 URI 的媒体库特征（`/audio/`、`/video/`）判断类型，并从 URI 反查真实文件名。
 */
@Singleton
class RecentMediaLibraryRepository @Inject constructor(
    private val recentAccessRepository: RecentAccessRepository,
    dispatchersHolder: DispatchersHolder,
) : MediaLibraryRepository {

    private val ioDispatcher = dispatchersHolder.ioDispatcher

    override fun observeRecent(kind: MediaKind): Flow<List<MediaEntry>> =
        recentAccessRepository.observeAll(RECENT_LIMIT).map { records ->
            withContext(ioDispatcher) {
                val entries = ArrayList<MediaEntry>(records.size)
                records.forEach { record ->
                    if (record.accessType != RecentAccessRepository.TYPE_FILE) return@forEach
                    if (kindOf(record.uri, record.displayName) != kind) return@forEach
                    // 打不开的格式不该留在"最近播放":点进去只会静默失败
                    if (!kind.canPlay(record.displayName)) return@forEach
                    entries += MediaEntry(
                        uri = record.uri,
                        displayName = record.displayName,
                        accessedAt = record.accessedAt,
                    )
                }
                // 只按 URI 去重：同名文件可能确实存在于不同目录，按文件名合并会把它们吃掉
                entries.distinctBy { it.uri }
            }
        }

    override suspend fun recordOpened(entry: MediaEntry) {
        recentAccessRepository.recordAccess(
            uri = entry.uri,
            displayName = entry.displayName,
            accessType = RecentAccessRepository.TYPE_FILE,
        )
    }

    /**
     * 清空媒体类最近记录。
     *
     * 共用表没有"按类型清空"的 DAO 方法，也不该为了媒体库给公共表加口子，
     * 所以逐条删：只删音频/视频后缀的记录，图片/文档的历史原样保留。
     */
    override suspend fun clearRecent(kind: MediaKind) {
        recentAccessRepository.getAll(RECENT_LIMIT)
            .filter { kindOf(it.uri, it.displayName) == kind }
            .forEach {
                recentAccessRepository.remove(it.uri)
            }
    }

    override suspend fun forget(uri: String) {
        recentAccessRepository.remove(uri)
    }

    /**
     * 判断一条记录属于哪类媒体。
     *
     * 后缀白名单来自 core:domain（全 App 唯一一份）；没有后缀的记录再看 URI：
     * MediaStore 的音频/视频 URI 分别带 `/audio/`、`/video/` 段。
     */
    private fun kindOf(uri: String, displayName: String): MediaKind? {
        val extension = displayName.substringAfterLast('.', "").lowercase()
        if (extension.isNotBlank()) {
            return when (extension) {
                in ContentType.VIDEO_EXTENSIONS -> MediaKind.VIDEO
                in ContentType.AUDIO_EXTENSIONS -> MediaKind.AUDIO
                else -> null
            }
        }
        return when {
            uri.contains("/video/") -> MediaKind.VIDEO
            uri.contains("/audio/") -> MediaKind.AUDIO
            else -> null
        }
    }

    private companion object {
        const val RECENT_LIMIT = 200
    }
}
