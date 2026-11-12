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

package com.shifenmiao.base.channel

/**
 * 渠道配置: 由 :app 模块从 BuildConfig 注入, 业务模块通过 Hilt 获取.
 *
 * 业务模块不应直接 import com.shifenmiao.app.BuildConfig (会形成反向依赖),
 * 而是通过注入 ChannelConfig 来判断当前渠道特性, 实现"切渠道不影响"的隔离.
 *
 * 字段含义与 app/build.gradle.kts 中 google flavor 的 buildConfigField 一一对应.
 */
data class ChannelConfig(
    val enableWechat: Boolean,
    val enableAlipay: Boolean,
    val enableHms: Boolean,
    val enableGms: Boolean,
    val apiBaseUrl: String,
    val webBaseUrl: String,
    val privacyPolicyUrl: String,
    val userAgreementUrl: String,
    /** 是否展示语言切换入口; 只有打包了多语言资源的渠道才应开启 */
    val showLanguageSetting: Boolean = false,
    /** Google Play Billing 应用内购买(消耗型积分商品), 仅 google 渠道开启 */
    val enablePlayBilling: Boolean = false,
) {
    /** 是否有可用的应用内支付渠道(微信/支付宝/Google Play Billing 任一) */
    val enablePayment: Boolean
        get() = enableWechat || enableAlipay || enablePlayBilling

    /** 是否展示「看广告赚积分」入口: google 渠道走 AdMob, 国内渠道走穿山甲, foss 无广告 */
    val enableRewardedAd: Boolean
        get() = enableGms || enableWechat
}
