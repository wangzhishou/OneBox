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