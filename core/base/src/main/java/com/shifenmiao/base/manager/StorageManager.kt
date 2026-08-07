package com.shifenmiao.base.manager

import com.shifenmiao.model.ai.AIEngineProvider
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.storage.AnnouncementListStore
import com.shifenmiao.storage.BlogListStore
import com.shifenmiao.storage.MarqueeSettingsStore
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.SearchHistoryStore
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageManager {

    fun clearAll() {
        CoroutineScope(Dispatchers.IO).launch {
            AIChatStorage.clearConfigs()
            AnnouncementListStore.clearAnnouncements()
            AIEngineProvider.clear()
            MarqueeSettingsStore.clear()
            RemoteConfigStorage.clearRemoteConfig()
            SearchHistoryStore.clearSearchHistory()
            BlogListStore.clearBlogsCache()
            BlogListStore.clearAllBlogDetailsCache()
            // 清理 Mermaid SVG 缓存（MMKV 实例）
            MMKV.mmkvWithID("mermaid_cache").clearAll()
        }
    }

    companion object {
        val instance: StorageManager by lazy { StorageManager() }
    }

}