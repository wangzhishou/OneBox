package com.shifenmiao.ai.agent.tool.predicate

import com.shifenmiao.ai.agent.tool.ToolFilterContext
import com.shifenmiao.ai.agent.tool.ToolPredicate
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import javax.inject.Inject

/**
 * 协议维度谓词：按当前引擎的请求协议筛选工具。
 *
 * 云端协议一律放行 —— 保证云端引擎最终工具集与引入谓词链前完全一致；
 * 端侧（LOCAL_ON_DEVICE）为 fail-closed 语义：只放行 [OnDeviceToolAllowlist]
 * 中列出的工具名，名单外一律不可见 —— 包括目录里查不到元数据
 * （catalogItem 为 null）的情况。理由：本地小模型能力有限，拿到跑不了的
 * 联网/登录类工具定义只会浪费本已紧张的上下文预算，并诱发必然失败的调用；
 * 因此端侧宁可误伤（漏放某个本可执行的本地工具），也不放行任何未经验证的工具。
 */
class ProtocolToolPredicate @Inject constructor() : ToolPredicate {

    override suspend fun isToolVisible(
        toolName: String,
        catalogItem: ToolCatalogItem?,
        context: ToolFilterContext
    ): Boolean {
        if (context.protocol != AiRequestProtocol.LOCAL_ON_DEVICE) return true
        return toolName in OnDeviceToolAllowlist.toolNames
    }
}
