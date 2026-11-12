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

package com.t8rin.imagetoolbox.feature.zip.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 解压目标目录的"上次选择"。
 *
 * 系统目录选择器每次都从根目录打开，而根目录和 Download 根目录都不可写
 * （SAF 会提示 "Can't use this folder"），用户得一层层点回去。记住上次选过的
 * 目录，下次直接从那里开始。
 */
@Singleton
class ZipExtractPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun lastDestination(): Uri? = prefs.getString(KEY_LAST_DESTINATION, null)
        ?.let { runCatching { Uri.parse(it) }.getOrNull() }
        ?.takeIf { it.scheme == "content" }

    fun saveLastDestination(uri: Uri) {
        prefs.edit().putString(KEY_LAST_DESTINATION, uri.toString()).apply()
    }

    private companion object {
        const val PREFS_NAME = "zip_extract"
        const val KEY_LAST_DESTINATION = "last_destination"
    }
}

/**
 * 目录选择器的默认定位。
 *
 * 顺序：上次选过的目录 → App 工作目录（用户授权过的 SAF 目录，通常可写）
 * → Download（只能选它下面的子目录，但至少不是存储根目录）。
 */
internal fun resolveExtractStartUri(
    lastDestination: Uri?,
    workspaceUri: Uri?,
): Uri? {
    lastDestination?.takeIf { it.scheme == "content" }?.let { return it }
    workspaceUri?.takeIf { it.scheme == "content" }?.let { return it }
    return downloadDocumentUri()
}

/**
 * `content://com.android.externalstorage.documents/document/primary:Download`
 *
 * 用外部存储提供者的公开 authority + document id，不依赖任何私有路径；
 * 选择器只接受 document/tree 两种形态的 content URI，所以不能传 file://。
 */
private fun downloadDocumentUri(): Uri? = runCatching {
    DocumentsContract.buildDocumentUri(
        EXTERNAL_STORAGE_AUTHORITY,
        "primary:${Environment.DIRECTORY_DOWNLOADS}",
    )
}.getOrNull()

private const val EXTERNAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents"
