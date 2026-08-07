package com.shifenmiao.ai.agent.callback

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenLifecycleEvent
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.random.Random

/**
 * 工具回调路由器 - 会话级实例
 *
 * 每个 AIChatComponent 持有一个独立的 Router 实例，
 * 支持：
 * 1. 嵌套工具调用（工具 A → 工具 B → 回调 → A 继续）
 * 2. Screen 导航并等待结果
 * 3. 多个并发回调
 *
 * 生命周期：与 AIChatComponent 绑定，组件销毁时自动清理
 */
class ToolCallbackRouter(
    private val conversationId: String,
    private val componentContext: ComponentContext
) : ToolCallback {

    /** 待处理的回调映射：callbackId → CompletableDeferred */
    private val pendingCallbacks = mutableMapOf<String, CompletableDeferred<CallbackResult>>()

    /** 生命周期轨迹：callbackId -> 已发生的 Screen 事件 */
    private val lifecycleTraceMap = mutableMapOf<String, MutableList<ScreenLifecycleEvent>>()

    /** 导航请求事件流，由 AIChatComponent 观察并转成队列 */
    private val _navigationRequests = MutableSharedFlow<NavigationRequest>(extraBufferCapacity = 32)
    val navigationRequests: SharedFlow<NavigationRequest> = _navigationRequests

    init {
        componentContext.lifecycle.doOnDestroy {
            cancelAll()
        }
    }

    /**
     * 调用子工具并等待结果
     *
     * 使用流程：
     * 1. 生成唯一的 callbackId
     * 2. 创建 CompletableDeferred 并存入 pendingCallbacks
     * 3. 通过 StateFlow 通知 AIChatComponent 执行子工具
     * 4. 挂起等待子工具完成
     * 5. 子工具完成后，通过 completeCallback 恢复挂起的协程
     */
    override suspend fun callTool(toolName: String, arguments: String): CallbackResult {
        val callbackId = generateCallbackId()
        val deferred = CompletableDeferred<CallbackResult>()
        pendingCallbacks[callbackId] = deferred

        "callTool: $toolName, callbackId=$callbackId".makeLog(TAG)

        // 通知 AIChatComponent 执行子工具
        _navigationRequests.emit(
            NavigationRequest.ToolCall(
            callbackId = callbackId,
            toolName = toolName,
            arguments = arguments
            )
        )

        return try {
            deferred.await()
        } finally {
            pendingCallbacks.remove(callbackId)
        }
    }

    /**
     * 导航到 Screen 并等待用户操作结果
     *
     * 使用流程：
     * 1. 生成唯一的 callbackId
     * 2. 创建 CompletableDeferred 并存入 pendingCallbacks
     * 3. 调用 screenBuilder 构建 Screen，传入 onResult lambda
     * 4. 通过 StateFlow 通知 AIChatComponent 导航
     * 5. 挂起等待用户操作
     * 6. 用户操作完成后，onResult 被调用，通过 completeCallback 恢复挂起的协程
     */
    override suspend fun <T> navigateToScreen(
        screenBuilder: (onResult: (T) -> Unit) -> Screen
    ): T? {
        val callbackId = generateCallbackId()
        val deferred = CompletableDeferred<CallbackResult>()
        pendingCallbacks[callbackId] = deferred
        lifecycleTraceMap[callbackId] = mutableListOf()

        // 构建 Screen，注入 onResult 回调
        val screen = screenBuilder { result ->
            if (result is ScreenCallbackResult) {
                val trace = lifecycleTraceMap.getOrPut(callbackId) { mutableListOf() }
                trace += ScreenLifecycleEvent(
                    status = result.status,
                    message = result.message
                )
                val enriched = result.copy(lifecycleTrace = trace.toList())
                if (enriched.isTerminal) {
                    completeCallback(callbackId, CallbackResult.success(enriched))
                }
            } else {
                completeCallback(callbackId, CallbackResult.success(result))
            }
        }

        "navigateToScreen: ${screen.simpleName}, callbackId=$callbackId".makeLog(TAG)

        // 通知 AIChatComponent 导航
        _navigationRequests.emit(
            NavigationRequest.ScreenNavigation(
            callbackId = callbackId,
            screen = screen
            )
        )

        return try {
            val result = deferred.await()
            @Suppress("UNCHECKED_CAST")
            result.data as? T
        } catch (e: Exception) {
            "navigateToScreen cancelled: ${e.message}".makeLog(TAG)
            null
        } finally {
            pendingCallbacks.remove(callbackId)
            lifecycleTraceMap.remove(callbackId)
        }
    }

    override fun openScreen(screen: Screen) {
        val requestId = generateCallbackId()
        "openScreen: ${screen.simpleName}, requestId=$requestId".makeLog(TAG)
        _navigationRequests.tryEmit(
            NavigationRequest.OpenScreen(
                requestId = requestId,
                screen = screen
            )
        )
    }

    /**
     * 完成回调 - 由子工具执行完成或 Screen 操作完成时调用
     */
    override fun completeCallback(callbackId: String, result: CallbackResult) {
        "completeCallback: callbackId=$callbackId, success=${result.isSuccess}".makeLog(TAG)
        pendingCallbacks[callbackId]?.complete(result)
            ?: "completeCallback: callbackId=$callbackId not found".makeLog(TAG)
    }

    /**
     * 取消所有待处理的回调
     */
    override fun cancelAll() {
        "cancelAll: ${pendingCallbacks.size} callbacks".makeLog(TAG)
        pendingCallbacks.values.forEach {
            it.complete(CallbackResult.error("cancelled"))
        }
        pendingCallbacks.clear()
        lifecycleTraceMap.clear()
    }

    /**
     * 取消指定的回调
     */
    fun cancelCallback(callbackId: String, reason: String = "cancelled") {
        pendingCallbacks[callbackId]?.complete(CallbackResult.error(reason))
        pendingCallbacks.remove(callbackId)
    }

    private fun generateCallbackId(): String =
        "cb_${conversationId}_${System.currentTimeMillis()}_${Random.nextInt(10000)}"

    companion object {
        private const val TAG = "ToolCallbackRouter"
    }
}
