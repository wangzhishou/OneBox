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
