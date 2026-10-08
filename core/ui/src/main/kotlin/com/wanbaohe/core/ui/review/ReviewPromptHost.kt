package com.wanbaohe.core.ui.review

import com.t8rin.imagetoolbox.core.utils.makeLog

/**
 * 应用内评分弹层的全局触发通道(与 flavor 无关)。
 *
 * 功能模块在"用户刚拿到价值"的时刻调用 [notifySuccess];AppActivity 在 onCreate 时绑定
 * [bind] 进来的 handler(内部持有当前 Activity、跑在它的 lifecycleScope),onDestroy 时 [unbind]。
 * 国内渠道绑定到的 InAppReviewPrompt 是空实现,调用即返回,不产生任何开销。
 *
 * 约束:
 * - [notifySuccess] 可能来自任意线程/协程,handler 必须自己保证线程安全(当前实现只做
 *   lifecycleScope.launch,天然满足),并且不得同步碰 Compose state;
 * - 异常一律吞掉只打日志:评分弹层绝不能影响消息落库、游戏结算这类主流程。
 */
object ReviewPromptHost {

    @Volatile
    private var handler: ((ReviewPromptTrigger) -> Unit)? = null

    fun bind(handler: (ReviewPromptTrigger) -> Unit) {
        this.handler = handler
    }

    /**
     * 解绑时做身份校验:极端时序下(新实例 onCreate 早于旧实例 onDestroy),
     * 无条件置空会误清新实例刚绑定进来的 handler。
     */
    fun unbind(handler: (ReviewPromptTrigger) -> Unit) {
        if (this.handler === handler) this.handler = null
    }

    fun notifySuccess(trigger: ReviewPromptTrigger) {
        val current = handler ?: return
        runCatching { current(trigger) }
            .onFailure { it.makeLog("ReviewPromptHost") }
    }
}
