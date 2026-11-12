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
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal class HuaweiObsAdapter : ObjectStorageVendorAdapter {
    override val bucketInPath: Boolean = false
    private val signer = HuaweiObsSigner()

    override fun buildListBucketsRequest(connection: CloudStorageConnection.S3Compat): Request {
        val endpoint = connection.endpoint.removePrefix("https://").removePrefix("http://")
        val url = "https://$endpoint/".toHttpUrl()
        val headers = signer.signHeaders(connection, "GET", bucket = "", key = "")
        val builder = Request.Builder().url(url).header("Host", endpoint)
        headers.forEach { (name, value) -> builder.header(name, value) }
        return builder.get().build()
    }

    override fun buildListObjectsRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        prefix: String,
        continuationToken: String?,
        delimiter: String,
        maxKeys: Int,
    ): Request {
        val query = linkedMapOf(
            "prefix" to prefix,
            "delimiter" to delimiter,
            "max-keys" to maxKeys.toString(),
        ).apply {
            continuationToken?.let { put("marker", it) }
        }
        return signedRequest(connection, "GET", bucket = bucket, query = query)
    }

    override fun buildHeadObjectRequest(connection: CloudStorageConnection.S3Compat, bucket: String, key: String): Request =
        signedRequest(connection, "HEAD", bucket = bucket, key = key)

    override fun buildPutObjectRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
        contentType: String,
        body: ByteArray,
    ): Request = signedRequest(
        connection = connection,
        method = "PUT",
        bucket = bucket,
        key = key,
        headers = mapOf("Content-Type" to contentType),
        body = body,
    )

    override fun buildDeleteObjectRequest(connection: CloudStorageConnection.S3Compat, bucket: String, key: String): Request =
        signedRequest(connection, "DELETE", bucket = bucket, key = key)

    override fun buildCopyObjectRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        sourceKey: String,
        targetKey: String,
    ): Request = signedRequest(
        connection = connection,
        method = "PUT",
        bucket = bucket,
        key = targetKey,
        headers = mapOf("x-obs-copy-source" to "/$bucket/$sourceKey"),
    )

    override fun buildSignedGetUrl(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
        expiresInSeconds: Int,
    ): String {
        val target = createTarget(connection, bucket, key)
        val signed = signer.presign(connection, bucket, key, expiresInSeconds)
        val builder = target.url.newBuilder()
        signed.forEach(builder::addQueryParameter)
        return builder.build().toString()
    }

    override fun signedRequest(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        bucket: String,
        key: String,
        query: Map<String, String?>,
        headers: Map<String, String>,
        body: ByteArray?,
    ): Request {
        val target = createTarget(connection, bucket, key, query)
        val signedHeaders = signer.signHeaders(connection, method, bucket, key, headers)
        val builder = Request.Builder().url(target.url).header("Host", target.host)
        signedHeaders.forEach(builder::header)
        when (method.uppercase()) {
            "GET" -> builder.get()
            "HEAD" -> builder.head()
            "DELETE" -> builder.delete()
            "PUT" -> builder.put((body ?: ByteArray(0)).toRequestBody(headers["Content-Type"]?.toMediaTypeOrNull()))
        }
        return builder.build()
    }
}
