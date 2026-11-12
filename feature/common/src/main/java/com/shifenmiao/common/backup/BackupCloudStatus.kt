/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
