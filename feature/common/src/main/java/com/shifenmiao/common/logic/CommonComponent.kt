package com.shifenmiao.common.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.withTransaction
import com.arkivanov.decompose.ComponentContext
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.model.StrapiImage
import com.shifenmiao.model.common.ImportOrExportProgress
import com.shifenmiao.model.common.ProgressType
import com.shifenmiao.model.prompt.AgentJson
import com.shifenmiao.network.api.ApiService
import com.t8rin.imagetoolbox.core.data.utils.getPath
import com.t8rin.imagetoolbox.core.data.utils.outputStream
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.logger.makeLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.use
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

abstract class CommonComponent(
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    componentContext: ComponentContext,
    val appDatabase: AppDatabase,
    private val apiService: ApiService,
    val fileController: FileController,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _settingsState = settingsManager.settingsState
    private val settingsState get() = _settingsState.value
    private val _importOrExportProgress = MutableStateFlow(ImportOrExportProgress())
    val importOrExportProgress: StateFlow<ImportOrExportProgress> = _importOrExportProgress


    fun uploadImages(
        imageUris: List<Uri>,
        ref: String? = null,
        refId: String? = null,
        field: String? = null,
        onUploadFailure: (index: Int, String) -> Unit = { _, _ -> },
        onUploadSuccess: (index: Int, List<StrapiImage>) -> Unit = { _, _ -> },
        onProgressUpdate: ((index: Int, progress: Float) -> Unit)? = null
    ) {
        componentScope.launch(ioDispatcher) {
            try {
                // Process each image serially
                imageUris.forEachIndexed { index, imageUri ->
                    try {
                        // Reset progress for current image
                        onProgressUpdate?.invoke(index, 0f)

                        // Verify it's an image
                        val mimeType =
                            AppContext.getContext().contentResolver.getType(imageUri)
                        if (mimeType?.startsWith("image/") != true) {
                            throw Exception("不是有效的图片格式")
                        }

                        // Get input stream from URI
                        val inputStream =
                            AppContext.getContext().contentResolver.openInputStream(
                                imageUri
                            )
                                ?: throw Exception("无法读取图片文件")

                        // Get original filename and sanitize it
                        val originalName =
                            imageUri.lastPathSegment ?: "image_${System.currentTimeMillis()}"
                        val fileExtension = originalName.substringAfterLast('.', "jpg")

                        // Sanitize filename - remove special characters and use only alphanumeric + underscore
                        val sanitizedName = originalName.substringBeforeLast('.')
                            .replace(Regex("[^a-zA-Z0-9_]"), "_")
                            .take(100) // Limit length

                        // Create safe filename with extension
                        val safeFileName =
                            "${sanitizedName}_${System.currentTimeMillis()}.$fileExtension"

                        // Create file part with sanitized name
                        val requestFile = inputStream.readBytes().toRequestBody(
                            mimeType.toMediaTypeOrNull()
                        )
                        val filePart =
                            MultipartBody.Part.createFormData("files", safeFileName, requestFile)

                        // Create optional parts if provided
                        val refPart = ref?.toRequestBody("text/plain".toMediaTypeOrNull())
                        val refIdPart = refId?.toRequestBody("text/plain".toMediaTypeOrNull())
                        val fieldPart = field?.toRequestBody("text/plain".toMediaTypeOrNull())

                        // Update progress to indicate we're about to make the request
                        onProgressUpdate?.invoke(index, 0.5f)

                        // Make API call
                        val response =
                            apiService.uploadFile(filePart, refPart, refIdPart, fieldPart).execute()

                        if (response.isSuccessful) {
                            val images = response.body() ?: emptyList()

                            // 关键: 即使 HTTP 200, 也要检查 X-Moderation-Blocked-Reason
                            // header. 服务端审核拦下时仍返 200 + 空 body + 这个 header,
                            // 用来告诉客户端"上传到了 Strapi 但被百度拒了".
                            // 之前没读这个 header 时, 客户端只能看到"上传成功但 0 张图",
                            // 体验黑洞 (review 反馈).
                            val blockedReason = response.headers()["X-Moderation-Blocked-Reason"]
                            if (blockedReason != null && images.isEmpty()) {
                                // 全部被审核拦下: 标这张图 error, 把原因透传出去.
                                onUploadFailure(index, formatModerationMessage(blockedReason))
                                onProgressUpdate?.invoke(index, 0f)
                            } else {
                                onProgressUpdate?.invoke(index, 1f)
                                onUploadSuccess(index, images)
                                // 部分被拦下时 (images 非空 + header 存在): images 是通过的子集,
                                // 通过 onUploadSuccess 正常处理; blocked 部分调用方不会知道,
                                // 但用户能看到"上传 X 张但只 Y 张成功"的视觉差异.
                                if (blockedReason != null) {
                                    onUploadFailure(
                                        index,
                                        "图片部分被审核拦截, 原因: $blockedReason",
                                    )
                                }
                            }
                        } else {
                            onUploadFailure(index, "上传失败: ${response.message()}")
                            // Reset progress on failure
                            onProgressUpdate?.invoke(index, 0f)
                        }
                    } catch (e: Exception) {
                        makeLog {
                            e.printStackTrace()
                        }
                        onUploadFailure(index, e.message ?: "Unknown error")
                        // Reset progress on failure
                        onProgressUpdate?.invoke(index, 0f)
                    }
                }
            } catch (e: Exception) {
                // Handle any unexpected errors outside the per-image loop
                makeLog {
                    e.printStackTrace()
                }
                onUploadFailure(-1, "批量上传初始化失败: ${e.message}")
            }
        }
    }

    /**
     * Backup Room database files to provided [fileUri].
     *
     * Strategy: checkpoint WAL, then create a ZIP containing:
     * - [AppDatabase.dbNameForLocale()] (main db) + WAL/SHM files
     * - [FeatureDatabase.dbNameForLocale()] (feature db) + WAL/SHM files
     *
     * This is more robust than a single-file copy because it preserves in-flight WAL state
     * on devices/OS builds where checkpoint isn't perfect or database is busy.
     */
    fun exportDatabaseToUri(
        context: Context,
        fileUri: Uri,
        onExportSuccess: () -> Unit = {},
        onExportFailure: (String) -> Unit = {},
        onResult: (SaveResult) -> Unit = {}
    ) {
        componentScope.launch(ioDispatcher) {
            try {
                _importOrExportProgress.value = ImportOrExportProgress(
                    visible = true,
                    progress = 0f,
                    message = "开始备份数据库...",
                    type = ProgressType.EXPORT
                )

                // Database names to backup (按当前语言隔离)
                val dbNames = listOf(AppDatabase.dbNameForLocale(), FeatureDatabase.dbNameForLocale())

                // 1) Make sure all WAL changes are in the main files
                try {
                    appDatabase.openHelper.writableDatabase
                        .query("PRAGMA wal_checkpoint(TRUNCATE)")
                        .use { /* no-op */ }
                } catch (_: Exception) {
                    // Best-effort
                }

                _importOrExportProgress.value = _importOrExportProgress.value.copy(
                    progress = 0.3f,
                    message = "正在打包备份文件..."
                )
                val savingPath = fileUri.toString().getPath(appContext.applicationContext)
                fileController.writeBytes(
                    message = AppContext.getString(com.shifenmiao.core.R.string.data_backup_success, savingPath),
                    uri = fileUri.toString(),
                    block = { out ->
                        ZipOutputStream(out.outputStream()).use { zip ->
                            fun add(name: String, file: File) {
                                if (file.exists()) {
                                    zip.putNextEntry(ZipEntry(name))
                                    file.inputStream().use { it.copyTo(zip) }
                                    zip.closeEntry()
                                }
                            }

                            // Backup all databases
                            dbNames.forEachIndexed { index, currentDbName ->
                                val progress = 0.3f + (index + 1).toFloat() / dbNames.size * 0.4f
                                _importOrExportProgress.value = _importOrExportProgress.value.copy(
                                    progress = progress,
                                    message = "正在备份 $currentDbName..."
                                )

                                val dbFile = context.getDatabasePath(currentDbName)
                                val walFile = File(dbFile.path + "-wal")
                                val shmFile = File(dbFile.path + "-shm")

                                // Add database files to ZIP
                                add(currentDbName, dbFile)
                                add("${currentDbName}-wal", walFile)
                                add("${currentDbName}-shm", shmFile)
                            }
                        }
                    }
                ).also(onResult).onSuccess(::registerSave)

                _importOrExportProgress.value = ImportOrExportProgress(
                    visible = false,
                    progress = 1f,
                    message = "数据库备份完成",
                    type = ProgressType.EXPORT
                )
                onExportSuccess()
            } catch (e: Exception) {
                _importOrExportProgress.value = ImportOrExportProgress(
                    visible = false,
                    progress = 0f,
                    message = "数据库备份失败: ${e.message}",
                    type = ProgressType.EXPORT
                )
                onExportFailure(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Restore Room databases from provided [fileUri].
     *
     * It closes the singleton Room instances, overwrites the db files, removes stale WAL/SHM files,
     * then cold-restarts the app process so DI/Room recreates everything from disk.
     */
    fun importDatabaseFromUri(
        context: Context,
        fileUri: Uri,
        onImportSuccess: () -> Unit = {},
        onImportFailure: (String) -> Unit = {}
    ) {
        componentScope.launch(ioDispatcher) {
            try {
                _importOrExportProgress.value = ImportOrExportProgress(
                    visible = true,
                    progress = 0f,
                    message = "开始恢复数据库...",
                    type = ProgressType.IMPORT
                )

                // 1) Close Room instances so db files are not held open
                AppDatabase.closeInstance()
                FeatureDatabase.closeInstance()

                _importOrExportProgress.value = _importOrExportProgress.value.copy(
                    progress = 0.3f,
                    message = "正在读取备份文件..."
                )

                // Database names to restore (按当前语言隔离)
                val dbNames = listOf(AppDatabase.dbNameForLocale(), FeatureDatabase.dbNameForLocale())

                // Prepare all database file paths
                val dbFiles = mutableMapOf<String, File>()
                val walFiles = mutableMapOf<String, File>()
                val shmFiles = mutableMapOf<String, File>()

                dbNames.forEach { currentDbName ->
                    val dbFile = context.getDatabasePath(currentDbName)
                    dbFile.parentFile?.mkdirs()

                    dbFiles[currentDbName] = dbFile
                    walFiles[currentDbName] = File(dbFile.path + "-wal")
                    shmFiles[currentDbName] = File(dbFile.path + "-shm")

                    // Clean old files first
                    runCatching { dbFile.delete() }
                    runCatching { walFiles[currentDbName]?.delete() }
                    runCatching { shmFiles[currentDbName]?.delete() }
                }

                _importOrExportProgress.value = _importOrExportProgress.value.copy(
                    progress = 0.5f,
                    message = "正在还原数据库文件..."
                )

                val input = context.contentResolver.openInputStream(fileUri)
                    ?: throw IllegalStateException("无法读取备份文件")

                val restoredDbs = mutableSetOf<String>()

                input.use { ins ->
                    ZipInputStream(ins).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            val entryName = entry.name

                            // Determine which file this entry corresponds to
                            val outFile = when {
                                entryName in dbFiles.keys -> {
                                    restoredDbs.add(entryName)
                                    dbFiles[entryName]
                                }
                                entryName.endsWith("-wal") -> {
                                    val dbKey = entryName.removeSuffix("-wal")
                                    walFiles[dbKey]
                                }
                                entryName.endsWith("-shm") -> {
                                    val dbKey = entryName.removeSuffix("-shm")
                                    shmFiles[dbKey]
                                }
                                else -> null
                            }

                            if (outFile != null) {
                                outFile.outputStream().use { zip.copyTo(it) }
                            }

                            zip.closeEntry()
                            entry = zip.nextEntry
                        }

                        // Verify at least the main database was restored
                        val mainDbName = AppDatabase.dbNameForLocale()
                        if (mainDbName !in restoredDbs || !dbFiles[mainDbName]!!.exists()) {
                            throw IllegalStateException("备份文件格式不正确或缺少主数据库")
                        }
                    }
                }

                _importOrExportProgress.value = ImportOrExportProgress(
                    visible = false,
                    progress = 1f,
                    message = "数据库恢复完成",
                    type = ProgressType.IMPORT
                )
                onImportSuccess()

                // Delay 10 seconds before cold restart so that DI/Room uses the restored files.
                kotlinx.coroutines.delay(3_000)
                restartToColdStart(context.applicationContext)
            } catch (e: Exception) {
                _importOrExportProgress.value = ImportOrExportProgress(
                    visible = false,
                    progress = 0f,
                    message = "数据库恢复失败: ${e.message}",
                    type = ProgressType.IMPORT
                )
                onImportFailure(e.message ?: "Unknown error")
            }
        }
    }

    private fun restartToColdStart(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return
        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        context.startActivity(launchIntent)
        // Ensure process is killed so Room reopens files from disk.
        exitProcess(0)
    }
}

// formatModerationMessage 把服务端 X-Moderation-Blocked-Reason header 值
// (百度返回的 conclusion: 不合规/疑似/审核失败) 翻成用户能看的中文消息.
//
// 配合 CommentsComponent 的 toast 提示, 解决"上传成功但图没了"的体验黑洞.
private fun formatModerationMessage(reason: String): String {
    val ctx = AppContext.getContext()
    return when (reason) {
        "不合规" -> ctx.getString(com.shifenmiao.core.R.string.comment_image_blocked)
        "疑似" -> ctx.getString(com.shifenmiao.core.R.string.comment_image_suspicious)
        "审核失败" -> ctx.getString(com.shifenmiao.core.R.string.comment_image_audit_failed)
        "audit_failed" -> ctx.getString(com.shifenmiao.core.R.string.comment_image_audit_failed)
        else -> ctx.getString(com.shifenmiao.core.R.string.comment_image_blocked_unknown, reason)
    }
}
