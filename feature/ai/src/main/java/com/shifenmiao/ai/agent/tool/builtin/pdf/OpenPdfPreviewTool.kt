package com.shifenmiao.ai.agent.tool.builtin.pdf

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.callback.ToolCallback
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.InteractiveAgentTool
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.pdf_tools.service.PdfToolsService
import javax.inject.Inject

class OpenPdfPreviewTool @Inject constructor(
    private val pdfToolsService: PdfToolsService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : InteractiveAgentTool {

    override val name: String = "open_pdf_preview"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_open_pdf_preview)

    override val title: String =
        textProvider.string(R.string.agent_tool_open_pdf_preview_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_open_pdf_preview_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_open_pdf_preview_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_open_pdf_preview_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_open_pdf_preview_param_uri)
            )
        ),
        required = listOf("uri")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(R.string.agent_tool_callback_required),
            isError = true
        )
    }

    override suspend fun execute(arguments: String, callback: ToolCallback): AgentToolResult {
        return runCatching {
            val params = gson.fromJson(arguments, OpenPdfPreviewParams::class.java)
            val rawUri = params.uri?.trim()?.takeIf { it.isNotEmpty() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_open_pdf_preview_missing_uri),
                    isError = true
                )

            pdfToolsService.resolveLocalPdfUri(rawUri).fold(
                onSuccess = { localUri ->
                    callback.openScreen(
                        Screen.PdfTools(Screen.PdfTools.Type.Preview(pdfUri = localUri))
                    )
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_open_pdf_preview_success,
                            localUri.toString()
                        )
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_open_pdf_preview_failed,
                            error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                        ),
                        isError = true
                    )
                }
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_open_pdf_preview_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private data class OpenPdfPreviewParams(
        val uri: String? = null
    )
}
