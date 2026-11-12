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
import java.net.URLDecoder
import java.time.Instant

internal class TencentCosSigner {

    fun signHeaders(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        target: ObjectStorageRequestFactory.RequestTarget,
        query: Map<String, String?>,
        headers: Map<String, String>,
        expiresInSeconds: Int = 3600,
    ): Map<String, String> {
        val now = Instant.now().epochSecond
        val auth = buildAuthorization(
            connection = connection,
            method = method,
            target = target,
            query = query,
            headers = headers + mapOf("Host" to target.host),
            startSeconds = now,
            endSeconds = now + expiresInSeconds,
        )
        return headers + mapOf(
            "Date" to java.util.Date(now * 1000).toString(),
            "Authorization" to auth,
        )
    }

    fun presign(
        connection: CloudStorageConnection.S3Compat,
        target: ObjectStorageRequestFactory.RequestTarget,
        expiresInSeconds: Int,
    ): HttpUrl {
        val now = Instant.now().epochSecond
        val auth = buildAuthorization(
            connection = connection,
            method = "GET",
            target = target,
            query = target.url.queryParameterNames.associateWith(target.url::queryParameter),
            headers = mapOf("Host" to target.host),
            startSeconds = now,
            endSeconds = now + expiresInSeconds,
        )
        return target.url.newBuilder()
            .encodedQuery(auth)
            .build()
    }

    private fun buildAuthorization(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        target: ObjectStorageRequestFactory.RequestTarget,
        query: Map<String, String?>,
        headers: Map<String, String>,
        startSeconds: Long,
        endSeconds: Long,
    ): String {
        val keyTime = "$startSeconds;$endSeconds"
        val signKey = SignatureUtils.hmacSha1Hex(
            key = connection.secretAccessKey.toByteArray(),
            value = keyTime,
        )
        val normalizedHeaders = headers.entries
            .associate { it.key.lowercase() to SignatureUtils.standardQueryEncode(it.value.trim().lowercase()) }
            .toSortedMap()
        val normalizedParams = query.entries
            .associate { it.key.lowercase() to SignatureUtils.standardQueryEncode(it.value.orEmpty().trim().lowercase()) }
            .toSortedMap()
        val headerList = normalizedHeaders.keys.joinToString(";")
        val paramList = normalizedParams.keys.joinToString(";")
        val httpString = buildString {
            appendLine(method.lowercase())
            appendLine(URLDecoder.decode(target.url.encodedPath, "UTF-8"))
            appendLine(normalizedParams.entries.joinToString("&") { "${it.key}=${it.value}" })
            append(normalizedHeaders.entries.joinToString("&") { "${it.key}=${it.value}" })
            append('\n')
        }
        val stringToSign = buildString {
            appendLine("sha1")
            appendLine(keyTime)
            appendLine(SignatureUtils.sha1Hex(httpString))
            append('\n')
        }
        val signature = SignatureUtils.hmacSha1Hex(
            key = signKey.toByteArray(),
            value = stringToSign,
        )
        return "q-sign-algorithm=sha1&q-ak=${connection.accessKeyId}" +
            "&q-sign-time=$keyTime&q-key-time=$keyTime" +
            "&q-header-list=$headerList&q-url-param-list=$paramList&q-signature=$signature"
    }
}
