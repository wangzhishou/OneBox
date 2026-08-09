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