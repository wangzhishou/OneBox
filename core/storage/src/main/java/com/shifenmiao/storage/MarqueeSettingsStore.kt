package com.shifenmiao.storage

import com.shifenmiao.model.marquee.MarqueeSettings
import com.tencent.mmkv.MMKV

object MarqueeSettingsStore {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.MARQUEE_SETTING)

    private const val KEY_MARQUEE_SETTING = "marquee_setting"
    private const val KEY_MARQUEE_HISTORY = "marquee_history"
    private const val HISTORY_SEPARATOR = "||_||"

    fun saveLocalMarqueeSettings(marqueeSettings: MarqueeSettings) {
        mmkv.encode(KEY_MARQUEE_SETTING, marqueeSettings)
    }

    fun loadLocalMarqueeSettings(): MarqueeSettings? {
        return mmkv.decodeParcelable(KEY_MARQUEE_SETTING, MarqueeSettings::class.java)
    }

    fun getMarqueeHistory(): List<String> {
        val historyString = mmkv.decodeString(KEY_MARQUEE_HISTORY, "") ?: ""
        if (historyString.isEmpty()) return emptyList()
        return historyString.split(HISTORY_SEPARATOR)
    }

    fun addMarqueeHistory(text: String) {
        if (text.isBlank()) return
        val currentHistory = getMarqueeHistory().toMutableList()
        // Remove if exists to move to top
        currentHistory.remove(text)
        // Add to top
        currentHistory.add(0, text)
        // Limit history size (e.g. 10)
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }
        val newHistoryString = currentHistory.joinToString(HISTORY_SEPARATOR)
        mmkv.encode(KEY_MARQUEE_HISTORY, newHistoryString)
    }

    fun clear() {
        mmkv.remove(KEY_MARQUEE_SETTING)
        mmkv.remove(KEY_MARQUEE_HISTORY)
    }

}