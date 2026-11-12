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
import okhttp3.Request

/**
 * S3 协议族的 vendor 适配器 —— 仅对 [CloudStorageConnection.S3Compat] 生效。
 *
 * 实现负责生成 AWS SigV4 / 类 SigV4 签名请求，封装在 [S3CompatAdapter] 中作为
 * [com.wanbaohe.cloud.storage.data.protocol.RemoteFileSystemAdapter] 的协议实现。
 */
internal interface ObjectStorageVendorAdapter {
    val bucketInPath: Boolean

    fun buildListBucketsRequest(
        connection: CloudStorageConnection.S3Compat,
    ): Request

    fun buildListObjectsRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        prefix: String,
        continuationToken: String? = null,
        delimiter: String = "/",
        maxKeys: Int = 200,
    ): Request

    fun buildGetObjectRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
    ): Request = signedRequest(connection, "GET", bucket, key)

    fun buildHeadObjectRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
    ): Request

    fun buildPutObjectRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
        contentType: String,
        body: ByteArray,
    ): Request

    fun buildDeleteObjectRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
    ): Request

    fun buildCopyObjectRequest(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        sourceKey: String,
        targetKey: String,
    ): Request

    fun buildSignedGetUrl(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
        expiresInSeconds: Int = 900,
    ): String

    fun createTarget(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String = "",
        query: Map<String, String?> = emptyMap(),
    ) = ObjectStorageRequestFactory.createTarget(
        connection = connection,
        bucket = bucket,
        key = key,
        query = query,
        bucketInPath = bucketInPath,
    )

    fun signedRequest(
        connection: CloudStorageConnection.S3Compat,
        method: String,
        bucket: String,
        key: String = "",
        query: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: ByteArray? = null,
    ): Request
}
