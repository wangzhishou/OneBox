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

import kotlinx.serialization.Serializable

/**
 * 备份清单（`manifest.json`）：恢复前的校验、页面上的备份详情都读它。
 *
 * 刻意只放「读一次就够」的元信息，不放分类统计——统计要查库，而查库的代价
 * 与备份体量成正比，页面每刷新一次就查一遍不划算。
 */
@Serializable
data class BackupManifest(
    val schema: Int = BackupFormat.SCHEMA,
    val createdAt: Long = 0L,
    val appVersionCode: Long = 0L,
    val appVersionName: String = "",
    val locale: String = "",
    val device: String = "",
    val databases: List<BackupDatabaseEntry> = emptyList(),
    val mmkvStores: List<String> = emptyList(),
    val userDirs: List<String> = emptyList(),
    val hasSettings: Boolean = false,
    val totalBytes: Long = 0L,
) {

    val roles: List<String> get() = databases.map { it.role }

    fun hasDatabase(role: String): Boolean = databases.any { it.role == role }
}

@Serializable
data class BackupDatabaseEntry(
    val role: String,
    val sizeBytes: Long = 0L,
)

/**
 * 对一个备份文件的「只读体检」结果，用于恢复前展示与拦截。
 *
 * @param manifest 为 null 表示旧格式（v1）容器，没有清单
 * @param issue 非空即不可恢复，具体原因交给 UI 映射文案
 */
data class BackupInspection(
    val manifest: BackupManifest?,
    val isLegacy: Boolean,
    val hasAppDatabase: Boolean,
    val sizeBytes: Long,
    val issue: RestoreIssue? = null,
) {

    val compatible: Boolean get() = issue == null

    val createdAt: Long get() = manifest?.createdAt ?: 0L
    val appVersionName: String get() = manifest?.appVersionName.orEmpty()
    val appVersionCode: Long get() = manifest?.appVersionCode ?: 0L
    val locale: String get() = manifest?.locale.orEmpty()
}
