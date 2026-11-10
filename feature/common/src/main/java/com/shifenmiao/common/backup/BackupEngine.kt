package com.shifenmiao.common.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.FeatureDatabase
import com.tencent.mmkv.MMKV
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.utils.fileProviderAuthority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 备份编排层：创建 / 体检 / 恢复。
 *
 * 恢复是这里唯一「有破坏性」的操作，所以顺序固定：
 * 校验 → 先存恢复点 → 关库删库 → 解包 → 设置与历史 → 交给 UI 冷启动。
 * 解包中途失败会拿恢复点回滚，不会再出现「写一半、库没了」的状态。
 */
@Singleton
class BackupEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val writer: BackupArchiveWriter,
    private val history: BackupHistoryStore,
    private val settingsManager: SettingsManager,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun createBackup(onPhase: (BackupPhase) -> Unit = {}): Result<BackupRecord> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(history.directory, fileNameForNow())
                val manifest = writer.write(file, onPhase)
                val record = recordOf(file, manifest)
                history.add(record)
                record
            }
        }

    suspend fun records(): List<BackupRecord> = history.records()

    /** 历史记录对应的本机文件；可能已被保留策略清理，用前先查 isFile。 */
    fun fileOf(record: BackupRecord) = history.fileOf(record)

    suspend fun delete(record: BackupRecord): List<BackupRecord> = history.remove(record.id)

    suspend fun markExported(
        record: BackupRecord,
        label: String,
        documentUri: String?,
    ): List<BackupRecord> = history.update(
        record.copy(
            destinationLabel = label,
            documentUri = documentUri ?: record.documentUri,
        )
    )

    /** 上次恢复前自动留的「后悔药」；不存在时为 null。 */
    suspend fun preRestoreRecord(): BackupRecord? = withContext(Dispatchers.IO) {
        val file = history.preRestoreFile()
        if (!file.exists()) return@withContext null
        val inspection = runCatching {
            inspectStream({ file.inputStream() }, file.length())
        }.getOrNull() ?: return@withContext null
        inspection.manifest?.let { recordOf(file, it) }
    }

    fun inspect(uri: Uri): Result<BackupInspection> = runCatching {
        val size = runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        }.getOrNull()?.coerceAtLeast(0L) ?: 0L
        inspectStream(
            open = { context.contentResolver.openInputStream(uri) ?: error("cannot open backup") },
            sizeBytes = size,
        )
    }

    suspend fun restore(
        uri: Uri,
        onPhase: (BackupPhase) -> Unit = {},
    ): RestoreOutcome = withContext(Dispatchers.IO) {
        restoreStream(
            open = { context.contentResolver.openInputStream(uri) ?: error("cannot open backup") },
            onPhase = onPhase,
        )
    }

    suspend fun restore(
        record: BackupRecord,
        onPhase: (BackupPhase) -> Unit = {},
    ): RestoreOutcome = withContext(Dispatchers.IO) {
        val file = history.fileOf(record)
        if (file.exists()) {
            // 恢复「恢复点」本身时要注意：后面的 pre-restore 快照会写到同一个文件，
            // 必须先把来源拷出来，否则会读到刚写下的当前状态、把恢复变成空操作。
            val isPreRestoreFile = file.absolutePath == history.preRestoreFile().absolutePath
            val source = if (isPreRestoreFile) {
                File(context.cacheDir, "restore_source_${System.currentTimeMillis()}.zip").also {
                    runCatching { file.copyTo(it, overwrite = true) }
                }
            } else {
                file
            }
            try {
                return@withContext restoreStream(open = { source.inputStream() }, onPhase = onPhase)
            } finally {
                if (isPreRestoreFile) runCatching { source.delete() }
            }
        }
        val uri = record.documentUri?.toUri()
        if (uri != null) {
            val reopened = runCatching {
                context.contentResolver.openInputStream(uri) ?: error("cannot open backup")
            }.getOrNull()
            if (reopened != null) {
                return@withContext restoreStream(open = { reopened }, onPhase = onPhase)
            }
        }
        RestoreOutcome.Rejected(RestoreIssue.MISSING_LOCAL_COPY)
    }

    // ── 内部实现 ───────────────────────────────────────────

    private suspend fun restoreStream(
        open: () -> InputStream,
        onPhase: (BackupPhase) -> Unit,
    ): RestoreOutcome {
        onPhase(BackupPhase.VALIDATING)
        val inspection = runCatching { inspectStream(open, 0L) }.getOrElse {
            return RestoreOutcome.Rejected(RestoreIssue.UNREADABLE)
        }
        inspection.issue?.let { issue ->
            return RestoreOutcome.Rejected(issue, inspection.manifest?.appVersionName)
        }

        onPhase(BackupPhase.SNAPSHOT)
        val snapshotFile = history.preRestoreFile()
        val snapshotReady = runCatching {
            writer.write(snapshotFile, onPhase = {})
        }.isSuccess
        if (!snapshotReady) {
            return RestoreOutcome.Failed(message = "snapshot failed", rolledBack = false)
        }

        return try {
            extract(open, onPhase)
            RestoreOutcome.Success
        } catch (t: Throwable) {
            onPhase(BackupPhase.ROLLING_BACK)
            val rolledBack = runCatching {
                extract(open = { snapshotFile.inputStream() }, onPhase = {})
            }.isSuccess
            RestoreOutcome.Failed(message = t.message, rolledBack = rolledBack)
        }
    }

    private suspend fun extract(
        open: () -> InputStream,
        onPhase: (BackupPhase) -> Unit,
    ) {
        onPhase(BackupPhase.REMOVING_OLD)
        AppDatabase.closeInstance()
        FeatureDatabase.closeInstance()

        val targets = BackupFormat.ROLES.associateWith { role ->
            File(context.getDatabasePath(BackupFormat.dbFileNameOf(role)).path)
        }
        targets.values.forEach { database ->
            runCatching { database.delete() }
            runCatching { File(database.path + "-wal").delete() }
            runCatching { File(database.path + "-shm").delete() }
            database.parentFile?.mkdirs()
        }
        // 用户文档目录整体替换，避免旧文件和新备份混在一起
        BackupFormat.USER_FILE_DIRS.forEach { dir ->
            runCatching { File(context.filesDir, dir).deleteRecursively() }
        }

        val mmkvDirectory = File(context.cacheDir, "restore_mmkv_${System.currentTimeMillis()}")
            .apply { mkdirs() }
        var settingsBytes: ByteArray? = null
        val restoredRoles = mutableSetOf<String>()

        open().use { input ->
            ZipInputStream(BufferedInputStream(input)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    val dbEntry = resolveDbEntry(name)
                    when {
                        dbEntry != null -> {
                            val (role, suffix) = dbEntry
                            targets[role]?.let { target ->
                                val output = if (suffix.isEmpty()) target else File(target.path + suffix)
                                output.outputStream().use { stream -> zip.copyTo(stream) }
                                if (suffix.isEmpty()) restoredRoles += role
                            }
                        }

                        name.startsWith("${BackupFormat.MMKV_DIR}/") -> {
                            val output = File(mmkvDirectory, name.removePrefix("${BackupFormat.MMKV_DIR}/"))
                            output.outputStream().use { stream -> zip.copyTo(stream) }
                        }

                        name.startsWith("${BackupFormat.FILES_DIR}/") -> {
                            restoreUserFile(name, zip)
                        }

                        name == BackupFormat.SETTINGS_ENTRY -> {
                            settingsBytes = zip.readBytes()
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        check(BackupFormat.ROLE_APP in restoredRoles) { "backup archive has no app database" }

        onPhase(BackupPhase.EXTRACTING_SETTINGS)
        settingsBytes?.let { bytes -> runCatching { restoreSettings(bytes) } }

        onPhase(BackupPhase.EXTRACTING_FILES)
        // 文件条目在上面的循环里已经落盘

        onPhase(BackupPhase.EXTRACTING_HISTORIES)
        runCatching { applyMmkv(mmkvDirectory) }
        mmkvDirectory.deleteRecursively()
    }

    /**
     * 把一个条目名解析成 (role, 后缀)。同时兼容两种容器：
     * v2 的 `db/app.db-wal`，和旧格式里直接叫 `one_box_zh-Hans-CN.db` 的条目。
     */
    /**
     * 还原一个 `files/<dir>/<relative>` 条目。
     *
     * 必须挡路径穿越：恶意 zip 里的 `files/../../databases/x` 会写到应用私有目录之外。
     * 只允许落在白名单目录下。
     */
    private fun restoreUserFile(entryName: String, zip: ZipInputStream) {
        val relative = entryName.removePrefix("${BackupFormat.FILES_DIR}/")
        val dir = relative.substringBefore('/')
        if (dir !in BackupFormat.USER_FILE_DIRS) return
        val root = File(context.filesDir, dir).canonicalFile
        val target = File(root, relative.substringAfter('/', "")).canonicalFile
        if (!target.path.startsWith(root.path + File.separator)) return
        target.parentFile?.mkdirs()
        target.outputStream().use { stream -> zip.copyTo(stream) }
    }

    private fun resolveDbEntry(entryName: String): Pair<String, String>? {
        if (entryName.startsWith("${BackupFormat.DB_DIR}/")) {
            val relative = entryName.removePrefix("${BackupFormat.DB_DIR}/")
            val role = relative.substringBefore('.')
            if (role !in BackupFormat.ROLES) return null
            val suffix = relative.substringAfter(role).removePrefix(".db")
            return if (suffix.isEmpty() || suffix == "-wal" || suffix == "-shm") role to suffix else null
        }
        val role = BackupFormat.roleOfLegacyEntry(entryName) ?: return null
        val suffix = when {
            entryName.endsWith("-wal") -> "-wal"
            entryName.endsWith("-shm") -> "-shm"
            entryName.endsWith(".db") -> ""
            else -> return null
        }
        return role to suffix
    }

    /**
     * 设置存储是 DataStore 的 preferences_pb，只接受 URI 入口。
     * 这里借 FileProvider 把字节包成一个 content URI，走原生的恢复链路。
     */
    private suspend fun restoreSettings(bytes: ByteArray) {
        val temporary = File(context.cacheDir, "restore_settings_${System.currentTimeMillis()}")
        temporary.writeBytes(bytes)
        try {
            val uri = FileProvider.getUriForFile(context, context.fileProviderAuthority, temporary)
            var failure: Throwable? = null
            settingsManager.restoreFromBackupFile(
                backupFileUri = uri.toString(),
                onSuccess = {},
                onFailure = { failure = it },
            )
            failure?.let { throw it }
        } finally {
            runCatching { temporary.delete() }
        }
    }

    /**
     * MMKV 是 mmap 文件，只能用「先 onExit 落盘、再整文件拷回去」的方式恢复；
     * 恢复后进程马上冷启动，实例会重新建。
     */
    private fun applyMmkv(directory: File) {
        val files = directory.listFiles()?.filter { it.isFile }.orEmpty()
        if (files.isEmpty()) return
        runCatching { MMKV.onExit() }
        val root = runCatching { MMKV.getRootDir() }.getOrNull()
            ?: File(context.filesDir, "mmkv").absolutePath
        File(root).mkdirs()
        files.forEach { file ->
            runCatching { file.copyTo(File(root, file.name), overwrite = true) }
        }
    }

    private fun inspectStream(
        open: () -> InputStream,
        sizeBytes: Long,
    ): BackupInspection {
        var parsedManifest: BackupManifest? = null
        val entryNames = mutableListOf<String>()

        open().use { input ->
            ZipInputStream(BufferedInputStream(input)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    entryNames += entry.name
                    if (entry.name == BackupFormat.MANIFEST_ENTRY) {
                        val text = zip.readBytes().decodeToString()
                        parsedManifest = runCatching {
                            json.decodeFromString<BackupManifest>(text)
                        }.getOrNull()
                    }
                    entry = zip.nextEntry
                }
            }
        }

        val manifest = parsedManifest
        val isLegacy = manifest == null && entryNames.any { BackupFormat.roleOfLegacyEntry(it) != null }
        val hasAppDatabase = if (manifest != null) {
            manifest.hasDatabase(BackupFormat.ROLE_APP)
        } else {
            entryNames.any {
                BackupFormat.roleOfLegacyEntry(it) == BackupFormat.ROLE_APP && it.endsWith(".db")
            }
        }

        val issue = when {
            manifest == null && !isLegacy -> RestoreIssue.UNREADABLE
            manifest != null && manifest.schema > BackupFormat.SCHEMA -> RestoreIssue.NEWER_SCHEMA
            manifest != null && manifest.appVersionCode > currentVersionCode() -> RestoreIssue.NEWER_APP
            !hasAppDatabase -> RestoreIssue.MISSING_APP_DATABASE
            else -> null
        }

        return BackupInspection(
            manifest = manifest,
            isLegacy = isLegacy,
            hasAppDatabase = hasAppDatabase,
            sizeBytes = sizeBytes,
            issue = issue,
        )
    }

    private fun recordOf(file: File, manifest: BackupManifest): BackupRecord = BackupRecord(
        id = UUID.randomUUID().toString(),
        createdAt = manifest.createdAt,
        fileName = file.name,
        sizeBytes = file.length(),
        schema = manifest.schema,
        appVersionCode = manifest.appVersionCode,
        appVersionName = manifest.appVersionName,
        locale = manifest.locale,
        roles = manifest.roles,
        mmkvStoreCount = manifest.mmkvStores.size,
        hasUserFiles = manifest.userDirs.isNotEmpty(),
        hasSettings = manifest.hasSettings,
        isLegacy = false,
    )

    private fun currentVersionCode(): Long = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        PackageInfoCompat.getLongVersionCode(info)
    }.getOrDefault(Long.MAX_VALUE)

    private fun fileNameForNow(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${BackupFormat.FILE_PREFIX}_$stamp.${BackupFormat.FILE_EXTENSION}"
    }
}
