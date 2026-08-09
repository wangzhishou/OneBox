/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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