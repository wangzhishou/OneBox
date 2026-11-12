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

package com.shifenmiao.lifetime.data

import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.lifetime.entity.PersonalMilestoneEntity
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalMilestoneRepository @Inject constructor(
    database: FeatureDatabase
) {
    private val dao = database.personalMilestoneDao()

    val allMilestonesFlow: Flow<List<PersonalMilestone>> = dao.getAllMilestones()
        .map { entities -> entities.map { it.toDomainModel() } }

    suspend fun addMilestone(milestone: PersonalMilestone): Long {
        return dao.insertMilestone(milestone.toEntity())
    }

    suspend fun updateMilestone(milestone: PersonalMilestone) {
        dao.updateMilestone(milestone.toEntity())
    }

    suspend fun deleteMilestone(milestone: PersonalMilestone) {
        dao.deleteMilestone(milestone.toEntity())
    }

    suspend fun deleteMilestoneById(id: Long) {
        dao.deleteMilestoneById(id)
    }

    suspend fun getMilestoneCount(): Int {
        return dao.getMilestoneCount()
    }

    suspend fun getMilestoneById(id: Long): PersonalMilestone? {
        return dao.getMilestoneById(id)?.toDomainModel()
    }
}

private fun PersonalMilestoneEntity.toDomainModel() = PersonalMilestone(
    id = id,
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.let { LocalDate.ofEpochDay(it) },
    targetDays = targetDays,
    startDate = startDate?.let { LocalDate.ofEpochDay(it) },
    note = note,
    color = color,
    sortOrder = sortOrder,
    createdAt = createdAt
)

private fun PersonalMilestone.toEntity() = PersonalMilestoneEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.toEpochDay(),
    targetDays = targetDays,
    startDate = startDate?.toEpochDay(),
    note = note,
    color = color,
    sortOrder = sortOrder,
    createdAt = createdAt
)
