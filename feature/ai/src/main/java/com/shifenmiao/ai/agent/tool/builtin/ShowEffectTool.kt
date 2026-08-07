package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.component.EffectHost
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject

/**
 * 内置工具：触发视觉效果。
 *
 * AI 主动调用此工具触发撒花、炸弹等视觉效果。
 * 轻量工具，不挂起，立即返回。
 */
class ShowEffectTool @Inject constructor(
    private val effectHost: EffectHost,
    private val gson: Gson
) : AgentTool {

    override val name: String = "show_effect"

    override val description: String = """触发视觉特效展示：礼花、爆炸、Toast 提示等动画。

使用场景：
- 用户完成某项任务后给正向反馈
- 庆祝、提醒或警告场景

可用效果：confetti（撒花庆祝）、bomb（炸弹警告）、toast（文字提示，需传 params.message）。"""

    override val title: String = "视觉效果"

    override val summary: String = "触发撒花、炸弹等视觉效果"

    override val category: ToolCategory = ToolCategory.FORM

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "effect" to ToolParameterProperty(
                type = "string",
                description = "效果名称：confetti（撒花）、bomb（炸弹）、toast（文字提示）"
            ),
            "message" to ToolParameterProperty(
                type = "string",
                description = "toast 效果的提示文字（仅 toast 时需要）"
            )
        ),
        required = listOf("effect")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = if (arguments.isBlank()) {
                EffectParams()
            } else {
                gson.fromJson(arguments, EffectParams::class.java)
            }
            val effect = params.effect.orEmpty().ifBlank { "confetti" }
            effectHost.triggerEffect(effect, params.message)
            AgentToolResult(
                content = gson.toJson(
                    mapOf("effect" to effect, "triggered" to true)
                )
            )
        }.getOrElse { e ->
            AgentToolResult(
                content = "效果触发失败: ${e.message}",
                isError = true
            )
        }
    }

    private data class EffectParams(
        val effect: String? = null,
        val message: String? = null
    )
}
