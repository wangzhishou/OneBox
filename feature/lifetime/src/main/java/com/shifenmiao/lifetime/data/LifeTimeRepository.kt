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

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.lifetimeDataStore by preferencesDataStore(name = "lifetime_preferences")

@Singleton
class LifeTimeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val BIRTH_DATE_KEY = longPreferencesKey("birth_date_epoch_day")
        private val END_DATE_KEY = longPreferencesKey("end_date_epoch_day")
        private val TARGET_AGE_KEY = intPreferencesKey("target_age")

        /** 预置节日是否已播过种；用标记而不是"条数为 0"，否则用户删光节日后会被重新塞回来 */
        private val PRESETS_SEEDED_KEY = booleanPreferencesKey("countdown_presets_seeded")
    }

    val birthDateFlow: Flow<LocalDate?> = context.lifetimeDataStore.data
        .map { preferences ->
            preferences[BIRTH_DATE_KEY]?.let { epochDay ->
                LocalDate.ofEpochDay(epochDay)
            }
        }

    val endDateFlow: Flow<LocalDate?> = context.lifetimeDataStore.data
        .map { preferences ->
            preferences[END_DATE_KEY]?.let { epochDay ->
                LocalDate.ofEpochDay(epochDay)
            }
        }

    val targetAgeFlow: Flow<Int> = context.lifetimeDataStore.data
        .map { preferences ->
            preferences[TARGET_AGE_KEY] ?: 80
        }

    suspend fun isCountdownPresetsSeeded(): Boolean =
        context.lifetimeDataStore.data.first()[PRESETS_SEEDED_KEY] ?: false

    suspend fun markCountdownPresetsSeeded() {
        context.lifetimeDataStore.edit { preferences ->
            preferences[PRESETS_SEEDED_KEY] = true
        }
    }

    suspend fun saveBirthDate(date: LocalDate) {
        context.lifetimeDataStore.edit { preferences ->
            preferences[BIRTH_DATE_KEY] = date.toEpochDay()
        }
    }

    suspend fun saveEndDate(date: LocalDate) {
        context.lifetimeDataStore.edit { preferences ->
            preferences[END_DATE_KEY] = date.toEpochDay()
        }
    }

    suspend fun saveTargetAge(age: Int) {
        context.lifetimeDataStore.edit { preferences ->
            preferences[TARGET_AGE_KEY] = age.coerceIn(50, 120)
        }
    }

    suspend fun clearBirthDate() {
        context.lifetimeDataStore.edit { preferences ->
            preferences.remove(BIRTH_DATE_KEY)
        }
    }
}
