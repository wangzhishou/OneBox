package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.feature.base64_tools.domain.Base64Converter
import javax.inject.Inject
import kotlin.math.min

class Base64Tool @Inject constructor(
    private val converter: Base64Converter,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "base64_tool"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_base64_tool)

    override val title: String = textProvider.string(R.string.agent_tool_base64_title)

    override val summary: String = textProvider.string(R.string.agent_tool_base64_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_base64_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_base64_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_base64_param_action),
                enum = listOf("encode", "decode")
            ),
            "uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_base64_param_uri)
            ),
            "base64" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_base64_param_base64)
            ),
            "return_full" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_base64_param_return_full)
            ),
            "preview_length" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_base64_param_preview_length)
            )
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) Base64Params() else {
                gson.fromJson(arguments, Base64Params::class.java)
            }
            when (params.action?.trim()) {
                "encode" -> executeEncode(params)
                "decode" -> executeDecode(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_base64_invalid_action,
                        params.action.orEmpty()
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_base64_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun executeEncode(params: Base64Params): AgentToolResult {
        val uri = params.uri?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_base64_missing_uri),
                isError = true
            )
        val encoded = converter.encode(uri)
        val previewLength = min(params.preview_length ?: 256, 2048).coerceAtLeast(32)
        val preview = encoded.take(previewLength)
        return AgentToolResult(
            content = gson.toJson(
                Base64EncodeResult(
                    action = "encode",
                    sourceUri = uri,
                    base64Length = encoded.length,
                    preview = preview,
                    isTruncated = encoded.length > preview.length,
                    base64 = encoded.takeIf { params.return_full == true }
                )
            )
        )
    }

    private suspend fun executeDecode(params: Base64Params): AgentToolResult {
        val base64 = params.base64?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_base64_missing_base64),
                isError = true
            )
        val outputUri = converter.decode(base64)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_base64_decode_failed),
                isError = true
            )
        return AgentToolResult(
            content = gson.toJson(
                Base64DecodeResult(
                    action = "decode",
                    inputLength = base64.length,
                    outputUri = outputUri
                )
            )
        )
    }
}

private data class Base64Params(
    val action: String? = null,
    val uri: String? = null,
    val base64: String? = null,
    val return_full: Boolean? = null,
    val preview_length: Int? = null
)

private data class Base64EncodeResult(
    val action: String,
    val sourceUri: String,
    val base64Length: Int,
    val preview: String,
    val isTruncated: Boolean,
    val base64: String?
)

private data class Base64DecodeResult(
    val action: String,
    val inputLength: Int,
    val outputUri: String
)
