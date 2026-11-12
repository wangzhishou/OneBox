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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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

/**
 * google 渠道真实实现: 封装 MobileAds 懒初始化 + RewardedAd 加载/展示。
 * 仅 src/google 编译, 其余渠道见 src/nogms 的同签名 stub。
 */
@Singleton
class GmsRewardedAdController @Inject constructor(
    @ApplicationContext private val context: Context,
) : RewardedAdController {

    private val _status = MutableStateFlow(RewardedAdStatus.IDLE)
    override val status: StateFlow<RewardedAdStatus> = _status.asStateFlow()

    override val isSupported: Boolean = true

    private var rewardedAd: RewardedAd? = null
    private val sdkInitialized = AtomicBoolean(false)
    private var loading = false

    private val mainHandler = Handler(Looper.getMainLooper())

    /** AdMob 所有接口强制主线程调用(登录成功回调等可能从子线程重试进来) */
    private inline fun onMain(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post { block() }
        }
    }

    override fun preload() = onMain {
        preloadInternal()
    }

    private fun preloadInternal() {
        if (rewardedAd != null || loading) return
        loading = true
        _status.value = RewardedAdStatus.LOADING
        ensureSdkInitialized {
            RewardedAd.load(
                context,
                AdWatchAds.REWARDED_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        loading = false
                        _status.value = RewardedAdStatus.READY
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        loading = false
                        _status.value = RewardedAdStatus.FAILED
                    }
                },
            )
        }
    }

    private fun ensureSdkInitialized(onReady: () -> Unit) {
        if (sdkInitialized.get()) {
            onReady()
            return
        }
        MobileAds.initialize(context) {
            sdkInitialized.set(true)
            onReady()
        }
    }

    override fun show(
        activity: Activity,
        onEarned: () -> Unit,
        onClosed: () -> Unit,
        onError: () -> Unit,
    ) = onMain {
        val ad = rewardedAd
        if (ad == null) {
            onError()
            preload()
            return@onMain
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _status.value = RewardedAdStatus.IDLE
                onClosed()
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _status.value = RewardedAdStatus.FAILED
                onError()
            }
        }
        _status.value = RewardedAdStatus.LOADING
        ad.show(activity) { onEarned() }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RewardedAdModule {

    @Binds
    @Singleton
    abstract fun bindRewardedAdController(
        impl: GmsRewardedAdController
    ): RewardedAdController
}
