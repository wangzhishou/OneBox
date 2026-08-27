package com.shifenmiao.imagegeneration.loader

import com.shifenmiao.imagegeneration.model.GeneratedImage
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageGenerationLoaderTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `one prompt generates once then returns persistent cache`() = runBlocking {
        val manager = FakeManager()
        val loader = loader(manager)

        val first = loader.load("a blue cat").getOrThrow()
        val second = loader.load("  a blue cat  ").getOrThrow()

        assertFalse(first.fromCache)
        assertTrue(second.fromCache)
        assertEquals(first.file, second.file)
        assertTrue(first.file.isFile)
        assertContentEquals(IMAGE_BYTES, first.file.readBytes())
        assertEquals(1, manager.generateCount.get())
    }

    @Test
    fun `same key concurrent calls share generation and download`() = runBlocking {
        val manager = FakeManager(delayMillis = 50)
        val downloads = AtomicInteger()
        val loader = loader(manager, downloads)

        val results = List(8) { async { loader.load("same prompt").getOrThrow() } }.awaitAll()

        assertEquals(1, manager.generateCount.get())
        assertEquals(1, downloads.get())
        assertEquals(1, results.map { it.file }.distinct().size)
        assertEquals(1, results.count { !it.fromCache })
    }

    @Test
    fun `request parameters and active config isolate cache keys`() = runBlocking {
        val manager = FakeManager()
        val loader = loader(manager)

        val first = loader.load(ImageGenerationRequest("cat", outputSize = "1024*1024")).getOrThrow()
        val second = loader.load(ImageGenerationRequest("cat", outputSize = "1024*1536")).getOrThrow()
        manager.config = manager.config.copy(id = "other-config")
        val third = loader.load(ImageGenerationRequest("cat", outputSize = "1024*1024")).getOrThrow()

        assertEquals(3, setOf(first.cacheKey, second.cacheKey, third.cacheKey).size)
        assertEquals(3, manager.generateCount.get())
    }

    @Test
    fun `force refresh replaces cache only after successful download`() = runBlocking {
        val manager = FakeManager()
        var failDownload = false
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            if (failDownload) throw IllegalStateException("download failed")
            imageResponse(chain.request(), IMAGE_BYTES)
        }.build()
        val loader = ImageGenerationLoaderImpl(manager, client, temporaryFolder.newFolder(), Unit)
        val original = loader.load("cat").getOrThrow()
        failDownload = true

        assertTrue(loader.load("cat", forceRefresh = true).isFailure)
        assertTrue(original.file.isFile)
        assertContentEquals(IMAGE_BYTES, original.file.readBytes())
    }

    @Test
    fun `non image response is rejected and not cached`() = runBlocking {
        val manager = FakeManager()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            imageResponse(chain.request(), "not image".toByteArray(), "text/plain")
        }.build()
        val directory = temporaryFolder.newFolder()
        val loader = ImageGenerationLoaderImpl(manager, client, directory, Unit)

        assertTrue(loader.load("cat").isFailure)
        assertTrue(directory.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun `blank prompt fails before provider call and cancellation propagates`() {
        val manager = FakeManager(cancel = true)
        val loader = loader(manager)

        runBlocking {
            assertTrue(loader.load(" ").isFailure)
            assertEquals(0, manager.generateCount.get())
        }
        assertFailsWith<CancellationException> {
            runBlocking { loader.load("cat") }
        }
    }

    private fun loader(
        manager: FakeManager,
        downloads: AtomicInteger = AtomicInteger(),
    ): ImageGenerationLoaderImpl {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            downloads.incrementAndGet()
            imageResponse(chain.request(), IMAGE_BYTES)
        }.build()
        return ImageGenerationLoaderImpl(manager, client, temporaryFolder.newFolder(), Unit)
    }

    private fun imageResponse(
        request: okhttp3.Request,
        bytes: ByteArray,
        contentType: String = "image/png",
    ) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(bytes.toResponseBody(contentType.toMediaType()))
        .build()

    private class FakeManager(
        private val delayMillis: Long = 0,
        private val cancel: Boolean = false,
    ) : ImageGenerationManager {
        var config = ImageProviderConfig(
            id = "config",
            providerId = "fake",
            displayName = "Fake",
            model = "fake-model",
        )
        val generateCount = AtomicInteger()

        override fun observeConfigs(): Flow<List<ImageProviderConfig>> = flowOf(listOf(config))
        override fun observeActiveConfig(): Flow<ImageProviderConfig?> = flowOf(config)
        override fun getConfigs(): List<ImageProviderConfig> = listOf(config)
        override fun getActiveConfig(): ImageProviderConfig = config
        override fun getProviderDescriptors(): List<ImageProviderDescriptor> = emptyList()
        override fun createDefaultConfig(providerId: String): ImageProviderConfig = config
        override fun saveConfig(config: ImageProviderConfig, makeActive: Boolean) = Unit
        override fun deleteConfig(id: String) = Unit
        override fun setActiveConfig(id: String) = Unit

        override suspend fun generate(request: ImageGenerationRequest): Result<ImageGenerationResult> =
            generateWithConfig(config, request)

        override suspend fun generateWithConfig(
            config: ImageProviderConfig,
            request: ImageGenerationRequest,
        ): Result<ImageGenerationResult> {
            generateCount.incrementAndGet()
            if (delayMillis > 0) delay(delayMillis)
            if (cancel) throw CancellationException("cancelled")
            return Result.success(
                ImageGenerationResult(
                    images = listOf(GeneratedImage("https://image.example/generated.png")),
                    providerId = config.providerId,
                    model = config.model,
                )
            )
        }
    }

    private companion object {
        val IMAGE_BYTES = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
    }
}
