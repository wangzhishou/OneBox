package com.wanbaohe.adwatch.component

import android.app.Activity
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.tencent.mmkv.MMKV
import com.wanbaohe.adwatch.R
import com.wanbaohe.adwatch.ads.AdWatchAds
import com.wanbaohe.adwatch.ads.RewardedAdController
import com.wanbaohe.adwatch.ads.RewardedAdStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 广告看看看主组件: 播放 AdMob 激励广告, 看完发系统积分。
 *
 * 发积分走现成通道 [BaseUtils.rewardPoints](服务端按 bizId 幂等),
 * 每日上限计数存 MMKV 按日期重置; 未登录时照 withAiGate 模式弹登录, 成功后重试。
 */
class AdWatchComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val rewardedAdController: RewardedAdController,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val mmkv by lazy { MMKV.mmkvWithID("ad_watch") }

    val adStatus: StateFlow<RewardedAdStatus> = rewardedAdController.status

    private val _uiState = MutableStateFlow(
        AdWatchUiState(
            rewardPoints = rewardPoints,
            watchedToday = watchedToday(),
            remainingToday = remainingToday(),
        )
    )
    val uiState = _uiState.asStateFlow()

    /** 单次奖励积分: 远程配置 adWatchRewardPoints, 未下发默认 15 */
    private val rewardPoints: Int
        get() = RemoteConfigStorage.getRemoteConfig().adWatchRewardPoints
            ?: AdWatchAds.DEFAULT_REWARD_POINTS

    init {
        rewardedAdController.preload()
    }

    private fun todayKey(): String =
        "count_" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun watchedToday(): Int = mmkv.decodeInt(todayKey(), 0)

    private fun remainingToday(): Int =
        (AdWatchAds.DAILY_LIMIT - watchedToday()).coerceAtLeast(0)

    /** 点击「观看」: 登录门控 → 次数门控 → 播放激励广告 */
    fun onWatchClick(activity: Activity) {
        if (!TokenStorage.isLogin()) {
            ActionUtils.showLogin(source = SOURCE) { onWatchClick(activity) }
            return
        }
        if (remainingToday() <= 0) {
            ActionUtils.showToast(R.string.ad_watch_daily_limit_reached)
            return
        }
        if (_uiState.value.isShowing) return
        _uiState.update { it.copy(isShowing = true) }
        rewardedAdController.show(
            activity = activity,
            onEarned = {
                mmkv.encode(todayKey(), watchedToday() + 1)
                BaseUtils.rewardPoints(
                    points = rewardPoints,
                    desc = REWARD_DESC,
                    source = SOURCE,
                    bizId = UUID.randomUUID().toString(),
                    showToast = true,
                )
                _uiState.update {
                    it.copy(
                        watchedToday = watchedToday(),
                        remainingToday = remainingToday(),
                    )
                }
            },
            onClosed = {
                _uiState.update { it.copy(isShowing = false) }
            },
            onError = {
                _uiState.update { it.copy(isShowing = false) }
                ActionUtils.showToast(R.string.ad_watch_ad_not_ready)
            },
        )
    }

    /** 加载失败后重试 */
    fun retryLoad() {
        rewardedAdController.preload()
    }

    private companion object {
        const val SOURCE = "ad_watch"
        const val REWARD_DESC = "观看激励广告奖励"
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): AdWatchComponent
    }
}
