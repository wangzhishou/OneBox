package com.wanbaohe.cloud.storage.data.vendor

import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import java.time.Instant

internal class HuaweiObsSigner {

    fun signHeaders(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        bucket: String,
        key: String,
        headers: Map<String, String> = emptyMap(),
    ): Map<String, String> {
        val date = java.util.Date().toString()
        val stringToSign = buildString {
            append(method.uppercase())
            append('\n')
            append(headers["Content-MD5"].orEmpty())
            append('\n')
            append(headers["Content-Type"].orEmpty())
            append('\n')
            append(date)
            append('\n')
            canonicalizedObsHeaders(headers).takeIf { it.isNotBlank() }?.let(::append)
            append(canonicalizedResource(bucket, key))
        }
        val signature = SignatureUtils.hmacSha1Base64(connection.secretAccessKey, stringToSign)
        return headers + mapOf(
            "Date" to date,
            "Authorization" to "OBS ${connection.accessKeyId}:$signature",
        )
    }

    fun presign(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
        expiresInSeconds: Int,
    ): Map<String, String> {
        val expires = Instant.now().epochSecond + expiresInSeconds
        val resource = canonicalizedResource(bucket, key)
        val signature = SignatureUtils.hmacSha1Base64(
            connection.secretAccessKey,
            "GET\n\n\n$expires\n$resource",
        )
        return mapOf(
            "AccessKeyId" to connection.accessKeyId,
            "Expires" to expires.toString(),
            "Signature" to signature,
        )
    }

    private fun canonicalizedObsHeaders(headers: Map<String, String>): String =
        headers.entries
            .filter { it.key.startsWith("x-obs-", ignoreCase = true) }
            .sortedBy { it.key.lowercase() }
            .joinToString(separator = "") { "${it.key.lowercase()}:${it.value.trim()}\n" }

    private fun canonicalizedResource(bucket: String, key: String): String =
        "/$bucket/${key.trimStart('/')}".replace("//", "/")
}
