/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.core.crash.data



import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import com.t8rin.logger.makeLog

/**
 * 国内渠道与 foss 的 [AnalyticsManager] 实现。
 *
 * 这两个渠道没有可用的原生崩溃/异常统计, 所以统计类方法保持 no-op,
 * 错误上报只打到 logcat(`adb logcat | grep CrashNotCollected`)——
 * 本地开发仍然看得到, 只是不出网、不入库。
 *
 * **接入国产崩溃统计(如 Bugly / 厂商服务)时改这里就够了**:
 *   1. 在 `core/crash/build.gradle.kts` 里给国内 flavor 加上对应 SDK 依赖;
 *   2. 在下面 [sendReport] / [reportNonFatal] 里把异常转交给该 SDK;
 *   3. 用设置项 `allowCollectCrashlytics` 决定是否上报(保持用户开关有效)。
 * 调用方只认 `ErrorReporter`, 不需要任何改动。
 */
internal object AnalyticsManagerImpl : AnalyticsManager {

    override var allowCollectCrashlytics: Boolean = false

    override var allowCollectAnalytics: Boolean = false

    override fun updateAnalyticsCollectionEnabled(value: Boolean) = Unit

    override fun updateAllowCollectCrashlytics(value: Boolean) = Unit

    override fun sendReport(throwable: Throwable) {
        throwable.makeLog("CrashNotCollected")
    }

    override fun reportNonFatal(throwable: Throwable, keys: Map<String, Any>) {
        throwable.makeLog("NonFatalNotCollected")
    }

    override fun registerScreenOpen(screenName: String) = Unit

    override fun logEvent(name: String, params: Map<String, Any>) = Unit

    override fun setUserProperty(name: String, value: String?) = Unit
}
