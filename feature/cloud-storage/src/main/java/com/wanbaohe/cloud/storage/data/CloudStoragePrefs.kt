package com.wanbaohe.cloud.storage.data

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * 云存储偏好设置（使用 MMKV）
 */
internal class CloudStoragePrefs(
    context: Context,
) {
    init {
        // 初始化 MMKV（如果尚未初始化）
        if (MMKV.getRootDir() == null) {
            MMKV.initialize(context)
        }
    }

    private val mmkv = MMKV.mmkvWithID(PREFS_NAME, MMKV.MULTI_PROCESS_MODE)

    fun saveLastConnectionId(id: String?) {
        if (id != null) {
            mmkv.encode(KEY_LAST_CONNECTION_ID, id)
        } else {
            mmkv.removeValueForKey(KEY_LAST_CONNECTION_ID)
        }
    }

    fun loadLastConnectionId(): String? = mmkv.decodeString(KEY_LAST_CONNECTION_ID, null)

    fun saveLastBucket(bucket: String?) {
        if (bucket != null) {
            mmkv.encode(KEY_LAST_BUCKET, bucket)
        } else {
            mmkv.removeValueForKey(KEY_LAST_BUCKET)
        }
    }

    fun loadLastBucket(): String? = mmkv.decodeString(KEY_LAST_BUCKET, null)

    fun saveLastPrefix(prefix: String) {
        mmkv.encode(KEY_LAST_PREFIX, prefix)
    }

    fun loadLastPrefix(): String = mmkv.decodeString(KEY_LAST_PREFIX, "") ?: ""

    fun saveSearchQuery(query: String) {
        mmkv.encode(KEY_LAST_SEARCH_QUERY, query)
    }

    fun loadSearchQuery(): String = mmkv.decodeString(KEY_LAST_SEARCH_QUERY, "") ?: ""

    fun saveGridMode(isGridMode: Boolean) {
        mmkv.encode(KEY_IS_GRID_MODE, isGridMode)
    }

    fun loadGridMode(): Boolean = mmkv.decodeBool(KEY_IS_GRID_MODE, true)

    private companion object {
        private const val PREFS_NAME = "cloud_storage_prefs"
        private const val KEY_LAST_CONNECTION_ID = "last_connection_id"
        private const val KEY_LAST_BUCKET = "last_bucket"
        private const val KEY_LAST_PREFIX = "last_prefix"
        private const val KEY_LAST_SEARCH_QUERY = "last_search_query"
        private const val KEY_IS_GRID_MODE = "is_grid_mode"
    }
}
