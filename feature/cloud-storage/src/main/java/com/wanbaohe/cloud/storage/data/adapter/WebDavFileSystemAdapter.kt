package com.wanbaohe.cloud.storage.data.adapter

import com.wanbaohe.cloud.storage.data.protocol.RemoteFileSystemAdapter
import com.wanbaohe.cloud.storage.data.protocol.ObjectStoragePathResolver
import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebDAV 协议适配器（RFC 4918）。
 *
 * 认证：Basic（默认）+ Digest（按 RFC 7616 实现）。
 * 方法映射：
 *  - listRoots      -> 无 / 返回单根
 *  - listDirectory  -> PROPFIND Depth:1
 *  - stat           -> PROPFIND Depth:0
 *  - readBytes      -> GET
 *  - upload         -> PUT
 *  - createDirectory-> MKCOL
 *  - delete         -> DELETE（目录需先 PROPFIND 子项递归）
 *  - rename         -> MOVE（带 Destination 头）
 */
@Singleton
class WebDavFileSystemAdapter @Inject constructor() : RemoteFileSystemAdapter {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .authenticator(DigestAuthenticator())
        .build()

    override fun listRoots(connection: CloudStorageConnection): Result<List<CloudBucket>> =
        Result.success(WebDavXmlParser.singleRoot(connection))

    override fun listDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<List<CloudObjectItem>> = runCatching {
        val c = connection.asWebDav()
        val url = buildItemUrl(c, path)
        val req: Request = newRequest(c, "PROPFIND", url)
            .newBuilder()
            .header("Depth", "1")
            .method("PROPFIND", PROPFIND_BODY)
            .build()
        execute(req) { response ->
            val xml = response.body?.string().orEmpty()
            WebDavXmlParser.entriesToItems(
                entries = WebDavXmlParser.parsePropfind(xml),
                basePath = ObjectStoragePathResolver.normalizePrefix(path).trimEnd('/'),
            )
        }
    }

    override fun stat(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<CloudObjectItem> = runCatching {
        val c = connection.asWebDav()
        val url = buildItemUrl(c, path)
        val req: Request = newRequest(c, "PROPFIND", url)
            .newBuilder()
            .header("Depth", "0")
            .method("PROPFIND", PROPFIND_BODY)
            .build()
        execute(req) { response ->
            val xml = response.body?.string().orEmpty()
            val entries = WebDavXmlParser.parsePropfind(xml)
            val match = entries.firstOrNull()
                ?: throw IllegalStateException("WebDAV stat: empty response")
            val key = match.href.trimStart('/')
            CloudObjectItem(
                key = if (match.isCollection) "$key/" else key,
                displayName = key.substringAfterLast('/'),
                size = match.contentLength,
                lastModified = match.lastModified,
                eTag = match.etag,
                contentType = match.contentType,
                isDirectory = match.isCollection,
                isImage = match.contentType?.startsWith("image/") == true,
            )
        }
    }

    override fun readBytes(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<ByteArray> = runCatching {
        val c = connection.asWebDav()
        val req = newRequest(c, "GET", buildItemUrl(c, path))
        execute(req) { response -> response.body?.bytes() ?: ByteArray(0) }
    }

    override fun upload(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        body: ByteArray,
        contentType: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> = runCatching {
        val c = connection.asWebDav()
        val req = Request.Builder()
            .url(buildItemUrl(c, path))
            .header("Content-Type", contentType)
        c.encodedBasicAuth()?.let { req.header("Authorization", it) }
        val finalBody: RequestBody = if (onProgress != null) {
            ProgressRequestBody(body, contentType.toMediaTypeOrNull(), onProgress)
        } else {
            body.toRequestBody(contentType.toMediaTypeOrNull())
        }
        execute(req.put(finalBody).build()) { response -> }
    }

    override fun createDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<Unit> = runCatching {
        val c = connection.asWebDav()
        val req = newRequest(c, "MKCOL", buildItemUrl(c, path))
        execute(req) { }
    }

    override fun delete(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        isDirectory: Boolean,
    ): Result<Unit> = runCatching {
        val c = connection.asWebDav()
        if (isDirectory) {
            val children = listDirectory(c, root, path).getOrThrow()
            children.forEach { child ->
                delete(c, root, child.key, child.isDirectory).getOrThrow()
            }
        }
        val req = newRequest(c, "DELETE", buildItemUrl(c, path))
        execute(req) { }
    }

    override fun rename(
        connection: CloudStorageConnection,
        root: String,
        fromPath: String,
        toPath: String,
    ): Result<Unit> = runCatching {
        val c = connection.asWebDav()
        val req = Request.Builder()
            .url(buildItemUrl(c, fromPath))
            .header("Destination", buildItemUrl(c, toPath).toString())
            .header("Overwrite", "F")
        c.encodedBasicAuth()?.let { req.header("Authorization", it) }
        execute(req.method("MOVE", null).build()) { }
    }

    override fun buildDownloadUrl(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        expiresInSeconds: Int,
    ): String? = buildItemUrl(connection.asWebDav(), path).toString()

    private fun <T> execute(
        request: Request,
        mapper: (okhttp3.Response) -> T,
    ): T {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string().orEmpty()
                throw IllegalStateException(
                    "WebDAV ${request.method} ${response.code}: ${errBody.take(200)}"
                )
            }
            return mapper(response)
        }
    }

    private fun newRequest(
        c: CloudStorageConnection.WebDav,
        method: String,
        url: okhttp3.HttpUrl,
    ): Request {
        val builder = Request.Builder().url(url)
        if (c.username.isNotBlank()) {
            // 凭据通过 OkHttp Tag 在进程内传递，**不**通过 HTTP 自定义头发给服务器。
            // DigestAuthenticator 收到 401 后会从 tag() 读取并替换 Authorization。
            builder.tag(CredentialsTag(c.username, c.password))
            // Basic 认证时直接发 Authorization（用户已同意 Basic 的明文 base64 形式）。
            builder.header("Authorization", c.encodedBasicAuth().orEmpty())
        }
        return builder.method(method, null).build()
    }

    private fun buildItemUrl(
        c: CloudStorageConnection.WebDav,
        path: String,
    ): okhttp3.HttpUrl {
        val baseUrl = c.baseUrl.trimEnd('/')
        val normalizedPath = if (path.isBlank()) "" else path.trimStart('/')
        val rootPath = c.rootPath.trim('/')

        // 用 java.net.URI 做路径拼接，避免 endsWith 的子串误匹配（例如 rootPath="oud"
        // 会被 baseUrl="https://example.com/cloud" 误判为已包含）。
        val rootUri = if (rootPath.isNotBlank()) {
            val base = java.net.URI(baseUrl)
            val alreadyHasRoot = base.path.trimEnd('/').endsWith("/$rootPath")
            if (alreadyHasRoot) {
                base
            } else {
                java.net.URI(baseUrl + "/" + rootPath)
            }
        } else {
            java.net.URI(baseUrl)
        }

        val finalUri = if (normalizedPath.isNotBlank()) {
            rootUri.resolve(normalizedPath)
        } else {
            rootUri
        }
        return finalUri.toString().toHttpUrl()
    }

    private fun CloudStorageConnection.asWebDav(): CloudStorageConnection.WebDav =
        this as? CloudStorageConnection.WebDav
            ?: throw IllegalStateException("WebDavFileSystemAdapter requires WebDav connection")

    private fun CloudStorageConnection.WebDav.encodedBasicAuth(): String? =
        if (username.isNotBlank()) Credentials.basic(username, password) else null

    companion object {
        private val PROPFIND_BODY: RequestBody = """<?xml version="1.0" encoding="utf-8" ?>
            <D:propfind xmlns:D="DAV:">
              <D:prop>
                <D:resourcetype/>
                <D:getcontentlength/>
                <D:getlastmodified/>
                <D:getcontenttype/>
                <D:getetag/>
              </D:prop>
            </D:propfind>
        """.trimIndent().toRequestBody("application/xml".toMediaTypeOrNull())
    }
}

/**
 * 当服务器返回 401 且 WWW-Authenticate 头要求 Digest 时，
 * OkHttp 内置只支持 Basic —— 此 authenticator 兜底 Digest 计算并重发。
 *
 * 凭据通过 [CredentialsTag] 内部传递（OkHttp Request.tag），**不会**作为 HTTP 头
 * 发给服务器。Basic 认证场景下，原始 Authorization 头已由 newRequest 写入。
 */
private class DigestAuthenticator : okhttp3.Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
        if (response.priorResponse != null) return null
        val wwwAuth = response.header("WWW-Authenticate") ?: return null
        if (!wwwAuth.startsWith("Digest", ignoreCase = true)) return null
        val original = response.request
        val tag = original.tag() as? CredentialsTag ?: return null
        val username = tag.username
        val password = tag.password
        if (username.isBlank() || password.isBlank()) return null

        val challenge = parseDigestChallenge(wwwAuth)
        val uri = original.url.encodedPath.let { if (it.isBlank()) "/" else it }
        val nc = "00000001"
        val cnonce = DigestHelpers.randomCnonce()
        val ha1 = DigestHelpers.md5("$username:${challenge.realm}:$password")
        val ha2 = DigestHelpers.md5("${original.method}:$uri")
        val responseHash = DigestHelpers.md5(
            "$ha1:${challenge.nonce}:$nc:$cnonce:${challenge.qop}:$ha2"
        )
        val authHeader = buildString {
            append("Digest ")
            append("username=\"$username\"")
            append(", realm=\"${challenge.realm}\"")
            append(", nonce=\"${challenge.nonce}\"")
            append(", uri=\"$uri\"")
            if (!challenge.opaque.isNullOrEmpty()) append(", opaque=\"${challenge.opaque}\"")
            append(", qop=${challenge.qop}")
            append(", nc=$nc")
            append(", cnonce=\"$cnonce\"")
            append(", response=\"$responseHash\"")
        }
        return original.newBuilder()
            .header("Authorization", authHeader)
            .build()
    }
}

/**
 * OkHttp Request Tag —— WebDAV 凭据内部传递载体。
 * 通过 request.tag() 存取，**不**会作为 HTTP 头出现在网络上。
 */
internal data class CredentialsTag(
    val username: String,
    val password: String,
)

private data class DigestChallenge(
    val realm: String,
    val nonce: String,
    val qop: String,
    val opaque: String?,
)

private fun parseDigestChallenge(header: String): DigestChallenge {
    val map = header.substringAfter("Digest", "").split(',')
        .associate {
            val parts = it.trim().split("=", limit = 2)
            parts[0] to parts.getOrNull(1)?.trim('"')?.trim().orEmpty()
        }
    return DigestChallenge(
        realm = map["realm"].orEmpty(),
        nonce = map["nonce"].orEmpty(),
        qop = map["qop"]?.split(' ', ',')?.firstOrNull() ?: "auth",
        opaque = map["opaque"],
    )
}

private object DigestHelpers {
    fun md5(value: String): String =
        java.security.MessageDigest.getInstance("MD5")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun randomCnonce(): String =
        java.security.SecureRandom().let { rnd ->
            ByteArray(8).also(rnd::nextBytes)
                .joinToString("") { "%02x".format(it) }
        }
}
