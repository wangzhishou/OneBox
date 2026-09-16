package com.shifenmiao.ai.agent.tool.predicate

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.ToolFilterContext
import com.shifenmiao.ai.agent.tool.ToolPredicate
import com.shifenmiao.model.ai.AiRequestProtocol
import javax.inject.Inject

/**
 * 协议维度谓词：按当前引擎的请求协议筛选工具。
 *
 * 云端协议一律放行 —— 保证云端引擎最终工具集与引入谓词链前完全一致；
 * 端侧（LOCAL_ON_DEVICE）只放行 [OnDeviceToolAllowlist] 中的纯本地工具，
 * 避免本地模型拿到无法执行的联网/登录类工具定义。
 */
class ProtocolToolPredicate @Inject constructor() : ToolPredicate {

    override suspend fun isToolVisible(tool: AgentTool, context: ToolFilterContext): Boolean {
        if (context.protocol != AiRequestProtocol.LOCAL_ON_DEVICE) return true
        return tool.name in OnDeviceToolAllowlist.toolNames
    }
}
