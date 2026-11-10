package com.shifenmiao.common.backup

import com.shifenmiao.common.backup.destination.BackupDestination
import com.shifenmiao.common.backup.destination.BackupDestinationIds
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 云盘落点的只读视图：有没有配连接、当前落点叫什么。
 *
 * 抽出来是为了让「自动备份开关」这种不持有备份目标集合的界面也能问一句，
 * 而不必把整个 [BackupDestination] 集合塞进 EntryPoint。
 */
@Singleton
class BackupCloudStatus @Inject constructor(
    private val destinations: Set<@JvmSuppressWildcards BackupDestination>,
) {

    private fun cloudDestination(): BackupDestination? =
        destinations.firstOrNull { it.id == BackupDestinationIds.CLOUD }

    suspend fun isAvailable(): Boolean =
        runCatching { cloudDestination()?.isAvailable() == true }.getOrDefault(false)

    suspend fun label(): String? {
        val destination = cloudDestination() ?: return null
        if (runCatching { destination.isAvailable() }.getOrDefault(false).not()) return null
        return runCatching { destination.describeTarget() }.getOrNull()
    }
}
