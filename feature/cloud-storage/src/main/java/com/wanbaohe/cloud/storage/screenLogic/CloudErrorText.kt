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
