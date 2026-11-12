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

package com.wanbaohe.adwatch.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdLoadType
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTCustomController
import com.bytedance.sdk.openadsdk.TTRewardVideoAd
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.storage.RemoteConfigStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/** GroMore 聚合平台「万宝盒」应用 ID (gromore 后台, 2026-09-13 创建) */
private const val GROMORE_APP_ID = "5882609"

/** GroMore 聚合广告位 ID:「广告看看看激励视频」(瀑布流: 穿山甲竞价/兜底 + GroMore ADX 竞价) */
private const val GROMORE_REWARD_SLOT_ID = "104527254"

/**
 * 国内渠道真实实现: 封装 GroMore 聚合 SDK (mediation-sdk, 内含穿山甲 ADN) 懒初始化 + 激励视频加载/展示。
 * 仅 src/domestic 编译, google 渠道见 src/google, foss 见 src/nogms 的同签名 stub。
 *
 * 合规红线: 用户未同意隐私协议前不得调 TTAdSdk.init/start,
 * 首次 preload 时才检查并初始化 (正常路径入口页已在隐私弹窗之后, 此处为防御性跳过)。
 */
@Singleton
class GmRewardedAdController @Inject constructor(
    @ApplicationContext private val context: Context,
) : RewardedAdController {

    private val _status = MutableStateFlow(RewardedAdStatus.IDLE)
    override val status: StateFlow<RewardedAdStatus> = _status.asStateFlow()

    override val isSupported: Boolean = true

    private var rewardedAd: TTRewardVideoAd? = null
    private val sdkStarted = AtomicBoolean(false)
    private var sdkStarting = false
    private var loading = false

    /** 840040 (首次冷启动聚合配置未拉取到) 已自动重试过 */
    private var configRetried = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private companion object {
        const val TAG = "GmRewardedAd"
        const val GROMORE_CONFIG_NOT_READY = 840040
        const val CONFIG_RETRY_DELAY_MS = 3000L
    }

    override fun preload() {
        if (rewardedAd != null || loading) return
        // 隐私协议未同意: 不触碰广告 SDK, 状态置 FAILED 供页面展示重试
        if (CoreUtils.isShowPrivacyPolicyDialog()) {
            _status.value = RewardedAdStatus.FAILED
            return
        }
        // 穿山甲系 SDK 要求 init/start 在主线程调用
        mainHandler.post { ensureSdkStarted { loadAd() } }
    }

    /** init 一次 + start 回调成功后执行 onReady */
    private fun ensureSdkStarted(onReady: () -> Unit) {
        if (sdkStarted.get()) {
            onReady()
            return
        }
        if (sdkStarting) return
        sdkStarting = true
        // init 幂等 (SDK 文档: 多次初始化以第一次配置为准), 无需先查 isInitSuccess (已弃用)
        TTAdSdk.init(context, buildAdConfig())
        TTAdSdk.start(object : TTAdSdk.Callback {
            override fun success() {
                sdkStarting = false
                sdkStarted.set(true)
                onReady()
            }

            override fun fail(code: Int, msg: String?) {
                Log.w(TAG, "TTAdSdk.start fail: code=$code msg=$msg")
                sdkStarting = false
                _status.value = RewardedAdStatus.FAILED
            }
        })
    }

    private fun buildAdConfig(): TTAdConfig =
        TTAdConfig.Builder()
            .appId(GROMORE_APP_ID)
            .appName(context.applicationInfo.loadLabel(context.packageManager).toString())
            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
            .allowShowNotify(true)
            .debug(false)
            .supportMultiProcess(false)
            // 聚合 SDK 必须显式开启聚合功能, 否则瀑布流不生效 (报 40006)
            .useMediation(true)
            // 合规: 关闭位置/应用列表/电话状态/WiFi 状态/外部存储等敏感采集
            .customController(object : TTCustomController() {
                override fun isCanUseLocation(): Boolean = false
                override fun alist(): Boolean = false
                override fun isCanUsePhoneState(): Boolean = false
                override fun isCanUseWifiState(): Boolean = false
                override fun isCanUseWriteExternal(): Boolean = false
            })
            .build()

    private fun loadAd() {
        if (rewardedAd != null || loading) return
        loading = true
        _status.value = RewardedAdStatus.LOADING
        val adNative: TTAdNative = TTAdSdk.getAdManager().createAdNative(context)
        val adSlot = AdSlot.Builder()
            .setCodeId(GROMORE_REWARD_SLOT_ID)
            .setOrientation(TTAdConstant.ORIENTATION_VERTICAL)
            .setAdLoadType(TTAdLoadType.LOAD)
            // 奖励配置: 用于竞价挽留弹窗展示, 与实际发放积分同一来源(远程配置 adWatchRewardPoints)
            .setRewardName("积分")
            .setRewardAmount(
                RemoteConfigStorage.getRemoteConfig().adWatchRewardPoints
                    ?: AdWatchAds.DEFAULT_REWARD_POINTS
            )
            .build()
        adNative.loadRewardVideoAd(adSlot, object : TTAdNative.RewardVideoAdListener {
            override fun onError(code: Int, msg: String?) {
                Log.w(TAG, "loadRewardVideoAd onError: code=$code msg=$msg")
                rewardedAd = null
                loading = false
                // 840040: 首次冷启动聚合配置未拉取到, SDK 文档建议稍后重试; 自动重试一次, 仍失败再走手动重试
                if (code == GROMORE_CONFIG_NOT_READY && !configRetried) {
                    configRetried = true
                    mainHandler.postDelayed({ loadAd() }, CONFIG_RETRY_DELAY_MS)
                    return
                }
                _status.value = RewardedAdStatus.FAILED
            }

            override fun onRewardVideoAdLoad(ad: TTRewardVideoAd) {
                // 物料已下发, 视频缓存完成 (onRewardVideoCached) 后才置 READY
                rewardedAd = ad
            }

            @Deprecated("SDK 兼容回调, 以带参版本为准")
            override fun onRewardVideoCached() = Unit

            override fun onRewardVideoCached(ad: TTRewardVideoAd) {
                rewardedAd = ad
                loading = false
                _status.value = RewardedAdStatus.READY
            }
        })
    }

    override fun show(
        activity: Activity,
        onEarned: () -> Unit,
        onClosed: () -> Unit,
        onError: () -> Unit,
    ) {
        // 穿山甲系展示接口同样要求主线程(登录成功回调等可能从子线程重试进来)
        mainHandler.post {
            showInternal(activity, onEarned, onClosed, onError)
        }
    }

    private fun showInternal(
        activity: Activity,
        onEarned: () -> Unit,
        onClosed: () -> Unit,
        onError: () -> Unit,
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onError()
            preload()
            return
        }
        ad.setRewardAdInteractionListener(object : TTRewardVideoAd.RewardAdInteractionListener {
            override fun onAdShow() = Unit

            override fun onAdVideoBarClick() = Unit

            override fun onAdClose() {
                rewardedAd = null
                _status.value = RewardedAdStatus.IDLE
                onClosed()
                preload()
            }

            override fun onVideoComplete() = Unit

            override fun onVideoError() {
                Log.w(TAG, "showRewardVideoAd onVideoError")
                rewardedAd = null
                _status.value = RewardedAdStatus.FAILED
                onError()
            }

            @Deprecated("SDK 兼容回调, 以 onRewardArrived 为准")
            override fun onRewardVerify(
                rewardValid: Boolean,
                rewardType: Int,
                rewardName: String?,
                rewardAmount: Int,
                errorMsg: String?,
            ) = Unit

            override fun onRewardArrived(isRewardValid: Boolean, rewardType: Int, extra: android.os.Bundle?) {
                if (isRewardValid) onEarned()
            }

            override fun onSkippedVideo() = Unit
        })
        _status.value = RewardedAdStatus.LOADING
        ad.showRewardVideoAd(activity)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RewardedAdModule {

    @Binds
    @Singleton
    abstract fun bindRewardedAdController(
        impl: GmRewardedAdController
    ): RewardedAdController
}
