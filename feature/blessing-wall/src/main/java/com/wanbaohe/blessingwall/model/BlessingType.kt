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

enum class BlessingType(val key: String) {
    WOODEN_FISH("woodenFish"),
    WEALTH_GOD("wealthGod"),
    GUANYIN("guanyin"),
    INCENSE("incense"),
    HAMSA("hamsa"),
    CUPID("cupid"),
    CLOVER("clover"),
    ANGEL("angel"),
    LUCKY_CAT("luckyCat"),
    DREAMCATCHER("dreamcatcher"),
    WISHBONE("wishbone"),
    HORSESHOE("horseshoe"),
    ;

    companion object {
        fun fromKey(key: String): BlessingType? = entries.find { it.key == key }
    }
}
