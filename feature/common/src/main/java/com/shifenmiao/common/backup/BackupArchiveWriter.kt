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

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.FeatureDatabase
import com.tencent.mmkv.MMKV
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 把当前设备数据打成 v2 备份容器。
 *
 * 只做「读本地 + 写 zip」，不碰网络、不做保留策略（那是 [BackupHistoryStore] 的事）。
 */
@Singleton
class BackupArchiveWriter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val settingsManager: SettingsManager,
) {

    private val json = Json { encodeDefaults = true }

    suspend fun write(
        target: File,
        onPhase: (BackupPhase) -> Unit = {},
    ): BackupManifest = withContext(Dispatchers.IO) {
        target.parentFile?.mkdirs()

        onPhase(BackupPhase.CHECKPOINT)
        checkpointWal()

        onPhase(BackupPhase.DATABASES)
        val databases = BackupFormat.ROLES.map { role ->
            BackupDatabaseEntry(
                role = role,
                sizeBytes = File(context.getDatabasePath(BackupFormat.dbFileNameOf(role)).path).length(),
            )
        }

        onPhase(BackupPhase.HISTORIES)
        val mmkvDirectory = File(context.cacheDir, "backup_mmkv_${System.currentTimeMillis()}")
            .apply { mkdirs() }
        val mmkvStores = packMmkv(mmkvDirectory)

        onPhase(BackupPhase.FILES)
        val userDirs = BackupFormat.USER_FILE_DIRS.filter { dir ->
            File(context.filesDir, dir).isDirectory
        }
        val userFilesBytes = userDirs.sumOf { dir ->
            val root = File(context.filesDir, dir)
            root.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        }

        val settingsBytes = runCatching { settingsManager.createBackupFile() }.getOrNull()

        val manifest = BackupManifest(
            schema = BackupFormat.SCHEMA,
            createdAt = System.currentTimeMillis(),
            appVersionCode = appVersionCode(),
            appVersionName = appVersionName(),
            locale = LocaleUtils.getCurrentLocaleTag(),
            device = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            databases = databases,
            mmkvStores = mmkvStores,
            userDirs = userDirs,
            hasSettings = settingsBytes != null,
            totalBytes = databases.sumOf { it.sizeBytes } +
                mmkvDirectory.walkTopDown().filter { it.isFile }.sumOf { it.length() } +
                userFilesBytes +
                (settingsBytes?.size ?: 0).toLong(),
        )

        onPhase(BackupPhase.PACKING)
        runCatching {
            ZipOutputStream(BufferedOutputStream(target.outputStream())).use { zip ->
                databases.forEach { entry ->
                    val name = BackupFormat.dbFileNameOf(entry.role)
                    zip.writeFile("${BackupFormat.DB_DIR}/${entry.role}.db", File(context.getDatabasePath(name).path))
                    zip.writeFile("${BackupFormat.DB_DIR}/${entry.role}.db-wal", File("${context.getDatabasePath(name).path}-wal"))
                    zip.writeFile("${BackupFormat.DB_DIR}/${entry.role}.db-shm", File("${context.getDatabasePath(name).path}-shm"))
                }
                mmkvDirectory.listFiles()?.forEach { file ->
                    zip.writeFile("${BackupFormat.MMKV_DIR}/${file.name}", file)
                }
                userDirs.forEach { dir ->
                    val root = File(context.filesDir, dir)
                    root.walkTopDown()
                        .filter { it.isFile }
                        .forEach { file ->
                            val relative = file.relativeTo(root).path
                            zip.writeFile("${BackupFormat.FILES_DIR}/$dir/$relative", file)
                        }
                }
                settingsBytes?.let { zip.writeBytes(BackupFormat.SETTINGS_ENTRY, it) }
                zip.writeBytes(BackupFormat.MANIFEST_ENTRY, json.encodeToString(manifest).toByteArray())
            }
        }.also {
            mmkvDirectory.deleteRecursively()
        }.getOrThrow()

        manifest
    }

    /**
     * 备份前把 WAL 落回主库，避免 zip 里主库与 WAL 处于撕裂状态。
     * 失败不阻断：后面仍会把 -wal/-shm 一并打进去。
     */
    private fun checkpointWal() {
        runCatching {
            appDatabase.openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(TRUNCATE)")
                .use { }
        }
    }

    /**
     * 用 MMKV 自己的备份接口按库导出（内部会先 sync），比直接拷 mmap 文件安全。
     * 返回真正导出成功的库 id 列表。
     */
    private fun packMmkv(directory: File): List<String> =
        BackupFormat.MMKV_WHITELIST.filter { id ->
            runCatching { MMKV.backupOneToDirectory(id, directory.absolutePath, null) }
                .getOrDefault(false)
        }

    private fun appVersionCode(): Long = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        PackageInfoCompat.getLongVersionCode(info)
    }.getOrDefault(0L)

    private fun appVersionName(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }.getOrDefault("")

    private fun ZipOutputStream.writeFile(entryName: String, file: File) {
        if (!file.exists() || !file.isFile) return
        putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(this) }
        closeEntry()
    }

    private fun ZipOutputStream.writeBytes(entryName: String, bytes: ByteArray) {
        putNextEntry(ZipEntry(entryName))
        write(bytes)
        closeEntry()
    }
}
