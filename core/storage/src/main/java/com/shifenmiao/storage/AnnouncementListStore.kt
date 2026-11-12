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

package com.shifenmiao.storage

import com.shifenmiao.model.common.AnnouncementItem
import com.shifenmiao.model.common.DataList
import com.tencent.mmkv.MMKV

object AnnouncementListStore {
    // 按语言隔离：公告内容按 locale 下发
    private val mmkv: MMKV get() = localizedMmkv(MMKVName.ANNOUNCEMENT_LIST)
    private val CACHE_TIMEOUT = RemoteConfigStorage.getRemoteConfig().cacheTimeout
    private const val KEY_ANNOUNCEMENTS = "announcements"

    fun saveAnnouncements(announcements: DataList<AnnouncementItem>) {
        mmkv.encode(KEY_ANNOUNCEMENTS, announcements, CACHE_TIMEOUT ?: (60 * 60))
    }

    fun loadAnnouncements(): DataList<AnnouncementItem>? {
        @Suppress("UNCHECKED_CAST")
        return mmkv.decodeParcelable(KEY_ANNOUNCEMENTS, DataList::class.java) as? DataList<AnnouncementItem>
    }

    fun clearAnnouncements() {
        mmkv.remove(KEY_ANNOUNCEMENTS)
    }
}