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

import com.shifenmiao.model.search.SearchHistory
import com.shifenmiao.model.search.SuggestionModel
import com.tencent.mmkv.MMKV

object SearchHistoryStore {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.SEARCH_HISTORY)
    private const val KEY_SEARCH_HISTORY = "search_history"
    private const val MAX_SIZE = 30

    // Add a memory cache for the current search history
    private var currentHistoryCache: SearchHistory? = null

    fun saveSearchHistory(searchHistory: SearchHistory) {
        val currentHistory = currentHistoryCache ?: SearchHistory(emptyList())
        val updatedData = mutableListOf<SuggestionModel>().apply {
            addAll(currentHistory.data)
            addAll(searchHistory.data)
        }
        // Limit the size of the list
        while (updatedData.size > MAX_SIZE) {
            updatedData.removeAt(0)
        }
        val updatedHistory = SearchHistory(updatedData)
        mmkv.encode(KEY_SEARCH_HISTORY, updatedHistory)
        // Update the memory cache
        currentHistoryCache = updatedHistory
    }

    private fun loadSearchHistory(): SearchHistory? {
        // Check the memory cache first
        if (currentHistoryCache == null) {
            currentHistoryCache =
                mmkv.decodeParcelable(KEY_SEARCH_HISTORY, SearchHistory::class.java)
        }
        return currentHistoryCache
    }

    fun getHistoryList(): List<SuggestionModel> {
        return loadSearchHistory()?.data ?: emptyList()
    }

    fun addHistoryItem(suggestionModel: SuggestionModel) {
        val currentHistory = currentHistoryCache ?: SearchHistory(emptyList())
        for (item in currentHistory.data) {
            if (item.id == suggestionModel.id) {
                return
            }
        }
        val updatedData = mutableListOf<SuggestionModel>().apply {
            addAll(currentHistory.data)
            add(suggestionModel)
        }
        // Limit the size of the list
        while (updatedData.size > MAX_SIZE) {
            updatedData.removeAt(0)
        }
        val updatedHistory = SearchHistory(updatedData)
        mmkv.encode(KEY_SEARCH_HISTORY, updatedHistory)
        // Update the memory cache
        currentHistoryCache = updatedHistory
    }

    fun clearSearchHistory() {
        mmkv.remove(KEY_SEARCH_HISTORY)
        // Clear the memory cache
        currentHistoryCache = null
    }

    fun removeHistory(suggestionModel: SuggestionModel) {
        val currentHistory = currentHistoryCache ?: SearchHistory(emptyList())
        val updatedData = mutableListOf<SuggestionModel>().apply {
            addAll(currentHistory.data)
            removeIf { suggestionModel.id == it.id }
        }
        val updatedHistory = SearchHistory(updatedData)
        mmkv.encode(KEY_SEARCH_HISTORY, updatedHistory)
        // Update the memory cache
        currentHistoryCache = updatedHistory

    }
}