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
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * foss 渠道 stub: 不打包任何广告 SDK,
 * 入口本就由 channelConfig.enableRewardedAd 隐藏 (foss 为 false),
 * 与 src/google / src/domestic 的真实实现签名保持一致。
 */
@Singleton
class NoopRewardedAdController @Inject constructor() : RewardedAdController {

    override val status: StateFlow<RewardedAdStatus> =
        MutableStateFlow(RewardedAdStatus.UNAVAILABLE).asStateFlow()

    override val isSupported: Boolean = false

    override fun preload() = Unit

    override fun show(
        activity: Activity,
        onEarned: () -> Unit,
        onClosed: () -> Unit,
        onError: () -> Unit,
    ) = onError()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RewardedAdModule {

    @Binds
    @Singleton
    abstract fun bindRewardedAdController(
        impl: NoopRewardedAdController
    ): RewardedAdController
}
