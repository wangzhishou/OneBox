package com.wanbaohe.core.ui.review

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.t8rin.imagetoolbox.core.di.entryPoint
import com.t8rin.imagetoolbox.core.settings.di.SettingsStateEntryPoint
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.utils.makeLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Google Play 应用内评分(In-App Review),google 渠道专用实现。
 *
 * 弹出的是 Play 托管的半屏评分层,不离开 App。注意:
 * Play 对该弹层有频率配额(约每月一次),配额耗尽时 launchReviewFlow 静默不弹、
 * API 不回调结果,所以这里记的是"发起过尝试",不代表用户真的看到了弹层;
 * 冷却时间因此与 Play 配额量级对齐,偶尔烧掉的尝试不算浪费。
 *
 * 触发时机:各类"成功时刻"经 [ReviewPromptHost.notifySuccess] 汇总到这里,由
 * AppActivity 绑定 Activity 后调用。门控对触发场景一视同仁——累计成功时刻达标、
 * 未超每安装上限、且过了冷却期,才向 Play 发起弹层。
 * 计数与冷却状态统一维护在全局设置 DataStore。
 */
object InAppReviewPrompt {

    /** 累计成功时刻(保存成功/AI 回答成功/小游戏通关)达到该次数后才考虑弹评分层 */
    private const val PROMPT_AFTER_MOMENTS = 3

    /** 每个安装最多发起弹出的次数 */
    private const val MAX_PROMPTS = 3

    /** 两次弹出之间的最短间隔,与 Play 配额量级对齐 */
    private val PROMPT_COOLDOWN_MS = TimeUnit.DAYS.toMillis(30)

    /** 成功提示/结算动画之后稍作停顿,避免评分弹层紧跟其后出现显得突兀 */
    private const val PROMPT_DELAY_MS = 1200L

    /** 等自定义 Toast 消失的最长时间:超时就不再等,免得用户已经切到别的页面 */
    private const val MAX_TOAST_WAIT_MS = 5_000L

    /** Toast 等待的轮询间隔 */
    private const val TOAST_POLL_MS = 200L

    /** 防止短时间内的多个成功时刻并发触发重复弹层 */
    private val promptInFlight = AtomicBoolean(false)

    /**
     * 成功时刻入口:先累计一次计数,弹出次数与冷却时间都满足才向 Play 发起弹层。
     * 整体 runCatching 保护:评分弹层失败绝不能影响保存、落库、游戏结算等主流程。
     *
     * @param trigger 触发场景,仅用于日志排查
     */
    fun maybePrompt(activity: ComponentActivity, trigger: ReviewPromptTrigger) {
        activity.lifecycleScope.launch {
            runCatching {
                var settingsManager: SettingsManager? = null
                activity.entryPoint<SettingsStateEntryPoint> {
                    settingsManager = this.settingsManager
                }
                val settings = settingsManager ?: return@runCatching

                settings.registerSuccessfulMoment()

                if (!promptInFlight.compareAndSet(false, true)) return@runCatching
                try {
                    // 门控判断全部落一行日志:Play 侧弹不弹永远无回调,能观测的只有"我们为什么没发起"
                    // 和"发起时 API 是成功还是失败"这两件事(logcat tag: InAppReviewPrompt)
                    val moments = settings.getSuccessfulMomentCount()
                    val launchedCount = settings.getInAppReviewPromptCount()
                    val lastPromptAt = settings.getInAppReviewLastPromptAt()
                    val cooldownLeftMs = (lastPromptAt + PROMPT_COOLDOWN_MS -
                        System.currentTimeMillis()).coerceAtLeast(0L)
                    val blockedReason = when {
                        moments < PROMPT_AFTER_MOMENTS -> "moments=$moments<${PROMPT_AFTER_MOMENTS}"
                        launchedCount >= MAX_PROMPTS -> "launched=$launchedCount>=${MAX_PROMPTS}"
                        cooldownLeftMs > 0L -> "cooldown=${cooldownLeftMs / 3_600_000L}h"
                        else -> null
                    }
                    if (blockedReason != null) {
                        "skip(trigger=$trigger): $blockedReason".makeLog("InAppReviewPrompt")
                        return@runCatching
                    }

                    delay(PROMPT_DELAY_MS)
                    awaitToastsGone()
                    if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        "skip(trigger=$trigger): activity not resumed".makeLog("InAppReviewPrompt")
                        return@runCatching
                    }

                    val manager = ReviewManagerFactory.create(activity)
                    val task = suspendCancellableCoroutine { continuation ->
                        manager.requestReviewFlow().addOnCompleteListener {
                            continuation.resume(it)
                        }
                    }
                    if (task.isSuccessful) {
                        // request 成功即记为一次尝试:配额耗尽时 launch 静默不弹且无回调,无法区分
                        settings.registerInAppReviewPrompted()
                        "launch review flow, trigger=$trigger".makeLog("InAppReviewPrompt")
                        manager.launchReviewFlow(activity, task.result)
                    } else {
                        // 失败(无 Play Store/侧载/Play 服务异常)也进入冷却并重攒计数,
                        // 否则每次成功时刻都会重试一次注定失败的 Play IPC
                        "request failed(trigger=$trigger): ${task.exception?.message}"
                            .makeLog("InAppReviewPrompt")
                        settings.registerInAppReviewFailedAttempt()
                    }
                } finally {
                    promptInFlight.set(false)
                }
            }.onFailure {
                if (it is CancellationException) throw it
                it.makeLog("InAppReviewPrompt")
            }
        }
    }

    /**
     * 等当前的自定义 Toast 消失再弹评分层:Toast 上可能挂着「打开」按钮或奖励提示
     * (如小游戏通关的积分奖励),和 Play 的半屏评分层叠在一起会互相遮挡。
     * 有上限地轮询,不做无限等待;保存成功路径本来就在 Toast 消失后才上报,这里通常直接返回。
     */
    private suspend fun awaitToastsGone() {
        val deadline = System.currentTimeMillis() + MAX_TOAST_WAIT_MS
        while (AppToastHost.state.currentToastData != null &&
            System.currentTimeMillis() < deadline
        ) {
            delay(TOAST_POLL_MS)
        }
    }
}
