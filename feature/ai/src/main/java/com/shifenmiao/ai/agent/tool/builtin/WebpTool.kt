package com.shifenmiao.ai.agent.tool.builtin

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
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.feature.webp_tools.domain.WebpParams
import com.t8rin.imagetoolbox.feature.webp_tools.service.WebpService
import javax.inject.Inject

class WebpTool @Inject constructor(
    private val webpService: WebpService,
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "webp_tool"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_webp_tool)

    override val title: String = textProvider.string(R.string.agent_tool_webp_title)

    override val summary: String = textProvider.string(R.string.agent_tool_webp_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_webp_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_webp_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_webp_param_action),
                enum = listOf("extract_frames", "create_webp")
            ),
            "webp_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_webp_param_webp_uri)
            ),
            "image_uris" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_webp_param_image_uris)
            ),
            "image_format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_webp_param_image_format),
                enum = listOf("png", "jpg", "webp")
            ),
            "quality" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_webp_param_quality)
            ),
            "repeat_count" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_webp_param_repeat_count)
            ),
            "delay" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_webp_param_delay)
            ),
            "width" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_webp_param_width)
            ),
            "height" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_webp_param_height)
            )
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) WebpParamsDto() else {
                gson.fromJson(arguments, WebpParamsDto::class.java)
            }
            when (params.action?.trim()) {
                "extract_frames" -> executeExtractFrames(params)
                "create_webp" -> executeCreateWebp(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_webp_invalid_action,
                        params.action.orEmpty()
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_webp_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun executeExtractFrames(params: WebpParamsDto): AgentToolResult {
        val webpUri = params.webp_uri?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_webp_missing_webp_uri),
                isError = true
            )
        val resolvedWebpUri = resolveInputUri(webpUri)
        val imageFormat = parseImageFormat(params.image_format)
        val quality = params.quality?.let { Quality.Base(it) } ?: Quality.Base(100)

        return webpService.extractFrames(
            webpUri = resolvedWebpUri,
            imageFormat = imageFormat,
            quality = quality
        ).fold(
            onSuccess = { result ->
                AgentToolResult(
                    content = gson.toJson(
                        WebpExtractResult(
                            action = "extract_frames",
                            webpUri = webpUri,
                            frameCount = result.frameCount,
                            frameUris = result.frameUris.map { resolveOutputUri(it) }
                        )
                    )
                )
            },
            onFailure = { error ->
                AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_webp_extract_failed,
                        error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                    ),
                    isError = true
                )
            }
        )
    }

    private suspend fun executeCreateWebp(params: WebpParamsDto): AgentToolResult {
        val imageUris = params.image_uris?.filter { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_webp_missing_image_uris),
                isError = true
            )
        if (imageUris.isEmpty()) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_webp_missing_image_uris),
                isError = true
            )
        }
        val resolvedImageUris = imageUris.map { resolveInputUri(it) }

        val webpParams = WebpParams(
            size = if (params.width != null && params.height != null) {
                IntegerSize(params.width, params.height)
            } else null,
            repeatCount = params.repeat_count ?: 1,
            delay = params.delay ?: 1000,
            quality = params.quality?.let { Quality.Base(it) } ?: Quality.Base(100)
        )

        return webpService.createWebp(
            imageUris = resolvedImageUris,
            params = webpParams,
            onProgress = {}
        ).fold(
            onSuccess = { result ->
                AgentToolResult(
                    content = gson.toJson(
                        WebpCreateResult(
                            action = "create_webp",
                            imageCount = imageUris.size,
                            fileSize = result.data.size,
                            suggestedFilename = result.suggestedFilename
                        )
                    )
                )
            },
            onFailure = { error ->
                AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_webp_create_failed,
                        error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                    ),
                    isError = true
                )
            }
        )
    }

    private fun parseImageFormat(format: String?): ImageFormat = when (format?.lowercase()) {
        "jpg", "jpeg" -> ImageFormat.Jpg
        "webp" -> ImageFormat.Webp.Lossless
        else -> ImageFormat.Png.Lossless
    }

    private suspend fun resolveInputUri(uri: String): String {
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }

    private suspend fun resolveOutputUri(uri: String?): String {
        if (uri.isNullOrBlank()) return uri.orEmpty()
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }
}

private data class WebpParamsDto(
    val action: String? = null,
    val webp_uri: String? = null,
    val image_uris: List<String>? = null,
    val image_format: String? = null,
    val quality: Int? = null,
    val repeat_count: Int? = null,
    val delay: Int? = null,
    val width: Int? = null,
    val height: Int? = null
)

private data class WebpExtractResult(
    val action: String,
    val webpUri: String,
    val frameCount: Int,
    val frameUris: List<String>
)

private data class WebpCreateResult(
    val action: String,
    val imageCount: Int,
    val fileSize: Int,
    val suggestedFilename: String
)
