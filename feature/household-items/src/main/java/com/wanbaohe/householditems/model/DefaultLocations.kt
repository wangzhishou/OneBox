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

package com.wanbaohe.householditems.model

import com.shifenmiao.database.household.entity.HouseholdLocationEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.householditems.R

/**
 * 预置储物位置:顶级「家」+「公司」,各带常用子位置。
 *
 * 首次进入页面且表为空时由 Component 播种(固定 id + upsert,幂等)。
 * 名称经 [localizedDefaultLocationName] 按当前 locale 解析;
 * 用户自建位置返回 null,用其自命名。
 */
object DefaultLocations {

    const val ID_HOME = "loc_home"
    const val ID_COMPANY = "loc_company"

    /** 每次调用重新解析名称(进程内切换语言后播种新库也能拿到正确语言) */
    fun all(): List<HouseholdLocationEntity> = listOf(
        location(ID_HOME, parentId = null, sortOrder = 0),
        location("loc_living_room", ID_HOME, 0),
        location("loc_master_bedroom", ID_HOME, 1),
        location("loc_second_bedroom", ID_HOME, 2),
        location("loc_kitchen", ID_HOME, 3),
        location("loc_bathroom", ID_HOME, 4),
        location("loc_study", ID_HOME, 5),
        location("loc_balcony", ID_HOME, 6),
        location("loc_storage_room", ID_HOME, 7),
        location(ID_COMPANY, parentId = null, sortOrder = 1),
        location("loc_desk", ID_COMPANY, 0),
        location("loc_meeting_room", ID_COMPANY, 1),
        location("loc_pantry", ID_COMPANY, 2),
        location("loc_locker", ID_COMPANY, 3),
    )

    private fun location(id: String, parentId: String?, sortOrder: Int) =
        HouseholdLocationEntity(
            id = id,
            name = localizedDefaultLocationName(id) ?: id,
            parentId = parentId,
            sortOrder = sortOrder,
        )
}

/** 预置位置 id → 多语言字符串资源 的映射 */
fun defaultLocationNameResId(id: String): Int? = when (id) {
    "loc_home" -> R.string.household_loc_home
    "loc_living_room" -> R.string.household_loc_living_room
    "loc_master_bedroom" -> R.string.household_loc_master_bedroom
    "loc_second_bedroom" -> R.string.household_loc_second_bedroom
    "loc_kitchen" -> R.string.household_loc_kitchen
    "loc_bathroom" -> R.string.household_loc_bathroom
    "loc_study" -> R.string.household_loc_study
    "loc_balcony" -> R.string.household_loc_balcony
    "loc_storage_room" -> R.string.household_loc_storage_room
    "loc_company" -> R.string.household_loc_company
    "loc_desk" -> R.string.household_loc_desk
    "loc_meeting_room" -> R.string.household_loc_meeting_room
    "loc_pantry" -> R.string.household_loc_pantry
    "loc_locker" -> R.string.household_loc_locker
    else -> null
}

/** 预置位置的本地化显示名;非预置位置返回 null */
fun localizedDefaultLocationName(id: String): String? =
    defaultLocationNameResId(id)?.let { AppContext.getString(it) }
