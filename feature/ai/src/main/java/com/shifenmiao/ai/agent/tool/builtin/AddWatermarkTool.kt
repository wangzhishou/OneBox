package com.shifenmiao.ai.agent.tool.builtin

import android.graphics.Bitmap
import android.graphics.Color
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
import com.t8rin.imagetoolbox.core.domain.image.model.BlendingMode
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.model.Position
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import com.t8rin.imagetoolbox.feature.watermarking.domain.TextParams
import com.t8rin.imagetoolbox.feature.watermarking.domain.WatermarkApplier
import com.t8rin.imagetoolbox.feature.watermarking.domain.WatermarkParams
import com.t8rin.imagetoolbox.feature.watermarking.domain.WatermarkingType
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class AddWatermarkTool @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val watermarkApplier: WatermarkApplier<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val fileController: FileController,
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "add_watermark"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_add_watermark)

    override val title: String = textProvider.string(R.string.agent_tool_add_watermark_title)

    override val summary: String = textProvider.string(R.string.agent_tool_add_watermark_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_add_watermark_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_add_watermark_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val executionTimeoutMs: Long = 180_000L

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "image_uris" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_add_watermark_param_image_uris)
            ),
            "text" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_watermark_param_text)
            ),
            "position" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_watermark_param_position),
                enum = listOf(
                    "center", "top_left", "top_right", "bottom_left", "bottom_right",
                    "top_center", "bottom_center", "center_left", "center_right"
                )
            ),
            "alpha" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_add_watermark_param_alpha)
            ),
            "size_ratio" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_add_watermark_param_size_ratio)
            ),
            "color" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_watermark_param_color)
            ),
            "quality" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_add_watermark_param_quality)
            )
        ),
        required = listOf("image_uris", "text")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) AddWatermarkParams() else {
                AppJson.decodeFromString<AddWatermarkParams>(arguments)
            }
            val imageUris = params.image_uris?.filter { it.isNotBlank() }
                ?: return missingImageUrisResult()
            if (imageUris.isEmpty()) return missingImageUrisResult()

            val text = params.text?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_add_watermark_missing_text),
                    isError = true
                )
            val textColor = parseColor(params.color)
                ?: return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_add_watermark_invalid_color,
                        params.color.orEmpty()
                    ),
                    isError = true
                )
            val watermarkParams = WatermarkParams(
                positionX = 0f,
                positionY = 0f,
                rotation = 0,
                alpha = (params.alpha ?: 0.5).toFloat().coerceIn(0f, 1f),
                isRepeated = false,
                overlayMode = BlendingMode.SrcOver,
                watermarkingType = WatermarkingType.Stamp.Text(
                    position = parsePosition(params.position),
                    padding = 20f,
                    params = TextParams(
                        color = textColor,
                        size = (params.size_ratio ?: 0.1).toFloat().coerceIn(0.02f, 0.5f),
                        font = null,
                        backgroundColor = Color.TRANSPARENT
                    ),
                    text = text
                )
            )
            val quality = Quality.Base((params.quality ?: 95).coerceIn(1, 100))

            val watermarked = mutableListOf<WatermarkedImageEntry>()
            val failed = mutableListOf<WatermarkFailedEntry>()
            imageUris.forEach { uri ->
                addWatermarkSingle(uri, watermarkParams, quality)
                    .onSuccess { watermarked.add(it) }
                    .onFailure { error ->
                        failed.add(
                            WatermarkFailedEntry(
                                input_uri = uri,
                                error = error.message
                                    ?: textProvider.string(R.string.agent_tool_unknown_error)
                            )
                        )
                    }
            }

            AgentToolResult(
                content = AppJson.encodeToString(
                    AddWatermarkResult(
                        text = text,
                        success_count = watermarked.size,
                        failed_count = failed.size,
                        watermarked = watermarked,
                        failed = failed
                    )
                ),
                isError = watermarked.isEmpty() && failed.isNotEmpty()
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_watermark_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun addWatermarkSingle(
        imageUri: String,
        watermarkParams: WatermarkParams,
        quality: Quality
    ): Result<WatermarkedImageEntry> = runCatching {
        val resolvedUri = resolveInputUri(imageUri)
        val imageData = imageGetter.getImage(resolvedUri)
            ?: error(textProvider.string(R.string.agent_tool_add_watermark_load_failed))

        val watermarked = watermarkApplier.applyWatermark(
            image = imageData.image,
            originalSize = true,
            params = watermarkParams
        ) ?: error(textProvider.string(R.string.agent_tool_add_watermark_apply_failed))

        // 默认保留原图格式,避免转换带来的质量损失
        val outputFormat = imageData.imageInfo.imageFormat
        val imageInfo = ImageInfo(
            width = watermarked.width,
            height = watermarked.height,
            imageFormat = outputFormat,
            quality = quality
        )
        val data = imageCompressor.compressAndTransform(
            image = watermarked,
            imageInfo = imageInfo
        )
        val saveResult = fileController.save(
            saveTarget = ImageSaveTarget(
                imageInfo = imageInfo,
                originalUri = resolvedUri,
                sequenceNumber = 1,
                data = data
            ),
            keepOriginalMetadata = true
        )
        when (saveResult) {
            is SaveResult.Success -> {
                // 保存成功只写 ActivityLog(不弹 toast/撒花,结果经 AgentToolResult 回对话流)
                SaveSuccessNotifier.logOnly(saveResult)
                WatermarkedImageEntry(
                    input_uri = imageUri,
                    output_uri = resolveOutputUri(saveResult.fileUri),
                    width = watermarked.width,
                    height = watermarked.height,
                    output_size_bytes = data.size
                )
            }

            is SaveResult.Error -> error(
                saveResult.throwable.message
                    ?: textProvider.string(R.string.agent_tool_add_watermark_save_failed)
            )

            else -> error(textProvider.string(R.string.agent_tool_add_watermark_save_failed))
        }
    }

    private fun parsePosition(position: String?): Position = when (position?.trim()?.lowercase()) {
        "center" -> Position.Center
        "top_left" -> Position.TopLeft
        "top_right" -> Position.TopRight
        "bottom_left" -> Position.BottomLeft
        "top_center" -> Position.TopCenter
        "bottom_center" -> Position.BottomCenter
        "center_left" -> Position.CenterLeft
        "center_right" -> Position.CenterRight
        else -> Position.BottomRight
    }

    private fun parseColor(color: String?): Int? {
        val hex = color?.trim()?.takeIf { it.isNotEmpty() } ?: return Color.WHITE
        return runCatching { Color.parseColor(hex) }.getOrNull()
    }

    private fun missingImageUrisResult() = AgentToolResult(
        content = textProvider.string(R.string.agent_tool_add_watermark_missing_image_uris),
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
private data class AddWatermarkParams(
    val image_uris: List<String>? = null,
    val text: String? = null,
    val position: String? = null,
    val alpha: Double? = null,
    val size_ratio: Double? = null,
    val color: String? = null,
    val quality: Int? = null
)

@Serializable
private data class WatermarkedImageEntry(
    val input_uri: String,
    val output_uri: String?,
    val width: Int,
    val height: Int,
    val output_size_bytes: Int
)

@Serializable
private data class WatermarkFailedEntry(
    val input_uri: String,
    val error: String
)

@Serializable
private data class AddWatermarkResult(
    val text: String,
    val success_count: Int,
    val failed_count: Int,
    val watermarked: List<WatermarkedImageEntry>,
    val failed: List<WatermarkFailedEntry>
)
