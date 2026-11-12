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
