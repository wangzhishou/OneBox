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

package com.shifenmiao.model.channel

import com.shifenmiao.model.BuildConfig

enum class FlavorType(val flavorName: String) {
    ONEBOX("onebox"),
    XIAOMI("xiaomi"),
    YYB("yyb"),
    OPPO("oppo"),
    VIVO("vivo"),
    GOOGLE("google"),
    HUAWEI("huawei"),
    // F-Droid 渠道: 海外功能形态(对齐 GOOGLE), 但不含任何 Google 系专有 SDK
    FOSS("foss");

    /** 海外形态渠道(google / foss): 邮箱登录、无国内合规文案、AI 引擎自带 token 直连等 */
    val isOverseas: Boolean
        get() = this == GOOGLE || this == FOSS

    companion object {
        // 未显式登记的渠道回退为 ONEBOX, 即国内功能形态
        fun fromName(name: String = BuildConfig.FLAVOR): FlavorType {
            return entries.find { it.flavorName == name } ?: ONEBOX
        }
    }
}
