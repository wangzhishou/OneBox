package com.shifenmiao.ai.agent.tool.builtin.visual

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.AttachedMedia
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.visual.automation.service.ScreenshotSnapshot
import com.wanbaohe.visual.automation.service.VisualAutomationService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * screenshot_ui - 截取当前 Activity 屏幕并以多模态方式回传给 LLM。
 *
 * 设计目标:
 * - 让 LLM 在工具结果返回的同一轮就能"看到"截图(走 AgentToolResult.multiModalAttachments)。
 * - tool result 文本只携带元数据(尺寸、KB 大小),避免 base64 撑爆上下文。
 * - 截图走与聊天附件同款的 WebP 压缩管道(1920px / WebP 80 / 1MB 目标体积),
 *   保证大小、压缩比、缩略图、缓存策略与用户手动发送图片时完全一致。
 *
 * 风险等级: SAFE。仅截屏,不修改任何 UI 状态,因此不需要用户确认。
 */
@Singleton
class ScreenshotUiTool @Inject constructor(
    private val service: VisualAutomationService,
    private val textProvider: AgentToolTextProvider,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "screenshot_ui"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_screenshot_ui)

    override val title: String =
        textProvider.string(R.string.agent_tool_screenshot_ui_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_screenshot_ui_summary)

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_screenshot_ui_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_screenshot_ui_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parallelizable: Boolean = true

    override val maxResultLength: Int = 1024

    override val parametersSchema = ToolParameters(
        type = "object",
        properties = mapOf(
            "maxBase64Length" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(
                    R.string.agent_tool_screenshot_ui_param_max_base64_length
                )
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = parseArgs(arguments)
        val maxBase64Length = (params["maxBase64Length"] as? Number)?.toInt()?.coerceAtLeast(0) ?: 0

        val result = service.captureScreenshot(maxBase64Length = maxBase64Length)
        return result.fold(
            onSuccess = { snapshot -> buildSuccess(snapshot, maxBase64Length) },
            onFailure = { e ->
                AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_screenshot_ui_failed,
                        e.message ?: "unknown"
                    ),
                    isError = true,
                )
            },
        )
    }

    private fun buildSuccess(snapshot: ScreenshotSnapshot, maxBase64Length: Int): AgentToolResult {
        val sizeKb = (snapshot.processedSize / 1024).toInt()
        val baseText = textProvider.string(
            R.string.agent_tool_screenshot_ui_success,
            snapshot.screenSize.width,
            snapshot.screenSize.height,
            sizeKb,
        )
        val finalText = if (snapshot.truncated) {
            baseText + "\n" + textProvider.string(
                R.string.agent_tool_screenshot_ui_truncated,
                maxBase64Length,
            )
        } else {
            baseText
        }
        val attachment = AttachedMedia(
            uri = android.net.Uri.EMPTY,
            url = snapshot.dataUri,
            name = "screenshot_${System.currentTimeMillis()}.webp",
            mimeType = snapshot.mimeType,
            localContent = snapshot.base64Webp,
            localPath = snapshot.cachedFilePath,
            isImage = true,
            size = snapshot.processedSize,
        )
        return AgentToolResult(
            content = finalText,
            isError = false,
            multiModalAttachments = listOf(attachment),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseArgs(arguments: String): Map<String, Any?> {
        if (arguments.isBlank() || arguments == "{}") return emptyMap()
        return runCatching { gson.fromJson(arguments, Map::class.java) as Map<String, Any?> }
            .getOrElse { emptyMap() }
    }
}