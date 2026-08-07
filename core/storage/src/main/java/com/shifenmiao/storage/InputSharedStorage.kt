package com.shifenmiao.storage

import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object InputSharedStorage {
    private val mmkv: MMKV = MMKV.defaultMMKV()
    private val memoryCache = mutableMapOf<String, String>()

    fun save(key: String, value: String) {
        memoryCache[key] = value
        CoroutineScope(Dispatchers.IO).launch {
            mmkv.encode(key, value)
        }
    }

    fun load(key: String, defaultValue: String? = null): String? {
        memoryCache[key]?.let {
            return it
        }
        return mmkv.decodeString(key, defaultValue).also {
            if (it != null) memoryCache[key] = it
        }
    }

    fun clear(key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mmkv.remove(key)
        }
        memoryCache.remove(key)
    }
}