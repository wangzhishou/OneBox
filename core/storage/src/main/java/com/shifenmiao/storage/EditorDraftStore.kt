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

import com.tencent.mmkv.MMKV

/**
 * 文本编辑器未保存草稿（进程被杀后恢复）。
 *
 * 每个「文件 / 新建页」一个桶，桶里同时存：
 * - `original`：进入编辑器时从磁盘读到的正文；
 * - `draft`：用户改到一半的正文。
 *
 * 恢复时必须 `original` 与当前磁盘内容一致才返回草稿 —— 文件在别处被改过、
 * 或者换了同名文件时，旧草稿会直接作废，不会把磁盘内容盖回去。
 * 存储走 MMKV（同步、进程被杀也不丢），按最近使用时间只保留 [MAX_DRAFTS] 份。
 */
object EditorDraftStore {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.CODE_EDITOR_DRAFT)

    private const val SUFFIX_ORIGINAL = "#original"
    private const val SUFFIX_DRAFT = "#draft"
    private const val SUFFIX_TIME = "#time"
    private const val MAX_DRAFTS = 24
    private const val MAX_DRAFT_CHARS = 400_000

    data class Draft(
        val original: String,
        val text: String,
    )

    fun save(key: String, original: String, text: String) {
        if (key.isBlank()) return
        if (text.length > MAX_DRAFT_CHARS || original.length > MAX_DRAFT_CHARS) return
        if (text == original) {
            // 没有实际改动就不留草稿，避免下次打开"恢复"出一个和磁盘一样的正文
            clear(key)
            return
        }
        mmkv.encode(key + SUFFIX_ORIGINAL, original)
        mmkv.encode(key + SUFFIX_DRAFT, text)
        mmkv.encode(key + SUFFIX_TIME, System.currentTimeMillis())
        trimToLimit()
    }

    fun load(key: String): Draft? {
        if (key.isBlank()) return null
        val draft = mmkv.decodeString(key + SUFFIX_DRAFT) ?: return null
        val original = mmkv.decodeString(key + SUFFIX_ORIGINAL).orEmpty()
        return Draft(original = original, text = draft)
    }

    fun clear(key: String) {
        if (key.isBlank()) return
        mmkv.removeValueForKey(key + SUFFIX_ORIGINAL)
        mmkv.removeValueForKey(key + SUFFIX_DRAFT)
        mmkv.removeValueForKey(key + SUFFIX_TIME)
    }

    private fun trimToLimit() {
        val timeKeys = mmkv.allKeys().orEmpty().filter { it.endsWith(SUFFIX_TIME) }
        if (timeKeys.size <= MAX_DRAFTS) return
        timeKeys
            .map { timeKey -> timeKey to mmkv.decodeLong(timeKey, 0L) }
            .sortedBy { (_, time) -> time }
            .take(timeKeys.size - MAX_DRAFTS)
            .forEach { (timeKey, _) -> clear(timeKey.removeSuffix(SUFFIX_TIME)) }
    }
}
