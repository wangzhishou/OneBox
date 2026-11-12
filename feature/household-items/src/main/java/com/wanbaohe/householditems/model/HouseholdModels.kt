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

import java.time.LocalDate

/** 保质期状态 */
enum class ExpiryStatus {
    /** 已过期 */
    EXPIRED,

    /** 30 天内到期 */
    EXPIRING_SOON,

    /** 正常(超过 30 天才到期) */
    FRESH,

    /** 无保质期 */
    NONE,
}

data class HouseholdItemUi(
    val id: String,
    val name: String,
    val category: String,
    val locationId: String?,
    val locationPath: String,
    val expireDate: LocalDate?,
    val photoPath: String?,
    val note: String,
    val expiryStatus: ExpiryStatus,
    /** 距到期天数(负数 = 已过期天数),无保质期为 null */
    val daysToExpire: Long?,
    /** 创建时间戳(ms),统计页"最近新增/本月新增"用 */
    val createdAt: Long,
)

data class HouseholdLocationUi(
    val id: String,
    val name: String,
    val parentId: String?,
    /** 用户自选图标(IconRegistry key),null = 默认映射 */
    val iconKey: String? = null,
)

/** 位置树节点 */
data class LocationTreeNode(
    val location: HouseholdLocationUi,
    val depth: Int,
    val children: List<LocationTreeNode>,
)

/** 把扁平位置列表组装成树(按 sortOrder/创建序已排好) */
fun buildLocationTree(locations: List<HouseholdLocationUi>): List<LocationTreeNode> {
    fun childrenOf(parentId: String?, depth: Int): List<LocationTreeNode> {
        return locations.filter { it.parentId == parentId }.map { location ->
            LocationTreeNode(
                location = location,
                depth = depth,
                children = childrenOf(location.id, depth + 1),
            )
        }
    }
    return childrenOf(null, 0)
}

/** 树 → 带缩进深度的扁平列表(位置选择器/管理用) */
fun flattenLocationTree(tree: List<LocationTreeNode>): List<LocationTreeNode> {
    val result = mutableListOf<LocationTreeNode>()
    fun walk(nodes: List<LocationTreeNode>) {
        nodes.forEach { node ->
            result += node
            walk(node.children)
        }
    }
    walk(tree)
    return result
}

/** 沿 parentId 拼出从根到目标位置的路径 */
fun locationPathOf(locations: List<HouseholdLocationUi>, locationId: String?): List<HouseholdLocationUi> {
    if (locationId == null) return emptyList()
    val segments = ArrayDeque<HouseholdLocationUi>()
    val visited = mutableSetOf<String>()
    var current = locations.firstOrNull { it.id == locationId }
    while (current != null && visited.add(current!!.id)) {
        segments.addFirst(current!!)
        current = locations.firstOrNull { it.id == current!!.parentId }
    }
    return segments.toList()
}

/**
 * 收集 rootId 及其全部后代位置的 id 集合(子树过滤用,UI 层算,不走 DAO)。
 * rootId = null 返回 null,表示不过滤(全部物品)。
 */
fun subtreeLocationIds(locations: List<HouseholdLocationUi>, rootId: String?): Set<String>? {
    if (rootId == null) return null
    val result = mutableSetOf(rootId)
    var frontier = listOf(rootId)
    while (frontier.isNotEmpty()) {
        val next = locations.filter { it.parentId in frontier }.map { it.id }
        frontier = next.filter { result.add(it) }
    }
    return result
}

/** 主页底部 tab */
enum class HouseholdTab {
    LIST,
    STATS,
    LOCATIONS,
}

/** 物品列表显示模式 */
enum class HouseholdDisplayMode {
    LIST,
    GRID,
}

data class HouseholdItemsUiState(
    val selectedTab: HouseholdTab = HouseholdTab.LIST,
    val searchQuery: String = "",
    val locations: List<HouseholdLocationUi> = emptyList(),
    val locationTree: List<LocationTreeNode> = emptyList(),
    /** 当前搜索/全部物品(带位置路径与保质期状态) */
    val items: List<HouseholdItemUi> = emptyList(),
    /** 当前浏览的位置层级,null = 根 */
    val currentLocationId: String? = null,
    /** 用户手动切换的显示模式;null = 跟随系统设置 groupOptionsByTypes */
    val displayModeOverride: HouseholdDisplayMode? = null,
    /** 统计 tab 数据(全量,不受搜索词影响) */
    val stats: HouseholdStatsUi = HouseholdStatsUi(),

    // ── 物品编辑表单 ──
    val showItemEditor: Boolean = false,
    val editingItemId: String? = null,
    val editorName: String = "",
    val editorCategory: String = "",
    val editorLocationId: String? = null,
    val editorExpireDate: LocalDate? = null,
    /** 新选的待导入图片(content uri 字符串) */
    val editorPhotoUri: String? = null,
    /** 已有的图片文件路径;编辑时移除图片后置 null */
    val editorPhotoPath: String? = null,
    val editorNote: String = "",

    // ── 位置管理(设置 tab) ──
    /** 位置删除被阻止(有子级/物品)时置 true,由设置页消费后清除 */
    val locationDeleteBlocked: Boolean = false,
)
