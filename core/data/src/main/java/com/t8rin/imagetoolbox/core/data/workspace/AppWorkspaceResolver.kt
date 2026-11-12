/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.core.data.workspace

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.settings.domain.SettingsProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 解析 App 工作目录:用户配置的保存文件夹优先,缺省为 Documents/OneBox。
 * 文件管理器、Agent 文件工具、AI 生成图片等需要"用户可见"的产物统一落在这里。
 */
@Singleton
class AppWorkspaceResolver @Inject constructor(
    private val settingsProvider: SettingsProvider,
) {

    fun resolve(): AppWorkspaceRoot {
        val configuredRoot = resolveConfiguredWorkspaceRootFile()
        return if (configuredRoot != null) {
            AppWorkspaceRoot(
                name = APP_WORKSPACE_NAME,
                file = configuredRoot,
                description = "Configured app save folder",
            )
        } else {
            AppWorkspaceRoot(
                name = APP_WORKSPACE_NAME,
                file = defaultWorkspaceRootFile(),
                description = "Default Documents/OneBox workspace",
            )
        }
    }

    fun resolveSearchTarget(): AppWorkspaceSearchTarget {
        val configuredUri = settingsProvider.settingsState.value.saveFolderUri
            ?.takeIf { it.isNotBlank() }
        if (configuredUri == null) {
            return defaultWorkspaceRootFile().toLocalSearchTarget()
        }

        return runCatching {
            val parsed = configuredUri.toInputUri()
            when (parsed.scheme) {
                null, "", "file" -> parsed.path
                    ?.let(::File)
                    ?.canonicalFile
                    ?.toLocalSearchTarget()
                    ?: AppWorkspaceSearchTarget.Unavailable

                "content" -> resolveConfiguredContentSearchTarget(parsed)
                else -> AppWorkspaceSearchTarget.Unavailable
            }
        }.getOrDefault(AppWorkspaceSearchTarget.Unavailable)
    }

    private fun resolveConfiguredContentSearchTarget(uri: Uri): AppWorkspaceSearchTarget {
        if (DocumentsContract.isTreeUri(uri)) {
            val documentId = DocumentsContract.getTreeDocumentId(uri)
            if (documentId.isBlank() || isForbiddenExternalStorageDocument(uri, documentId)) {
                return AppWorkspaceSearchTarget.Unavailable
            }
            return AppWorkspaceSearchTarget.Saf(treeUri = uri)
        }

        return SafUriUtils.documentUriToFileUri(uri)
            ?.path
            ?.let(::File)
            ?.canonicalFile
            ?.toLocalSearchTarget()
            ?: AppWorkspaceSearchTarget.Unavailable
    }

    private fun File.toLocalSearchTarget(): AppWorkspaceSearchTarget {
        return if (isForbiddenLocalSearchRoot()) {
            AppWorkspaceSearchTarget.Unavailable
        } else {
            AppWorkspaceSearchTarget.Local(root = this)
        }
    }

    @Suppress("DEPRECATION")
    private fun File.isForbiddenLocalSearchRoot(): Boolean {
        val rootPath = canonicalPath
        if (rootPath == File.separator) return true

        val externalStorageRoot = Environment.getExternalStorageDirectory().canonicalFile
        if (rootPath == externalStorageRoot.path || externalStorageRoot.path.startsWith(rootPath + File.separator)) {
            return true
        }

        val downloadRoot = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS,
        ).canonicalFile
        if (rootPath == downloadRoot.path) return true

        val parentPath = parentFile?.canonicalPath
        return parentPath == "/storage" || parentPath == "/mnt/media_rw"
    }

    private fun isForbiddenExternalStorageDocument(uri: Uri, documentId: String): Boolean {
        if (uri.authority != EXTERNAL_STORAGE_AUTHORITY) return false
        val relativePath = documentId.substringAfter(':', missingDelimiterValue = "")
        return relativePath.isBlank() || relativePath.trim('/').equals("Download", ignoreCase = true)
    }

    private fun resolveConfiguredWorkspaceRootFile(): File? {
        val rawUri = settingsProvider.settingsState.value.saveFolderUri ?: return null
        return runCatching {
            val parsed = rawUri.toInputUri()
            val localUri = when (parsed.scheme) {
                null, "", "file" -> parsed
                "content" -> SafUriUtils.treeUriToFileUri(parsed) ?: SafUriUtils.documentUriToFileUri(parsed)
                else -> null
            } ?: return null
            val path = localUri.path ?: return null
            File(path).canonicalFile
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun defaultWorkspaceRootFile(): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            DEFAULT_WORKSPACE_FOLDER_NAME,
        ).canonicalFile
    }

    private fun String.toInputUri() = toUri().let { uri ->
        if (uri.scheme.isNullOrBlank()) Uri.fromFile(File(this)) else uri
    }

    companion object {
        private const val APP_WORKSPACE_NAME = "app_workspace"
        private const val DEFAULT_WORKSPACE_FOLDER_NAME = "OneBox"
        private const val EXTERNAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents"
    }
}

data class AppWorkspaceRoot(
    val name: String,
    val file: File,
    val description: String,
)

sealed interface AppWorkspaceSearchTarget {
    data class Local(val root: File) : AppWorkspaceSearchTarget

    data class Saf(val treeUri: Uri) : AppWorkspaceSearchTarget

    data object Unavailable : AppWorkspaceSearchTarget
}
