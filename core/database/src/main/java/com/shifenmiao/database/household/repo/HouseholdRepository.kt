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

package com.shifenmiao.database.household.repo

import androidx.room.withTransaction
import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.household.dao.HouseholdItemDao
import com.shifenmiao.database.household.dao.HouseholdLocationDao
import com.shifenmiao.database.household.entity.HouseholdItemEntity
import com.shifenmiao.database.household.entity.HouseholdLocationEntity
import com.shifenmiao.database.household.model.HouseholdItemWithPath
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HouseholdRepository @Inject constructor(
    private val database: FeatureDatabase,
    private val locationDao: HouseholdLocationDao,
    private val itemDao: HouseholdItemDao,
) {

    fun observeLocations(): Flow<List<HouseholdLocationEntity>> {
        return locationDao.observeAll()
    }

    /** 预置位置播种:表为空才写入,固定 id + upsert 保证幂等。 */
    suspend fun ensureDefaultLocations(locations: List<HouseholdLocationEntity>) {
        if (locationDao.count() > 0) return
        locations.forEach { locationDao.upsert(it) }
    }

    fun observeLocationChildren(parentId: String?): Flow<List<HouseholdLocationEntity>> {
        return locationDao.observeChildren(parentId)
    }

    suspend fun upsertLocation(location: HouseholdLocationEntity) {
        locationDao.upsert(location)
    }

    suspend fun getLocationById(id: String): HouseholdLocationEntity? {
        return locationDao.getById(id)
    }

    suspend fun deleteLocation(id: String) {
        if (locationDao.countChildren(id) > 0) {
            throw IllegalStateException("Location $id still has child locations")
        }
        if (itemDao.countByLocation(id) > 0) {
            throw IllegalStateException("Location $id still has items")
        }
        locationDao.delete(id)
    }

    suspend fun buildLocationPath(locationId: String?): String {
        if (locationId == null) return ""
        val segments = ArrayDeque<String>()
        val visited = mutableSetOf<String>()
        var current = locationDao.getById(locationId)
        while (current != null && visited.add(current.id)) {
            segments.addFirst(current.name)
            current = current.parentId?.let { locationDao.getById(it) }
        }
        return segments.joinToString(" / ")
    }

    suspend fun findOrCreateLocationByPath(path: List<String>): HouseholdLocationEntity {
        return database.withTransaction {
            var parentId: String? = null
            var location: HouseholdLocationEntity? = null
            path.forEach { name ->
                location = locationDao.getByNameAndParent(name, parentId)
                    // 当前父级下没有同名位置时,全局按名称找唯一匹配并复用,
                    // 避免在已有层级(如「家/主卧」)之外重复新建同名位置;
                    // 无匹配或有歧义(多个同名)时才在当前父级下新建
                    ?: locationDao.getByName(name).singleOrNull()
                    ?: run {
                        val created = HouseholdLocationEntity(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            parentId = parentId,
                        )
                        locationDao.upsert(created)
                        created
                    }
                parentId = location!!.id
            }
            location ?: throw IllegalArgumentException("path must not be empty")
        }
    }

    fun observeItems(): Flow<List<HouseholdItemEntity>> {
        return itemDao.observeAll()
    }

    fun observeItemsByLocation(locationId: String): Flow<List<HouseholdItemEntity>> {
        return itemDao.observeByLocation(locationId)
    }

    fun search(query: String): Flow<List<HouseholdItemEntity>> {
        return itemDao.search(query)
    }

    fun searchWithLocation(query: String): Flow<List<HouseholdItemWithPath>> {
        return itemDao.search(query).map { items ->
            items.map { item ->
                HouseholdItemWithPath(
                    item = item,
                    locationPath = buildLocationPath(item.locationId),
                )
            }
        }
    }

    suspend fun upsertItem(item: HouseholdItemEntity) {
        itemDao.upsert(item)
    }

    suspend fun getItemById(id: String): HouseholdItemEntity? {
        return itemDao.getById(id)
    }

    suspend fun deleteItem(id: String) {
        itemDao.delete(id)
    }
}
