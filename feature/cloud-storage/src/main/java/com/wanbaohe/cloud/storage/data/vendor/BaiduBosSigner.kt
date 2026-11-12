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

package com.wanbaohe.cloud.storage.data.vendor

import com.wanbaohe.cloud.storage.data.protocol.ObjectStorageRequestFactory
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import okhttp3.HttpUrl
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class BaiduBosSigner {

    companion object {
        const val EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }

    private val algorithm = "AWS4-HMAC-SHA256"
    private val service = "s3"
    private val terminator = "aws4_request"
    private val keyPrefix = "AWS4"

    private val amzFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun signHeaders(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        target: ObjectStorageRequestFactory.RequestTarget,
        query: Map<String, String?>,
        headers: Map<String, String>,
        payloadHash: String = EMPTY_SHA256,
        now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    ): Map<String, String> {
        val dateTime = now.format(amzFormatter)
        val shortDate = now.format(dateFormatter)
        val headersToSign = linkedMapOf(
            "host" to target.host,
            "x-amz-date" to dateTime,
            "x-amz-content-sha256" to payloadHash,
        )
        headers.entries.forEach { (key, value) -> headersToSign[key.lowercase()] = value.trim() }
        val canonicalHeaders = headersToSign.toSortedMap().entries.joinToString(separator = "") {
            "${it.key}:${it.value.trim()}\n"
        }
        val signedHeaders = headersToSign.keys.sorted().joinToString(separator = ";")
        val canonicalQuery = canonicalQuery(query)
        val canonicalRequest = buildString {
            appendLine(method.uppercase())
            appendLine(SignatureUtils.awsUriEncode(target.pathForSigning, encodeSlash = false))
            appendLine(canonicalQuery)
            append(canonicalHeaders)
            appendLine()
            appendLine(signedHeaders)
            append(payloadHash)
        }
        val scope = "$shortDate/${connection.region}/$service/$terminator"
        val stringToSign = buildString {
            appendLine(algorithm)
            appendLine(dateTime)
            appendLine(scope)
            append(SignatureUtils.sha256Hex(canonicalRequest))
        }
        val signature = SignatureUtils.hmacSha256Hex(
            key = signingKey(connection.secretAccessKey, shortDate, connection.region),
            value = stringToSign,
        )
        return buildMap {
            putAll(headers)
            put("x-amz-date", dateTime)
            put("x-amz-content-sha256", payloadHash)
            put(
                "Authorization",
                "$algorithm Credential=${connection.accessKeyId}/$scope, SignedHeaders=$signedHeaders, Signature=$signature",
            )
        }
    }

    fun presign(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        target: ObjectStorageRequestFactory.RequestTarget,
        expiresInSeconds: Int,
        now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    ): HttpUrl {
        val dateTime = now.format(amzFormatter)
        val shortDate = now.format(dateFormatter)
        val scope = "$shortDate/${connection.region}/$service/$terminator"
        val query = linkedMapOf(
            "X-Amz-Algorithm" to algorithm,
            "X-Amz-Credential" to "${connection.accessKeyId}/$scope",
            "X-Amz-Date" to dateTime,
            "X-Amz-Expires" to expiresInSeconds.toString(),
            "X-Amz-SignedHeaders" to "host",
        )
        val canonicalRequest = buildString {
            appendLine(method.uppercase())
            appendLine(SignatureUtils.awsUriEncode(target.pathForSigning, encodeSlash = false))
            appendLine(canonicalQuery(query))
            appendLine("host:${target.host}")
            appendLine()
            appendLine("host")
            append("UNSIGNED-PAYLOAD")
        }
        val stringToSign = buildString {
            appendLine(algorithm)
            appendLine(dateTime)
            appendLine(scope)
            append(SignatureUtils.sha256Hex(canonicalRequest))
        }
        val signature = SignatureUtils.hmacSha256Hex(
            key = signingKey(connection.secretAccessKey, shortDate, connection.region),
            value = stringToSign,
        )
        return target.url.newBuilder()
            .addQueryParameter("X-Amz-Algorithm", algorithm)
            .addQueryParameter("X-Amz-Credential", "${connection.accessKeyId}/$scope")
            .addQueryParameter("X-Amz-Date", dateTime)
            .addQueryParameter("X-Amz-Expires", expiresInSeconds.toString())
            .addQueryParameter("X-Amz-SignedHeaders", "host")
            .addQueryParameter("X-Amz-Signature", signature)
            .build()
    }

    private fun signingKey(secret: String, date: String, region: String): ByteArray {
        val kDate = SignatureUtils.hmacSha256("$keyPrefix$secret".toByteArray(), date)
        val kRegion = SignatureUtils.hmacSha256(kDate, region)
        val kService = SignatureUtils.hmacSha256(kRegion, service)
        return SignatureUtils.hmacSha256(kService, terminator)
    }

    private fun canonicalQuery(query: Map<String, String?>): String = query.entries
        .sortedWith(compareBy({ it.key }, { it.value ?: "" }))
        .joinToString("&") { (key, value) ->
            "${SignatureUtils.awsUriEncode(key, encodeSlash = true)}=${SignatureUtils.awsUriEncode(value.orEmpty(), encodeSlash = true)}"
        }
}
