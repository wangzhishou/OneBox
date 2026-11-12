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
