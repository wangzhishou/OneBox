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

package com.wanbaohe.blessingwall.model

import com.shifenmiao.model.channel.FlavorType

/**
 * 祈福墙 tab 展示顺序。
 * - 国内渠道：传统祈福在前，国际主题在后。
 * - 海外渠道（google / foss）：守护天使、招财猫优先，其余国际主题在前。
 */
object BlessingWallOrder {

    private val domestic = listOf(
        BlessingType.WOODEN_FISH,
        BlessingType.WEALTH_GOD,
        BlessingType.GUANYIN,
        BlessingType.INCENSE,
        BlessingType.HAMSA,
        BlessingType.CUPID,
        BlessingType.CLOVER,
        BlessingType.ANGEL,
        BlessingType.LUCKY_CAT,
        BlessingType.DREAMCATCHER,
        BlessingType.WISHBONE,
        BlessingType.HORSESHOE,
    )

    private val overseas = listOf(
        BlessingType.ANGEL,
        BlessingType.LUCKY_CAT,
        BlessingType.HAMSA,
        BlessingType.CUPID,
        BlessingType.CLOVER,
        BlessingType.DREAMCATCHER,
        BlessingType.WISHBONE,
        BlessingType.HORSESHOE,
        BlessingType.WOODEN_FISH,
        BlessingType.WEALTH_GOD,
        BlessingType.GUANYIN,
        BlessingType.INCENSE,
    )

    fun types(
        isOverseas: Boolean = FlavorType.fromName().isOverseas,
    ): List<BlessingType> = if (isOverseas) overseas else domestic

    fun indexOf(
        type: BlessingType?,
        isOverseas: Boolean = FlavorType.fromName().isOverseas,
    ): Int {
        if (type == null) return 0
        return types(isOverseas).indexOf(type).coerceAtLeast(0)
    }

    fun indexOfKey(
        key: String?,
        isOverseas: Boolean = FlavorType.fromName().isOverseas,
    ): Int = indexOf(BlessingType.fromKey(key.orEmpty()), isOverseas)
}
