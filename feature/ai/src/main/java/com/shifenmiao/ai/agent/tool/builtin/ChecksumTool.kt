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
import com.t8rin.imagetoolbox.core.domain.model.HashingType
import com.t8rin.imagetoolbox.feature.checksum_tools.domain.ChecksumManager
import com.t8rin.imagetoolbox.feature.checksum_tools.domain.ChecksumSource
import java.util.Locale
import javax.inject.Inject

class ChecksumTool @Inject constructor(
    private val checksumManager: ChecksumManager,
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "checksum_tool"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_checksum_tool)

    override val title: String = textProvider.string(R.string.agent_tool_checksum_title)

    override val summary: String = textProvider.string(R.string.agent_tool_checksum_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_checksum_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_checksum_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_checksum_param_action),
                enum = listOf("calculate", "compare")
            ),
            "algorithm" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_checksum_param_algorithm),
                enum = HashingType.entries.map { it.digest }
            ),
            "source_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_checksum_param_source_type),
                enum = listOf("text", "uri")
            ),
            "text" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_checksum_param_text)
            ),
            "uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_checksum_param_uri)
            ),
            "expected_checksum" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_checksum_param_expected_checksum)
            )
        ),
        required = listOf("action", "algorithm", "source_type")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) ChecksumParams() else {
                gson.fromJson(arguments, ChecksumParams::class.java)
            }
            val action = params.action?.trim().orEmpty()
            val type = resolveAlgorithm(params.algorithm)
                ?: return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_checksum_invalid_algorithm,
                        params.algorithm.orEmpty(),
                        HashingType.entries.joinToString(", ") { it.digest }
                    ),
                    isError = true
                )
            val source = resolveSource(params)

            when (action) {
                "calculate" -> {
                    val value = checksumManager.calculateChecksum(type = type, source = source)
                    AgentToolResult(
                        content = gson.toJson(
                            ChecksumCalculateResult(
                                action = action,
                                algorithm = type.digest,
                                sourceType = params.source_type.orEmpty(),
                                checksum = value
                            )
                        )
                    )
                }

                "compare" -> {
                    val expected = params.expected_checksum?.trim().orEmpty()
                    if (expected.isBlank()) {
                        return AgentToolResult(
                            content = textProvider.string(R.string.agent_tool_checksum_missing_expected_checksum),
                            isError = true
                        )
                    }
                    val actual = checksumManager.calculateChecksum(type = type, source = source)
                    AgentToolResult(
                        content = gson.toJson(
                            ChecksumCompareResult(
                                action = action,
                                algorithm = type.digest,
                                sourceType = params.source_type.orEmpty(),
                                expectedChecksum = expected,
                                actualChecksum = actual,
                                matched = actual.equals(expected, ignoreCase = true)
                            )
                        )
                    )
                }

                else -> AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_checksum_invalid_action, action),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_checksum_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun resolveSource(params: ChecksumParams): ChecksumSource {
        return when (params.source_type?.trim()) {
            "text" -> {
                val text = params.text?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException(
                        textProvider.string(R.string.agent_tool_checksum_missing_text)
                    )
                ChecksumSource.Text(text)
            }

            "uri" -> {
                val uri = params.uri?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException(
                        textProvider.string(R.string.agent_tool_checksum_missing_uri)
                    )
                ChecksumSource.Uri(resolveInputUri(uri))
            }

            else -> throw IllegalArgumentException(
                textProvider.string(
                    R.string.agent_tool_checksum_invalid_source_type,
                    params.source_type.orEmpty()
                )
            )
        }
    }

    private suspend fun resolveInputUri(uri: String): String {
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }

    private fun resolveAlgorithm(value: String?): HashingType? {
        val normalized = value
            ?.trim()
            ?.uppercase(Locale.ROOT)
            ?.replace("_", "-")
            ?: return null
        return HashingType.entries.firstOrNull { type ->
            val digest = type.digest.uppercase(Locale.ROOT).replace("_", "-")
            val name = type.name.uppercase(Locale.ROOT).replace("_", "-")
            val compact = name.replace("-", "")
            normalized == digest || normalized == name || normalized.replace("-", "") == compact
        }
    }
}

private data class ChecksumParams(
    val action: String? = null,
    val algorithm: String? = null,
    val source_type: String? = null,
    val text: String? = null,
    val uri: String? = null,
    val expected_checksum: String? = null
)

private data class ChecksumCalculateResult(
    val action: String,
    val algorithm: String,
    val sourceType: String,
    val checksum: String
)

private data class ChecksumCompareResult(
    val action: String,
    val algorithm: String,
    val sourceType: String,
    val expectedChecksum: String,
    val actualChecksum: String,
    val matched: Boolean
)
