package com.wanbaohe.core.ui.review

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.t8rin.imagetoolbox.core.di.entryPoint
import com.t8rin.imagetoolbox.core.settings.di.SettingsStateEntryPoint
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Google Play 应用内评分(In-App Review)入口,google 渠道专用实现。
 *
 * 弹出的是 Play 托管的半屏评分层,不离开 App。注意:
 * 只有从 Play 商店安装的包才能真正弹出;侧载/调试包 requestReviewFlow 会失败。
 * Play 对该弹层有频率配额,是否真正展示由 Play 决定,API 不会告知结果。
 *
 * 启动次数与"是否已自动弹过"标记统一维护在全局设置 DataStore:
 * APP_OPEN_COUNT(每次启动自增)与 IN_APP_REVIEW_AUTO_PROMPTED。
 */
object InAppReviewPrompt {

    /** APP_OPEN_COUNT 达到该次数后,自动弹一次评分层(每个安装只弹一次) */
    private const val AUTO_PROMPT_MIN_LAUNCHES = 5

    /** 自动弹出前的延时,避免在启动瞬间打扰用户 */
    private const val AUTO_PROMPT_DELAY_MS = 8000L

    /**
     * 是否从 Play 商店安装。侧载/调试包调用 In-App Review 经常"成功但什么都不弹"
     * (API 不回调失败),表现为点击无反应,所以非 Play 安装直接走回退逻辑。
     */
    private fun isInstalledFromPlayStore(activity: Activity): Boolean {
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                activity.packageManager
                    .getInstallSourceInfo(activity.packageName)
                    .installingPackageName
            }.getOrNull()
        } else {
            @Suppress("DEPRECATION")
            activity.packageManager.getInstallerPackageName(activity.packageName)
        }
        return installer == "com.android.vending"
    }

    /**
     * 尝试弹出应用内评分层;流程不可用时(如非 Play 安装)回调 [onUnavailable],
     * 由调用方回退到跳转应用商店。
     */
    fun launch(activity: Activity, onUnavailable: () -> Unit) {
        if (!isInstalledFromPlayStore(activity)) {
            onUnavailable()
            return
        }
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(activity, task.result)
            } else {
                onUnavailable()
            }
        }
    }

    /**
     * 自动弹出:全局 APP_OPEN_COUNT 达到 [AUTO_PROMPT_MIN_LAUNCHES] 后,
     * 延时弹一次评分层,之后不再自动打扰。
     */
    fun maybeAutoPrompt(activity: ComponentActivity) {
        if (!isInstalledFromPlayStore(activity)) return

        activity.lifecycleScope.launch {
            var settingsManager: SettingsManager? = null
            activity.entryPoint<SettingsStateEntryPoint> {
                settingsManager = this.settingsManager
            }
            val settings = settingsManager ?: return@launch

            if (settings.isInAppReviewAutoPrompted()) return@launch
            if (settings.getSettingsState().appOpenCount < AUTO_PROMPT_MIN_LAUNCHES) return@launch

            delay(AUTO_PROMPT_DELAY_MS)
            if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return@launch

            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 无论用户评分还是跳过,每个安装只自动弹这一次
                    activity.lifecycleScope.launch {
                        settings.setInAppReviewAutoPrompted()
                    }
                    manager.launchReviewFlow(activity, task.result)
                }
            }
        }
    }
}
