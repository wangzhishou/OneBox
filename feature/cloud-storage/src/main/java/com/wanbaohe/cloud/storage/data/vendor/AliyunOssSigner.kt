package com.wanbaohe.cloud.storage.data.vendor

import com.wanbaohe.cloud.storage.data.protocol.ObjectStorageRequestFactory
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import okhttp3.HttpUrl
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class AliyunOssSigner {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    private fun normalizeRegion(region: String): String {
        return region.trim()
            .removePrefix("oss-")
            .removeSuffix(".aliyuncs.com")
            .ifBlank { "cn-hangzhou" }
    }

    private fun getAliyunSigningPath(
        connection: CloudStorageConnection.S3Compat,
        target: ObjectStorageRequestFactory.RequestTarget,
    ): String {
        val bucket = target.virtualHostedBucket(connection)
        val path = target.pathForSigning.trim()
        if (bucket.isBlank()) return path.ifBlank { "/" }
        val normalizedPath = path.ifBlank { "/" }
        return "/$bucket${if (normalizedPath.startsWith("/")) normalizedPath else "/$normalizedPath"}"
    }

    fun signHeaders(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        target: ObjectStorageRequestFactory.RequestTarget,
        query: Map<String, String?>,
        headers: Map<String, String>,
        payloadHash: String = "UNSIGNED-PAYLOAD",
    ): Map<String, String> {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        val date = now.format(dateFormatter)
        val dateTime = now.format(timeFormatter)
        val normalizedRegion = normalizeRegion(connection.region)
        val scope = "$date/$normalizedRegion/oss/aliyun_v4_request"
        val ossHeaders = buildMap {
            put("host", target.host)
            put("x-oss-content-sha256", payloadHash)
            put("x-oss-date", dateTime)
            headers.entries
                .filter { (key, _) -> key.isOssCanonicalHeader() }
                .forEach { (key, value) -> put(key.lowercase(), value.trim()) }
        }
        val canonicalRequest = buildCanonicalRequest(
            method = method,
            signingPath = getAliyunSigningPath(connection, target),
            query = query,
            canonicalHeaders = canonicalHeaders(ossHeaders),
            payloadHash = payloadHash,
        )
        val stringToSign = buildString {
            appendLine("OSS4-HMAC-SHA256")
            appendLine(dateTime)
            appendLine(scope)
            append(SignatureUtils.sha256Hex(canonicalRequest))
        }
        val signature = SignatureUtils.hmacSha256Hex(
            key = signingKey(connection.secretAccessKey, date, normalizedRegion),
            value = stringToSign,
        )
        return buildMap {
            putAll(headers)
            put("x-oss-date", dateTime)
            put("x-oss-content-sha256", payloadHash)
            put(
                "Authorization",
                "OSS4-HMAC-SHA256 Credential=${connection.accessKeyId}/$scope," +
                    "AdditionalHeaders=host,Signature=$signature",
            )
        }
    }

    fun presign(
        connection: CloudStorageConnection.S3Compat,
        target: ObjectStorageRequestFactory.RequestTarget,
        expiresInSeconds: Int,
        now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    ): HttpUrl {
        val date = now.format(dateFormatter)
        val dateTime = now.format(timeFormatter)
        val normalizedRegion = normalizeRegion(connection.region)
        val scope = "$date/$normalizedRegion/oss/aliyun_v4_request"
        val query = linkedMapOf(
            "x-oss-signature-version" to "OSS4-HMAC-SHA256",
            "x-oss-credential" to "${connection.accessKeyId}/$scope",
            "x-oss-date" to dateTime,
            "x-oss-expires" to expiresInSeconds.toString(),
            "x-oss-additional-headers" to "host",
        )
        val aliyunPath = getAliyunSigningPath(connection, target)
        val canonicalRequest = buildCanonicalRequest(
            method = "GET",
            signingPath = aliyunPath,
            query = query,
            canonicalHeaders = "host:${target.host}\n",
            payloadHash = "UNSIGNED-PAYLOAD",
        )
        val stringToSign = buildString {
            appendLine("OSS4-HMAC-SHA256")
            appendLine(dateTime)
            appendLine(scope)
            append(SignatureUtils.sha256Hex(canonicalRequest))
        }
        val signature = SignatureUtils.hmacSha256Hex(
            key = signingKey(connection.secretAccessKey, date, normalizedRegion),
            value = stringToSign,
        )
        return target.url.newBuilder()
            .addQueryParameter("x-oss-signature-version", "OSS4-HMAC-SHA256")
            .addQueryParameter("x-oss-credential", "${connection.accessKeyId}/$scope")
            .addQueryParameter("x-oss-date", dateTime)
            .addQueryParameter("x-oss-expires", expiresInSeconds.toString())
            .addQueryParameter("x-oss-additional-headers", "host")
            .addQueryParameter("x-oss-signature", signature)
            .build()
    }

    private fun buildCanonicalRequest(
        method: String,
        signingPath: String,
        query: Map<String, String?>,
        canonicalHeaders: String,
        payloadHash: String,
    ): String = buildString {
        appendLine(method.uppercase())
        appendLine(SignatureUtils.awsUriEncode(signingPath, encodeSlash = false))
        appendLine(canonicalQuery(query))
        append(canonicalHeaders)
        appendLine()
        appendLine("host")
        append(payloadHash)
    }

    private fun canonicalHeaders(headers: Map<String, String>): String =
        headers.toSortedMap().entries.joinToString(separator = "") {
            "${it.key}:${it.value.trim()}\n"
        }

    private fun canonicalQuery(query: Map<String, String?>): String = query.entries
        .sortedWith(compareBy({ it.key }, { it.value.orEmpty() }))
        .joinToString("&") { (key, value) ->
            val encodedKey = SignatureUtils.awsUriEncode(key, encodeSlash = true)
            if (value.isNullOrEmpty()) {
                encodedKey
            } else {
                "$encodedKey=${SignatureUtils.awsUriEncode(value, encodeSlash = true)}"
            }
        }

    private fun String.isOssCanonicalHeader(): Boolean {
        return equals("content-md5", ignoreCase = true) ||
            equals("content-type", ignoreCase = true) ||
            startsWith("x-oss-", ignoreCase = true)
    }

    private fun signingKey(secret: String, date: String, region: String): ByteArray {
        val kDate = SignatureUtils.hmacSha256("aliyun_v4$secret".toByteArray(), date)
        val kRegion = SignatureUtils.hmacSha256(kDate, region)
        val kService = SignatureUtils.hmacSha256(kRegion, "oss")
        return SignatureUtils.hmacSha256(kService, "aliyun_v4_request")
    }

    private fun ObjectStorageRequestFactory.RequestTarget.virtualHostedBucket(connection: CloudStorageConnection.S3Compat): String {
        val endpointHost = connection.endpoint
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore('/')
        return host
            .takeIf { it.endsWith(".$endpointHost") }
            ?.removeSuffix(".$endpointHost")
            .orEmpty()
    }
}
