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

import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import com.tencent.mmkv.MMKV

/**
 * 版本变化的时候，这里可以改下key
 */
object MMKVName {
    const val APP_SHARED: String = "app"
    const val TOKEN: String = "token"
    const val DEVICE: String = "device"
    const val RECENT_LIST: String = "recent"
    const val SCAN_HISTORY: String = "scan_history"
    const val COLOR_PICK_HISTORY: String = "color_pick_history"
    const val MARQUEE_SETTING: String = "marquee"
    const val AI_CHAT_SETTING: String = "ai_chat"
    const val AI_ENGINE_SETTING: String = "ai_engine"
    const val SEARCH_HISTORY: String = "search_history"
    const val REMOTE_CONFIG: String = "remote_config"
    const val ANNOUNCEMENT_LIST: String = "announcement"
    const val BLOG_LIST = "blog"
    const val MARKDOWN_HISTORY: String = "markdown_history"
    const val MERMAID_CACHE: String = "mermaid_cache"
    const val POEM_FILTER: String = "poem_filter"
}

/**
 * 按当前语言隔离的 MMKV 文件 id："<base>_<localeTag>"，与 Room 分库命名同规则
 * （见 AppDatabase.dbNameForLocale）。语言切换后进程会冷重启（见 LocaleSwitchWatcher），
 * 这里仍在每次访问时动态解析：MMKV 内部按 id 缓存实例，开销可忽略，
 * 且极端情况下（重启前）也能立即读写新语言的文件。
 *
 * 只用于"内容随语言下发/与分库数据绑定"的缓存（同步水位线、远程配置、引擎目录等），
 * 用户私有数据（token、搜索历史等）不要使用。
 */
fun localizedMmkvId(base: String): String = "${base}_${LocaleUtils.getCurrentLocaleTag()}"

fun localizedMmkv(base: String): MMKV = MMKV.mmkvWithID(localizedMmkvId(base))
