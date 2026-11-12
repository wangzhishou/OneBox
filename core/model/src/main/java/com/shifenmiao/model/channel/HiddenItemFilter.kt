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

/**
 * 按渠道写死隐藏的服务端条目(item-list)。
 *
 * 服务端下发条目时无法按 flavor 过滤(Go 网关只按 versionCode / locale / vipLevel 筛),
 * 所以只能在客户端拦截: 同步落库前丢弃, 并把本地可能已有的残留一并删掉
 * (覆盖旧版本已经同步过该条目的情况)。
 *
 * 与 [NeedPrivacyPolicyDialog]、[PromptSensitiveCheckConfig] 同属"按渠道硬编码"的策略,
 * 区别是这里返回的是条目清单, 不是枚举开关。
 */
object HiddenItemFilter {

    /**
     * 「广告看看看」条目的 miniProgramId, 与 Screen.AdWatch.id 一致。
     *
     * 该页面的广告实现按 flavor 分: google 走 AdMob、国内渠道走 GroMore、
     * foss 是 NoopRewardedAdController(空实现)。foss 没有任何广告 SDK,
     * 入口点进去无广告可看, 故整条隐藏。
     */
    private const val AD_WATCH_ITEM_ID = "1095"

    /** 当前渠道要隐藏的条目 miniProgramId 集合 */
    fun hiddenMiniProgramIds(
        flavorType: FlavorType = FlavorType.fromName(),
    ): Set<String> = when (flavorType) {
        FlavorType.FOSS -> setOf(AD_WATCH_ITEM_ID)
        else -> emptySet()
    }

    /** 该条目在当前渠道是否应当隐藏 */
    fun isHidden(
        miniProgramId: String?,
        flavorType: FlavorType = FlavorType.fromName(),
    ): Boolean = !miniProgramId.isNullOrBlank() && miniProgramId in hiddenMiniProgramIds(flavorType)
}
