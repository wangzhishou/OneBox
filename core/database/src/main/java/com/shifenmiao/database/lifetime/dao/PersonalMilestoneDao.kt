package com.shifenmiao.database.lifetime.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.lifetime.entity.PersonalMilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalMilestoneDao {

    @Query("SELECT * FROM personal_milestones ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllMilestones(): Flow<List<PersonalMilestoneEntity>>

    @Query("SELECT * FROM personal_milestones WHERE id = :id")
    suspend fun getMilestoneById(id: Long): PersonalMilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: PersonalMilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: PersonalMilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: PersonalMilestoneEntity)

    @Query("DELETE FROM personal_milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: Long)

    @Query("SELECT COUNT(*) FROM personal_milestones")
    suspend fun getMilestoneCount(): Int
}
