package com.shifenmiao.ai.component

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.ToolCall
import com.shifenmiao.model.ai.Usage
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CancellationException

/**
 * Agent Loop 拦截链 —— 借鉴 Google ADK 的 plugin/callback 设计
 * (beforeModel/afterModel/beforeTool/afterTool/onRunError),
 * 在循环关键节点向外部暴露观察/扩展钩子。
 *
 * 注册方式:Hilt @IntoSet 多绑定(见 di/AgentLoopInterceptorModule),
 * AgentLoopRunner / AgentLoopExecutor 注入 Set<AgentLoopInterceptor> 后按 [priority]
 * 升序逐顺序调用(同 priority 顺序不保证)。
 *
 * 语义约定:
 * - 所有方法默认空实现,实现方按需重写。
 * - 取消(CancellationException)是正常控制流(用户停止/新一轮请求接管),不触发 [onLoopError]。
 * - [beforeToolExecute] 只在 guard(鉴权/表达式校验/登录/权限/确认)全部通过后触发;
 *   guard 拦截产生的失败结果仍会走 [afterToolExecute] —— 那也是"执行结果"的一种。
 */
interface AgentLoopInterceptor {

    /**
     * 调用优先级,小的先调用;同 priority 顺序不保证。
     */
    val priority: Int get() = 0

    /** 循环内每次 LLM 请求发出前(首轮与 follow-up) */
    suspend fun beforeLlmTurn(context: AgentTurnContext) {}

    /** LLM 流正常结束后(usage 可能为空:上游未上报) */
    suspend fun afterLlmTurn(context: AgentTurnContext, usage: Usage?) {}

    /** 单个工具 guard 全部通过、实际执行前(超时计时与重试循环开始之前) */
    suspend fun beforeToolExecute(toolCall: ToolCall, tool: AgentTool?) {}

    /**
     * 单个工具拿到最终结果后 —— 含 guard 拦截/超时/重试耗尽等错误结果,
     * 值为重试循环结束后的最终值(与落库内容一致,早于 Runner 回灌上下文时的截断)。
     * 工具执行被取消时不触发(没有结果可言)。
     */
    suspend fun afterToolExecute(toolCall: ToolCall, result: AgentToolResult) {}

    /** 循环因异常退出时(取消不触发,见类注释) */
    suspend fun onLoopError(context: AgentTurnContext, error: Throwable) {}
}

/**
 * 一次 LLM 回合的上下文快照,字段均为循环内现成可得的信息。
 */
data class AgentTurnContext(
    val conversation: Conversation,
    val conversationId: String,
    val completionId: String,
    val iteration: Int,
)

/**
 * 安全调用拦截器:单个拦截器抛异常只记录日志,绝不影响主循环。
 * 按 [AgentLoopInterceptor.priority] 升序调用(同 priority 顺序不保证)。
 * CancellationException 原样上抛 —— 它属于调用协程自身的取消信号,吞掉会破坏结构化取消。
 */
internal suspend inline fun Set<AgentLoopInterceptor>.forEachInterceptor(
    crossinline action: suspend (AgentLoopInterceptor) -> Unit,
) {
    for (interceptor in this.sortedBy { it.priority }) {
        try {
            action(interceptor)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            "AgentLoopInterceptor ${interceptor.javaClass.simpleName} failed: ${t.message}"
                .makeLog("AgentLoopInterceptor")
        }
    }
}
