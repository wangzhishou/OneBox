/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

@file:SuppressLint("StringFormatMatches")

package com.t8rin.imagetoolbox.core.ui.utils.helper

import android.annotation.SuppressLint
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.shifenmiao.interfaces.logging.ImageSaveLogger
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.utils.ListUtils.firstOfType
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Info
import com.t8rin.imagetoolbox.core.resources.icons.Save
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.utils.getString
import com.t8rin.logger.makeLog
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ImageSaveLoggerEntryPoint {
    fun imageSaveLogger(): ImageSaveLogger
}

interface SaveResultHandler {
    fun parseSaveResult(saveResult: SaveResult)

    fun parseFileSaveResult(saveResult: SaveResult)

    fun parseSaveResults(results: List<SaveResult>)

    /**
     * 把保存成功消息里的文件名/保存路径替换为可点击链接(dir:// 与文件 URI),
     * 供手动记录 ActivityLog 的场景复用(不经过 [parseSaveResult] 时)。
     */
    fun linkifiedSaveMessage(saveResult: SaveResult.Success): String?
}

internal object SaveResultHandlerImpl : SaveResultHandler {
    override fun parseSaveResult(saveResult: SaveResult) {
        when (saveResult) {
            is SaveResult.Error.Exception -> {
                saveResult.throwable.makeLog("parseSaveResult")
                AppToastHost.showFailureToast(
                    throwable = saveResult.throwable
                )
            }

            is SaveResult.Skipped -> {
                AppToastHost.showToast(
                    message = getString(R.string.skipped_saving),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                    duration = ToastDuration.Short
                )
            }

            is SaveResult.Success -> {
                updateSaveResult(saveResult)
                AppToastHost.showFileSuccessToast(
                    uri = saveResult.fileUri?.takeIf(String::isNotBlank)?.toUri(),
                    message = saveSuccessMessage(saveResult)
                )
                AppToastHost.showConfetti()
            }

            SaveResult.Error.MissingPermissions -> AppToastHost.showToast(AppToastHost.PERMISSION)
        }
    }

    override fun parseFileSaveResult(saveResult: SaveResult) {
        when (saveResult) {
            is SaveResult.Error.Exception -> {
                AppToastHost.showFailureToast(
                    throwable = saveResult.throwable
                )
            }

            is SaveResult.Skipped -> {
                AppToastHost.showToast(
                    message = getString(R.string.skipped_saving),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo
                )
            }

            is SaveResult.Success -> {
                updateSaveResult(saveResult)
                AppToastHost.showFileSuccessToast(
                    uri = saveResult.fileUri?.takeIf(String::isNotBlank)?.toUri(),
                    message = saveSuccessMessage(saveResult)
                )
                AppToastHost.showConfetti()
            }

            SaveResult.Error.MissingPermissions -> AppToastHost.showToast(AppToastHost.PERMISSION)
        }
    }

    override fun parseSaveResults(results: List<SaveResult>) {
        if (results.size == 1) {
            return parseSaveResult(
                saveResult = results.first()
            )
        }

        if (results.any { it == SaveResult.Error.MissingPermissions }) {
            AppToastHost.showToast(AppToastHost.PERMISSION)
            return
        }

        val skipped = results.count { it is SaveResult.Skipped }
        val failed = results.count { it is SaveResult.Error }
        val done = results.count { it is SaveResult.Success }

        if (failed == 0 && done > 0) {
            if (done == 1) {
                val saveResult = results.firstOfType<SaveResult.Success>()
                AppToastHost.showFileSuccessToast(
                    uri = saveResult?.fileUri?.takeIf(String::isNotBlank)?.toUri(),
                    message = saveResult?.let { saveSuccessMessage(it) }
                        ?: getString(R.string.saved_to_without_filename, getString(R.string.default_folder))
                )
            } else {
                val saveResult = results.firstOfType<SaveResult.Success>()

                if (saveResult?.isOverwritten == true) {
                    AppToastHost.showToast(
                        message = getString(R.string.images_overwritten),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                        duration = ToastDuration.Long
                    )
                } else {
                    AppToastHost.showToast(
                        message = saveResult?.let { saveSuccessMessage(it) }
                            ?: getString(
                                R.string.saved_to_without_filename,
                                getString(R.string.default_folder)
                            ),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                        duration = ToastDuration.Long
                    )
                }
            }

            if (skipped > 0) {
                AppToastHost.showToast(
                    message = getString(R.string.skipped_saving_multiple, skipped),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                    duration = ToastDuration.Short
                )
            }

            AppToastHost.showConfetti()
            return
        }

        if (failed > 0) {
            val saveResult = results.firstOfType<SaveResult.Success>()
            val errorSaveResult = results.firstOfType<SaveResult.Error>()

            if (done > 0) {
                AppToastHost.showToast(
                    message = saveResult?.let { saveSuccessMessage(it) }
                        ?: getString(
                            R.string.saved_to_without_filename,
                            getString(R.string.default_folder)
                        ),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    duration = ToastDuration.Long
                )
            }
            AppToastHost.showFailureToast(getString(R.string.failed_to_save, failed))
            AppToastHost.showToast(
                message = getString(
                    R.string.smth_went_wrong,
                    errorSaveResult?.throwable?.localizedMessage ?: ""
                )
            )

            if (skipped > 0) {
                AppToastHost.showToast(
                    message = getString(R.string.skipped_saving_multiple, skipped),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                    duration = ToastDuration.Short
                )
            }
            return
        }

        if (skipped > 0 && done == 0 && failed == 0) {
            AppToastHost.showToast(
                message = getString(R.string.skipped_saving_multiple, skipped),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                duration = ToastDuration.Short
            )
        }

        // 批量结果只记一条汇总日志
        recordBatchSummary(results, done, failed, skipped)
    }

    private fun recordBatchSummary(
        results: List<SaveResult>,
        done: Int,
        failed: Int,
        skipped: Int
    ) {
        val recorder = runCatching {
            EntryPointAccessors.fromApplication(
                AppContext.getContext(),
                ImageSaveLoggerEntryPoint::class.java
            ).imageSaveLogger()
        }.getOrNull() ?: return

        val savingPath = results.firstOfType<SaveResult.Success>()?.savingPath
            ?: getString(R.string.default_folder)
        val description = buildString {
            append(getString(R.string.batch_save_summary, done, failed, skipped))
            append(" · ")
            append(savingPath)
        }

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                recorder.recordImageSave(
                    screenId = AppContext.getCurrentScreen(),
                    screenName = AppContext.getCurrentScreenName(),
                    description = description
                )
            }
        }
    }

    fun saveSuccessMessage(saveResult: SaveResult.Success): String {
        val folder = folderLabel(saveResult.savingPath)
        val fileName = saveResult.fileName?.takeIf { it.isNotBlank() }
        return if (fileName != null) {
            getString(R.string.saved_to, folder, fileName)
        } else {
            getString(R.string.saved_to_without_filename, folder)
        }
    }

    /**
     * 提示里只留目录最后一段。完整路径对用户没有用，空路径退回默认目录名。
     */
    private fun folderLabel(savingPath: String?): String {
        val raw = savingPath?.trim()?.trimEnd('/')?.trimEnd('\\').orEmpty()
        if (raw.isEmpty()) return getString(R.string.default_folder)
        val leaf = raw.substringAfterLast('/').substringAfterLast('\\').substringAfterLast(':').trim()
        return leaf.ifBlank { getString(R.string.default_folder) }
    }

    override fun linkifiedSaveMessage(
        saveResult: SaveResult.Success
    ): String? {
        val message = saveResult.message
        if (message.isNullOrEmpty()) return message
        return runCatching {
            val fileName = saveResult.fileName
            val savingPath = saveResult.savingPath
            val fileUri = saveResult.fileUri

            val map = buildMap<String?, String?> {
                // folderUri: 优先 treeUri（SAF 授权的目录 URI），否则从 fileUri 反推父目录 URI
                val folderUri = saveResult.treeUri
                    ?: fileUri?.let { deriveFolderUri(it) }
                // 先替换 savingPath（较长字符串），再替换 fileName，避免包含关系导致匹配失败
                if (!savingPath.isNullOrEmpty() && !folderUri.isNullOrEmpty() && message.contains(savingPath)) {
                    put(savingPath, "dir://$folderUri")
                }
                if (!fileName.isNullOrEmpty() && !fileUri.isNullOrEmpty() && message.contains(fileName)) {
                    put(fileName, fileUri)
                }
            }
            if (map.isNotEmpty()) {
                LinkUtils.replaceWithLinks(message, map)
            } else {
                message
            }
        }.getOrDefault(message)
    }

    internal fun updateSaveResult(
        saveResult: SaveResult.Success?
    ) {
        val replaceMessage = saveResult?.let { linkifiedSaveMessage(it) }
        if (!replaceMessage.isNullOrEmpty() && saveResult != null) {
            val recorder = runCatching {
                EntryPointAccessors.fromApplication(
                    AppContext.getContext(),
                    ImageSaveLoggerEntryPoint::class.java
                ).imageSaveLogger()
            }.getOrNull()

            recorder?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    it.recordImageSave(
                        screenId = AppContext.getCurrentScreen(),
                        screenName = AppContext.getCurrentScreenName(),
                        description = replaceMessage,
                        fileUri = saveResult.fileUri.orEmpty(),
                        fileName = saveResult.fileName.orEmpty(),
                        savePath = saveResult.savingPath.orEmpty()
                    )
                }
            }
        }
    }
}

/**
 * 非 BaseComponent 场景(Service、Dialog、Screen 回调里拿不到组件时)的
 * 标准保存成功提示入口,行为与 [SaveResultHandler.parseSaveResult] 一致:
 * 带「打开」按钮的 toast + 撒花 + 写 ActivityLog。
 */
object SaveSuccessNotifier {

    /** 已有 [SaveResult] 时直接使用(如 FileController.save 的返回值)。 */
    fun notify(saveResult: SaveResult) = SaveResultHandlerImpl.parseSaveResult(saveResult)

    /** 批量保存结果(只写一条汇总 ActivityLog)。 */
    fun notifyAll(results: List<SaveResult>) = SaveResultHandlerImpl.parseSaveResults(results)

    /**
     * 只有文件信息时拼装标准消息(「已使用名称 X 保存到 Y 文件夹」)后走标准管线。
     */
    fun notifyFileSaved(
        fileUri: String,
        fileName: String = "",
        savingPath: String = ""
    ) {
        val folder = savingPath.ifBlank { getString(R.string.default_folder) }
        val message = if (fileName.isNotBlank()) {
            getString(R.string.saved_to, folder, fileName)
        } else {
            getString(R.string.saved_to_without_filename, folder)
        }
        SaveResultHandlerImpl.parseSaveResult(
            SaveResult.Success(
                message = message,
                savingPath = savingPath.ifBlank { null },
                fileUri = fileUri,
                fileName = fileName.ifBlank { null }
            )
        )
    }

    /**
     * 标准 toast 文案(目录只留最后一段),供需要自行写 ActivityLog 的场景复用。
     */
    fun successMessage(fileName: String, savingPath: String): String =
        SaveResultHandlerImpl.saveSuccessMessage(
            SaveResult.Success(savingPath = savingPath, fileName = fileName)
        )

    /**
     * 只写 ActivityLog、不弹 toast 不撒花:AI Agent 工具等结果走对话流、
     * 不打扰用户的场景。描述消息同样会做文件名/目录的链接化处理。
     */
    fun logOnly(saveResult: SaveResult.Success) =
        SaveResultHandlerImpl.updateSaveResult(saveResult)
}

/**
 * 从文件 URI 推导其父目录的 file:// URI。
 *
 * 1. DocumentsProvider → 从 document ID 截取父目录
 * 2. MediaStore → 查询 DATA 列获取真实路径
 *
 * @return 父目录的 file:// URI 字符串，推导失败返回 null
 */
private fun deriveFolderUri(fileUri: String): String? {
    return runCatching {
        val uri = fileUri.toUri()
        when {
            // DocumentsProvider — 从 document ID 截取父目录
            uri.authority?.contains("externalstorage.documents") == true -> {
                val documentId = DocumentsContract.getDocumentId(uri)
                val parentId = documentId.substringBeforeLast('/', documentId)
                val split = parentId.split(":")
                if (split.size >= 2 && split[0].equals("primary", ignoreCase = true)) {
                    val dir = java.io.File(
                        android.os.Environment.getExternalStorageDirectory(),
                        split[1]
                    )
                    if (dir.exists()) android.net.Uri.fromFile(dir).toString() else null
                } else null
            }
            // MediaStore — 查询 DATA 列获取真实路径
            uri.authority?.startsWith("media") == true -> {
                val context = AppContext.getContext()
                @Suppress("DEPRECATION")
                context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.MediaStore.MediaColumns.DATA),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val filePath = cursor.getString(0)
                        if (filePath != null) {
                            val parent = java.io.File(filePath).parentFile
                            if (parent != null) android.net.Uri.fromFile(parent).toString() else null
                        } else null
                    } else null
                }
            }
            else -> null
        }
    }.getOrNull()
}