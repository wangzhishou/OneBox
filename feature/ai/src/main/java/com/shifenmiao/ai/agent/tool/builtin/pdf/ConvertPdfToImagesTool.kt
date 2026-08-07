package com.shifenmiao.ai.agent.tool.builtin.pdf

import android.net.Uri
import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.feature.pdf_tools.service.PdfConversionService
import com.t8rin.imagetoolbox.feature.pdf_tools.service.PdfToolsService
import com.t8rin.imagetoolbox.feature.pdf_tools.service.model.PdfToImagesStage
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

class ConvertPdfToImagesTool @Inject constructor(
    private val pdfToolsService: PdfToolsService,
    private val pdfConversionService: PdfConversionService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "convert_pdf_to_images"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_convert_pdf_to_images)

    override val title: String =
        textProvider.string(R.string.agent_tool_convert_pdf_to_images_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_convert_pdf_to_images_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_convert_pdf_to_images_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_convert_pdf_to_images_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_convert_pdf_to_images_param_uri)
            ),
            "pages" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_convert_pdf_to_images_param_pages)
            ),
            "format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_convert_pdf_to_images_param_format)
            ),
            "quality" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_convert_pdf_to_images_param_quality)
            ),
            "preset" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_convert_pdf_to_images_param_preset)
            )
        ),
        required = listOf("uri")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = gson.fromJson(arguments, ConvertPdfToImagesParams::class.java)
            val rawUri = params.uri?.trim()?.takeIf { it.isNotEmpty() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_convert_pdf_to_images_missing_uri),
                    isError = true
                )

            val localUri = pdfToolsService.resolveLocalPdfUri(rawUri).getOrElse { error ->
                return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_convert_pdf_to_images_resolve_failed,
                        error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                    ),
                    isError = true
                )
            }

            val preflight = pdfConversionService.preflightPdfToImages(localUri, password = null)
                .getOrElse { error ->
                    return AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_convert_pdf_to_images_preflight_failed,
                            error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                        ),
                        isError = true
                    )
                }

            val pageIndices = resolvePageIndices(params.pages, preflight.pageCount)
                ?: return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_convert_pdf_to_images_invalid_pages,
                        preflight.pageCount
                    ),
                    isError = true
                )

            val format = ImageFormat[params.format ?: "PNG"]
            val quality = (params.quality ?: 100).coerceIn(1, 100)
            val presetValue = (params.preset ?: 100).coerceIn(1, 100)

            val imageInfo = ImageInfo(
                quality = Quality.Base(quality),
                imageFormat = format
            )

            val stages = pdfConversionService.convertPdfToImages(
                uri = localUri,
                password = null,
                pages = pageIndices,
                preset = Preset.Percentage(presetValue)
            ).toList()

            stages.firstOrNull { it is PdfToImagesStage.Failed }?.let { failed ->
                val cause = (failed as PdfToImagesStage.Failed).cause
                return AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_convert_pdf_to_images_failed,
                        cause.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                    ),
                    isError = true
                )
            }

            val bitmaps = (stages.lastOrNull { it is PdfToImagesStage.Done } as? PdfToImagesStage.Done)
                ?.bitmaps
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_convert_pdf_to_images_no_bitmaps),
                    isError = true
                )

            val baseName = deriveBaseName(rawUri, localUri)
            val saveResult = pdfConversionService.savePdfImagesToDownloads(
                bitmaps = bitmaps,
                baseName = baseName,
                originalUri = localUri,
                imageInfo = imageInfo,
                preset = Preset.Percentage(presetValue),
                screenRoute = AGENT_TOOL_ROUTE
            )

            saveResult.fold(
                onSuccess = { savedFiles ->
                    AgentToolResult(
                        content = gson.toJson(
                            ConvertPdfToImagesResult(
                                page_count = savedFiles.size,
                                format = format.title,
                                files = savedFiles.map {
                                    SavedImageFile(
                                        file_name = it.fileName,
                                        file_uri = it.fileUri,
                                        save_path = it.savePath
                                    )
                                }
                            )
                        )
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_convert_pdf_to_images_save_failed,
                            error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                        ),
                        isError = true
                    )
                }
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_convert_pdf_to_images_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private fun resolvePageIndices(rawPages: List<Int>?, totalPages: Int): List<Int>? {
        if (rawPages.isNullOrEmpty()) return (0 until totalPages).toList()
        val converted = rawPages.map { input ->
            when {
                input < 0 -> return null
                input == 0 -> 0
                input > totalPages -> return null
                else -> input - 1
            }
        }.toSet().toList().sorted()
        return converted.takeIf { it.isNotEmpty() }
    }

    private fun deriveBaseName(rawInput: String, localUri: Uri): String {
        val candidate = runCatching {
            if (rawInput.startsWith("http", ignoreCase = true)) {
                val path = java.net.URL(rawInput).path
                path.substringAfterLast('/').substringBefore('?')
            } else {
                localUri.lastPathSegment.orEmpty()
            }
        }.getOrNull().orEmpty()
        val withoutQuery = candidate.substringBefore('?')
        val base = withoutQuery.substringBeforeLast('.')
        return base.takeIf { it.isNotBlank() } ?: "pdf_pages"
    }

    private data class ConvertPdfToImagesParams(
        val uri: String? = null,
        val pages: List<Int>? = null,
        val format: String? = null,
        val quality: Int? = null,
        val preset: Int? = null
    )

    private data class ConvertPdfToImagesResult(
        val page_count: Int,
        val format: String,
        val files: List<SavedImageFile>
    )

    private data class SavedImageFile(
        val file_name: String,
        val file_uri: String,
        val save_path: String
    )

    companion object {
        private const val AGENT_TOOL_ROUTE = "agent_tool"
    }
}
