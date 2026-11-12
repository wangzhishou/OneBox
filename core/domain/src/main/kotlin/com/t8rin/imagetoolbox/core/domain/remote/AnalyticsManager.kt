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

package com.t8rin.imagetoolbox.core.domain.remote

interface AnalyticsManager {

    val allowCollectCrashlytics: Boolean

    val allowCollectAnalytics: Boolean

    fun updateAnalyticsCollectionEnabled(value: Boolean)

    fun updateAllowCollectCrashlytics(value: Boolean)

    fun sendReport(throwable: Throwable)

    /**
     * 上报非致命异常(业务异常)。
     *
     * 与 [sendReport] 的区别: 那是致命崩溃, 会顺带把待发报告一起送出去; 这里是
     * "用户还能继续用、但这事不该发生", 只记录、不触发强制上传。
     * 未接入原生统计的渠道是 no-op。
     *
     * @param keys 低基数上下文(出错位置、引擎名等), 作为自定义键展示在后台
     */
    fun reportNonFatal(throwable: Throwable, keys: Map<String, Any> = emptyMap())

    fun registerScreenOpen(screenName: String)

    /**
     * 上报自定义事件(如 item 点击), 字符串值原样上报, 数字值(value/points 等)按数值上报
     */
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())

    /**
     * 设置用户属性(仅限低基数枚举值, 如 vip 等级/登录方式), value 传 null 清除
     */
    fun setUserProperty(name: String, value: String?)

}