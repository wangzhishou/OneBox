package com.shifenmiao.imagegeneration.provider.hunyuan

import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.imagegeneration.model.GeneratedImage
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import com.shifenmiao.imagegeneration.provider.ImageGenerationProvider
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class HunyuanImageProvider @Inject constructor(
    @Named("DirectHunyuanImageApi") private val directApi: HunyuanImageApi,
    @Named("ProxyHunyuanImageApi") private val proxyApi: HunyuanImageApi,
) : ImageGenerationProvider {

    override val descriptor = ImageProviderDescriptor(
        providerId = PROVIDER_ID,
        displayName = "混元图像 (Hunyuan)",
        defaultBaseUrl = UrlConstants.HUNYUAN_IMAGE_BASE_URL,
        defaultProxyUrl = UrlConstants.RELEASE_URL,
        defaultProxyPath = UrlConstants.TENCENT_HUNYUAN_IMAGE_PROXY_PATH,
        defaultModel = MODEL_V35,
        availableModels = listOf(MODEL_V35),
        supportsGeneration = true,
        supportsEditing = true,
        maxInputImages = 3,
    )

    override suspend fun generate(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
    ): Result<ImageGenerationResult> = runCatching {
        validate(request)
        val model = request.model?.takeIf(String::isNotBlank)
            ?: config.model.takeIf(String::isNotBlank)
            ?: descriptor.defaultModel
        val body = request.toHunyuanRequest(model)
        val token = config.apiToken.trim()
        val response = if (token.isNotEmpty()) {
            directApi.generateOrEdit(
                url = joinUrl(
                    config.baseUrl.ifBlank { descriptor.defaultBaseUrl },
                    UrlConstants.HUNYUAN_IMAGE_GENERATION_ENDPOINT,
                ),
                authorization = "Bearer $token",
                request = body,
            )
        } else {
            proxyApi.generateOrEdit(
                url = joinUrl(
                    config.proxyUrl.ifBlank { descriptor.defaultProxyUrl },
                    config.proxyPath.ifBlank { descriptor.defaultProxyPath },
                ),
                request = body,
            )
        }
        val responseBody = response.body()
            ?: error(response.errorMessage())
        responseBody.error?.let { error("${it.code.orEmpty()}: ${it.message.orEmpty()}".trim(':', ' ')) }
        // 成功以最终交付图 URL 存在为准,finish_reason 可能为 null
        val image = responseBody.choices.orEmpty()
            .mapNotNull(HunyuanChoice::delta)
            .mapNotNull(HunyuanDelta::image)
            .firstOrNull { !it.url.isNullOrBlank() }
            ?: error("Image provider returned no image")
        ImageGenerationResult(
            images = listOf(GeneratedImage(image.url.orEmpty())),
            providerId = descriptor.providerId,
            model = model,
            width = image.width,
            height = image.height,
        )
    }.onFailure { error ->
        if (error is CancellationException) throw error
    }

    private fun validate(request: ImageGenerationRequest) {
        require(request.prompt.isNotBlank()) { "prompt must not be blank" }
        require(request.inputImages.size <= descriptor.maxInputImages) {
            "at most ${descriptor.maxInputImages} input images are supported"
        }
        require(request.inputImages.none(String::isBlank)) { "input images must not be blank" }
        request.seed?.let { require(it >= 0) { "seed must be between 0 and 2147483647" } }
        request.outputSize?.let(::validateSize)
    }

    private fun validateSize(size: String) {
        val (width, height) = parseSize(size)
        require(width in MIN_SIDE..MAX_SIDE && height in MIN_SIDE..MAX_SIDE) {
            "outputSize width and height must be between $MIN_SIDE and $MAX_SIDE"
        }
        require(width.toLong() * height <= MAX_PIXELS) {
            "outputSize pixel count must not exceed $MAX_PIXELS"
        }
    }

    private fun parseSize(size: String): Pair<Int, Int> {
        val match = Regex("^(\\d+)\\*(\\d+)$").matchEntire(size)
        requireNotNull(match) { "outputSize must use WIDTH*HEIGHT format" }
        val width = match.groupValues[1].toIntOrNull()
        val height = match.groupValues[2].toIntOrNull()
        require(width != null && height != null) { "invalid outputSize" }
        return width to height
    }

    private fun ImageGenerationRequest.toHunyuanRequest(model: String) = HunyuanRequest(
        model = model,
        // 混元的 size 用小写 x 分隔,内部格式是 WIDTH*HEIGHT
        size = outputSize?.let { size ->
            val (width, height) = parseSize(size)
            "${width}x${height}"
        },
        seed = seed?.toLong(),
        messages = listOf(
            HunyuanMessage(
                content = inputImages.map {
                    HunyuanContent(type = "image_url", imageUrl = HunyuanImageUrl(url = it))
                } + HunyuanContent(type = "text", text = prompt)
            )
        ),
    )

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

    /**
     * 提取错误响应中的可读信息:网关错误体为 {"error": "..."},
     * 上游混元错误体为 {"error": {"type": "...", "code": "...", "message": "..."}}。
     */
    private fun Response<HunyuanResponse>.errorMessage(): String {
        val raw = runCatching { errorBody()?.string().orEmpty() }.getOrDefault("")
        if (raw.isBlank()) return "Image generation failed: HTTP ${code()}"
        val parsed = runCatching {
            val obj = JsonParser.parseString(raw).asJsonObject
            val error = obj.get("error")
            when {
                error == null || error.isJsonNull -> obj.get("message")?.asString
                error.isJsonPrimitive -> error.asString
                else -> error.asJsonObject.let {
                    val message = it.get("message")?.asString
                    val code = it.get("code")?.asString
                    when {
                        message != null && code != null -> "$code: $message"
                        else -> message ?: code
                    }
                }
            }
        }.getOrNull()
        return parsed ?: raw
    }

    companion object {
        const val PROVIDER_ID = "hunyuan-image"
        const val MODEL_V35 = "hy-image-v3.5-preview"
        private const val MIN_SIDE = 256
        private const val MAX_SIDE = 8192
        private const val MAX_PIXELS = 16_777_216L
    }
}

interface HunyuanImageApi {
    @POST
    suspend fun generateOrEdit(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body request: HunyuanRequest,
    ): Response<HunyuanResponse>
}

// R8 full mode 下字段名会被混淆,Gson 序列化依赖 @SerializedName 固定 JSON key。
// null 字段默认不参与序列化,未传的 size/seed 不会出现在请求体里。
data class HunyuanRequest(
    @SerializedName("model") val model: String,
    @SerializedName("size") val size: String? = null,
    @SerializedName("seed") val seed: Long? = null,
    @SerializedName("messages") val messages: List<HunyuanMessage>,
)

data class HunyuanMessage(
    @SerializedName("role") val role: String = "user",
    @SerializedName("content") val content: List<HunyuanContent>,
)

data class HunyuanContent(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("image_url") val imageUrl: HunyuanImageUrl? = null,
)

data class HunyuanImageUrl(@SerializedName("url") val url: String)

data class HunyuanResponse(
    @SerializedName("choices") val choices: List<HunyuanChoice>? = null,
    @SerializedName("error") val error: HunyuanError? = null,
)

data class HunyuanChoice(
    @SerializedName("delta") val delta: HunyuanDelta? = null,
)

data class HunyuanDelta(
    @SerializedName("image") val image: HunyuanImage? = null,
)

data class HunyuanImage(
    @SerializedName("url") val url: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
)

data class HunyuanError(
    @SerializedName("type") val type: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
)
