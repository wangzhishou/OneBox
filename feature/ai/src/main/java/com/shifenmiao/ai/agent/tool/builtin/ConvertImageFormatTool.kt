package com.shifenmiao.ai.agent.tool.builtin

import android.graphics.Bitmap
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.file.AgentFileService
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class ConvertImageFormatTool @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val fileController: FileController,
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "convert_image_format"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_convert_image_format)

    override val title: String = textProvider.string(R.string.agent_tool_convert_format_title)

    override val summary: String = textProvider.string(R.string.agent_tool_convert_format_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_convert_format_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_convert_format_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val executionTimeoutMs: Long = 180_000L

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "image_uris" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_convert_format_param_image_uris)
            ),
            "target_format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_convert_format_param_target_format),
                enum = listOf("png", "jpg", "jpeg", "webp", "webp_lossy", "bmp")
            ),
            "quality" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_convert_format_param_quality)
            ),
            "keep_exif" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_convert_format_param_keep_exif)
            )
        ),
        required = listOf("image_uris", "target_format")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) ConvertFormatParams() else {
                AppJson.decodeFromString<ConvertFormatParams>(arguments)
            }
            val imageUris = params.image_uris?.filter { it.isNotBlank() }
                ?: return missingImageUrisResult()
            if (imageUris.isEmpty()) return missingImageUrisResult()

            val targetFormat = parseTargetFormat(params.target_format)
                ?: return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_convert_format_invalid_format,
                        params.target_format.orEmpty()
                    ),
                    isError = true
                )
            val quality = Quality.Base((params.quality ?: 90).coerceIn(1, 100))
            val keepExif = params.keep_exif == true

            val converted = mutableListOf<ConvertedImageEntry>()
            val failed = mutableListOf<FailedImageEntry>()
            imageUris.forEach { uri ->
                convertSingle(uri, targetFormat, quality, keepExif)
                    .onSuccess { converted.add(it) }
                    .onFailure { error ->
                        failed.add(
                            FailedImageEntry(
                                input_uri = uri,
                                error = error.message
                                    ?: textProvider.string(R.string.agent_tool_unknown_error)
                            )
                        )
                    }
            }

            AgentToolResult(
                content = AppJson.encodeToString(
                    ConvertFormatResult(
                        target_format = targetFormat.title,
                        success_count = converted.size,
                        failed_count = failed.size,
                        converted = converted,
                        failed = failed
                    )
                ),
                isError = converted.isEmpty() && failed.isNotEmpty()
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_convert_format_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun convertSingle(
        imageUri: String,
        targetFormat: ImageFormat,
        quality: Quality,
        keepExif: Boolean
    ): Result<ConvertedImageEntry> = runCatching {
        val resolvedUri = resolveInputUri(imageUri)
        val imageData = imageGetter.getImage(resolvedUri)
            ?: error(textProvider.string(R.string.agent_tool_convert_format_load_failed))

        val bitmap = imageData.image
        val imageInfo = ImageInfo(
            width = bitmap.width,
            height = bitmap.height,
            imageFormat = targetFormat,
            quality = quality
        )
        val data = imageCompressor.compressAndTransform(
            image = bitmap,
            imageInfo = imageInfo
        )
        val saveResult = fileController.save(
            saveTarget = ImageSaveTarget(
                imageInfo = imageInfo,
                originalUri = resolvedUri,
                sequenceNumber = 1,
                data = data
            ),
            keepOriginalMetadata = keepExif
        )
        when (saveResult) {
            is SaveResult.Success -> {
                // 保存成功只写 ActivityLog(不弹 toast/撒花,结果经 AgentToolResult 回对话流)
                SaveSuccessNotifier.logOnly(saveResult)
                ConvertedImageEntry(
                    input_uri = imageUri,
                    output_uri = resolveOutputUri(saveResult.fileUri),
                    width = bitmap.width,
                    height = bitmap.height,
                    output_size_bytes = data.size
                )
            }

            is SaveResult.Error -> error(
                saveResult.throwable.message
                    ?: textProvider.string(R.string.agent_tool_convert_format_save_failed)
            )

            else -> error(textProvider.string(R.string.agent_tool_convert_format_save_failed))
        }
    }

    private fun parseTargetFormat(format: String?): ImageFormat? =
        when (format?.trim()?.lowercase()) {
            "png" -> ImageFormat.Png.Lossless
            "jpg" -> ImageFormat.Jpg
            "jpeg" -> ImageFormat.Jpeg
            "webp" -> ImageFormat.Webp.Lossless
            "webp_lossy" -> ImageFormat.Webp.Lossy
            "bmp" -> ImageFormat.Bmp
            else -> null
        }

    private fun missingImageUrisResult() = AgentToolResult(
        content = textProvider.string(R.string.agent_tool_convert_format_missing_image_uris),
        isError = true
    )

    private suspend fun resolveInputUri(uri: String): String {
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }

    private suspend fun resolveOutputUri(uri: String?): String? {
        if (uri.isNullOrBlank()) return uri
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }
}

@Serializable
private data class ConvertFormatParams(
    val image_uris: List<String>? = null,
    val target_format: String? = null,
    val quality: Int? = null,
    val keep_exif: Boolean? = null
)

@Serializable
private data class ConvertedImageEntry(
    val input_uri: String,
    val output_uri: String?,
    val width: Int,
    val height: Int,
    val output_size_bytes: Int
)

@Serializable
private data class FailedImageEntry(
    val input_uri: String,
    val error: String
)

@Serializable
private data class ConvertFormatResult(
    val target_format: String,
    val success_count: Int,
    val failed_count: Int,
    val converted: List<ConvertedImageEntry>,
    val failed: List<FailedImageEntry>
)
