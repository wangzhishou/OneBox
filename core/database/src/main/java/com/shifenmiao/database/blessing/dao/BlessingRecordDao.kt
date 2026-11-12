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

package com.shifenmiao.database.blessing.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.blessing.entity.BlessingRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BlessingRecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(entity: BlessingRecordEntity)

    @Query(
        """
        UPDATE blessing_record
        SET count = count + 1, updated_at = :updatedAt
        WHERE date = :date AND type = :type
        """
    )
    protected abstract suspend fun incrementExisting(
        date: String,
        type: String,
        updatedAt: Long,
    )

    @Transaction
    open suspend fun increment(
        id: String,
        date: String,
        type: String,
        updatedAt: Long,
    ) {
        insertIfAbsent(
            BlessingRecordEntity(
                id = id,
                date = date,
                type = type,
                count = 0,
                updatedAt = updatedAt,
            )
        )
        incrementExisting(date = date, type = type, updatedAt = updatedAt)
    }

    @Query("SELECT * FROM blessing_record WHERE date = :date")
    abstract suspend fun getByDate(date: String): List<BlessingRecordEntity>

    @Query("SELECT * FROM blessing_record WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    abstract fun observeInDateRange(
        startDate: String,
        endDate: String,
    ): Flow<List<BlessingRecordEntity>>

    @Query("SELECT * FROM blessing_record WHERE date = :date")
    abstract fun observeByDate(date: String): Flow<List<BlessingRecordEntity>>
}
