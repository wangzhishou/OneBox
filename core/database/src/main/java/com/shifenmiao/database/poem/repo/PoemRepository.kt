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

package com.shifenmiao.database.poem.repo

import com.shifenmiao.database.poem.dao.PoemDao
import com.shifenmiao.database.poem.entity.PoemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoemRepository @Inject constructor(
    private val poemDao: PoemDao,
) {

    suspend fun upsert(entity: PoemEntity) {
        poemDao.upsert(entity)
    }

    fun observeAll(): Flow<List<PoemEntity>> {
        return poemDao.observeAll()
    }

    fun observeFavorites(): Flow<List<PoemEntity>> {
        return poemDao.observeFavorites()
    }

    suspend fun getById(id: Long): PoemEntity? {
        return poemDao.getById(id)
    }

    fun observeById(id: Long): Flow<PoemEntity?> {
        return poemDao.observeById(id)
    }

    suspend fun updateAiInsight(id: Long, insight: String) {
        poemDao.updateAiInsight(id = id, insight = insight)
    }

    suspend fun updatePinyin(id: Long, pinyin: String) {
        poemDao.updatePinyin(id = id, pinyin = pinyin)
    }

    suspend fun updateTranslation(id: Long, translation: String) {
        poemDao.updateTranslation(id = id, translation = translation)
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        poemDao.updateFavorite(id = id, isFavorite = isFavorite)
    }

    suspend fun deleteById(id: Long) {
        poemDao.deleteById(id)
    }

    suspend fun deleteAllNonFavorites() {
        poemDao.deleteAllNonFavorites()
    }
}
