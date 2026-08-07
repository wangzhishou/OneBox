package com.shifenmiao.ai.agent.tool.builtin

import android.graphics.Bitmap
import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.ImageScaleMode
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.feature.limits_resize.domain.LimitsImageScaler
import com.t8rin.imagetoolbox.feature.limits_resize.domain.LimitsResizeType
import javax.inject.Inject

class LimitResizeTool @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageScaler: LimitsImageScaler<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val fileController: FileController,
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "limit_resize_image"

    override val description: String = textProvider.string(R.string.agent_tool_limit_resize_description)

    override val title: String = textProvider.string(R.string.agent_tool_limit_resize_title)

    override val summary: String = textProvider.string(R.string.agent_tool_limit_resize_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_limit_resize_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_limit_resize_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "image_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_limit_resize_param_image_uri)
            ),
            "max_width" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_limit_resize_param_max_width)
            ),
            "max_height" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_limit_resize_param_max_height)
            ),
            "resize_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_limit_resize_param_resize_type),
                enum = listOf("skip", "recode", "zoom")
            ),
            "image_format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_limit_resize_param_image_format)
            ),
            "quality" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_limit_resize_param_quality)
            )
        ),
        required = listOf("image_uri")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) LimitResizeParams() else {
                gson.fromJson(arguments, LimitResizeParams::class.java)
            }
            val imageUri = params.image_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_limit_resize_missing_image_uri),
                    isError = true
                )

            val resolvedUri = resolveInputUri(imageUri)
            val imageData = imageGetter.getImage(resolvedUri)
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_limit_resize_load_failed),
                    isError = true
                )

            val bitmap = imageData.image
            val imageFormat = ImageFormat[params.image_format]
            val quality = Quality.Base((params.quality ?: 90).coerceIn(1, 100))

            val resizeType = when (params.resize_type?.lowercase()) {
                "skip" -> LimitsResizeType.Skip()
                "zoom" -> LimitsResizeType.Zoom()
                else -> LimitsResizeType.Recode()
            }

            val maxWidth = params.max_width ?: 0
            val maxHeight = params.max_height ?: 0

            val scaledBitmap = imageScaler.scaleImage(
                image = bitmap,
                width = maxWidth,
                height = maxHeight,
                resizeType = resizeType,
                imageScaleMode = ImageScaleMode.Default
            )

            if (scaledBitmap == null && resizeType is LimitsResizeType.Skip) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_limit_resize_skipped)
                )
            }

            val resultBitmap = scaledBitmap ?: bitmap

            val imageInfo = ImageInfo(
                width = resultBitmap.width,
                height = resultBitmap.height,
                imageFormat = imageFormat,
                quality = quality
            )

            val compressedData = imageCompressor.compressAndTransform(
                image = resultBitmap,
                imageInfo = imageInfo
            )

            val saveTarget = ImageSaveTarget(
                imageInfo = imageInfo,
                originalUri = resolvedUri,
                sequenceNumber = 1,
                data = compressedData
            )

            val saveResult = fileController.save(saveTarget, keepOriginalMetadata = false)

            when (saveResult) {
                is SaveResult.Success -> {
                    val outputUri = resolveOutputUri(saveResult.fileUri)
                    val result = LimitResizeResult(
                        output_uri = outputUri,
                        original_width = bitmap.width,
                        original_height = bitmap.height,
                        output_width = resultBitmap.width,
                        output_height = resultBitmap.height,
                        resize_type = params.resize_type ?: "recode",
                        format = imageFormat.title,
                        quality = quality.qualityValue
                    )
                    AgentToolResult(content = gson.toJson(result))
                }

                is SaveResult.Error -> {
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_limit_resize_save_failed,
                            saveResult.throwable.message
                                ?: textProvider.string(R.string.agent_tool_unknown_error)
                        ),
                        isError = true
                    )
                }

                else -> {
                    AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_limit_resize_save_failed),
                        isError = true
                    )
                }
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_limit_resize_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun resolveInputUri(uri: String): String {
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }

    private suspend fun resolveOutputUri(uri: String?): String? {
        if (uri.isNullOrBlank()) return uri
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }
}

private data class LimitResizeParams(
    val image_uri: String? = null,
    val max_width: Int? = null,
    val max_height: Int? = null,
    val resize_type: String? = null,
    val image_format: String? = null,
    val quality: Int? = null
)

private data class LimitResizeResult(
    val output_uri: String?,
    val original_width: Int,
    val original_height: Int,
    val output_width: Int,
    val output_height: Int,
    val resize_type: String,
    val format: String,
    val quality: Int
)
