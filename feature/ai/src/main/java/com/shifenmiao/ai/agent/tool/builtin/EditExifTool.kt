package com.shifenmiao.ai.agent.tool.builtin

import android.graphics.Bitmap
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
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.Metadata
import com.t8rin.imagetoolbox.core.domain.image.clearAllAttributes
import com.t8rin.imagetoolbox.core.domain.image.clearAttribute
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.t8rin.imagetoolbox.core.domain.image.toMap
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.ui.utils.helper.SaveSuccessNotifier
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class EditExifTool @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val fileController: FileController,
    private val agentFileService: AgentFileService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "edit_exif"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_edit_exif)

    override val title: String = textProvider.string(R.string.agent_tool_edit_exif_title)

    override val summary: String = textProvider.string(R.string.agent_tool_edit_exif_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_edit_exif_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_edit_exif_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_exif_param_action),
                enum = listOf("read", "clear", "set_tag", "remove_tag")
            ),
            "image_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_exif_param_image_uri)
            ),
            "tag" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_exif_param_tag)
            ),
            "value" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_exif_param_value)
            )
        ),
        required = listOf("action", "image_uri")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) EditExifParams() else {
                AppJson.decodeFromString<EditExifParams>(arguments)
            }
            val imageUri = params.image_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_edit_exif_missing_image_uri),
                    isError = true
                )
            when (params.action?.trim()) {
                "read" -> executeRead(imageUri)
                "clear" -> executeClear(imageUri)
                "set_tag" -> executeSetTag(imageUri, params)
                "remove_tag" -> executeRemoveTag(imageUri, params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_edit_exif_invalid_action,
                        params.action.orEmpty()
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_edit_exif_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun executeRead(imageUri: String): AgentToolResult {
        val resolvedUri = resolveInputUri(imageUri)
        val metadata = fileController.readMetadata(resolvedUri)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_edit_exif_read_failed),
                isError = true
            )
        val attributes = metadata.toMap()
            .mapKeys { it.key.key }
            .toSortedMap()
        return AgentToolResult(
            content = AppJson.encodeToString(
                ExifReadResult(
                    image_uri = imageUri,
                    tag_count = attributes.size,
                    attributes = attributes
                )
            )
        )
    }

    private suspend fun executeClear(imageUri: String): AgentToolResult {
        return saveWithMetadata(imageUri) { it.clearAllAttributes() }
    }

    private suspend fun executeSetTag(imageUri: String, params: EditExifParams): AgentToolResult {
        val tag = resolveTag(params.tag) ?: return unknownTagResult(params.tag)
        val value = params.value
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_edit_exif_missing_value),
                isError = true
            )
        return saveWithMetadata(imageUri) { it.setAttribute(tag, value) }
    }

    private suspend fun executeRemoveTag(
        imageUri: String,
        params: EditExifParams
    ): AgentToolResult {
        val tag = resolveTag(params.tag) ?: return unknownTagResult(params.tag)
        return saveWithMetadata(imageUri) { it.clearAttribute(tag) }
    }

    /**
     * 修改元数据后另存为新文件（不覆盖原图），复用 edit-exif 页面的保存路径。
     */
    private suspend fun saveWithMetadata(
        imageUri: String,
        update: (Metadata) -> Metadata
    ): AgentToolResult {
        val resolvedUri = resolveInputUri(imageUri)
        val imageData = imageGetter.getImage(resolvedUri)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_edit_exif_load_failed),
                isError = true
            )
        val metadata = imageData.metadata?.let(update)

        val saveResult = fileController.save(
            saveTarget = ImageSaveTarget(
                imageInfo = imageData.imageInfo,
                originalUri = resolvedUri,
                sequenceNumber = 1,
                metadata = metadata,
                data = ByteArray(0),
                readFromUriInsteadOfData = true
            ),
            keepOriginalMetadata = false
        )
        return when (saveResult) {
            is SaveResult.Success -> {
                // 保存成功只写 ActivityLog(不弹 toast/撒花,结果经 AgentToolResult 回对话流)
                SaveSuccessNotifier.logOnly(saveResult)
                AgentToolResult(
                    content = AppJson.encodeToString(
                        ExifWriteResult(
                            input_uri = imageUri,
                            output_uri = resolveOutputUri(saveResult.fileUri)
                        )
                    )
                )
            }

            is SaveResult.Error -> AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_edit_exif_save_failed,
                    saveResult.throwable.message
                        ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )

            else -> AgentToolResult(
                content = textProvider.string(R.string.agent_tool_edit_exif_save_failed),
                isError = true
            )
        }
    }

    private fun resolveTag(tag: String?): MetadataTag? {
        val key = tag?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return MetadataTag.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }

    private fun unknownTagResult(tag: String?) = AgentToolResult(
        content = textProvider.string(R.string.agent_tool_edit_exif_unknown_tag, tag.orEmpty()),
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
private data class EditExifParams(
    val action: String? = null,
    val image_uri: String? = null,
    val tag: String? = null,
    val value: String? = null
)

@Serializable
private data class ExifReadResult(
    val image_uri: String,
    val tag_count: Int,
    val attributes: Map<String, String>
)

@Serializable
private data class ExifWriteResult(
    val input_uri: String,
    val output_uri: String?
)
