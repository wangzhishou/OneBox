package com.wanbaohe.core.ui.review

import android.app.Activity
import androidx.activity.ComponentActivity

/**
 * 国内渠道无 Google Play 服务,应用内评分不可用:
 * [launch] 直接回调 [onUnavailable](由调用方回退到跳应用商店),
 * [maybeAutoPrompt] 为空实现。与 src/google 下的实现形成 flavor 隔离。
 */
object InAppReviewPrompt {

    fun launch(activity: Activity, onUnavailable: () -> Unit) = onUnavailable()

    fun maybeAutoPrompt(activity: ComponentActivity) = Unit
}
