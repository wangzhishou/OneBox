package com.shifenmiao.database.habit.repo

import androidx.room.withTransaction
import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.habit.dao.HabitCheckInDao
import com.shifenmiao.database.habit.dao.HabitDao
import com.shifenmiao.database.habit.entity.HabitCheckInEntity
import com.shifenmiao.database.habit.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val database: FeatureDatabase,
    private val habitDao: HabitDao,
    private val checkInDao: HabitCheckInDao,
) {

    fun observeHabits(): Flow<List<HabitEntity>> {
        return habitDao.observeActive()
    }

    fun observeHabit(habitId: String): Flow<HabitEntity?> {
        return habitDao.observeById(habitId)
    }

    suspend fun getHabit(habitId: String): HabitEntity? {
        return habitDao.getById(habitId)
    }

    suspend fun upsertHabit(habit: HabitEntity) {
        habitDao.upsert(habit)
    }

    /** 删除习惯并级联清掉其全部打卡记录。 */
    suspend fun deleteHabit(habitId: String) {
        database.withTransaction {
            checkInDao.deleteByHabitId(habitId)
            habitDao.deleteById(habitId)
        }
    }

    fun observeCheckIns(epochDay: Long): Flow<List<HabitCheckInEntity>> {
        return checkInDao.observeByDate(epochDay)
    }

    /** 日期范围 [startDay, endDay] 内的全部打卡,统计用。 */
    suspend fun getCheckInsBetween(startDay: Long, endDay: Long): List<HabitCheckInEntity> {
        return checkInDao.getBetween(startDay, endDay)
    }

    /** 打卡。同一天重复打卡会被忽略,返回是否真正写入。 */
    suspend fun checkIn(
        habitId: String,
        epochDay: Long,
        checkedAt: Long = System.currentTimeMillis(),
    ): Boolean {
        val inserted = checkInDao.insertIgnore(
            HabitCheckInEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                dateEpochDay = epochDay,
                checkedAt = checkedAt,
            )
        )
        return inserted != -1L
    }

    suspend fun removeCheckIn(habitId: String, epochDay: Long) {
        checkInDao.deleteByHabitAndDate(habitId, epochDay)
    }
}
