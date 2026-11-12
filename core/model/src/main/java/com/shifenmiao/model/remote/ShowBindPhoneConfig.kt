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

package com.shifenmiao.model.remote



import com.shifenmiao.model.channel.FlavorType

enum class ShowBindPhoneConfig(val isShow: Boolean) {
    ENABLED(true),
    DISABLED(false);

    companion object {
        fun getConfigByFlavor(flavorType: FlavorType = FlavorType.fromName()): ShowBindPhoneConfig {
            return when (flavorType) {
                FlavorType.ONEBOX -> ENABLED
                FlavorType.XIAOMI -> ENABLED
                FlavorType.YYB -> DISABLED
                FlavorType.OPPO -> ENABLED
                FlavorType.VIVO -> ENABLED
                FlavorType.HUAWEI -> ENABLED
                // 海外渠道(google / foss)不提供绑定手机功能
                FlavorType.GOOGLE, FlavorType.FOSS -> DISABLED
                else -> {
                    DISABLED
                }
            }
        }
    }
}