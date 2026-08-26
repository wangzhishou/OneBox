package com.shifenmiao.model.ai.image

import com.google.gson.annotations.SerializedName

/**
 * 千问图像生成与编辑 3.0 的原生 DashScope 请求。
 *
 * 同一个结构同时支持文生图和图像编辑：不传 [QwenImageContent.image] 为文生图，
 * 传入 1-3 张图片（公网 URL 或 data URI）为图像编辑。该结构可原样发送给后端代理。
 */
data class QwenImageRequest(
    val model: String = MODEL_PRO,
    val input: QwenImageInput,
    val parameters: QwenImageParameters = QwenImageParameters(),
) {
    fun validate() {
        require(model.isNotBlank()) { "model must not be blank" }
        require(input.messages.size == 1) { "Qwen Image 3.0 supports exactly one message" }
        val message = input.messages.single()
        require(message.role == "user") { "message role must be user" }
        val texts = message.content.filter { it.text != null }
        val images = message.content.filter { it.image != null }
        require(texts.size == 1 && texts.single().text?.isNotBlank() == true) {
            "message content must contain exactly one non-blank text"
        }
        require(images.size <= MAX_INPUT_IMAGES) { "at most $MAX_INPUT_IMAGES input images are supported" }
        require(message.content.all { (it.text == null) xor (it.image == null) }) {
            "each content item must contain either text or image"
        }
        require(parameters.n in 1..MAX_OUTPUT_IMAGES) { "n must be between 1 and $MAX_OUTPUT_IMAGES" }
        parameters.seed?.let { require(it in 0..Int.MAX_VALUE) { "seed must be between 0 and 2147483647" } }
        parameters.size?.let(::validateSize)
        if (images.isNotEmpty()) {
            require(parameters.promptExtendMode != "agent") {
                "prompt_extend_mode=agent is not supported for image editing"
            }
        }
    }

    companion object {
        const val MODEL_PRO = "qwen-image-3.0-pro"
        const val MODEL_STANDARD = "qwen-image-3.0"
        const val MAX_INPUT_IMAGES = 3
        const val MAX_OUTPUT_IMAGES = 6

        fun create(
            prompt: String,
            images: List<String> = emptyList(),
            model: String = MODEL_PRO,
            parameters: QwenImageParameters = QwenImageParameters(),
        ): QwenImageRequest = QwenImageRequest(
            model = model,
            input = QwenImageInput(
                messages = listOf(
                    QwenImageMessage(
                        content = images.map { QwenImageContent(image = it) } +
                            QwenImageContent(text = prompt)
                    )
                )
            ),
            parameters = parameters,
        ).also(QwenImageRequest::validate)

        private fun validateSize(size: String) {
            val match = Regex("^(\\d+)\\*(\\d+)$").matchEntire(size)
            requireNotNull(match) { "size must use WIDTH*HEIGHT format" }
            val (width, height) = match.destructured
            val w = width.toLong()
            val h = height.toLong()
            val pixels = w * h
            require(pixels in 512L * 512L..2048L * 2048L) {
                "size pixel count must be between 512*512 and 2048*2048"
            }
            require(w <= h * 8 && h <= w * 8) { "size aspect ratio must be between 1:8 and 8:1" }
        }
    }
}

data class QwenImageInput(
    val messages: List<QwenImageMessage>,
)

data class QwenImageMessage(
    val role: String = "user",
    val content: List<QwenImageContent>,
)

data class QwenImageContent(
    val image: String? = null,
    val text: String? = null,
    val type: String? = null,
)

data class QwenImageParameters(
    @SerializedName("prompt_extend")
    val promptExtend: Boolean = true,
    @SerializedName("prompt_extend_mode")
    val promptExtendMode: String = "direct",
    @SerializedName("enable_thinking")
    val enableThinking: Boolean = true,
    val n: Int = 1,
    val size: String? = null,
    @SerializedName("negative_prompt")
    val negativePrompt: String? = null,
    val seed: Int? = null,
    val watermark: Boolean = false,
)

/** DashScope 同步响应；错误响应也会通过 [code]、[message] 保持原样。 */
data class QwenImageResponse(
    val output: QwenImageOutput? = null,
    val usage: QwenImageUsage? = null,
    @SerializedName("request_id")
    val requestId: String? = null,
    val code: String? = null,
    val message: String? = null,
) {
    val imageUrls: List<String>
        get() = output?.choices.orEmpty()
            .flatMap { it.message?.content.orEmpty() }
            .mapNotNull(QwenImageContent::image)
}

data class QwenImageOutput(
    @SerializedName("rewrite_status")
    val rewriteStatus: String? = null,
    val choices: List<QwenImageChoice> = emptyList(),
)

data class QwenImageChoice(
    @SerializedName("finish_reason")
    val finishReason: String? = null,
    val message: QwenImageMessage? = null,
)

data class QwenImageUsage(
    @SerializedName("output_height")
    val outputHeight: Int? = null,
    @SerializedName("output_width")
    val outputWidth: Int? = null,
    @SerializedName("input_image_count")
    val inputImageCount: Int? = null,
    @SerializedName("input_image_type")
    val inputImageType: String? = null,
    @SerializedName("output_image_count")
    val outputImageCount: Int? = null,
    @SerializedName("output_image_type")
    val outputImageType: String? = null,
)
