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

package com.wanbaohe.cloud.storage.screenLogic

import android.content.Context
import com.wanbaohe.cloud.storage.R

/**
 * 把 OkHttp / smbj / 各云厂商的原始异常转成用户能看懂的一句话。
 *
 * 原始信息仍然保留在 [Throwable] 里，日志/调试不受影响。
 */
internal fun Throwable.toCloudErrorMessage(context: Context): String {
    val raw = message.orEmpty()
    val lower = raw.lowercase()
    return when {
        raw.isBlank() -> context.getString(R.string.cloud_storage_error_generic)
        // SMB 适配器已经把 NT_STATUS 翻译成本地化文案，原样透出
        lower.startsWith("smb") -> raw
        lower.contains("cleartext") ->
            context.getString(R.string.cloud_storage_error_cleartext)
        lower.contains("401") ||
            lower.contains("unauthorized") ||
            lower.contains("authentication") ->
            context.getString(R.string.cloud_storage_error_auth)
        lower.contains("403") ||
            lower.contains("forbidden") ||
            lower.contains("access denied") ->
            context.getString(R.string.cloud_storage_error_permission)
        lower.contains("404") ||
            lower.contains("not found") ||
            lower.contains("no such") ->
            context.getString(R.string.cloud_storage_error_not_found)
        lower.contains("failed to connect") ||
            lower.contains("timeout") ||
            lower.contains("timed out") ||
            lower.contains("unknownhost") ||
            lower.contains("unable to resolve host") ||
            lower.contains("connectexception") ||
            lower.contains("econnrefused") ->
            context.getString(R.string.cloud_storage_error_network)
        else -> context.getString(R.string.cloud_storage_error_server, raw.take(160))
    }
}
