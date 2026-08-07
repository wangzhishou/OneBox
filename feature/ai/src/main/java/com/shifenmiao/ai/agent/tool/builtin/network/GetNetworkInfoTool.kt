package com.shifenmiao.ai.agent.tool.builtin.network

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.network.service.NetworkInfoService
import javax.inject.Inject

/**
 * Agent 工具：获取网络状态
 *
 * 获取当前设备的网络连接信息，包括连接类型、信号强度、IP地址等
 */
class GetNetworkInfoTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val networkInfoService: NetworkInfoService
) : AgentTool {

    override val name: String = "get_network_info"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_get_network_info)

    override val title: String =
        textProvider.string(R.string.agent_tool_get_network_info_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_get_network_info_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_get_network_info_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_get_network_info_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return networkInfoService.getNetworkInfo().fold(
            onSuccess = { info ->
                AgentToolResult(
                    content = buildString {
                        appendLine(textProvider.string(R.string.agent_tool_get_network_info_success))
                        appendLine()
                        appendLine("连接状态: ${if (info.isConnected) "已连接" else "未连接"}")
                        appendLine("连接类型: ${info.connectionType.displayName}")

                        info.wifiInfo?.let { wifi ->
                            appendLine()
                            appendLine("=== WiFi 信息 ===")
                            appendLine("SSID: ${wifi.ssid ?: "未知"}")
                            appendLine("信号强度: ${wifi.signalStrength ?: "未知"} dBm")
                            appendLine("连接速度: ${wifi.linkSpeed ?: "未知"} Mbps")
                            wifi.frequency?.let { appendLine("频率: ${it} MHz") }
                            wifi.ipAddress?.let { appendLine("IP地址: ${it}") }
                        }

                        info.mobileInfo?.let { mobile ->
                            appendLine()
                            appendLine("=== 移动网络信息 ===")
                            appendLine("网络类型: ${mobile.networkType ?: "未知"}")
                            appendLine("运营商: ${mobile.carrierName ?: "未知"}")
                            appendLine("漫游: ${if (mobile.isRoaming) "是" else "否"}")
                        }

                        info.ipAddress?.let {
                            appendLine()
                            appendLine("本地IP: $it")
                        }
                    }
                )
            },
            onFailure = { error ->
                AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_get_network_info_failed,
                        error.message ?: "未知错误"
                    ),
                    isError = true
                )
            }
        )
    }
}
