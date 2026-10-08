package com.wanbaohe.core.ui.review

import androidx.activity.ComponentActivity

/**
 * 国内渠道无 Google Play 服务,应用内评分不可用,[maybePrompt] 为空实现。
 * 与 src/google 下的实现形成 flavor 隔离。
 */
object InAppReviewPrompt {

    fun maybePrompt(activity: ComponentActivity, trigger: ReviewPromptTrigger) = Unit
}
