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

package com.wanbaohe.file.browser.model

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * 文件浏览器纯配置项。
 *
 * 注意：文件打开逻辑已迁移到 [com.shifenmiao.common.content.ContentRouter]。
 * 本对象仅保留运行时可选覆盖的配置常量，不再包含类型判断或打开逻辑。
 */
object FileBrowserConfig {

    /**
     * Workspace root Uri provider.
     *
     * - Default: Documents/OneBox (created if missing)
     * - You can override it from host app at runtime, e.g.
     *   FileBrowserConfig.workspaceRootUriProvider = { ctx -> yourUri }
     *
     * Note: In later steps we can make it user-configurable via SAF + persisted permission.
     */
    var workspaceRootUriProvider: (Context) -> Uri? = { _ ->
        // Prefer a dedicated workspace folder under Documents.
        // Keep it simple: file:// based; if the app doesn't have permission, UI will show NoPermission.
        runCatching {
            @Suppress("DEPRECATION")
            val documentsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOCUMENTS
            )
            val workspaceDir = File(documentsDir, "OneBox")
            if (!workspaceDir.exists()) {
                workspaceDir.mkdirs()
            }
            if (workspaceDir.exists() && workspaceDir.isDirectory) {
                Uri.fromFile(workspaceDir)
            } else {
                null
            }
        }.getOrNull() ?: runCatching {
            // Fallback: public Downloads
            @Suppress("DEPRECATION")
            Uri.fromFile(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
            )
        }.getOrNull()
    }
}
