package com.shifenmiao.imagegeneration.provider.openai

import android.content.Context
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.imagegeneration.R
import com.shifenmiao.imagegeneration.model.GeneratedImage
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import com.shifenmiao.imagegeneration.provider.ImageGenerationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * OpenAI 生图 provider:纯 BYOK,只在海外渠道(google/foss)经
 * src/overseas 的 OpenAiImageModule 注册,不走 Go 网关代理。
 * baseUrl 开放给用户改,兼容第三方 OpenAI 兼容端点。
 */
@Singleton
class OpenAiImageProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("DirectOpenAiImageApi") private val directApi: OpenAiImageApi,
) : ImageGenerationProvider {

    override val descriptor = ImageProviderDescriptor(
        providerId = PROVIDER_ID,
        displayName = "OpenAI (GPT Image)",
        defaultBaseUrl = UrlConstants.OPENAI_BASE_URL,
        defaultProxyUrl = "",
        defaultProxyPath = "",
        defaultModel = MODEL_GPT_IMAGE,
        availableModels = listOf(MODEL_GPT_IMAGE, MODEL_GPT_IMAGE_MINI),
        supportsGeneration = true,
        // edits 接口是 multipart 另一套,暂不支持图生图
        supportsEditing = false,
        maxInputImages = 0,
    )

    override suspend fun generate(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
    ): Result<ImageGenerationResult> = runCatching {
        validate(request)
        val token = config.apiToken.trim()
        require(token.isNotEmpty()) { context.getString(R.string.openai_image_token_required) }
        val model = request.model?.takeIf(String::isNotBlank)
            ?: config.model.takeIf(String::isNotBlank)
            ?: descriptor.defaultModel
        val body = OpenAiImageRequest(
            model = model,
            prompt = request.prompt,
            n = request.outputCount.coerceAtLeast(1),
            size = request.outputSize?.let(::mapSize),
        )
        val response = directApi.generate(
            url = joinUrl(
                config.baseUrl.ifBlank { descriptor.defaultBaseUrl },
                UrlConstants.OPENAI_IMAGE_GENERATIONS_ENDPOINT,
            ),
            authorization = "Bearer $token",
            request = body,
        )
        val responseBody = response.body()
            ?: error(response.errorMessage())
        responseBody.error?.let { error(it.message ?: it.code ?: "Image generation failed") }
        val images = responseBody.data.orEmpty().mapNotNull { item ->
            item.url?.takeIf(String::isNotBlank)
                // gpt-image-1 系列只回 b64_json,包成 data: URI 由 loader 解码落盘
                ?: item.b64Json?.takeIf(String::isNotBlank)?.let { "data:image/png;base64,$it" }
        }.map(::GeneratedImage)
        require(images.isNotEmpty()) { "Image provider returned no image" }
        ImageGenerationResult(
            images = images,
            providerId = descriptor.providerId,
            model = model,
        )
    }.onFailure { error ->
        if (error is CancellationException) throw error
    }

    private fun validate(request: ImageGenerationRequest) {
        require(request.prompt.isNotBlank()) { "prompt must not be blank" }
        require(request.inputImages.isEmpty()) { "image editing is not supported by this provider" }
    }

    /**
     * gpt-image-1 只支持 1024x1024 / 1536x1024 / 1024x1536 三档,
     * 内部任意 WIDTH*HEIGHT 按宽高比就近归档,分界取两档比值的几何中点 √1.5。
     */
    private fun mapSize(size: String): String {
        val match = Regex("^(\\d+)\\*(\\d+)$").matchEntire(size)
        requireNotNull(match) { "outputSize must use WIDTH*HEIGHT format" }
        val width = match.groupValues[1].toDoubleOrNull()
        val height = match.groupValues[2].toDoubleOrNull()
        require(width != null && height != null && width > 0 && height > 0) { "invalid outputSize" }
        val ratio = width / height
        return when {
            ratio >= ASPECT_BOUNDARY -> SIZE_LANDSCAPE
            ratio <= 1 / ASPECT_BOUNDARY -> SIZE_PORTRAIT
            else -> SIZE_SQUARE
        }
    }

    private fun joinUrl(baseUrl: String, path: String): String {
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val normalizedBase = baseUrl.trim().let {
            when {
                it.startsWith("http://") || it.startsWith("https://") -> it
                else -> "https://$it"
            }
        }.trimEnd('/')
        return "$normalizedBase/${path.trimStart('/')}"
    }

    /** OpenAI 错误体为 {"error": {"message": "...", "type": "...", "code": "..."}}。 */
    private fun Response<OpenAiImageResponse>.errorMessage(): String {
        val raw = runCatching { errorBody()?.string().orEmpty() }.getOrDefault("")
        if (raw.isBlank()) return "Image generation failed: HTTP ${code()}"
        val parsed = runCatching {
            val error = JsonParser.parseString(raw).asJsonObject.get("error")
            when {
                error == null || error.isJsonNull -> null
                error.isJsonPrimitive -> error.asString
                else -> error.asJsonObject.get("message")?.asString
            }
        }.getOrNull()
        return parsed ?: raw
    }

    companion object {
        const val PROVIDER_ID = "openai-image"
        const val MODEL_GPT_IMAGE = "gpt-image-1"
        const val MODEL_GPT_IMAGE_MINI = "gpt-image-1-mini"
        private const val SIZE_SQUARE = "1024x1024"
        private const val SIZE_LANDSCAPE = "1536x1024"
        private const val SIZE_PORTRAIT = "1024x1536"
        private val ASPECT_BOUNDARY = sqrt(1.5)
    }
}

interface OpenAiImageApi {
    @POST
    suspend fun generate(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: OpenAiImageRequest,
    ): Response<OpenAiImageResponse>
}

// R8 full mode 下字段名会被混淆,Gson 序列化依赖 @SerializedName 固定 JSON key。
// null 字段默认不参与序列化,未传的 size 不会出现在请求体里。
data class OpenAiImageRequest(
    @SerializedName("model") val model: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("n") val n: Int = 1,
    @SerializedName("size") val size: String? = null,
)

data class OpenAiImageResponse(
    @SerializedName("data") val data: List<OpenAiImageData>? = null,
    @SerializedName("error") val error: OpenAiImageError? = null,
)

// gpt-image-1 只回 b64_json,dall-e-3 回 url,两种都兼容解析
data class OpenAiImageData(
    @SerializedName("url") val url: String? = null,
    @SerializedName("b64_json") val b64Json: String? = null,
)

data class OpenAiImageError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("code") val code: String? = null,
)
