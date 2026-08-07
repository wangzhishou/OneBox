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
