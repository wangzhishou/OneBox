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

import com.shifenmiao.database.household.entity.HouseholdItemEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** "临期"判定阈值:30 天内到期(未过期) */
internal const val EXPIRING_SOON_DAYS = 30L

// ─────────────────────────────────────────────────────────────────────────────
// 统计 tab 数据模型与聚合(纯 UI 层计算,数据量小,不加 DAO 聚合查询)
// ─────────────────────────────────────────────────────────────────────────────

/** 分类占比条目;colorIndex 用于 UI 层取调色板 */
data class CategoryCountUi(
    /** 空串 = 未分类(展示时本地化) */
    val name: String,
    val count: Int,
    val colorIndex: Int,
)

/** 位置分布条目(顶级位置聚合子树物品数) */
data class LocationCountUi(
    /** null = 未设置位置(展示时本地化) */
    val locationId: String?,
    val name: String,
    val count: Int,
    /** 位置的用户自选图标 key */
    val iconKey: String? = null,
)

data class HouseholdStatsUi(
    val totalItems: Int = 0,
    /** 本月新增物品数(按 createdAt,真实数据) */
    val addedThisMonth: Int = 0,
    val locationCount: Int = 0,
    /** 临期物品数:已设置保质期、未过期且 30 天内到期 */
    val expiringSoonCount: Int = 0,
    val categorySlices: List<CategoryCountUi> = emptyList(),
    val locationBars: List<LocationCountUi> = emptyList(),
    /** 最近新增(按 createdAt 倒序,最多 5 条) */
    val recentAdded: List<HouseholdItemUi> = emptyList(),
    /** 即将过期(未过期,按到期日升序,最多 5 条) */
    val upcomingExpiry: List<HouseholdItemUi> = emptyList(),
)

/** 实体 → UI 模型(列表/搜索/统计共用) */
internal fun HouseholdItemEntity.toUi(
    locs: List<HouseholdLocationUi>,
    today: LocalDate = LocalDate.now(),
): HouseholdItemUi {
    val expireDate = expireAt?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val daysToExpire = expireDate?.let {
        ChronoUnit.DAYS.between(today, it)
    }
    return HouseholdItemUi(
        id = id,
        name = name,
        category = category.orEmpty(),
        locationId = locationId,
        locationPath = locationPathOf(locs, locationId).joinToString(" / ") { it.name },
        expireDate = expireDate,
        photoPath = photoPath,
        note = note.orEmpty(),
        expiryStatus = expiryStatusOf(daysToExpire),
        daysToExpire = daysToExpire,
        createdAt = createdAt,
    )
}

internal fun expiryStatusOf(daysToExpire: Long?): ExpiryStatus = when {
    daysToExpire == null -> ExpiryStatus.NONE
    daysToExpire < 0 -> ExpiryStatus.EXPIRED
    daysToExpire <= EXPIRING_SOON_DAYS -> ExpiryStatus.EXPIRING_SOON
    else -> ExpiryStatus.FRESH
}

/** 全量物品 + 位置 → 统计页数据。"较上月"趋势只有"本月新增"可真实计算,其余只给当前值。 */
internal fun buildHouseholdStats(
    items: List<HouseholdItemEntity>,
    locations: List<HouseholdLocationUi>,
    today: LocalDate = LocalDate.now(),
): HouseholdStatsUi {
    val monthStart = today.withDayOfMonth(1)
    val addedThisMonth = items.count { entity ->
        val created = Instant.ofEpochMilli(entity.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        !created.isBefore(monthStart)
    }
    val expiringSoonCount = items.count { entity ->
        val days = entity.expireAt?.let {
            ChronoUnit.DAYS.between(
                today,
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate(),
            )
        }
        days != null && days in 0..EXPIRING_SOON_DAYS
    }

    // 分类占比:按分类文本分组,未分类归到空串桶
    val categorySlices = items.groupingBy { it.category.orEmpty() }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .mapIndexed { index, entry ->
            CategoryCountUi(name = entry.key, count = entry.value, colorIndex = index)
        }

    // 位置分布:顶级位置聚合整棵子树的物品;无位置/位置已失效归为"未设置位置"
    val topLevel = locations.filter { it.parentId == null }
    val countedIds = mutableSetOf<String>()
    val locationBars = topLevel.map { root ->
        val subtreeIds = subtreeLocationIds(locations, root.id).orEmpty()
        val count = items.count { entity ->
            val inSubtree = entity.locationId != null && entity.locationId in subtreeIds
            if (inSubtree) entity.locationId?.let(countedIds::add)
            inSubtree
        }
        LocationCountUi(locationId = root.id, name = root.name, count = count, iconKey = root.iconKey)
    }.toMutableList()
    val noLocationCount = items.count { it.locationId == null || it.locationId !in countedIds }
    if (noLocationCount > 0) {
        locationBars += LocationCountUi(locationId = null, name = "", count = noLocationCount)
    }
    locationBars.sortByDescending { it.count }

    val uiItems = items.map { it.toUi(locations, today) }
    return HouseholdStatsUi(
        totalItems = items.size,
        addedThisMonth = addedThisMonth,
        locationCount = locations.size,
        expiringSoonCount = expiringSoonCount,
        categorySlices = categorySlices,
        locationBars = locationBars,
        recentAdded = uiItems.sortedByDescending { it.createdAt }.take(5),
        upcomingExpiry = uiItems
            .filter { it.daysToExpire != null && it.daysToExpire in 0..EXPIRING_SOON_DAYS }
            .sortedBy { it.daysToExpire }
            .take(5),
    )
}
