package com.wanbaohe.cloud.storage.data.vendor

import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal class TencentCosAdapter : ObjectStorageVendorAdapter {
    override val bucketInPath: Boolean = false
    private val signer = TencentCosSigner()

    override fun buildListBucketsRequest(connection: CloudStorageConnection.S3Compat): Request {
        val endpoint = connection.endpoint.removePrefix("https://").removePrefix("http://")
        val target = com.wanbaohe.cloud.storage.data.protocol.ObjectStorageRequestFactory.RequestTarget(
            url = "https://$endpoint/".toHttpUrl(),
            pathForSigning = "/",
            host = endpoint,
        )
        val headers = signer.signHeaders(connection, "GET", target, emptyMap(), emptyMap())
        val builder = Request.Builder().url(target.url).header("Host", endpoint)
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
            "list-type" to "2",
            "delimiter" to delimiter,
            "prefix" to prefix,
            "max-keys" to maxKeys.toString(),
        ).apply {
            continuationToken?.let { put("continuation-token", it) }
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
        headers = mapOf("x-cos-copy-source" to "$bucket.${connection.endpoint.removePrefix("https://").removePrefix("http://")}/$sourceKey"),
    )

    override fun buildSignedGetUrl(
        connection: CloudStorageConnection.S3Compat,
        bucket: String,
        key: String,
        expiresInSeconds: Int,
    ): String = signer.presign(connection, createTarget(connection, bucket, key), expiresInSeconds).toString()

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
        val signedHeaders = signer.signHeaders(connection, method, target, query, headers)
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
