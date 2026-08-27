package com.shifenmiao.imagegeneration.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProviderConfigRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val legacyPreferences = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    private val preferences = createEncryptedPreferences(context)
    private val migrationComplete = migrateLegacyPreferences()
    private val gson = Gson()
    private val _configs = MutableStateFlow(loadConfigs())
    private val _activeConfigId = MutableStateFlow(preferences.getString(KEY_ACTIVE_CONFIG_ID, null))

    val configs: StateFlow<List<ImageProviderConfig>> = _configs.asStateFlow()
    val activeConfigId: StateFlow<String?> = _activeConfigId.asStateFlow()

    fun replace(configs: List<ImageProviderConfig>, activeConfigId: String?) {
        val normalized = configs.distinctBy(ImageProviderConfig::id)
        val activeId = activeConfigId?.takeIf { id -> normalized.any { it.id == id && it.enabled } }
            ?: normalized.firstOrNull(ImageProviderConfig::enabled)?.id
        _configs.value = normalized
        _activeConfigId.value = activeId
        preferences.edit()
            .putString(KEY_CONFIGS, gson.toJson(normalized))
            .putString(KEY_ACTIVE_CONFIG_ID, activeId)
            .putBoolean(KEY_INITIALIZED, true)
            .apply()
    }

    fun upsert(config: ImageProviderConfig, makeActive: Boolean = false) {
        val updated = _configs.value.toMutableList().apply {
            val index = indexOfFirst { it.id == config.id }
            if (index >= 0) set(index, config) else add(config)
        }
        replace(updated, if (makeActive) config.id else _activeConfigId.value)
    }

    fun delete(id: String) {
        replace(_configs.value.filterNot { it.id == id }, _activeConfigId.value)
    }

    fun setActive(id: String) {
        require(_configs.value.any { it.id == id && it.enabled }) { "Image provider config is unavailable" }
        replace(_configs.value, id)
    }

    private fun loadConfigs(): List<ImageProviderConfig> {
        check(migrationComplete)
        val json = preferences.getString(KEY_CONFIGS, null).orEmpty()
        if (json.isBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<ImageProviderConfig>>() {}.type
            gson.fromJson<List<ImageProviderConfig>>(json, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    fun isInitialized(): Boolean = preferences.getBoolean(KEY_INITIALIZED, false)

    private fun migrateLegacyPreferences(): Boolean {
        if (!preferences.contains(KEY_CONFIGS) && legacyPreferences.contains(KEY_CONFIGS)) {
            preferences.edit()
                .putString(KEY_CONFIGS, legacyPreferences.getString(KEY_CONFIGS, null))
                .putString(KEY_ACTIVE_CONFIG_ID, legacyPreferences.getString(KEY_ACTIVE_CONFIG_ID, null))
                .putBoolean(KEY_INITIALIZED, true)
                .commit()
            legacyPreferences.edit().clear().apply()
        }
        return true
    }

    companion object {
        private const val LEGACY_PREFS_NAME = "image_generation_config"
        private const val PREFS_NAME = "image_generation_config_encrypted"
        private const val KEY_CONFIGS = "provider_configs"
        private const val KEY_ACTIVE_CONFIG_ID = "active_config_id"
        private const val KEY_INITIALIZED = "initialized"

        private fun createEncryptedPreferences(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }
}
