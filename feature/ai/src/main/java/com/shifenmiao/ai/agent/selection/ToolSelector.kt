package com.shifenmiao.ai.agent.selection

import com.shifenmiao.model.ai.tool.ToolSelectionResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolSelector @Inject constructor() {

    /**
     * 根据模型能力确定是否支持工具调用。
     *
     * 工具可见性由工具目录统一管理，不再做会话级权限筛选。
     */
    fun buildDispatchPlan(
        modelSupportsToolCalls: Boolean,
    ): ToolSelectionResult {
        return ToolSelectionResult
    }
}
