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
    }
}

data class AppWorkspaceRoot(
    val name: String,
    val file: File,
    val description: String,
)
