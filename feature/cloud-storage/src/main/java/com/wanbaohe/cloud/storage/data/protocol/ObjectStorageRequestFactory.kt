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

package com.wanbaohe.cloud.storage.data.protocol

import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

object ObjectStorageRequestFactory {

    data class RequestTarget(
        val url: HttpUrl,
        val pathForSigning: String,
        val host: String,
    )

    fun createTarget(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String = "",
        query: Map<String, String?> = emptyMap(),
        bucketInPath: Boolean = false,
    ): RequestTarget {
        val normalizedEndpoint = connection.endpoint.removePrefix("https://").removePrefix("http://")
        val pathSegments = mutableListOf<String>()
        if (bucketInPath) {
            pathSegments += bucket
        }
        val keyWithoutLeadingSlash = key.trimStart('/')
        keyWithoutLeadingSlash.trim('/').takeIf { it.isNotBlank() }?.split('/')?.let(pathSegments::addAll)
        if (keyWithoutLeadingSlash.endsWith("/")) {
            pathSegments += ""
        }

        val urlBuilder = "https://$normalizedEndpoint/".toHttpUrl().newBuilder()
        pathSegments.forEach(urlBuilder::addPathSegment)
        query.forEach { (name, value) -> urlBuilder.addQueryParameter(name, value) }
        val url = urlBuilder.build()
        val signingPath = "/" + pathSegments.joinToString("/")
        val host = if (bucketInPath) normalizedEndpoint else "$bucket.$normalizedEndpoint"
        val finalUrl = if (bucketInPath) {
            url
        } else {
            url.newBuilder().host(host).build()
        }
        return RequestTarget(
            url = finalUrl,
            pathForSigning = if (signingPath == "/") "/" else signingPath,
            host = host,
        )
    }

    fun requestBuilder(target: RequestTarget): Request.Builder = Request.Builder()
        .url(target.url)
        .header("Host", target.host)
}
