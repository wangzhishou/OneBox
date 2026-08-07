package com.shifenmiao.lifetime.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
