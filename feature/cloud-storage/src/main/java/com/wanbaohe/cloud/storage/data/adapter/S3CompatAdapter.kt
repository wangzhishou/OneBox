package com.wanbaohe.cloud.storage.data.adapter

import com.wanbaohe.cloud.storage.data.protocol.ObjectStorageErrorParser
import com.wanbaohe.cloud.storage.data.protocol.ObjectStoragePathResolver
import com.wanbaohe.cloud.storage.data.protocol.RemoteFileSystemAdapter
import com.wanbaohe.cloud.storage.data.vendor.AliyunOssAdapter
import com.wanbaohe.cloud.storage.data.vendor.AwsS3Adapter
import com.wanbaohe.cloud.storage.data.vendor.BaiduBosAdapter
import com.wanbaohe.cloud.storage.data.vendor.HuaweiObsAdapter
import com.wanbaohe.cloud.storage.data.vendor.ObjectStorageVendorAdapter
import com.wanbaohe.cloud.storage.data.vendor.TencentCosAdapter
import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.model.S3Vendor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import com.shifenmiao.network.NetworkBuilder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * S3 兼容族适配器：把 [com.wanbaohe.cloud.storage.data.protocol.ObjectStorageVendorAdapter]
 * 的"SigV4 / 类 SigV4 请求构造 + 解析"封装成 [RemoteFileSystemAdapter] 协议无关接口。
 *
 * 内部按 [S3Vendor] 选具体 vendor adapter（AliyunOss / TencentCos / ...）。
 */
@Singleton
class S3CompatAdapter @Inject constructor() : RemoteFileSystemAdapter {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // 保留原 CloudStorageRepositoryImpl 的日志拦截器 —— S3 签名请求的调试强依赖。
        .addInterceptor(NetworkBuilder.provideHttpLoggingInterceptor())
        .build()

    private val vendorAdapters: Map<S3Vendor, ObjectStorageVendorAdapter> = mapOf(
        S3Vendor.AWS_S3 to AwsS3Adapter(),
        S3Vendor.S3_COMPATIBLE to AwsS3Adapter(),
        S3Vendor.ALIYUN_OSS to AliyunOssAdapter(),
        S3Vendor.TENCENT_COS to TencentCosAdapter(),
        S3Vendor.HUAWEI_OBS to HuaweiObsAdapter(),
        S3Vendor.BAIDU_BOS to BaiduBosAdapter(),
    )

    private fun adapterOf(c: CloudStorageConnection.S3Compat): ObjectStorageVendorAdapter {
        val ad = vendorAdapters[c.vendor]
        requireNotNull(ad) { "Unsupported S3 vendor: ${c.vendor}" }
        return ad
    }

    override fun listRoots(connection: CloudStorageConnection): Result<List<CloudBucket>> = runCatching {
        val c = connection.asS3()
        execute(
            vendorAdapters.getValue(c.vendor)
                .buildListBucketsRequest(c)
        ) { response ->
            val xml = response.body?.string().orEmpty()
            com.wanbaohe.cloud.storage.data.protocol.ObjectStorageXmlParser.parseBuckets(xml)
        }
    }

    override fun listDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<List<CloudObjectItem>> = runCatching {
        val c = connection.asS3()
        val normalized = ObjectStoragePathResolver.normalizePrefix(path)
        execute(
            adapterOf(c).buildListObjectsRequest(
                connection = c,
                bucket = root,
                prefix = normalized,
            )
        ) { response ->
            val xml = response.body?.string().orEmpty()
            val items = com.wanbaohe.cloud.storage.data.protocol.ObjectStorageXmlParser
                .parseListObjects(xml).items
            items.filterNot { it.key == normalized }
        }
    }

    override fun stat(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<CloudObjectItem> = runCatching {
        val c = connection.asS3()
        execute(adapterOf(c).buildHeadObjectRequest(c, root, path)) { response ->
            CloudObjectItem(
                key = path,
                displayName = ObjectStoragePathResolver.displayName(path, isDirectory = false),
                size = response.header("Content-Length")?.toLongOrNull() ?: 0L,
                lastModified = response.header("Last-Modified"),
                eTag = response.header("ETag")?.trim('"'),
                contentType = response.header("Content-Type"),
                isImage = response.header("Content-Type")?.startsWith("image/") == true,
            )
        }
    }

    override fun readBytes(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<ByteArray> = runCatching {
        val c = connection.asS3()
        execute(adapterOf(c).buildGetObjectRequest(c, root, path)) { response ->
            response.body.bytes()
        }
    }

    override fun upload(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        body: ByteArray,
        contentType: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> = runCatching {
        val c = connection.asS3()
        val originalRequest = adapterOf(c).buildPutObjectRequest(
            connection = c,
            bucket = root,
            key = path,
            contentType = contentType,
            body = body,
        )
        val finalRequest = if (onProgress != null) {
            val mediaType = contentType.toMediaTypeOrNull()
            val progressBody = ProgressRequestBody(body, mediaType, onProgress)
            originalRequest.newBuilder().method(originalRequest.method, progressBody).build()
        } else {
            originalRequest
        }
        execute(finalRequest) { }
    }

    override fun createDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<Unit> {
        val normalized = ObjectStoragePathResolver.normalizePrefix(path)
        return upload(
            connection = connection,
            root = root,
            path = normalized,
            body = ByteArray(0),
            contentType = "application/x-directory",
            onProgress = null,
        )
    }

    override fun delete(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        isDirectory: Boolean,
    ): Result<Unit> = runCatching {
        val c = connection.asS3()
        if (!isDirectory) {
            execute(adapterOf(c).buildDeleteObjectRequest(c, root, path)) { }
            return@runCatching
        }
        var token: String? = null
        do {
            val page: com.wanbaohe.cloud.storage.data.protocol.ObjectStorageXmlParser.ListObjectsResult =
                execute(
                    adapterOf(c).buildListObjectsRequest(
                        connection = c,
                        bucket = root,
                        prefix = path,
                        continuationToken = token,
                        delimiter = "",
                        maxKeys = 1000,
                    )
                ) { response ->
                    val xml = response.body?.string().orEmpty()
                    com.wanbaohe.cloud.storage.data.protocol.ObjectStorageXmlParser.parseListObjects(xml)
                }
            page.items.forEach { child ->
                execute(adapterOf(c).buildDeleteObjectRequest(c, root, child.key)) { }
            }
            token = page.nextContinuationToken
        } while (!token.isNullOrBlank())
        execute(adapterOf(c).buildDeleteObjectRequest(c, root, path)) { }
    }

    override fun rename(
        connection: CloudStorageConnection,
        root: String,
        fromPath: String,
        toPath: String,
    ): Result<Unit> = runCatching {
        val c = connection.asS3()
        execute(adapterOf(c).buildCopyObjectRequest(c, root, fromPath, toPath)) { }
        execute(adapterOf(c).buildDeleteObjectRequest(c, root, fromPath)) { }
    }

    override fun buildDownloadUrl(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        expiresInSeconds: Int,
    ): String? {
        val c = connection.asS3()
        return adapterOf(c).buildSignedGetUrl(c, root, path, expiresInSeconds)
    }

    private fun <T> execute(
        request: okhttp3.Request,
        mapper: (okhttp3.Response) -> T,
    ): T {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body.string()
                val detail = ObjectStorageErrorParser.parse(errBody) ?: "HTTP ${response.code}"
                throw IllegalStateException(detail)
            }
            return mapper(response)
        }
    }

    private fun CloudStorageConnection.asS3(): CloudStorageConnection.S3Compat =
        this as? CloudStorageConnection.S3Compat
            ?: throw IllegalStateException("S3CompatAdapter requires S3Compat connection")
}
