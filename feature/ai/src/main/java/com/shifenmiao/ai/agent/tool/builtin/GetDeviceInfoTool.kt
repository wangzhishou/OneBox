package com.shifenmiao.ai.agent.tool.builtin

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.R
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * 内置工具：获取设备信息。
 *
 * 返回设备型号、系统版本、屏幕分辨率、内存等基本信息，
 * 可用于 Agent 做出设备适配相关建议或故障排查。
 */
class GetDeviceInfoTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "get_device_info"

    override val description: String = textProvider.string(R.string.agent_tool_get_device_info_description)

    override val title: String = textProvider.string(R.string.agent_tool_get_device_info_title)

    override val summary: String = textProvider.string(R.string.agent_tool_get_device_info_summary)

    override val category: ToolCategory = ToolCategory.DEVICE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_get_device_info_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_get_device_info_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val displayMetrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(displayMetrics)

        val runtime = Runtime.getRuntime()
        val maxMemoryMB = runtime.maxMemory() / (1024 * 1024)
        val totalMemoryMB = runtime.totalMemory() / (1024 * 1024)
        val freeMemoryMB = runtime.freeMemory() / (1024 * 1024)

        val result = buildString {
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_device_info_line_model,
                    Build.MANUFACTURER,
                    Build.MODEL
                )
            )
            appendLine(textProvider.string(R.string.agent_tool_get_device_info_line_brand, Build.BRAND))
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_device_info_line_os,
                    Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT
                )
            )
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_device_info_line_resolution,
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels
                )
            )
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_device_info_line_density,
                    displayMetrics.densityDpi,
                    displayMetrics.density.toString()
                )
            )
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_device_info_line_abis,
                    Build.SUPPORTED_ABIS.joinToString(", ")
                )
            )
            appendLine(textProvider.string(R.string.agent_tool_get_device_info_line_jvm_max, maxMemoryMB))
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_device_info_line_jvm_allocated,
                    totalMemoryMB,
                    freeMemoryMB
                )
            )
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_device_info_line_fingerprint,
                    Build.FINGERPRINT
                )
            )
        }

        return AgentToolResult(content = result.trim())
    }
}
