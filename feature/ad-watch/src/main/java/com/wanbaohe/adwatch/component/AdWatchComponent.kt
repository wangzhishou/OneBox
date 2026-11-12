/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 广告看看看主组件: 播放激励广告, 看完发系统积分。
 *
 * 发积分走现成通道 [BaseUtils.rewardPoints](服务端按 bizId 幂等),
 * 每日上限计数存 MMKV 按日期重置。未登录也能看; 激励到账后先记一笔待领取,
 * 广告关掉再弹出登录(页内可去注册), 登录或注册成功后把待领取积分发出去。
 */
class AdWatchComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val rewardedAdController: RewardedAdController,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val mmkv by lazy { MMKV.mmkvWithID("ad_watch") }

    val adStatus: StateFlow<RewardedAdStatus> = rewardedAdController.status

    /** 本条广告是否已经记过奖励, 防止 SDK 重复回调 */
    private val rewardRecorded = AtomicBoolean(false)

    /** 激励已经入账, 等广告关掉再发积分或弹登录, 避免被全屏广告盖住 */
    private var settleAfterClose = false

    private var settleJob: Job? = null

    private val _uiState = MutableStateFlow(
        AdWatchUiState(
            rewardPoints = rewardPoints,
            watchedToday = watchedToday(),
            remainingToday = remainingToday(),
            unclaimedPoints = pendingPoints(),
        )
    )
    val uiState = _uiState.asStateFlow()

    /** 单次奖励积分: 远程配置 adWatchRewardPoints, 未下发默认 15 */
    private val rewardPoints: Int
        get() = RemoteConfigStorage.getRemoteConfig().adWatchRewardPoints
            ?: AdWatchAds.DEFAULT_REWARD_POINTS

    init {
        rewardedAdController.preload()
        if (TokenStorage.isLogin()) {
            flushPending()
        }
    }

    private fun todayKey(): String =
        "count_" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun watchedToday(): Int = mmkv.decodeInt(todayKey(), 0)

    private fun remainingToday(): Int =
        (AdWatchAds.DAILY_LIMIT - watchedToday()).coerceAtLeast(0)

    private fun pendingPoints(): Int = mmkv.decodeInt(KEY_PENDING, 0)

    private fun publishCounts() {
        _uiState.update {
            it.copy(
                rewardPoints = rewardPoints,
                watchedToday = watchedToday(),
                remainingToday = remainingToday(),
                unclaimedPoints = pendingPoints(),
            )
        }
    }

    /** 点击「观看」: 次数门控 → 播放激励广告。未登录直接看。 */
    fun onWatchClick(activity: Activity) {
        if (TokenStorage.isLogin()) {
            flushPending()
        }
        if (remainingToday() <= 0) {
            if (!TokenStorage.isLogin() && pendingPoints() > 0) {
                settleJob?.cancel()
                promptLoginToClaim()
            } else {
                ActionUtils.showToast(R.string.ad_watch_daily_limit_reached)
            }
            return
        }
        if (_uiState.value.isShowing) return
        settleJob?.cancel()
        rewardRecorded.set(false)
        settleAfterClose = false
        _uiState.update { it.copy(isShowing = true) }
        rewardedAdController.show(
            activity = activity,
            onEarned = {
                if (!rewardRecorded.compareAndSet(false, true)) return@show
                componentScope.launch { onRewardEarned() }
            },
            onClosed = {
                componentScope.launch { finishShowing() }
            },
            onError = {
                componentScope.launch {
                    val shouldSettle = settleAfterClose
                    settleAfterClose = false
                    _uiState.update { it.copy(isShowing = false) }
                    if (shouldSettle) {
                        scheduleSettle()
                    } else if (!rewardRecorded.get()) {
                        ActionUtils.showToast(R.string.ad_watch_ad_not_ready)
                    }
                }
            },
        )
    }

    /** 激励到账: 计入今日次数, 积分先挂起, 广告关掉再发或提示登录。 */
    private fun onRewardEarned() {
        mmkv.encode(todayKey(), watchedToday() + 1)
        mmkv.encode(KEY_PENDING, pendingPoints() + rewardPoints)
        publishCounts()
        if (_uiState.value.isShowing) {
            settleAfterClose = true
        } else {
            scheduleSettle()
        }
    }

    private fun finishShowing() {
        val shouldSettle = settleAfterClose
        settleAfterClose = false
        _uiState.update { it.copy(isShowing = false) }
        if (shouldSettle) {
            scheduleSettle()
        }
    }

    /** 等广告页退场后再发积分或弹登录, 登录页才能露出来 */
    private fun scheduleSettle() {
        settleJob?.cancel()
        settleJob = componentScope.launch {
            delay(300.milliseconds)
            if (TokenStorage.isLogin()) {
                flushPending()
            } else if (pendingPoints() > 0) {
                promptLoginToClaim()
            }
        }
    }

    /** 登录页自带注册入口, 成功回调里把挂起的积分发出去 */
    private fun promptLoginToClaim() {
        ActionUtils.showToast(R.string.ad_watch_login_to_claim)
        ActionUtils.showLogin(source = SOURCE) { flushPending() }
    }

    private fun flushPending() {
        if (!TokenStorage.isLogin()) return
        val points = pendingPoints()
        if (points <= 0) return
        mmkv.encode(KEY_PENDING, 0)
        publishCounts()
        BaseUtils.rewardPoints(
            points = points,
            desc = REWARD_DESC,
            source = SOURCE,
            bizId = UUID.randomUUID().toString(),
            showToast = true,
        )
    }

    /** 加载失败后重试 */
    fun retryLoad() {
        rewardedAdController.preload()
    }

    private companion object {
        const val SOURCE = "ad_watch"
        const val REWARD_DESC = "观看激励广告奖励"
        const val KEY_PENDING = "pending_points"
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): AdWatchComponent
    }
}
