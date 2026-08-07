package com.shifenmiao.ai.agent.tool.builtin.pdf

import androidx.core.net.toUri
import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.feature.pdf_tools.service.PdfConversionService
import com.t8rin.imagetoolbox.feature.pdf_tools.service.PdfToolsService
import com.t8rin.imagetoolbox.feature.pdf_tools.service.model.ImagesToPdfStage
import kotlinx.coroutines.flow.toList
import java.util.UUID
import javax.inject.Inject

class ConvertImagesToPdfTool @Inject constructor(
    private val pdfConversionService: PdfConversionService,
    private val pdfToolsService: PdfToolsService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "convert_images_to_pdf"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_convert_images_to_pdf)

    override val title: String =
        textProvider.string(R.string.agent_tool_convert_images_to_pdf_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_convert_images_to_pdf_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_convert_images_to_pdf_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_convert_images_to_pdf_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "uris" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_convert_images_to_pdf_param_uris)
            ),
            "preset" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_convert_images_to_pdf_param_preset)
            ),
            "scale_small_to_large" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_convert_images_to_pdf_param_scale_small)
            ),
            "filename" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_convert_images_to_pdf_param_filename)
            )
        ),
        required = listOf("uris")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = gson.fromJson(arguments, ConvertImagesToPdfParams::class.java)
            val rawUris = params.uris?.mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
                ?: emptyList()
            if (rawUris.isEmpty()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_convert_images_to_pdf_missing_uris),
                    isError = true
                )
            }

            val invalidScheme = rawUris.firstOrNull { uri ->
                !(uri.startsWith("content://", true) || uri.startsWith("file://", true))
            }
            if (invalidScheme != null) {
                return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_convert_images_to_pdf_invalid_scheme,
                        invalidScheme
                    ),
                    isError = true
                )
            }

            val imageUris = rawUris.map { resolveImageUri(it) }
            val presetValue = (params.preset ?: 100).coerceIn(1, 100)
            val scaleSmall = params.scale_small_to_large ?: false
            val outputFilename = ensurePdfExtension(
                params.filename?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "images_${UUID.randomUUID().toString().take(8)}.pdf"
            )

            val stages = pdfConversionService.convertImagesToPdf(
                imageUris = imageUris,
                scaleSmallImagesToLarge = scaleSmall,
                preset = Preset.Percentage(presetValue),
                tempFilename = outputFilename
            ).toList()

            stages.firstOrNull { it is ImagesToPdfStage.Failed }?.let { failed ->
                val cause = (failed as ImagesToPdfStage.Failed).cause
                return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_convert_images_to_pdf_failed,
                        cause.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                    ),
                    isError = true
                )
            }

            val tempPath = (stages.lastOrNull { it is ImagesToPdfStage.Done } as? ImagesToPdfStage.Done)
                ?.tempPdfPath
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_convert_images_to_pdf_no_output),
                    isError = true
                )

            pdfConversionService.savePdfBytesToDownloads(
                tempPdfPath = tempPath,
                fileName = outputFilename,
                screenRoute = AGENT_TOOL_ROUTE
            ).fold(
                onSuccess = { savedFile ->
                    AgentToolResult(
                        content = gson.toJson(
                            ConvertImagesToPdfResult(
                                page_count = imageUris.size,
                                file_name = savedFile.fileName,
                                file_uri = savedFile.fileUri,
                                save_path = savedFile.savePath
                            )
                        )
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_convert_images_to_pdf_save_failed,
                            error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                        ),
                        isError = true
                    )
                }
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_convert_images_to_pdf_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun resolveImageUri(raw: String): android.net.Uri {
        return pdfToolsService.resolveLocalPdfUri(raw).getOrNull()?.takeIf {
            it.scheme == "file" || it.scheme == "content"
        } ?: raw.toUri()
    }

    private fun ensurePdfExtension(name: String): String {
        return if (name.endsWith(".pdf", ignoreCase = true)) name else "$name.pdf"
    }

    private data class ConvertImagesToPdfParams(
        val uris: List<String?>? = null,
        val preset: Int? = null,
        val scale_small_to_large: Boolean? = null,
        val filename: String? = null
    )

    private data class ConvertImagesToPdfResult(
        val page_count: Int,
        val file_name: String,
        val file_uri: String,
        val save_path: String
    )

    companion object {
        private const val AGENT_TOOL_ROUTE = "agent_tool"
    }
}
