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
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageScaleMode
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.feature.weight_resize.domain.WeightImageScaler
import javax.inject.Inject

class CompressImageTool @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageScaler: WeightImageScaler<Bitmap>,
    private val fileController: FileController,
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "compress_image"

    override val description: String = textProvider.string(R.string.agent_tool_compress_image_description)

    override val title: String = textProvider.string(R.string.agent_tool_compress_image_title)

    override val summary: String = textProvider.string(R.string.agent_tool_compress_image_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_compress_image_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_compress_image_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "image_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_compress_image_param_image_uri)
            ),
            "max_bytes" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_compress_image_param_max_bytes)
            ),
            "image_format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_compress_image_param_image_format)
            )
        ),
        required = listOf("image_uri", "max_bytes")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) CompressImageParams() else {
                gson.fromJson(arguments, CompressImageParams::class.java)
            }
            val imageUri = params.image_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_compress_image_missing_image_uri),
                    isError = true
                )
            val maxBytes = params.max_bytes
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_compress_image_missing_max_bytes),
                    isError = true
                )

            val resolvedUri = resolveInputUri(imageUri)
            val imageData = imageGetter.getImage(resolvedUri)
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_compress_image_load_failed),
                    isError = true
                )

            val bitmap = imageData.image
            val imageFormat = ImageFormat[params.image_format]

            val compressed = imageScaler.scaleByMaxBytes(
                image = bitmap,
                imageFormat = imageFormat,
                imageScaleMode = ImageScaleMode.Default,
                maxBytes = maxBytes
            )

            if (compressed == null) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_compress_image_already_small)
                )
            }

            val (compressedData, resultImageInfo) = compressed

            val saveTarget = ImageSaveTarget(
                imageInfo = resultImageInfo,
                originalUri = resolvedUri,
                sequenceNumber = 1,
                data = compressedData
            )

            val saveResult = fileController.save(saveTarget, keepOriginalMetadata = false)

            when (saveResult) {
                is SaveResult.Success -> {
                    val outputUri = resolveOutputUri(saveResult.fileUri)
                    val result = CompressImageResult(
                        output_uri = outputUri,
                        original_width = bitmap.width,
                        original_height = bitmap.height,
                        output_width = resultImageInfo.width,
                        output_height = resultImageInfo.height,
                        output_size_bytes = compressedData.size,
                        target_size_bytes = maxBytes,
                        format = imageFormat.title
                    )
                    AgentToolResult(content = gson.toJson(result))
                }

                is SaveResult.Error -> {
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_compress_image_save_failed,
                            saveResult.throwable.message
                                ?: textProvider.string(R.string.agent_tool_unknown_error)
                        ),
                        isError = true
                    )
                }

                else -> {
                    AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_compress_image_save_failed),
                        isError = true
                    )
                }
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_compress_image_failed,
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

private data class CompressImageParams(
    val image_uri: String? = null,
    val max_bytes: Long? = null,
    val image_format: String? = null
)

private data class CompressImageResult(
    val output_uri: String?,
    val original_width: Int,
    val original_height: Int,
    val output_width: Int,
    val output_height: Int,
    val output_size_bytes: Int,
    val target_size_bytes: Long,
    val format: String
)
