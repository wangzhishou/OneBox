package com.shifenmiao.imagegeneration.loader

import android.content.Context
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.t8rin.imagetoolbox.core.data.workspace.AppWorkspaceResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ImageGenerationLoaderImpl private constructor(
    private val manager: ImageGenerationManager,
    private val client: OkHttpClient,
    private val cacheDirectoryProvider: () -> File,
) : ImageGenerationLoader {

    @Inject
    constructor(
        manager: ImageGenerationManager,
        @Named("DirectImageGenerationClient") client: OkHttpClient,
        @ApplicationContext context: Context,
        appWorkspaceResolver: AppWorkspaceResolver,
    ) : this(
        manager = manager,
        client = client,
        cacheDirectoryProvider = { resolveCacheDirectory(context, appWorkspaceResolver) },
    )

    internal constructor(
        manager: ImageGenerationManager,
        client: OkHttpClient,
        cacheDirectory: File,
        @Suppress("UNUSED_PARAMETER") testing: Unit = Unit,
    ) : this(manager, client, { cacheDirectory })

    private val keyLocks = mutableMapOf<String, Mutex>()
    private val keyLocksGuard = Mutex()

    override suspend fun load(
        prompt: String,
        forceRefresh: Boolean,
    ): Result<CachedGeneratedImage> = load(
        request = ImageGenerationRequest(prompt = prompt),
        forceRefresh = forceRefresh,
    )

    override suspend fun load(
        request: ImageGenerationRequest,
        forceRefresh: Boolean,
    ): Result<CachedGeneratedImage> {
        if (request.prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("prompt must not be blank"))
        }
        val config = manager.getActiveConfig()
            ?: return Result.failure(IllegalStateException("No active image provider config"))
        val cacheKey = createCacheKey(config, request)
        val mutex = keyLocksGuard.withLock { keyLocks.getOrPut(cacheKey, ::Mutex) }
        return try {
            mutex.withLock {
                loadLocked(config, request, cacheKey, forceRefresh)
            }
        } finally {
            keyLocksGuard.withLock {
                if (!mutex.isLocked) keyLocks.remove(cacheKey, mutex)
            }
        }
    }

    override fun clearCache(): Result<Unit> = runCatching {
        val cacheDirectory = cacheDirectoryProvider()
        if (cacheDirectory.exists()) {
            check(cacheDirectory.deleteRecursively()) { "Failed to clear generated image cache" }
        }
    }

    /**
     * 生成 + 下载全程切到 IO 线程:download 里是 OkHttp 同步 execute,
     * 调用方(如组件 componentScope)多在主线程,不切线程会 NetworkOnMainThreadException。
     */
    private suspend fun loadLocked(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
        cacheKey: String,
        forceRefresh: Boolean,
    ): Result<CachedGeneratedImage> = withContext(Dispatchers.IO) {
        // 每次调用动态解析:用户可能中途改了保存文件夹,工作目录随之变化
        val cacheDirectory = cacheDirectoryProvider()
        val existing = findCachedFile(cacheDirectory, cacheKey)
        if (!forceRefresh && existing != null) {
            return@withContext Result.success(CachedGeneratedImage(existing, cacheKey, fromCache = true))
        }

        return@withContext try {
            val generation = manager.generateWithConfig(config, request).getOrThrow()
            val imageUrl = generation.images.firstOrNull()?.url
                ?.takeIf(String::isNotBlank)
                ?: error("Image provider returned no image")
            val downloaded = download(cacheDirectory, imageUrl, cacheKey)
            existing?.takeIf { it != downloaded }?.delete()
            Result.success(CachedGeneratedImage(downloaded, cacheKey, fromCache = false))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findCachedFile(cacheDirectory: File, cacheKey: String): File? = cacheDirectory
        .takeIf(File::isDirectory)
        ?.listFiles()
        ?.firstOrNull { it.isFile && it.length() > 0L && it.name.startsWith("$cacheKey.") }

    private fun download(cacheDirectory: File, url: String, cacheKey: String): File {
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Image download failed: HTTP ${response.code}" }
            val body = response.body
            val mediaType = body.contentType()
            check(mediaType?.type == "image") { "Generated content is not an image" }
            cacheDirectory.mkdirs()
            check(cacheDirectory.isDirectory) { "Failed to create generated image cache directory" }

            val extension = extensionFor(mediaType.subtype)
            val target = File(cacheDirectory, "$cacheKey.$extension")
            val temporary = File(cacheDirectory, "$cacheKey.${UUID.randomUUID()}.part")
            val backup = File(cacheDirectory, "$cacheKey.${UUID.randomUUID()}.bak")
            try {
                FileOutputStream(temporary).use { output ->
                    body.byteStream().use { input -> input.copyTo(output) }
                    output.fd.sync()
                }
                check(temporary.length() > 0L) { "Generated image is empty" }
                if (target.exists()) check(target.renameTo(backup)) { "Failed to prepare generated image cache replacement" }
                check(temporary.renameTo(target)) { "Failed to commit generated image cache" }
                backup.delete()
                return target
            } catch (e: Exception) {
                if (!target.exists() && backup.exists()) backup.renameTo(target)
                throw e
            } finally {
                temporary.delete()
                backup.delete()
            }
        }
    }

    private fun createCacheKey(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
    ): String {
        val canonical = buildString {
            appendLine(CACHE_VERSION)
            appendLine(config.id)
            appendLine(config.providerId)
            appendLine(request.model ?: config.model)
            appendLine(request.prompt.trim())
            appendLine(request.inputImages.joinToString("\u001f"))
            appendLine(request.negativePrompt.orEmpty())
            appendLine(request.outputSize.orEmpty())
            appendLine(request.outputCount)
            appendLine(request.seed ?: "")
            appendLine(request.watermark)
            appendLine(request.promptExtend)
            appendLine(request.promptExtendMode)
            appendLine(request.enableThinking)
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun extensionFor(subtype: String?): String = when (subtype?.lowercase()) {
        "jpeg", "jpg" -> "jpg"
        "png" -> "png"
        "webp" -> "webp"
        "gif" -> "gif"
        "avif" -> "avif"
        else -> "img"
    }

    private companion object {
        const val CACHE_VERSION = "v1"

        /** 工作目录下存放生成图片的子目录,用户在文件管理器中可直接查看。 */
        const val GENERATED_IMAGES_DIRECTORY = "generated-images"

        /** 无外部存储写权限时的私有回退目录。 */
        const val FALLBACK_CACHE_DIRECTORY = "image-generation/$CACHE_VERSION"

        /**
         * 缓存目录优先落在 App 工作目录(用户保存文件夹或 Documents/OneBox)下,
         * 让生成的图片可以在文件管理器中查看;目录不可创建时回退到应用私有目录。
         */
        fun resolveCacheDirectory(
            context: Context,
            appWorkspaceResolver: AppWorkspaceResolver,
        ): File {
            val workspaceRoot = runCatching { appWorkspaceResolver.resolve().file }.getOrNull()
            if (workspaceRoot != null) {
                val workspaceCache = File(workspaceRoot, GENERATED_IMAGES_DIRECTORY)
                if ((workspaceCache.exists() || workspaceCache.mkdirs()) && workspaceCache.isDirectory) {
                    return workspaceCache
                }
            }
            return File(context.filesDir, FALLBACK_CACHE_DIRECTORY)
        }
    }
}
