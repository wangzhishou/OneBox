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

package com.shifenmiao.database.passwordvault.repo

import com.shifenmiao.database.passwordvault.dao.PasswordVaultCategoryDao
import com.shifenmiao.database.passwordvault.dao.PasswordVaultEntryDao
import com.shifenmiao.database.passwordvault.entity.PasswordVaultCategoryEntity
import com.shifenmiao.database.passwordvault.entity.PasswordVaultEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordVaultRepository @Inject constructor(
    private val entryDao: PasswordVaultEntryDao,
    private val categoryDao: PasswordVaultCategoryDao,
) {

    // ── Entry ─────────────────────────────────────────────

    fun observeAll(): Flow<List<PasswordVaultEntryEntity>> = entryDao.observeAll()

    suspend fun getAll(): List<PasswordVaultEntryEntity> = entryDao.getAll()

    suspend fun getById(entryId: String): PasswordVaultEntryEntity? = entryDao.getById(entryId)

    suspend fun save(entity: PasswordVaultEntryEntity) {
        entryDao.upsert(entity)
    }

    suspend fun delete(entryId: String) {
        entryDao.deleteById(entryId)
    }

    suspend fun deleteAll() {
        entryDao.deleteAll()
    }

    suspend fun hasEntries(): Boolean = entryDao.count() > 0

    // ── Category ──────────────────────────────────────────

    fun observeCategories(): Flow<List<PasswordVaultCategoryEntity>> = categoryDao.observeAll()

    suspend fun getAllCategories(): List<PasswordVaultCategoryEntity> = categoryDao.getAll()

    suspend fun getCategoryById(categoryId: String): PasswordVaultCategoryEntity? =
        categoryDao.getById(categoryId)

    suspend fun upsertCategory(category: PasswordVaultCategoryEntity) {
        categoryDao.upsert(category)
    }

    suspend fun updateCategoryOrder(categoryId: String, order: Int) {
        categoryDao.updateSortOrder(categoryId = categoryId, sortOrder = order)
    }

    suspend fun deleteCustomCategory(categoryId: String) {
        categoryDao.deleteCustomById(categoryId)
    }

    suspend fun ensureDefaultCategories(defaults: List<PasswordVaultCategoryEntity>) {
        if (categoryDao.count() > 0) return
        defaults.forEach { categoryDao.upsert(it) }
    }
}
