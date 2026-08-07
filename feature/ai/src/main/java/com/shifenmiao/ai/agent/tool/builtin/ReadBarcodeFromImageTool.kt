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
import com.t8rin.imagetoolbox.core.domain.model.QrType
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ImageBarcodeReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ReadBarcodeFromImageTool @Inject constructor(
    private val imageBarcodeReader: ImageBarcodeReader,
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "read_barcode_from_image"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_read_barcode_from_image)

    override val title: String = textProvider.string(R.string.agent_tool_read_barcode_title)

    override val summary: String = textProvider.string(R.string.agent_tool_read_barcode_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_read_barcode_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_read_barcode_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "image_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_read_barcode_param_image_uri)
            )
        ),
        required = listOf("image_uri")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) ReadBarcodeFromImageParams() else {
                gson.fromJson(arguments, ReadBarcodeFromImageParams::class.java)
            }
            val imageUri = params.image_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_read_barcode_missing_image_uri),
                    isError = true
                )
            val resolvedUri = resolveInputUri(imageUri)

            imageBarcodeReader.readBarcode(resolvedUri).fold(
                onSuccess = { qrType ->
                    AgentToolResult(
                        content = gson.toJson(
                            BarcodeReadResult(
                                type = toTypeName(qrType),
                                raw = qrType.raw,
                                structured = qrType.toStructuredData()
                            )
                        )
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_read_barcode_failed,
                            error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                        ),
                        isError = true
                    )
                }
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_read_barcode_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun resolveInputUri(uri: String): String {
        return agentFileService.resolveContentUriToFile(uri) ?: uri
    }

    private fun toTypeName(qrType: QrType): String = when (qrType) {
        is QrType.Plain -> "plain"
        is QrType.Url -> "url"
        is QrType.Wifi -> "wifi"
        is QrType.Sms -> "sms"
        is QrType.Geo -> "geo"
        is QrType.Email -> "email"
        is QrType.Phone -> "phone"
        is QrType.Contact -> "contact"
        is QrType.Calendar -> "calendar"
    }

    private fun QrType.toStructuredData(): Map<String, Any?> = when (this) {
        is QrType.Plain -> mapOf("text" to raw)
        is QrType.Url -> mapOf("title" to title, "url" to url)
        is QrType.Wifi -> mapOf(
            "ssid" to ssid,
            "password" to password,
            "encryptionType" to encryptionType.name
        )
        is QrType.Sms -> mapOf("phoneNumber" to phoneNumber, "message" to message)
        is QrType.Geo -> mapOf("latitude" to latitude, "longitude" to longitude)
        is QrType.Email -> mapOf(
            "address" to address,
            "subject" to subject,
            "body" to body,
            "type" to type
        )
        is QrType.Phone -> mapOf("number" to number, "type" to type)
        is QrType.Contact -> mapOf(
            "organization" to organization,
            "title" to title,
            "urls" to urls,
            "name" to mapOf(
                "formattedName" to name.formattedName,
                "first" to name.first,
                "last" to name.last,
                "middle" to name.middle,
                "prefix" to name.prefix,
                "suffix" to name.suffix
            ),
            "emails" to emails.map {
                mapOf(
                    "address" to it.address,
                    "subject" to it.subject,
                    "body" to it.body,
                    "type" to it.type
                )
            },
            "phones" to phones.map { mapOf("number" to it.number, "type" to it.type) },
            "addresses" to addresses.map { mapOf("lines" to it.addressLines, "type" to it.type) }
        )
        is QrType.Calendar -> mapOf(
            "summary" to summary,
            "description" to description,
            "location" to location,
            "organizer" to organizer,
            "status" to status,
            "start" to start.toIso(),
            "end" to end.toIso()
        )
    }

    private fun Date?.toIso(): String? {
        if (this == null) return null
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(this)
    }
}

private data class ReadBarcodeFromImageParams(
    val image_uri: String? = null
)

private data class BarcodeReadResult(
    val type: String,
    val raw: String,
    val structured: Map<String, Any?>
)
