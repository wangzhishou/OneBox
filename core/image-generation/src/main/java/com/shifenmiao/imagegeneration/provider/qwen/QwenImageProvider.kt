package com.shifenmiao.imagegeneration.provider.qwen

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
class QwenImageProvider @Inject constructor(
    @Named("DirectImageGenerationApi") private val directApi: QwenImageApi,
    @Named("ProxyImageGenerationApi") private val proxyApi: QwenImageApi,
) : ImageGenerationProvider {

    override val descriptor = ImageProviderDescriptor(
        providerId = PROVIDER_ID,
        displayName = "Alibaba Qwen Image",
        defaultBaseUrl = UrlConstants.Q_WEN_AI_BASE_URL,
        defaultProxyUrl = UrlConstants.RELEASE_URL,
        defaultProxyPath = UrlConstants.ALIBABA_QWEN_IMAGE_PROXY_PATH,
        defaultModel = MODEL_PRO,
        availableModels = listOf(MODEL_PRO, MODEL_STANDARD),
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
        val body = request.toQwenRequest(model)
        val token = config.apiToken.trim()
        val response = if (token.isNotEmpty()) {
            directApi.generateOrEdit(
                url = joinUrl(
                    config.baseUrl.ifBlank { descriptor.defaultBaseUrl },
                    UrlConstants.Q_WEN_IMAGE_GENERATION_ENDPOINT,
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
            ?: error(response.errorBody()?.string().orEmpty().ifBlank { "Image generation failed: HTTP ${response.code()}" })
        responseBody.code?.let { error("$it: ${responseBody.message.orEmpty()}") }
        val images = responseBody.output?.choices.orEmpty()
            .flatMap { it.message?.content.orEmpty() }
            .mapNotNull(QwenContent::image)
            .map(::GeneratedImage)
        require(images.isNotEmpty()) { "Image provider returned no image" }
        ImageGenerationResult(
            images = images,
            providerId = descriptor.providerId,
            model = model,
            requestId = responseBody.requestId,
            width = responseBody.usage?.outputWidth,
            height = responseBody.usage?.outputHeight,
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
        require(request.outputCount in 1..6) { "outputCount must be between 1 and 6" }
        request.seed?.let { require(it >= 0) { "seed must be between 0 and 2147483647" } }
        if (request.inputImages.isNotEmpty()) {
            require(request.promptExtendMode != "agent") {
                "promptExtendMode=agent is not supported for image editing"
            }
        }
        request.outputSize?.let(::validateSize)
    }

    private fun validateSize(size: String) {
        val match = Regex("^(\\d+)\\*(\\d+)$").matchEntire(size)
        requireNotNull(match) { "outputSize must use WIDTH*HEIGHT format" }
        val width = match.groupValues[1].toLongOrNull()
        val height = match.groupValues[2].toLongOrNull()
        require(width != null && height != null && width > 0 && height > 0) { "invalid outputSize" }
        require(width <= Long.MAX_VALUE / height) { "invalid outputSize" }
        require(width * height in 512L * 512L..2048L * 2048L) {
            "outputSize pixel count must be between 512*512 and 2048*2048"
        }
        require(width <= height * 8 && height <= width * 8) {
            "outputSize aspect ratio must be between 1:8 and 8:1"
        }
    }

    private fun ImageGenerationRequest.toQwenRequest(model: String) = QwenRequest(
        model = model,
        input = QwenInput(
            messages = listOf(
                QwenMessage(
                    content = inputImages.map { QwenContent(image = it) } + QwenContent(text = prompt)
                )
            )
        ),
        parameters = QwenParameters(
            promptExtend = promptExtend,
            promptExtendMode = promptExtendMode,
            enableThinking = enableThinking,
            n = outputCount,
            size = outputSize,
            negativePrompt = negativePrompt,
            seed = seed,
            watermark = watermark,
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

    companion object {
        const val PROVIDER_ID = "qwen-image"
        const val MODEL_PRO = "qwen-image-3.0-pro"
        const val MODEL_STANDARD = "qwen-image-3.0"
    }
}

interface QwenImageApi {
    @POST
    suspend fun generateOrEdit(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body request: QwenRequest,
    ): Response<QwenResponse>
}

// R8 full mode 下字段名会被混淆,Gson 序列化依赖 @SerializedName 固定 JSON key,
// 否则 Release 包发出的请求网关解析不到 model("model not allowed: ")
data class QwenRequest(
    @SerializedName("model") val model: String,
    @SerializedName("input") val input: QwenInput,
    @SerializedName("parameters") val parameters: QwenParameters,
)

data class QwenInput(@SerializedName("messages") val messages: List<QwenMessage>)

data class QwenMessage(
    @SerializedName("role") val role: String = "user",
    @SerializedName("content") val content: List<QwenContent>,
)

data class QwenContent(
    @SerializedName("image") val image: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("type") val type: String? = null,
)

data class QwenParameters(
    @SerializedName("prompt_extend") val promptExtend: Boolean,
    @SerializedName("prompt_extend_mode") val promptExtendMode: String,
    @SerializedName("enable_thinking") val enableThinking: Boolean,
    @SerializedName("n") val n: Int,
    @SerializedName("size") val size: String?,
    @SerializedName("negative_prompt") val negativePrompt: String?,
    @SerializedName("seed") val seed: Int?,
    @SerializedName("watermark") val watermark: Boolean,
)

data class QwenResponse(
    @SerializedName("output") val output: QwenOutput? = null,
    @SerializedName("usage") val usage: QwenUsage? = null,
    @SerializedName("request_id") val requestId: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
)

data class QwenOutput(@SerializedName("choices") val choices: List<QwenChoice> = emptyList())

data class QwenChoice(@SerializedName("message") val message: QwenMessage? = null)

data class QwenUsage(
    @SerializedName("output_height") val outputHeight: Int? = null,
    @SerializedName("output_width") val outputWidth: Int? = null,
)
